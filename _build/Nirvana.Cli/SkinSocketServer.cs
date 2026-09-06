using System;
using System.Collections.Concurrent;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Net.Http;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Nirvana.Cipher.Cipher.Nirvana;
using Nirvana.WPFLauncher.Entities.WPFLauncher;
using Nirvana.WPFLauncher.Entities.WPFLauncher.Minecraft;
using Nirvana.WPFLauncher.Entities.WPFLauncher.NetGame.GameLaunch.Texture;
using Nirvana.WPFLauncher.Protocol;
using Serilog;

namespace Nirvana.Cli;

/// <summary>
/// 监听 9876 端口，处理网易 mod 的皮肤请求 (SMID=2050) 和重连请求 (SMID=1282)。
/// 在 fantnelcli 进程内运行，用 fantnelcli 自己的网易会话调用 API，不会 token 冲突。
///
/// 注意: 游戏端 NetworkSocket 对所有消息只维护【一条】TCP 连接(Sender/Receiver 各一个线程),
/// 而 Tab 玩家列表最多约 80 人、游戏侧皮肤加载线程池只有 3 个 Worker 线程。
/// 因此本服务器必须:
///   1. 读到帧后【并行】处理(不能处理完一帧再读下一帧, 否则 2050 全部串行排队);
///   2. 同一玩家的并发请求(身体皮肤 + Tab 头像)合并为一次在线查询, 结果内存缓存;
///   3. 在线 API 并行查询、短超时, 先回默认皮肤也不能让游戏等满 60s。
/// 回复帧按 SMID + 玩家名匹配, 乱序回复不影响游戏。
/// </summary>
public static class SkinSocketServer {
    private const int PORT = 9876;
    private const int SMID_RECONNECT_REQUEST = 0x0502; // 1282
    private const int SMID_RECONNECT_REPLY = 0x0704;  // 1796
    private const int SMID_SKIN = 0x0802;               // 2050
    private const int SMID_CHAT_BAN = 0x1205;           // 4613 聊天禁言查询(游戏发消息前阻塞等待)
    private const int SMID_ITEM_BAN = 0x1206;           // 4614 物品名封禁查询(同样阻塞等待)

    private static readonly string SKINS_DIR = Program.SkinsDir ?? Path.Combine(AppContext.BaseDirectory, "skins_local");
    private static readonly string? VANILLA_SKIN_CACHE = Program.GameDir != null
        ? Path.Combine(Program.GameDir, "skins") : null;

    private static readonly Skip32Cipher Skip32 = new("SaintSteve"u8.ToArray());
    private static readonly HttpClient Http = new() { Timeout = TimeSpan.FromSeconds(10) };

    // ===== 皮肤解析缓存 =====
    private sealed record SkinResult(string Path, int Mode);

    private sealed class CachedSkin {
        public required SkinResult Result { get; init; }
        public long ExpiresAtTicks { get; init; } // long.MaxValue = 永久有效
    }

    private static readonly ConcurrentDictionary<string, Task<SkinResult>> InFlight = new();
    private static readonly ConcurrentDictionary<string, CachedSkin> SkinCache = new();
    private const long PermanentTicks = long.MaxValue;
    private static readonly TimeSpan NegativeTtl = TimeSpan.FromMinutes(3); // 无皮肤/默认结果短缓存
    private static readonly TimeSpan OnlineBudget = TimeSpan.FromSeconds(9); // 在线取皮总预算, 超时先回默认
    private static readonly SemaphoreSlim GatewayGate = new(12); // 限制网易 Gateway API 并发, 防 80 人 Tab 同时查询互相拖慢/限流
    private static readonly SemaphoreSlim DownloadGate = new(4); // 皮肤下载并发(网易CDN高并发反而限速/拖慢, 实测 4 比 12 稳定)
    // 后台【预热】独立小闸门: 预热是低优先级, 只能涓流(3并发)进行, 绝不能挤占真人 2050(方案一实时API)的槽位。
    // 整个 PreheatUid 流程(查询+下载)都在该闸门内, 故预热最多占用 3 个 GatewayGate/DownloadGate 槽位, 其余留给真人请求。
    private static readonly SemaphoreSlim PreheatGate = new(3);
    // skinId -> 资源下载URL 静态缓存(资源ID与下载地址映射不变), 省掉"取下载列表"网关往返
    private static readonly ConcurrentDictionary<string, string> ResUrlCache = new();

