using System.Diagnostics;
using System.IO;
using System.Net.NetworkInformation;
using System.Text;
using System.Text.Json;
using Nirvana.Cipher.Cipher.Nirvana.Connection;
using Nirvana.Common.Entities.Login;
using Nirvana.Common.Utils;
using Nirvana.Common.Utils.CodeTools;
using Nirvana.Public.Message;
using Nirvana.WPFLauncher.Entities.WPFLauncher.Login;
using Nirvana.WPFLauncher.Entities.WPFLauncher.NetGame.GameCharacters;
using Nirvana.WPFLauncher.Protocol;
using Serilog;

namespace Nirvana.Cli;

/// <summary>
/// 命令行「布吉岛」网易 Java 服务器代理启动器。
///
/// 严格参照 Fantnel 源代码 (AccountMessage / NPFLauncher / ProxiesMessage /
/// InterceptorManager / NetEaseConnection) 的工作原理实现，不引入 Web API。
///
/// 流程：
///   打开 -> 输入 4399com 账号 或 选择旧账号(本地 account.json)
///        -> 输入服务器内的玩家名称(创建/选择历史, 一个 4399 最多创建 3 个)
///        -> 自动代理布吉岛服务器 25565 端口
///           如被占用则尝试终止占用进程并重试
/// </summary>
public static class Program {
    // 布吉岛·新玩法上线 (网易网络服, mode = net)
    private const string BujidaoServerId = "4661334467366178884";

    // Minecraft TCP 默认端口
    private const int LocalPort = 25565;

    /// <summary>游戏目录 (.minecraft)，供 SkinSocketServer 预置 vanilla 皮肤缓存用</summary>
    public static string? GameDir { get; private set; }

    /// <summary>本地皮肤目录 (skins_local)，供 SkinSocketServer 查找手工皮肤用</summary>
    public static string? SkinsDir { get; private set; }

