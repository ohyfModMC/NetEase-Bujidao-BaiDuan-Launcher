using System.Text.Json.Serialization;

namespace Nirvana.Public.Entities.Plugin;

public class EntityPluginDependency {
    [JsonPropertyName("id")]
    [JsonInclude]
    public required string Id { get; set; }

    [JsonPropertyName("name")]
    [JsonInclude]
    public required string Name { get; set; }
}