    // ===== Tab 列表预热: 进服瞬间(服务器下发 0x3E Player Info)就预查询+预下载全员皮肤,
    //      2050 到达时命中 UidPlanCache 跳过 API 查询、命中磁盘跳过下载, 秒回 =====
    private sealed record SkinPlan(string SkinId, int RawMode);
    private static readonly ConcurrentDictionary<long, SkinPlan> UidPlanCache = new();
    private static readonly ConcurrentDictionary<long, DateTime> UidNoSkinCache = new(); // 无装备皮肤 uid, 短期内不重复预热
    private static readonly HashSet<long> PreheatingUids = new();
    private static readonly ConcurrentDictionary<long, Task> PreheatTasks = new(); // 进行中的预热任务, 2050 可等待复用
    private static readonly object PreheatLock = new();
    private static readonly TimeSpan PreheatNegativeTtl = TimeSpan.FromMinutes(5);

    public static void Start() {
        // 订阅 Tab 列表包(0x3E)解析出的玩家 UUID, 进服即预热
        Heypixel.Play.SaClientboundPlayerInfoUpdatePacket.OnPlayerUuids += PreheatUuids;
        var t = new Thread(Run) { IsBackground = true, Name = "SkinSocket9876" };
        t.Start();
    }

    private static void PreheatUuids(List<string> uuids) {
        int started = 0;
        foreach (var uuid in uuids) {
            var uid = Skip32.ComputeUserIdFromUuid(uuid);
            if (uid == 0) continue; // 离线 UUID(人机)不走网易 API
            if (UidPlanCache.ContainsKey(uid)) continue;
            if (UidNoSkinCache.TryGetValue(uid, out var t) && DateTime.UtcNow - t < PreheatNegativeTtl) continue;
            lock (PreheatLock) {
                if (!PreheatingUids.Add(uid)) continue;
            }
            started++;
            var task = Task.Run(() => PreheatUid(uid));
            PreheatTasks[uid] = task;
            _ = task.ContinueWith(_ => PreheatTasks.TryRemove(uid, out _));
        }
        if (started > 0) Log.Information("[SkinSocket] tab-list preheat: {Started} player(s) queued (list={All})", started, uuids.Count);
    }

    private static async Task PreheatUid(long uid) {
        await PreheatGate.WaitAsync(); // 预热低优先级: 全局最多 3 个玩家同时预热, 把闸门槽位留给真人 2050
        try {
            var outcome = await QuerySkinPlanAsync(uid, null);
            if (outcome == null) {
                UidNoSkinCache[uid] = DateTime.UtcNow;
                return;
            }
            var plan = outcome.Value.Plan;
            UidPlanCache[uid] = plan;
            // 预热期就把首选皮肤下载落盘, 2050 到达时磁盘命中秒回
            await DownloadSkinByIdAsync(plan.SkinId);
            Log.Information("[SkinSocket] preheat uid={Uid} ready skin_id={SkinId}", uid, plan.SkinId);
        } catch (Exception e) {
            Log.Warning("[SkinSocket] preheat uid={Uid} error: {Msg}", uid, e.Message);
        } finally {
            PreheatGate.Release();
            lock (PreheatLock) { PreheatingUids.Remove(uid); }
        }
    }

    private static void Run() {
        var listener = new TcpListener(IPAddress.Loopback, PORT);
        try {
            listener.Start();
            Log.Information("[SkinSocket] listening on 127.0.0.1:{Port} | skinsDir={Skins} | vanillaCache={Vanilla}",
                PORT, SKINS_DIR, VANILLA_SKIN_CACHE ?? "(null)");
            while (true) {
                var client = listener.AcceptTcpClient();
                var t = new Thread(() => Hold(client)) { IsBackground = true, Name = "skin-conn" };
                t.Start();
            }
        } catch (Exception e) {
            Log.Error("[SkinSocket] listen error: {Msg}", e.Message);
        }
    }

