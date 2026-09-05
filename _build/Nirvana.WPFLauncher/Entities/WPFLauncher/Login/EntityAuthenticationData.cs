using System.Text.Json.Serialization;

namespace Nirvana.WPFLauncher.Entities.WPFLauncher.Login;

public class EntityAuthenticationData {
    [JsonPropertyName("sa_data")]
    [JsonInclude]
    public required string SaData { get; set; }

    [JsonPropertyName("sauth_json")]
    [JsonInclude]
    public required string AuthJson { get; set; }

    [JsonPropertyName("version")]
    [JsonInclude]
    public required EntityAuthenticationVersion Version { get; set; }

    [JsonPropertyName("sdkuid")]
    [JsonInclude]
    public string? SdkUid { get; set; }

    [JsonPropertyName("aid")]
    [JsonInclude]
    public required string Aid { get; set; }

    [JsonPropertyName("hasMessage")]
    [JsonInclude]
    public bool HasMessage { get; set; }

    [JsonPropertyName("hasGmail")]
    [JsonInclude]
    public bool HasGmail { get; set; }

    [JsonPropertyName("otp_token")]
    [JsonInclude]
    public required string OtpToken { get; set; }

    [JsonPropertyName("otp_pwd")]
    [JsonInclude]
    public string? OtpPwd { get; set; }

    [JsonPropertyName("lock_time")]
    [JsonInclude]
    public int LockTime { get; set; }

    [JsonPropertyName("env")]
    [JsonInclude]
    public string? Env { get; set; }

    [JsonPropertyName("min_engine_version")]
    [JsonInclude]
    public string? MinEngineVersion { get; set; }

    [JsonPropertyName("min_patch_version")]
    [JsonInclude]
    public string? MinPatchVersion { get; set; }

    [JsonPropertyName("verify_status")]
    [JsonInclude]
    public int VerifyStatus { get; set; }

    [JsonPropertyName("unisdk_login_json")]
    [JsonInclude]
    public string? UniSdkLoginJson { get; set; }

    [JsonPropertyName("token")]
    [JsonInclude]
    public string? Token { get; set; }

    [JsonPropertyName("is_register")]
    [JsonInclude]
    public bool IsRegister { get; set; } = true;

    [JsonPropertyName("entity_id")]
    [JsonInclude]
    public string? EntityId { get; set; }
}