    public static async Task<int> Main(string[] args) {
        Console.OutputEncoding = Encoding.UTF8;
        Encoding.RegisterProvider(CodePagesEncodingProvider.Instance);
        InitLog();

        // 确保运行所需目录存在 (源代码 AccountMessage.SaveAccount 写 account.json 需 resources 目录)
        Directory.CreateDirectory(PathUtil.ResourcePath);
        Directory.CreateDirectory(PathUtil.CachePath);
        Directory.CreateDirectory(PathUtil.PluginsPath);

        Log.Information("----- FantnelCli / 布吉岛代理启动器 -----");

        // 可选: 关闭涅槃云兜底认证, 仅使用网易官方认证
        foreach (var a in args) {
            if ("--authenticated_false".Equals(a)) {
                NetEaseConnection.IsServerAuthenticated = false;
                Log.Warning("已关闭涅槃云兜底认证，仅使用网易官方认证。");
            }
            // 读取 --gameDir 参数，供 SkinSocketServer 预置 vanilla 皮肤缓存用
            if (a.StartsWith("--gameDir=")) {
                GameDir = a["--gameDir=".Length..];
            }
            // 读取 --skinsDir 参数，供 SkinSocketServer 查找本地手工皮肤
            if (a.StartsWith("--skinsDir=")) {
                SkinsDir = a["--skinsDir=".Length..];
            }
        }
        // 也支持环境变量 GAMEDIR
        GameDir ??= Environment.GetEnvironmentVariable("GAMEDIR");
        // 也支持环境变量 SKINS_DIR
        SkinsDir ??= Environment.GetEnvironmentVariable("SKINS_DIR");

        try {
            // 0. 启动 9876 端口皮肤 socket 服务器 (在 fantnelcli 进程内, 不会 token 冲突)
            //    必须在登录之前启动: 网易 mod 的 NetworkSocket.init() 在游戏启动时就会
            //    连 127.0.0.1:9876 做 check-in, 连不上就 closeMinecraft() 退出。
            //    登录前的皮肤请求会回复空串(默认皮肤), 登录完成后在线取皮就可用。
            Log.Information("--------------");
            Log.Information("[步骤] 启动 9876 皮肤 socket 服务器");
            SkinSocketServer.Start();

            // 1. 登录 (4399com 账号 / 选择旧账号)
            var account = await RunStepAsync("登录", ResolveLoginAsync);

            // 2. 布吉岛角色 选择/创建
            var roleName = await RunStepAsync("选择/创建角色", SelectOrCreateRoleAsync);

            // 记录自己的角色名: skins_local/skin_me.png 可自定义自己皮肤
            SkinSocketServer.SelfPlayerName = roleName;

            // 3. 端口占用处理 (被占则终止占用进程并重试)
            Log.Information("--------------");
            Log.Information("[步骤] 检查端口 {0}", LocalPort);
            EnsurePortFree(LocalPort);

            // 4. 启动代理 (严格按源代码: ProxiesMessage.StartProxyAsync)
            Log.Information("--------------");
            Log.Information("[步骤] 启动本地代理: 布吉岛·新玩法上线 (角色: {0})", roleName);
            await ProxiesMessage.StartProxyAsync(BujidaoServerId, roleName).ConfigureAwait(false);

            Log.Information("[代理启动完成] 请使用已打补丁的网易 Java 客户端连接 本机端口(默认 25565)。");

            Log.Information("输入 [q]+回车 或 Ctrl+C 退出代理。");

            await KeepAliveAsync();

            Log.Information("已退出。");
            return 0;
        } catch (ErrorCodeException ex) {
            Log.Error("操作失败: {0}", ex.Message);
            return 1;
        } catch (JsonException ex) {
            // JSON 解析失败: 通常是 4399/网易返回了非 JSON 文本(如中文错误消息)
            Log.Error("数据解析失败 [{0}]: {1}", ex.GetType().Name, ex.Message);
            Log.Error("  路径: {0}  行: {1}  列: {2}", ex.Path, ex.LineNumber, ex.BytePositionInLine);
            Log.Error("  这通常是账号/密码/验证码有误, 或验证码已过期, 服务器返回了中文错误文本而非 JSON。");
            Log.Error("  请核对 4399com 账号密码, 并重新运行 (验证码有效期较短, 输入请尽快)。");
            return 3;
        } catch (Exception ex) {
            Log.Error("未预期错误 [{0}]: {1}", ex.GetType().Name, ex.Message);
            Log.Error("  内层: {0}", ex.GetBaseException().Message);
            return 2;
        } finally {
            // 退出时暂停, 防止窗口一闪而过看不清错误信息
            try {
                Log.Information("--------------");
                Log.Information("程序即将退出, 按回车键关闭窗口 ...");
                Console.Out.Flush();
                Console.ReadLine();
            } catch {
                // 无控制台环境下忽略
            }
            Log.CloseAndFlush();
        }
    }

    // ---------------- 登录 ----------------

    private static async Task<T> RunStepAsync<T>(string stepName, Func<Task<T>> step) {
        Log.Information("--------------");
        Log.Information("[步骤] {0}", stepName);
        try {
            return await step();
        } catch (Exception e) {
            Log.Error("[步骤] {0} 失败: {1}", stepName, e.GetBaseException().Message);
            throw;
        }
    }