    private static void Hold(TcpClient client) {
        try {
            using var c = client;
            using var stream = c.GetStream();
            var writeLock = new object(); // 同一连接上的回复帧可能由多个线程写出, 必须加锁防止帧交错
            var buf = new byte[65536];
            while (true) {
                // 读取 2 字节小端长度
                int b1 = stream.ReadByte();
                if (b1 < 0) break;
                int b2 = stream.ReadByte();
                if (b2 < 0) break;
                int len = b1 | (b2 << 8);
                if (len <= 0) break;

                // 读取 payload
                var payload = new byte[len];
                int read = 0;
                while (read < len) {
                    int n = stream.Read(payload, read, len - read);
                    if (n <= 0) break;
                    read += n;
                }
                if (read < len) break;

                // SMID 大端
                int smid = len >= 2 ? (payload[0] << 8 | payload[1]) : -1;
                switch (smid) {
                    case SMID_RECONNECT_REQUEST:
                        Log.Information("[SkinSocket] got 1282 reconnect request, sending 1796 reply");
                        WriteFrame(stream, BuildReconnectReply(), writeLock);
                        break;
                    case SMID_SKIN:
                        // 皮肤查询涉及 HTTP, 丢到线程池【并行】处理, 本线程立刻继续读下一帧。
                        // (游戏所有 2050 都走这一条连接, 串行处理会让 Tab 头像排队几十秒)
                        var captured = payload;
                        _ = Task.Run(() => HandleSkinRequestAsync(stream, captured, writeLock));
                        break;
                    case SMID_CHAT_BAN:
                        // 游戏发聊天前查询禁言状态, 不回复会卡 10s. 直接回"未禁言"(ban=false),
                        // 游戏收到后永久放行, 之后不再查询.
                        WriteFrame(stream, BuildChatBanReply(), writeLock);
                        Log.Information("[SkinSocket] 4613 chat-ban query -> replied not-banned");
                        break;
                    case SMID_ITEM_BAN: {
                        // 物品名封禁查询, 同样阻塞. 回 ban=false, 原样回传 itemName.
                        string? itemName = ReadString(payload, 2); // 跳过 SMID(2)
                        WriteFrame(stream, BuildItemBanReply(itemName ?? ""), writeLock);
                        Log.Information("[SkinSocket] 4614 item-ban query for \"{Item}\" -> replied not-banned", itemName ?? "?");
                        break;
                    }
                    default:
                        Log.Information("[SkinSocket] frame smid=0x{Smid:X4} len={Len} (ignored)", smid, len);
                        break;
                }
            }
        } catch (Exception e) {
            Log.Error("[SkinSocket] conn error: {Msg}", e.Message);
        }
    }

    private static async Task HandleSkinRequestAsync(NetworkStream stream, byte[] payload, object writeLock) {
        // 跳过 SMID(2) + gameid(2)
        string? player = ReadString(payload, 4);
        if (player == null) {
            Log.Warning("[SkinSocket] 2050 skin request with bad payload");
            return;
        }
        int off = 4 + 2 + Encoding.UTF8.GetByteCount(player);
        string? uuid = ReadString(payload, off);
        var sw = Stopwatch.StartNew();
        Log.Information("[SkinSocket] 2050 skin request for \"{Player}\" uuid={Uuid}", player, uuid ?? "(?)");

        try {
            var result = await ResolveSkinAsync(player, uuid);
            SeedVanillaCache(result.Path);
            WriteFrame(stream, BuildSkinReply(player, result.Path, result.Mode), writeLock);
            Log.Information("[SkinSocket] 2050 replied \"{Player}\" -> {Path} mode={Mode} in {Ms}ms",
                player, string.IsNullOrEmpty(result.Path) ? "(empty)" : result.Path, result.Mode, sw.ElapsedMilliseconds);
        } catch (Exception e) {
            Log.Error("[SkinSocket] 2050 handle error for \"{Player}\": {Msg}", player, e.Message);
            // 兜底: 尽量回默认皮肤, 避免游戏端 wait(60000) 干等
            try {
                var fallback = DefaultResult();
                WriteFrame(stream, BuildSkinReply(player, fallback.Path, fallback.Mode), writeLock);
            } catch { /* 连接可能已关闭 */ }
        }
    }

    /// <summary>
    /// 解析玩家皮肤: 本地手工皮肤 -> 内存缓存 -> 在线查询(同玩家并发合并)。
    /// 在线查询超过 <see cref="OnlineBudget"/> 未完成时先回默认皮肤, 后台查询完成后会写入缓存供下次使用。
    /// </summary>
    /// <summary>自己的角色名(登录后由 Program 设置)。skins_local/skin_me.png 可自定义自己皮肤。</summary>
    public static volatile string? SelfPlayerName;

