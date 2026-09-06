using Nirvana.Development.Manager;
using Nirvana.Heypixel.Configuration;
using Nirvana.Heypixel.Play;
using Serilog;

namespace Nirvana.Heypixel;

public class HeypixelProtocol {
    public const string GameId = "4661334467366178884";

    static HeypixelProtocol()
    {
        PacketManager.BasePackets.Add(new C2SConfigPluginMessage(), C2SConfigPluginMessage.RegisterPacket);
        PacketManager.BasePackets.Add(new SaClientboundSetPlayerTeamPacket(), SaClientboundSetPlayerTeamPacket.RegisterPacket);
        // 注意: SaClientboundPlayerInfoUpdatePacket 暂不注册。
        // Heypixel 是魔改协议, 所有包 ID 重新编号(Team 包=96 而非 vanilla 0x4E),
        // vanilla 的 0x3E(PlayerInfoUpdate) 在本协议里是另一个 20 字节高频小包, 注册会导致反复误解析。
        // 待抓包确认真实 PlayerInfoUpdate 包 ID 后再启用(类与解析逻辑已写好备用)。
        // PacketManager.BasePackets.Add(new SaClientboundPlayerInfoUpdatePacket(), SaClientboundPlayerInfoUpdatePacket.RegisterPacket);
    }

    public static void Init()
    {
        Log.Information("[Heypixel] Initializing.");
    }
}