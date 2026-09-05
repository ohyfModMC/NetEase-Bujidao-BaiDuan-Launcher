using System.Text.Json.Serialization;

namespace Nirvana.Public.Entities.Nirvana;

public class EntityPluginDownResponse {
    [JsonPropertyName("fileHash")]
    [JsonInclude]
    public string? FileHash { get; set; }

    [JsonPropertyName("fileSize")]
    [JsonInclude]
    public long? FileSize { get; set; }

    [JsonPropertyName("id")]
    [JsonInclude]
    public required string Id { get; set; }

    [JsonPropertyName("dependencies")]
    [JsonInclude]
    public EntityPluginDownResponse[]? Dependencies { get; set; }
}