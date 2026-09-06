using DotNetty.Buffers;
using Nirvana.DevPlugin;
using Nirvana.DevPlugin.Enums;
using Nirvana.DevPlugin.Extensions;
using Nirvana.DevPlugin.Packet;
using Serilog;

namespace Nirvana.Heypixel.Play;

/// <summary>
/// Tab 玩家列表包 (1.20.6 clientbound 0x3E Player Info Update)。
/// 进服时服务器会立刻下发全部在线玩家的 UUID —— 提取出来触发皮肤预热,
/// 使游戏随后发出的 2050 皮肤请求直接命中已下载缓存, 头像/身体秒出。
/// FPacket 为透传包(原始字节快照回写), 解析不影响原包转发。
///
/// 注意: 每个玩家 entry = UUID(16B) + 按 action bitmask 的可变字段。
/// 提取完 UUID 后【必须】按 bitmask 跳过这些可变字段, 才能读到下一个玩家的 UUID;
/// 否则 readerIndex 错位, 后续 UUID 全是垃圾甚至越界异常(早期版本 bug)。
/// </summary>
public class SaClientboundPlayerInfoUpdatePacket : FPacket {
    public static readonly RegisterPacket RegisterPacket = new(EnumConnectionState.Play, EnumPacketDirection.ClientBound, 0x3E, HeypixelProtocol.GameId, EnumProtocolVersion.V1206);

    // Player Info Update action bitmask
    private const long ADD_PLAYER = 0x01;
    private const long INIT_CHAT = 0x02;
    private const long UPDATE_GAMEMODE = 0x04;
    private const long UPDATE_LISTED = 0x08;
    private const long UPDATE_LATENCY = 0x10;
    private const long UPDATE_DISPLAY_NAME = 0x20;
    private const long UPDATE_LIST_ORDER = 0x40;

    /// <summary>解析出的玩家 UUID 回调(Java toString 格式), SkinSocketServer 订阅后预热皮肤。</summary>
    public static Action<List<string>>? OnPlayerUuids;

    public override void ReadFromBuffer(BGameConnection connection, IByteBuffer buffer) {
        base.ReadFromBuffer(buffer); // 快照原始字节供转发
        try {
            // Actions: BitSet = VarInt(long 个数) + longCount*8 字节
            var longCount = buffer.ReadVarIntFromBuffer();
            if (longCount is < 1 or > 4) return;
            long actions = buffer.ReadLong();        // action 标志都在第一个 long
            buffer.SkipBytes((longCount - 1) * 8);

            var count = buffer.ReadVarIntFromBuffer();
            if (count is <= 0 or > 200) return;

            var uuids = new List<string>(count);
            for (int i = 0; i < count; i++) {
                ulong msb = (ulong)buffer.ReadLong();
                ulong lsb = (ulong)buffer.ReadLong();
                uuids.Add(FormatUuid(msb, lsb));

                // 按 bitmask 跳过该玩家的可变字段(顺序与协议一致)
                if ((actions & ADD_PLAYER) != 0) {
                    buffer.ReadStringFromBuffer(); // 玩家名
                    buffer.ReadWithCount(() => {   // 属性列表(textures 等)
                        buffer.ReadStringFromBuffer(); // name
                        buffer.ReadStringFromBuffer(); // value
                        buffer.ReadNullable(() => buffer.ReadStringFromBuffer()); // signature?
                    });
                }
                if ((actions & INIT_CHAT) != 0) {
                    bool hasSession = buffer.ReadBoolean();
                    if (hasSession) {
                        buffer.SkipBytes(16);            // chat session UUID
                        buffer.SkipBytes(8);             // expiresAt (long)
                        buffer.ReadByteArrayFromBuffer(); // encoded public key (VarInt+bytes)
                        buffer.ReadByteArrayFromBuffer(); // key signature (VarInt+bytes)
                    }
                }
                if ((actions & UPDATE_GAMEMODE) != 0) buffer.ReadVarIntFromBuffer();
                if ((actions & UPDATE_LISTED) != 0) buffer.ReadByte();
                if ((actions & UPDATE_LATENCY) != 0) buffer.ReadVarIntFromBuffer();
                if ((actions & UPDATE_DISPLAY_NAME) != 0) {
                    bool hasName = buffer.ReadBoolean();
                    if (hasName) SkipNbt(buffer); // display name 是 NBT compound
                }
                if ((actions & UPDATE_LIST_ORDER) != 0) buffer.ReadVarIntFromBuffer();
            }
            OnPlayerUuids?.Invoke(uuids);
        } catch (Exception e) {
            // 透传包, 解析失败不影响转发; 但要打日志便于定位(不再静默吞掉)
            Log.Warning("[PlayerInfo] parse failed (forward unaffected): {Msg}", e.Message);
        }
    }

    public override bool HandlePacket(BGameConnection connection) => false;

    // Java UUID.toString(): 高 8 字节(msb) + 低 8 字节(lsb), 各段无符号十六进制
    private static string FormatUuid(ulong msb, ulong lsb) {
        return string.Format("{0:x8}-{1:x4}-{2:x4}-{3:x4}-{4:x12}",
            (uint)(msb >> 32), (ushort)(msb >> 16), (ushort)msb,
            (ushort)(lsb >> 48), lsb & 0xFFFFFFFFFFFFUL);
    }

    // 跳过一个网络 NBT tag(root 无 name, 自 1.20.5)
    private static void SkipNbt(IByteBuffer b) {
        int rootType = b.ReadByte();
        SkipNbtPayload(b, rootType);
    }

    private static void SkipNbtPayload(IByteBuffer b, int type) {
        switch (type) {
            case 0: break;                                              // TAG_End
            case 1: b.SkipBytes(1); break;                              // Byte
            case 2: b.SkipBytes(2); break;                              // Short
            case 3: b.SkipBytes(4); break;                              // Int
            case 4: b.SkipBytes(8); break;                              // Long
            case 5: b.SkipBytes(4); break;                              // Float
            case 6: b.SkipBytes(8); break;                              // Double
            case 7: { b.SkipBytes(b.ReadInt()); break; }                // Byte Array (int len)
            case 8: { ushort n = b.ReadUnsignedShort(); b.SkipBytes(n); break; } // String (ushort len)
            case 9: {                                                  // List (元素无 name)
                int elem = b.ReadByte();
                int len = b.ReadInt();
                for (int i = 0; i < len; i++) { SkipNbtPayload(b, elem); }
                break;
            }
            case 10: {                                                 // Compound
                while (true) {
                    int t = b.ReadByte();
                    if (t == 0) break;
                    SkipNbtName(b);
                    SkipNbtPayload(b, t);
                }
                break;
            }
            case 11: { b.SkipBytes(b.ReadInt() * 4); break; }           // Int Array
            case 12: { b.SkipBytes(b.ReadInt() * 8); break; }           // Long Array
            default: throw new Exception("unknown NBT tag type " + type);
        }
    }

    // NBT 字段名: ushort 长度 + UTF8 字节(compound 内每个字段都有 name; root 无 name)
    private static void SkipNbtName(IByteBuffer b) {
        ushort n = b.ReadUnsignedShort();
        b.SkipBytes(n);
    }
}
