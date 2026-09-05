using System.Text.Json.Serialization;
using Nirvana.Common.Manager;

namespace Nirvana.Common.Entities;

// ReSharper disable once InconsistentNaming
public class EntityWPFResponse {
    [JsonPropertyName("code")]
    public int Code { get; set; }

    [JsonPropertyName("message")]
    public string Message { get; set; } = string.Empty;

    protected void SafeEntity()
    {
        // Token 过期 | 账号被顶
        if (Code is 10 or 22) {
            InfoManager.SetGameAccount(null);
            InfoManager.DeleteAccount(this);
        }
    }
}