    private static async Task<EntityAccount> ResolveLoginAsync() {
        // 列出本地 account.json 中已保存的旧账号 (循环菜单: 可选号/删除/新建)
        while (true) {
            EntityAccount[] list;
            try {
                list = AccountMessage.GetAccountList();
            } catch (Exception e) {
                Log.Warning("读取本地账号列表失败: {0}", e.Message);
                list = [];
            }

            if (list.Length > 0) {
                Log.Information("检测到 {0} 个已保存账号(历史用户):", list.Length);
                for (var i = 0; i < list.Length; i++) {
                    var a = list[i];
                    var display = a.Type == "cookie"
                        ? $"[Cookie] {a.Name}"
                        : $"[{a.Type}] {a.Account} ({a.Name})";
                    var uid = string.IsNullOrEmpty(a.UserId) ? "" : $"  UID={a.UserId}";
                    Log.Information("  [{0}] {1}{2}", i, display, uid);
                }

                Log.Information("输入已有账号编号选择; 输入 c 使用 Cookie 登录; 输入 d+编号 删除账号(如 d0); 直接回车新建 4399com 账号: ");
                var sel = Console.ReadLine()?.Trim() ?? "";

                // Cookie 登录
                if (sel.Equals("c", StringComparison.OrdinalIgnoreCase)) {
                    return await LoginWithCookieAsync();
                }

                // 删除账号: d<编号> / del<编号>
                if ((sel.StartsWith("d") || sel.StartsWith("D")) && sel.Length > 1) {
                    var numPart = sel.Substring(sel.StartsWith("del", StringComparison.OrdinalIgnoreCase) ? 3 : 1).Trim();
                    if (int.TryParse(numPart, out var delIdx) && delIdx >= 0 && delIdx < list.Length) {
                        var target = list[delIdx];
                        Log.Warning("确认删除账号 [{0}] {1} ({2}) ? 输入 y 确认, 其它键取消:",
                            delIdx, target.Account, target.Name);
                        var confirm = Console.ReadLine()?.Trim().ToLower();
                        if (confirm == "y" || confirm == "yes") {
                            try {
                                AccountMessage.DeleteAccount(target.Id ?? delIdx);
                                Log.Information("已删除账号 {0}。", target.Account);
                            } catch (Exception e) {
                                Log.Error("删除账号失败: {0}", e.Message);
                            }
                        } else {
                            Log.Information("已取消删除。");
                        }
                        continue; // 重新显示账号列表
                    }
                    Log.Warning("无法识别的删除指令: {0}", sel);
                    continue;
                }

                if (int.TryParse(sel, out var idx) && idx >= 0 && idx < list.Length) {
                    return await LoginExistingAsync(list[idx]);
                }

                if (sel.Length == 0) {
                    return await LoginNewAsync();
                }

                Log.Warning("无效输入: {0}, 请重新选择。", sel);
                continue;
            }

            Log.Information("未检测到已保存账号。输入 c 使用 Cookie 登录, 或直接回车新建 4399com 账号: ");
            var choice = Console.ReadLine()?.Trim() ?? "";
            if (choice.Equals("c", StringComparison.OrdinalIgnoreCase)) {
                return await LoginWithCookieAsync();
            }
            return await LoginNewAsync();
        }
    }

