using System.Text.Json.Serialization;

namespace Nirvana.Public.Entities.Login;

public class Entity4399CaptchaOk {
    [JsonPropertyName("captcha")]
    [JsonInclude]
    public string? Captcha { get; set; }
}