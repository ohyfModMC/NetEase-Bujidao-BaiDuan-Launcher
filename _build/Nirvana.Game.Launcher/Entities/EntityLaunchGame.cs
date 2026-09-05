using System.Text.Json.Serialization;
using Nirvana.Common.Entities.Login;
using Nirvana.WPFLauncher.Entities.WPFLauncher.Minecraft;
using Nirvana.WPFLauncher.Entities.WPFLauncher.NetGame.GameLaunch.Texture;

namespace Nirvana.Game.Launcher.Entities;

public class EntityLaunchGame {
    [JsonPropertyName("game_name")]
    [JsonInclude]
    public string GameName { get; set; } = string.Empty;

    [JsonPropertyName("game_id")]
    [JsonInclude]
    public string GameId { get; init; } = string.Empty;

    [JsonPropertyName("role_name")]
    [JsonInclude]
    public string RoleName { get; init; } = string.Empty;

    [JsonPropertyName("client_type")]
    [JsonInclude]
    public EnumGameClientType ClientType { get; set; }

    [JsonPropertyName("game_type")]
    [JsonInclude]
    public EnumGType GameType { get; init; }

    [JsonPropertyName("game_version_id")]
    [JsonInclude]
    public int GameVersionId { get; init; }

    [JsonPropertyName("game_version")]
    [JsonInclude]
    public string GameVersion { get; init; } = string.Empty;

    [JsonPropertyName("account")]
    [JsonInclude]
    public required EntityUserInfo Account { get; init; }

    [JsonPropertyName("server_ip")]
    [JsonInclude]
    public string ServerIp { get; init; } = string.Empty;

    [JsonPropertyName("server_port")]
    [JsonInclude]
    public int ServerPort { get; init; }

    [JsonPropertyName("load_core_mods")]
    [JsonInclude]
    public bool LoadCoreMods { get; init; }

    [JsonPropertyName("id")]
    [JsonInclude]
    public required int Id { get; set; }

    /**
     * 清理 相同/过期 的代理
     * @param gameUser 游戏用户
     * @param serverId 服务器ID
     * @param nickname 昵称
     * @return 是否为同一个用户
     */
    public bool Equals(EntityAccount gameAccount, string serverId, string nickname)
    {
        if (gameAccount.UserId != null) {
            if (Equals(gameAccount.UserId, serverId, nickname)) {
                return true;
            }
        }

        return Equals(gameAccount);
    }

    /**
     * 判断是否为同一个用户, 服务器, 昵称
     * 主要是为了清理相同的代理，避免重复启动
     * @param userId 用户ID
     * @param serverId 服务器ID
     * @param nickname 昵称
     * @return 是否为同一个用户
     */
    private bool Equals(string userId, string serverId, string nickname)
    {
        return userId == Account.UserId && serverId == GameId && nickname == RoleName;
    }

    /**
     * 判断是否为同一个用户, 但是Token不同
     * 主要是为了清理过期的代理
     * @param userId 用户ID
     * @param userToken 用户Token
     * @return 是否为同一个用户
     */
    private bool Equals(EntityAccount? gameUser)
    {
        return gameUser?.UserId == Account.UserId && gameUser?.Token != Account.Token;
    }
}