    // 登录旧账号
    private static async Task<EntityAccount> LoginExistingAsync(EntityAccount account) {
        // Cookie 账号: 无需验证码, 直接用保存的 Cookie 重新登录
        if ("cookie".Equals(account.Type)) {
            Log.Information("选择 Cookie 账号: {0}", account.Name ?? account.Account);
            if (!string.IsNullOrEmpty(account.UserId) && !string.IsNullOrEmpty(account.Token)) {
                Log.Information("Cookie 账号仍处于登录态, 直接切换使用。");
                try {
                    AccountMessage.SwitchAccount(account.Id ?? 0);
                    return account;
                } catch (Exception e) {
                    Log.Warning("直接切换失败: {0}, 重新 Cookie 登录。", e.Message);
                }
            }
            // 重新用 Cookie 登录 (底层 AccountMessage.Login -> NPFLauncher.LoginWithCookie)
            AccountMessage.Login(account.Id ?? 0);
            AccountMessage.SwitchAccount(account.Id ?? 0);
            return AccountMessage.GetAccountList().FirstOrDefault(a => a.Id == account.Id) ?? account;
        }

        // 本 CLI 仅支持 4399com / 4399 账号登录
        if (!"4399com".Equals(account.Type) && !"4399".Equals(account.Type)) {
            Log.Warning("旧账号类型 {0} 不被本 CLI 支持, 改为新建 4399com 账号。", account.Type);
            return await LoginNewAsync();
        }

        if (string.IsNullOrEmpty(account.Account) || string.IsNullOrEmpty(account.Password)) {
            Log.Warning("旧账号信息不完整, 改为新建。");
            return await LoginNewAsync();
        }

        Log.Information("选择账号: {0} ({1})", account.Account, account.Type);

        // 若旧账号仍处于登录态 (UserId/Token 有效), 直接切换, 免验证码
        if (!string.IsNullOrEmpty(account.UserId) && !string.IsNullOrEmpty(account.Token)) {
            Log.Information("旧账号仍处于登录态, 直接切换使用。");
            try {
                AccountMessage.SwitchAccount(account.Id ?? 0);
                return account;
            } catch (Exception e) {
                Log.Warning("直接切换失败: {0}, 改为重新登录。", e.Message);
            }
        }

        // 否则走验证码登录 (AccountMessage.Login 走 NCom4399 + NPFLauncher.LoginWithCookie)
        await PrepareCaptchaAsync();
        AccountMessage.Login(account.Id ?? 0);
        AccountMessage.SwitchAccount(account.Id ?? 0);

        // 返回登录后的最新账号信息 (含 UserId/Token)
        return AccountMessage.GetAccountList().FirstOrDefault(a => a.Id == account.Id) ?? account;
    }

    // 新建 4399com 账号
    private static async Task<EntityAccount> LoginNewAsync() {
        var user = Ask("请输入 4399com 用户名: ");
        var pass = ReadPassword("请输入 4399 账号密码: ");
        Log.Information("");
        if (string.IsNullOrEmpty(user) || string.IsNullOrEmpty(pass)) {
            throw new ErrorCodeException(ErrorCode.AccountError);
        }

        // 准备验证码 (本地保存图片 + 人工输入, 不走涅槃云 OCR)
        await PrepareCaptchaAsync();

        var account = new EntityAccount {
            Account = user,
            Password = pass,
            Type = "4399com",
            Name = user
        };

        // 保存到 account.json (源代码: AccountMessage.SaveAccount)
        // 注: 对 4399com 类型, SaveAccount 内部的 AutoLogin 不会触发登录, 安全
        Log.Information("[登录] 保存账号到本地 account.json ...");
        AccountMessage.SaveAccount(account);

        // 重新取出带 Id 的账号 (Id 由 GetAccountList 在读取时按序赋值)
        var saved = AccountMessage.GetAccountList()
            .FirstOrDefault(a => user.Equals(a.Account) && "4399com".Equals(a.Type));
        if (saved == null) {
            throw new ErrorCodeException(ErrorCode.AccountError);
        }

        // 登录 (源代码: AccountMessage.Login -> NCom4399.LoginWithPassword + NPFLauncher.LoginWithCookie)
        Log.Information("[登录] 正在向 4399 提交账号密码 + 验证码, 并换取网易 X19 令牌 ...");
        AccountMessage.Login(saved.Id ?? 0);
        Log.Information("[登录] 登录成功, 切换为当前游戏账号。");
        AccountMessage.SwitchAccount(saved.Id ?? 0);

        return AccountMessage.GetAccountList().FirstOrDefault(a => a.Id == saved.Id) ?? saved;
    }