    private static async Task<SkinResult> ResolveSkinAsync(string player, string? uuid) {
        // a0. 自己的皮肤: skins_local/skin_me.png(粗手臂Steve,mode=0) / skin_me_slim.png(细手臂Alex,mode=1), slim 优先
        if (!string.IsNullOrEmpty(SelfPlayerName) && player.Equals(SelfPlayerName, StringComparison.OrdinalIgnoreCase)) {
            var meSlim = Path.Combine(SKINS_DIR, "skin_me_slim.png");
            var me = Path.Combine(SKINS_DIR, "skin_me.png");
            string? selfPath;
            int selfMode;
            if (File.Exists(meSlim)) {
                selfPath = meSlim; selfMode = 1;
            } else if (File.Exists(me)) {
                selfPath = me; selfMode = 0;
            } else {
                selfPath = null; selfMode = 0;
            }
            if (selfPath != null) {
                Log.Information("[SkinSocket]    self skin -> {Path} mode={Mode} ({Arms})", selfPath, selfMode, selfMode == 1 ? "细手臂Alex" : "粗手臂Steve");
                var self = new SkinResult(selfPath, selfMode);
                PutCache(player, self, permanent: true);
                return self;
            }
        }

        // a. 本地手工皮肤 skins_local/<玩家名>.png (最高优先级, 永久缓存)
        var localSkin = FindSkin(player);
        if (localSkin != null) {
            Log.Information("[SkinSocket]    local skin -> {Path}", localSkin);
            var local = new SkinResult(localSkin, 0);
            PutCache(player, local, permanent: true);
            return local;
        }

        // b. 内存缓存: 在线皮肤永久缓存; 无皮肤/默认结果短 TTL(允许后续重试)
        if (SkinCache.TryGetValue(player, out var cached) &&
            (cached.ExpiresAtTicks == PermanentTicks || DateTime.UtcNow.Ticks < cached.ExpiresAtTicks)) {
            return cached.Result;
        }

        // c. 在线取皮: 同一玩家的并发请求(身体 + Tab)共用同一个查询任务
        var task = InFlight.GetOrAdd(player, _ => Task.Run(() => FetchAndCacheAsync(player, uuid)));
        _ = task.ContinueWith(_ => InFlight.TryRemove(player, out Task<SkinResult>? _), TaskScheduler.Default);

        var winner = await Task.WhenAny(task, Task.Delay(OnlineBudget));
        if (winner == task) {
            return await task; // 结果已在 FetchAndCacheAsync 内写入缓存
        }

        Log.Warning("[SkinSocket]    online resolve budget {Ms}ms exceeded for \"{Player}\", replying default early (fetch continues in background)",
            (int)OnlineBudget.TotalMilliseconds, player);
        return DefaultResult();
    }

    private static async Task<SkinResult> FetchAndCacheAsync(string player, string? uuid) {
        TimeSpan? transientTtl = null; // 会话未就绪等临时性失败: 短缓存 20s, 恢复后立刻重试
        try {
            if (uuid != null) {
                var online = await FetchNeteaseSkin(player, uuid);
                if (online != null) {
                    var result = new SkinResult(online.Value.Path, online.Value.Mode);
                    PutCache(player, result, permanent: true);
                    Log.Information("[SkinSocket]    online skin -> {Path} mode={Mode}", result.Path, result.Mode);
                    return result;
                }
            }
        } catch (SessionNotReadyException e) {
            transientTtl = TimeSpan.FromSeconds(20); // 会话重建后很快恢复, 不做长负缓存
            Log.Warning("[SkinSocket]    session not ready for \"{Player}\": {Msg} (short cache {S}s)", player, e.Message, 20);
        } catch (Exception e) {
            Log.Error("[SkinSocket]    online fetch error for \"{Player}\": {Msg}", player, e.Message);
        }

        // 无皮肤玩家: 回默认皮肤(和网易官方一致), 短 TTL 缓存; 临时失败用更短 TTL
        var fallback = DefaultResult();
        PutCache(player, fallback, permanent: false, ttl: transientTtl);
        Log.Information("[SkinSocket]    no online skin for \"{Player}\", using {Fallback}",
            player, string.IsNullOrEmpty(fallback.Path) ? "empty reply" : "default skin_10000.png");
        return fallback;
    }

    private static SkinResult DefaultResult() {
        var defaultSkin = Path.Combine(SKINS_DIR, "skin_10000.png");
        return new SkinResult(File.Exists(defaultSkin) ? defaultSkin : "", 0);
    }

    private static void PutCache(string player, SkinResult result, bool permanent, TimeSpan? ttl = null) {
        SkinCache[player] = new CachedSkin {
            Result = result,
            ExpiresAtTicks = permanent ? PermanentTicks : DateTime.UtcNow.Ticks + (ttl ?? NegativeTtl).Ticks
        };
    }

