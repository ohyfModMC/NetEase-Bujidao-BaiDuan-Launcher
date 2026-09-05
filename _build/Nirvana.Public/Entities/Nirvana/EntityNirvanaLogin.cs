using System.Text.Json.Serialization;
using Nirvana.Common.Entities;

namespace Nirvana.Public.Entities.Nirvana;

public class EntityNirvanaLogin : EntityResponseBase {
    [JsonPropertyName("online")]
    public string? Token { get; set; }
}