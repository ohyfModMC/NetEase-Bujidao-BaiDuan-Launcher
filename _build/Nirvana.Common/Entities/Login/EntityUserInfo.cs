using System.Text.Json.Serialization;
using Nirvana.Common.Utils.CodeTools;

namespace Nirvana.Common.Entities.Login;

// 登录信息
public class EntityUserInfo {
    [JsonPropertyName("token")]
    [JsonInclude]
    public string? Token { get; set; }

    [JsonPropertyName("userId")]
    [JsonInclude]
    public string? UserId { get; set; }

    public string GetUserId()
    {
        return UserId ?? throw new ErrorCodeException(ErrorCode.LogInNot);
    }

    public string GetToken()
    {
        return Token ?? throw new ErrorCodeException(ErrorCode.LogInNot);
    }

    public bool IsNotNuLl()
    {
        return Token != null && UserId != null;
    }
}