    /// <summary>
    /// 网易在线取皮：UUID → UID → 查已装备皮肤(Java→Cpp→All 串行fallback) → 取下载URL → 下载PNG。
    /// GameType=2(联机) 已在请求内, 首查即返回当前装备皮肤; 大多数玩家 1 次查询完成,
    /// 相比三路并行(80人=240次API排队)总量降 2/3, 进服加载显著加快。
    /// Tab 预热(UidPlanCache)命中时跳过全部 API, 直接下载(通常已落盘)秒回。
    /// </summary>
    private static async Task<(string Path, int Mode)?> FetchNeteaseSkin(string player, string uuid) {
        var uid = Skip32.ComputeUserIdFromUuid(uuid);
        if (uid == 0) {
            Log.Warning("[SkinSocket]    uid=0 from uuid {Uuid} (offline uuid?)", uuid);
            return null;
        }

        // 0a) Tab 预热正在跑: 稍等它完成(最多2.5秒)复用结果; 预热被限流可能排队, 超时就走真人优先通道, 不久等
        if (PreheatTasks.TryGetValue(uid, out var running)) {
            await Task.WhenAny(running, Task.Delay(TimeSpan.FromSeconds(2.5)));
            if (running.IsCompletedSuccessfully)
                Log.Information("[SkinSocket]    uid={Uid} joined in-flight preheat", uid);
        }

        // 0b) Tab 预热命中: 跳过全部 API 查询, 直接下载(预热期通常已落盘, 秒回)
        if (UidPlanCache.TryGetValue(uid, out var preheatPlan)) {
            var preheated = await DownloadSkinByIdAsync(preheatPlan.SkinId);
            if (preheated != null) {
                int hitMode = preheatPlan.RawMode;
                if (hitMode != 0) {
                    Log.Information("[SkinSocket]    skin mode {Mode}(Alex) -> 0(Steve)", hitMode);
                    hitMode = 0;
                }
                Log.Information("[SkinSocket]    uid={Uid} preheat hit, skin_id={SkinId} instant", uid, preheatPlan.SkinId);
                return (preheated, hitMode);
            }
            Log.Warning("[SkinSocket]    preheated skin {SkinId} download failed, fallback to full query", preheatPlan.SkinId);
        }

        // 1) 查询 + 选择首选皮肤(与预热共用逻辑)
        var outcome = await QuerySkinPlanAsync(uid, player);
        if (outcome == null) {
            Log.Information("[SkinSocket]    no equipped skin for uid={Uid}", uid);
            return null;
        }
        var (plan, distinctIds, total) = outcome.Value;
        UidPlanCache[uid] = plan;
        string skinId = plan.SkinId;
        int mode = plan.RawMode;

        // 2) 只同步下载【首选】皮肤即回复(磁盘缓存后瞬时返回); 失败立即回默认(快速响应),
        //    其余皮肤(含首选重试)全部后台并行预热落盘, 完成后写入缓存供下次请求命中。
        string? preferredPath = await DownloadSkinByIdAsync(skinId);
        if (preferredPath == null) {
            Log.Warning("[SkinSocket]    preferred skin {SkinId} download failed, replying default; preheating {N} skins in background",
                skinId, distinctIds.Count);
        }

        var rest = distinctIds.ToList(); // 全部(含首选重试)后台预热
        if (rest.Count > 0) {
            _ = Task.Run(async () => {
                try {
                    var dls = rest.Select(id => DownloadSkinByIdAsync(id)).ToArray();
                    await Task.WhenAll(dls);
                    int ok = dls.Count(t => t.IsCompletedSuccessfully && t.Result != null);
                    Log.Information("[SkinSocket]    background preheat uid={Uid}: {Ok}/{N} skins cached", uid, ok, rest.Count);
                } catch (Exception e) {
                    Log.Warning("[SkinSocket]    background preheat error uid={Uid}: {Msg}", uid, e.Message);
                }
            });
        }

        // Alex(纤细, mode=1) 皮肤按需求默认改为 Steve(经典, mode=0) 回复
        if (mode != 0) {
            Log.Information("[SkinSocket]    skin mode {Mode}(Alex) -> 0(Steve)", mode);
            mode = 0;
        }

        Log.Information("[SkinSocket]    uid={Uid} reply uses skin_id={SkinId} ({N} texture item(s), {D} more preheating)",
            uid, skinId, total, rest.Count);
        return (preferredPath, mode);
    }

