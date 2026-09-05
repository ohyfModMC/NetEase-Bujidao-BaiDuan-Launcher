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
    }

    public static void Init()
    {
        Log.Information("[Heypixel] Initializing.");
    }
}