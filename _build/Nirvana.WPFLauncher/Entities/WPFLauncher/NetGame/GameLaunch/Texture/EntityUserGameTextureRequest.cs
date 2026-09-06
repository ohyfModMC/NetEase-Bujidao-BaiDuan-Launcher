using System.Text.Json.Serialization;
using Nirvana.WPFLauncher.Entities.WPFLauncher.Minecraft;

namespace Nirvana.WPFLauncher.Entities.WPFLauncher.NetGame.GameLaunch.Texture;

public class EntityUserGameTextureRequest {
    [JsonPropertyName("user_id")]
    public string UserId { get; set; } = string.Empty;

    [JsonPropertyName("game_type")]
    public string GameType { get; set; } = string.Empty;

    [JsonPropertyName("client_type")]
    public EnumGameClientType ClientType { get; set; }

    /// <summary>当前联机服 game_id(服务器上下文)。不显式携带时 API 可能返回 code=0 + dataCount=0。</summary>
    [JsonPropertyName("game_id")]
    public string GameId { get; set; } = string.Empty;
}