    /// <summary>
    /// 查询 uid 在联机场景当前装备的皮肤并选出首选: Java→Cpp→All 串行 fallback,
    /// 严格按端对应不混用; 优先 NetGame + SKIN(31) 类型条目。player 仅为日志用(预热时可空)。
    /// 返回 (首选计划, 全部有效 skinId 去重, 查询条目总数); 无可用皮肤返回 null。
    /// </summary>
    private static async Task<(SkinPlan Plan, List<string> AllSkinIds, int Total)?> QuerySkinPlanAsync(long uid, string? player) {
        // 按端 fallback 查询: Java(自身端) -> Cpp(基岩) -> All, 查到即停
        EntityUserGameTexture[]? found = null;
        foreach (var ct in new[] { EnumGameClientType.Java, EnumGameClientType.Cpp, EnumGameClientType.All }) {
            found = await QueryEquippedSkinsAsync(uid, ct);
            if (found is { Length: > 0 }) break;
        }
        var skins = found ?? Array.Empty<EntityUserGameTexture>();
        if (skins.Length == 0) return null;

        // 按客户端类型严格对应, 不混用:
        //   Java 端有已装备皮肤 -> Java 玩家, 只显示 Java 皮肤;
        //   Java 没有、Cpp 有   -> 基岩玩家, 只显示基岩(Cpp)皮肤;
        //   两端都没有          -> 最后看 All 类型。
        // 优先取 game_type=NetGame(联机) 的条目(与查询参数一致), 防止 API 忽略过滤参数。
        static bool ValidSkin(EntityUserGameTexture s) => !string.IsNullOrEmpty(s.SkinId) && s.SkinId.Length > 5;
        var javaSkins = skins.Where(s => s.ClientType == EnumGameClientType.Java && ValidSkin(s)).ToArray();
        var cppSkins = skins.Where(s => s.ClientType == EnumGameClientType.Cpp && ValidSkin(s)).ToArray();
        var allSkins = skins.Where(s => s.ClientType == EnumGameClientType.All && ValidSkin(s)).ToArray();

        var pool = javaSkins.Length > 0 ? javaSkins
                 : cppSkins.Length > 0 ? cppSkins
                 : allSkins;
        var netGameSkins = pool.Where(s => s.GameType == EnumGType.NetGame).ToArray();
        if (netGameSkins.Length > 0) pool = netGameSkins;
        // 优先 skin_type=SKIN(31) 普通皮肤条目; 41=四维皮肤/42=特殊皮肤, 对其下载列表 API 常返回空
        var normalSkins = pool.Where(s => s.SkinType == EnumTextureType.SKIN).ToArray();
        if (normalSkins.Length > 0) pool = normalSkins;
        if (pool.Length == 0) {
            // 诊断: 打印条目明细, 看 skin_id 为空/过短的条目到底是什么类型
            foreach (var s in skins.Take(10)) {
                Log.Information("[SkinSocket]      item: ct={Ct} skinType={St} gameType={Gt} skinId='{Sid}' mode={M}",
                    s.ClientType, s.SkinType, s.GameType, s.SkinId, s.SkinMode);
            }
            Log.Information("[SkinSocket]    no valid skin_id (len>5) for uid={Uid}{Player}", uid, player == null ? "" : $" player={player}");
            return null;
        }
        var chosen = pool[0];
        if (player != null) {
            Log.Information("[SkinSocket]    player {Player} is {Kind} client (java={J} cpp={C} all={A})",
                player, javaSkins.Length > 0 ? "java" : cppSkins.Length > 0 ? "cpp(基岩)" : "all",
                javaSkins.Length, cppSkins.Length, allSkins.Length);
        }

        var distinctIds = skins
            .Where(s => !string.IsNullOrEmpty(s.SkinId) && s.SkinId.Length > 5)
            .Select(s => s.SkinId)
            .Distinct()
            .ToList();
        return (new SkinPlan(chosen.SkinId, chosen.SkinMode), distinctIds, skins.Length);
    }

    private sealed class SessionNotReadyException : Exception {
        public SessionNotReadyException(string msg) : base(msg) { }
    }

    private static async Task<EntityUserGameTexture[]?> QueryEquippedSkinsAsync(long uid, EnumGameClientType ct) {
        try {
            var req = new EntityUserGameTextureRequest {
                UserId = uid.ToString(),
                ClientType = ct,
                GameType = "2", // EnumGType.NetGame: 只查玩家在【联机】场景当前装备的皮肤(网易官方机制, 不传则返回拥有列表导致皮肤重复)
                GameId = InterConn.LastGameId ?? Nirvana.Heypixel.HeypixelProtocol.GameId // 显式携带当前联机服 game_id(服务器上下文), 否则可能 code=0 但 dataCount=0
            };
            async Task<EntitiesWPFLauncher<EntityUserGameTexture>?> CallApiAsync() {
                await GatewayGate.WaitAsync();
                try { return await NPFLauncher.GetSkinListInGameAAsyncRaw(req); }
                finally { GatewayGate.Release(); }
            }
            var entity = await CallApiAsync();
            // code=10(请先登录): game-play 会话过期, 重建会话后重试一次
            if (entity != null && entity.Code == 10) {
                Log.Warning("[SkinSocket]    uid={Uid} ct={Ct} code=10 会话过期, 重建游戏会话后重试", uid, ct);
                await InterConn.EnsureSessionAsync();
                entity = await CallApiAsync();
                if (entity != null && entity.Code == 10) {
                    throw new SessionNotReadyException($"uid={uid} code=10 after session refresh");
                }
            }
            if (entity == null) {
                Log.Warning("[SkinSocket]    uid={Uid} ct={Ct} API returned null entity", uid, ct);
            } else {
                Log.Information("[SkinSocket]    uid={Uid} ct={Ct} code={Code} msg={Msg} dataCount={Count}",
                    uid, ct, entity.Code, entity.Message ?? "(null)", entity.Data?.Length ?? 0);
            }
            return entity?.Data;
        } catch (SessionNotReadyException) {
            throw; // 会话仍未就绪: 向上传播, 不写负缓存, 短缓存后可快速重试
        } catch (Exception e) {
            Log.Warning("[SkinSocket]    uid={Uid} ct={Ct} query failed: {Msg}", uid, ct, e.Message);
            return null;
        }
    }

