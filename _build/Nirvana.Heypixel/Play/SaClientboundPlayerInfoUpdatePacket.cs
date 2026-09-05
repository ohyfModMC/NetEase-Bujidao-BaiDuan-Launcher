using DotNetty.Buffers;
using Nirvana.DevPlugin;
using Nirvana.DevPlugin.Enums;
using Nirvana.DevPlugin.Extensions;
using Nirvana.DevPlugin.Packet;

namespace Nirvana.Heypixel.Play;

/// <summary>
/// Tab 玩家列表包 (1.20.6 clientbound 0x3E Player Info Update)。
/// 进服时服务器会立刻下发全部在线玩家的 UUID —— 提取出来触发皮肤预热,
/// 使游戏随后发出的 2050 皮肤请求直接命中已下载缓存, 头像/身体秒出。
/// FPacket 为透传包(原始字节快照回写), 解析不影响原包转发。
/// </summary>
public class SaClientboundPlayerInfoUpdatePacket : FPacket {
    public static readonly RegisterPacket RegisterPacket = new(EnumConnectionState.Play, EnumPacketDirection.ClientBound, 0x3E, HeypixelProtocol.GameId, EnumProtocolVersion.V1206);

    /// <summary>解析出的玩家 UUID 回调(Java toString 格式), SkinSocketServer 订阅后预热皮肤。</summary>
    public static Action<List<string>>? OnPlayerUuids;

    public override void ReadFromBuffer(BGameConnection connection, IByteBuffer buffer) {
        base.ReadFromBuffer(buffer); // 快照原始字节供转发
        try {
            // 1.20.6 结构: Actions=BitSet(VarInt longCount + longCount*8B) + Count(VarInt) + N×[UUID(16B) + 各action数据...]
            var longCount = buffer.ReadVarIntFromBuffer();
            if (longCount is < 0 or > 4) return;
            buffer.SkipBytes(longCount * 8);
            var count = buffer.ReadVarIntFromBuffer();
            if (count is <= 0 or > 200) return;

            var uuids = new List<string>(count);
            for (int i = 0; i < count; i++) {
                ulong msb = (ulong)buffer.ReadLong();
                ulong lsb = (ulong)buffer.ReadLong();
                uuids.Add(FormatUuid(msb, lsb));
                // 各 action 数据无需解析(预热只认 UUID), FPacket 转发与 readerIndex 无关
            }
            OnPlayerUuids?.Invoke(uuids);
        } catch {
            // 解析失败静默: 透传包, 不影响转发
        }
    }

    public override bool HandlePacket(BGameConnection connection) => false;

    // Java UUID.toString(): 高 8 字节(msb) + 低 8 字节(lsb), 各段无符号十六进制
    private static string FormatUuid(ulong msb, ulong lsb) {
        return string.Format("{0:x8}-{1:x4}-{2:x4}-{3:x4}-{4:x12}",
            (uint)(msb >> 32), (ushort)(msb >> 16), (ushort)msb,
            (ushort)(lsb >> 48), lsb & 0xFFFFFFFFFFFFUL);
    }
}