    // 新建 Cookie 登录账号 (网易 X19 Cookie, 无需验证码)
    private static async Task<EntityAccount> LoginWithCookieAsync() {
        Log.Information("请提供网易 X19 Cookie 用于直接登录 (无需验证码)。");
        Log.Information("方式一: 直接粘贴完整 Cookie JSON (以 {{ 开头)");
        Log.Information("方式二: 回车后分行输入 sdkuid / sessionid / udid / deviceid");
        var first = Ask("Cookie: ")?.Trim();

        string cookieJson;
        if (!string.IsNullOrEmpty(first) && first.StartsWith("{")) {
            cookieJson = first;
        } else {
            var sdkuid = string.IsNullOrEmpty(first) ? Ask("sdkuid: ").Trim() : first;
            var sessionid = Ask("sessionid: ").Trim();
            var udid = Ask("udid: ").Trim();
            var deviceid = Ask("deviceid: ").Trim();
            if (string.IsNullOrEmpty(sdkuid) || string.IsNullOrEmpty(sessionid) ||
                string.IsNullOrEmpty(udid) || string.IsNullOrEmpty(deviceid)) {
                throw new ErrorCodeException(ErrorCode.AccountError, "Cookie 字段不完整 (需 sdkuid/sessionid/udid/deviceid)");
            }
            cookieJson = JsonSerializer.Serialize(new EntityX19Cookie {
                SdkUid = sdkuid,
                SessionId = sessionid,
                Udid = udid,
                DeviceId = deviceid
            });
        }

        var account = new EntityAccount {
            Type = "cookie",
            Password = cookieJson,
            Name = "Cookie账号"
        };

        // 保存到 account.json (SaveAccount 内部 AutoLogin 对 cookie 类型会直接调用 Login, 即登录)
        Log.Information("[登录] 保存 Cookie 账号并登录 ...");
        AccountMessage.SaveAccount(account);

        var saved = AccountMessage.GetAccountList()
            .FirstOrDefault(a => "cookie".Equals(a.Type) && cookieJson.Equals(a.Password));
        if (saved == null) {
            throw new ErrorCodeException(ErrorCode.AccountError, "Cookie 账号保存失败");
        }

        Log.Information("[登录] Cookie 登录成功, 切换为当前游戏账号。");
        AccountMessage.SwitchAccount(saved.Id ?? 0);
        return AccountMessage.GetAccountList().FirstOrDefault(a => a.Id == saved.Id) ?? saved;
    }

    // 准备验证码 (本地保存图片 + 人工输入内容, 严格遵循源 CLI 行为, 无涅槃云 OCR)
    private static async Task PrepareCaptchaAsync() {
        for (var attempt = 1; attempt <= 6; attempt++) {
            // 源代码: AccountMessage.UpdateCaptcha -> 下载验证码图片 + 生成 captchaId
            AccountMessage.UpdateCaptcha();

            if (AccountMessage.Captcha4399Bytes == null) {
                Log.Warning("第 {0} 次获取验证码失败, 重试...", attempt);
                await Task.Delay(500);
                continue;
            }

            var png = Path.Combine(AppContext.BaseDirectory, "captcha_" + attempt + ".png");
            await File.WriteAllBytesAsync(png, AccountMessage.Captcha4399Bytes);
            Log.Warning("验证码图片已保存到: {0}  请打开查看并输入其内容。", png);

            var text = Ask("请输入验证码内容(留空重新获取): ").Trim();
            if (text.Length == 0) {
                continue;
            }

            // 源代码: AccountMessage.Captcha4399 被 NCom4399.LoginWithPassword 用作 "captcha" 字段
            AccountMessage.Captcha4399 = text;
            return;
        }

        throw new ErrorCodeException(ErrorCode.LoginError);
    }

    // ---------------- 布吉岛角色 ----------------