    /// <summary>
    /// 按皮肤资源ID下载PNG(已缓存则直接复用), 返回本地文件绝对路径.
    /// </summary>
    private static async Task<string?> DownloadSkinByIdAsync(string skinId) {
        Directory.CreateDirectory(SKINS_DIR);
        var local = Path.Combine(SKINS_DIR, "skin_" + skinId + ".png");
        if (File.Exists(local) && new FileInfo(local).Length > 8) {
            return local; // 已缓存
        }

        // 取下载 URL(资源ID->URL 映射静态, 命中缓存直接省掉一次网关往返)
        string? resUrl;
        if (ResUrlCache.TryGetValue(skinId, out var cachedUrl)) {
            resUrl = cachedUrl;
        } else {
            await GatewayGate.WaitAsync();
            try {
                var dlInfo = await NPFLauncher.GetNetGameComponentDownloadListAAsync(skinId);
                resUrl = dlInfo?.SubEntities?
                    .Select(sub => sub.ResUrl)
                    .FirstOrDefault(u => !string.IsNullOrEmpty(u));
            } finally {
                GatewayGate.Release();
            }
            if (resUrl == null) {
                Log.Warning("[SkinSocket]    no res_url for skin {SkinId}", skinId);
                return null;
            }
            ResUrlCache[skinId] = resUrl;
        }

        await DownloadGate.WaitAsync();
        byte[]? png;
        try {
            png = await Http.GetByteArrayAsync(resUrl);
        } finally {
            DownloadGate.Release();
        }
        if (png == null || png.Length < 8) {
            Log.Warning("[SkinSocket]    skin download failed: {Url}", resUrl);
            return null;
        }
        await File.WriteAllBytesAsync(local, png);
        return local;
    }

    // ===== 协议工具 =====

    private static string? ReadString(byte[] payload, int off) {
        if (off + 2 > payload.Length) return null;
        int l = payload[off] | (payload[off + 1] << 8);
        if (off + 2 + l > payload.Length) return null;
        return Encoding.UTF8.GetString(payload, off + 2, l);
    }

    /// <summary>写一帧(2B 小端长度 + payload)。多线程并行处理时必须持同一把锁, 防止帧交错。</summary>
    private static void WriteFrame(NetworkStream stream, byte[] payload, object? writeLock = null) {
        var frame = new byte[payload.Length + 2];
        frame[0] = (byte)(payload.Length & 0xFF);
        frame[1] = (byte)((payload.Length >> 8) & 0xFF);
        Buffer.BlockCopy(payload, 0, frame, 2, payload.Length);
        lock (writeLock ?? stream) {
            stream.Write(frame, 0, frame.Length);
            stream.Flush();
        }
    }

    private static void WriteString(MemoryStream p, string s) {
        var b = Encoding.UTF8.GetBytes(s);
        p.WriteByte((byte)(b.Length & 0xFF));
        p.WriteByte((byte)((b.Length >> 8) & 0xFF));
        p.Write(b, 0, b.Length);
    }

    private static void WriteIntLE(MemoryStream p, int v) {
        p.WriteByte((byte)(v & 0xFF));
        p.WriteByte((byte)((v >> 8) & 0xFF));
        p.WriteByte((byte)((v >> 16) & 0xFF));
        p.WriteByte((byte)((v >> 24) & 0xFF));
    }

    private static void WriteLongLE(MemoryStream p, long v) {
        for (int i = 0; i < 8; i++) p.WriteByte((byte)((v >> (8 * i)) & 0xFF));
    }

