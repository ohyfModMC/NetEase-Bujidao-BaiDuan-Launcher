using System.Text.Json.Serialization;

namespace Nirvana.WPFLauncher.Entities.WPFLauncher.Login;

public class EntityX19Cookie {
    [JsonPropertyName("gameid")]
    [JsonInclude]
    public string GameId { get; set; } = "x19";

    [JsonPropertyName("login_channel")]
    [JsonInclude]
    public string LoginChannel { get; set; } = "netease";

    [JsonPropertyName("app_channel")]
    [JsonInclude]
    public string AppChannel { get; set; } = "netease";

    [JsonPropertyName("platform")]
    [JsonInclude]
    public string Platform { get; set; } = "pc";

    [JsonPropertyName("sdkuid")]
    [JsonInclude]
    public required string SdkUid { get; set; }

    [JsonPropertyName("sessionid")]
    [JsonInclude]
    public required string SessionId { get; set; }

    [JsonPropertyName("sdk_version")]
    [JsonInclude]
    public string SdkVersion { get; set; } = "4.2.0";

    [JsonPropertyName("udid")]
    [JsonInclude]
    public required string Udid { get; set; }

    [JsonPropertyName("deviceid")]
    [JsonInclude]
    public required string DeviceId { get; set; }

    [JsonPropertyName("aim_info")]
    [JsonInclude]
    public string AimInfo { get; set; } = "{\"aim\":\"127.0.0.1\",\"country\":\"CN\",\"tz\":\"+0800\",\"tzid\":\"\"}";
}