    private static async Task<string> SelectOrCreateRoleAsync() {
        List<EntityGameCharacter> list;
        try {
            // 源代码: NPFLauncher.GetNetGameCharactersAsync 查询该服角色
            list = (await NPFLauncher.GetNetGameCharactersAsync(BujidaoServerId).ConfigureAwait(false)).ToList();
        } catch (Exception e) {
            Log.Warning("查询布吉岛角色失败(可能尚未在网易端授权): {0}", e.Message);
            list = [];
        }

        if (list.Count > 0) {
            Log.Information("账号在布吉岛已有 {0} 个游戏角色:", list.Count);
            for (var i = 0; i < list.Count; i++) {
                Log.Information("  [{0}] {1}  (创建于 {2})",
                    i, list[i].Name,
                    DateTimeOffset.FromUnixTimeSeconds(list[i].CreateTime).LocalDateTime);
            }

            if (list.Count >= 3) {
                Log.Warning("账号在布吉岛角色已达上限(3), 请输入已有角色的编号: ");
                var idx = ReadIndex("请输入要使用的角色编号: ", list.Count);
                return list[idx].Name;
            }

            Log.Information("输入已有角色编号, 或直接回车新建一个(上限3): ");
            var sel = Console.ReadLine()?.Trim();
            if (int.TryParse(sel, out var selIdx) && selIdx >= 0 && selIdx < list.Count) {
                return list[selIdx].Name;
            }
        } else {
            Log.Information("账号尚未在布吉岛创建游戏角色。");
        }

        // 新建
        for (var tries = 0; tries < 5; tries++) {
            var newName = Ask("请输入要在布吉岛使用的新角色昵称: ").Trim();
            if (newName.Length is < 1 or > 32) {
                Log.Warning("昵称需 1~32 字符。");
                continue;
            }

            try {
                // 源代码: NPFLauncher.CreateCharacterAsync
                await NPFLauncher.CreateCharacterAsync(BujidaoServerId, newName).ConfigureAwait(false);
                Log.Information("已提交创建角色: {0}", newName);
                // 网易可能异步生效, 尽力拉取确认
                for (var i = 0; i < 10; i++) {
                    await Task.Delay(1200).ConfigureAwait(false);
                    try {
                        var got = (await NPFLauncher.GetNetGameCharactersAsync(BujidaoServerId).ConfigureAwait(false))
                            .FirstOrDefault(c => c.Name == newName);
                        if (got != null) {
                            return got.Name;
                        }
                    } catch {
                        // 重试
                    }
                }

                return newName;
            } catch (Exception e) {
                Log.Error("创建角色失败: {0}  (若昵称已存在或达到上限请换名)", e.Message);
            }
        }

        throw new ErrorCodeException(ErrorCode.NotFoundName);
    }

    // ---------------- 端口占用处理 ----------------

    /// <summary>
    /// 确保端口空闲: 若被占用则尝试终止占用进程并重试。
    /// 严格使用 Windows 自带 netstat/taskkill, 不依赖任何外部 API。
    /// </summary>
    private static void EnsurePortFree(int port) {
        for (var attempt = 1; attempt <= 3; attempt++) {
            if (!IsPortListening(port)) {
                Log.Information("端口 {0} 空闲, 可用。", port);
                return;
            }

            Log.Warning("端口 {0} 被占用, 尝试终止占用进程 (第 {1} 次)...", port, attempt);

            var pids = GetPortOwnerPids(port);
            if (pids.Count == 0) {
                Log.Warning("未找到占用端口 {0} 的进程 PID, 可能是系统保留或正在释放, 等待后重试。", port);
                Thread.Sleep(1000);
                continue;
            }

            foreach (var pid in pids.Distinct()) {
                if (pid == Environment.ProcessId) {
                    // 不终止自己
                    continue;
                }

                try {
                    var psi = new ProcessStartInfo("taskkill", $"/F /PID {pid}") {
                        CreateNoWindow = true,
                        UseShellExecute = false
                    };
                    var p = Process.Start(psi);
                    p?.WaitForExit(3000);
                    Log.Information("已发送终止信号给进程 PID={0}", pid);
                } catch (Exception e) {
                    Log.Warning("终止进程 PID={0} 失败: {1}", pid, e.Message);
                }
            }

            // 等待端口释放
            Thread.Sleep(800);
            if (!IsPortListening(port)) {
                Log.Information("端口 {0} 已释放。", port);
                return;
            }
        }

        // 仍被占用: 交给源代码 ProxiesMessage -> Tools.GetUnusedPort 自动选择空闲端口
        Log.Warning("端口 {0} 仍被占用, 代理将自动选择空闲端口继续。", port);
    }