    /// <summary>
    /// 4613 聊天禁言回复: handler(boolean ban, long banChatExpiredAt, long delta).
    /// 回 ban=false / expireAt=0 / delta=0 -> 游戏判定未禁言且永久放行.
    /// </summary>
    private static byte[] BuildChatBanReply() {
        var p = new MemoryStream();
        p.WriteByte((byte)(SMID_CHAT_BAN >> 8)); // 0x12
        p.WriteByte((byte)(SMID_CHAT_BAN & 0xFF)); // 0x05
        p.WriteByte(0);                 // ban = false (boolean, 1B)
        WriteLongLE(p, 0L);             // banChatExpiredAt = 0 (long, 8B LE)
        WriteLongLE(p, 0L);             // delta = 0 (long, 8B LE)
        return p.ToArray();
    }

    /// <summary>
    /// 4614 物品名封禁回复: handler(String itemName, boolean ban, long expireAt, String reason, long delta).
    /// itemName 原样回传(游戏按名字更新对应 ItemBanInfo), ban=false.
    /// </summary>
    private static byte[] BuildItemBanReply(string itemName) {
        var p = new MemoryStream();
        p.WriteByte((byte)(SMID_ITEM_BAN >> 8)); // 0x12
        p.WriteByte((byte)(SMID_ITEM_BAN & 0xFF)); // 0x06
        WriteString(p, itemName);       // itemName (String)
        p.WriteByte(0);                 // ban = false (boolean, 1B)
        WriteLongLE(p, 0L);             // expireAt = 0 (long, 8B LE)
        WriteString(p, "");             // reason = "" (String)
        WriteLongLE(p, 0L);             // delta = 0 (long, 8B LE)
        return p.ToArray();
    }

    private static byte[] BuildReconnectReply() {
        var p = new MemoryStream();
        p.WriteByte((byte)(SMID_RECONNECT_REPLY >> 8)); // 0x07
        p.WriteByte((byte)(SMID_RECONNECT_REPLY & 0xFF)); // 0x04
        WriteString(p, "127.0.0.1"); // host
        WriteIntLE(p, 25565); // port
        WriteString(p, "127.0.0.1:25565"); // room
        p.WriteByte(0); // isNew=false
        return p.ToArray();
    }

    private static byte[] BuildSkinReply(string player, string skinPath, int mode) {
        var p = new MemoryStream();
        p.WriteByte((byte)(SMID_SKIN >> 8)); // 0x08
        p.WriteByte((byte)(SMID_SKIN & 0xFF)); // 0x02
        WriteString(p, player);
        WriteString(p, skinPath);
        WriteString(p, ""); // cape
        WriteIntLE(p, mode);
        return p.ToArray();
    }

    // ===== 皮肤缓存工具 =====

    private static string? FindSkin(string player) {
        if (!Directory.Exists(SKINS_DIR)) return null;
        var exact = Path.Combine(SKINS_DIR, player + ".png");
        if (File.Exists(exact)) return exact;
        foreach (var f in Directory.GetFiles(SKINS_DIR, "*.png")) {
            var name = Path.GetFileNameWithoutExtension(f);
            if (name.Equals(player, StringComparison.OrdinalIgnoreCase)) return f;
        }
        return null;
    }

    /// <summary>
    /// 预置 vanilla SkinManager 皮肤缓存，使游戏直接从磁盘读取皮肤。
    /// sha256 = SHA-256(文件内容)
    /// cacheHash = SHA-1(sha256Hex 按 UTF-16BE 编码)
    /// 缓存路径 = &lt;gameDir&gt;/skins/&lt;cacheHash[:2]&gt;/&lt;cacheHash&gt;
    /// </summary>
    private static void SeedVanillaCache(string skinPath) {
        if (string.IsNullOrEmpty(VANILLA_SKIN_CACHE) || string.IsNullOrEmpty(skinPath)) return;
        try {
            var data = File.ReadAllBytes(skinPath);
            var sha256 = SHA256.HashData(data);
            var sha256Hex = Convert.ToHexStringLower(sha256);
            var sha1 = SHA1.HashData(Encoding.Unicode.GetBytes(sha256Hex));
            var cacheHash = Convert.ToHexStringLower(sha1);
            var dir = Path.Combine(VANILLA_SKIN_CACHE, cacheHash.Length > 2 ? cacheHash[..2] : "xx");
            var dest = Path.Combine(dir, cacheHash);
            if (File.Exists(dest)) return;
            Directory.CreateDirectory(dir);
            File.Copy(skinPath, dest, true);
            Log.Information("[SkinSocket] seeded vanilla skin cache: {Path}", dest);
        } catch (Exception e) {
            Log.Warning("[SkinSocket] seed cache error: {Msg}", e.Message);
        }
    }
}