    private static bool IsPortListening(int port) {
        // 源代码 Tools.IsPortInUse 的同款实现
        var props = IPGlobalProperties.GetIPGlobalProperties();
        return props.GetActiveTcpListeners().Any(e => e.Port == port);
    }

    private static List<int> GetPortOwnerPids(int port) {
        var pids = new List<int>();
        try {
            var psi = new ProcessStartInfo("netstat", "-ano") {
                CreateNoWindow = true,
                UseShellExecute = false,
                RedirectStandardOutput = true
            };
            var p = Process.Start(psi);
            if (p == null) {
                return pids;
            }

            var output = p.StandardOutput.ReadToEnd();
            p.WaitForExit(5000);

            var lines = output.Split('\n', StringSplitOptions.RemoveEmptyEntries);
            foreach (var line in lines) {
                // 形如:  TCP    0.0.0.0:25565    0.0.0.0:0    LISTENING    1234
                if (!line.Contains(":" + port + " ")) {
                    continue;
                }

                var parts = line.Split(new[] { ' ' }, StringSplitOptions.RemoveEmptyEntries);
                if (parts.Length < 5) {
                    continue;
                }

                if (int.TryParse(parts[^1], out var pid)) {
                    pids.Add(pid);
                }
            }
        } catch (Exception e) {
            Log.Warning("netstat 解析失败: {0}", e.Message);
        }

        return pids;
    }

    // ---------------- 保活 ----------------

    private static async Task KeepAliveAsync() {
        using var cts = new CancellationTokenSource();
        Console.CancelKeyPress += (_, e) => {
            e.Cancel = true;
            Log.Information("收到退出信号, 正在关闭...");
            cts.Cancel();
        };

        while (!cts.IsCancellationRequested) {
            if (Console.KeyAvailable) {
                var line = Console.ReadLine();
                if (string.Equals(line?.Trim(), "q", StringComparison.OrdinalIgnoreCase)) {
                    break;
                }
            } else {
                await Task.Delay(500, cts.Token).ConfigureAwait(false);
            }
        }
    }

    // ---------------- 日志 ----------------

    private static void InitLog() {
        // 说明: Cli 不调用原 Logger.LogoInit(), 因其会执行需要真实控制台的 Console.Clear(),
        // 在重定向(管道/后台)场景会抛 IOException。此处改用纯 Serilog 控制台、不做 Clear。
        Log.Logger = new LoggerConfiguration()
            .MinimumLevel.Information()
            .WriteTo.Console()
            .CreateLogger();
        Log.Information("日志已就绪。");
    }

    // ---------------- 小工具 ----------------

    private static string Ask(string prompt) {
        Console.Write(prompt);
        return Console.ReadLine() ?? string.Empty;
    }

    private static string ReadPassword(string prompt) {
        Console.Write(prompt);
        if (Console.IsInputRedirected) {
            return Console.ReadLine() ?? string.Empty;
        }

        var sb = new StringBuilder();
        while (true) {
            var k = Console.ReadKey(true);
            if (k.Key == ConsoleKey.Enter) {
                Console.WriteLine();
                break;
            }

            if (k.Key == ConsoleKey.Backspace && sb.Length > 0) {
                sb.Length--;
                continue;
            }

            if (k.KeyChar == '\0') {
                continue;
            }

            sb.Append(k.KeyChar);
        }

        return sb.ToString();
    }

    private static int ReadIndex(string prompt, int max) {
        while (true) {
            if (int.TryParse(Console.ReadLine(), out var v) && v >= 0 && v < max) {
                return v;
            }

            Console.Write(prompt);
        }
    }
}
