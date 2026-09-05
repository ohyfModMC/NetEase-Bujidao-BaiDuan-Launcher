using System.Text.Json.Serialization;

namespace Nirvana.Public.Entities.Plugin;

public class EntityPlugin {
    [JsonPropertyName("detailDescription")]
    [JsonInclude]
    public string? DetailDescription { get; set; }

    [JsonPropertyName("downloadCount")]
    [JsonInclude]
    public int? DownloadCount { get; set; }

    [JsonPropertyName("id")]
    [JsonInclude]
    public string? Id { get; set; }

    [JsonPropertyName("logoUrl")]
    [JsonInclude]
    public string? LogoUrl { get; set; }

    [JsonPropertyName("name")]
    [JsonInclude]
    public string? Name { get; set; }

    [JsonPropertyName("publishDate")]
    [JsonInclude]
    public string? PublishDate { get; set; }

    [JsonPropertyName("publisher")]
    [JsonInclude]
    public string? Publisher { get; set; }

    [JsonPropertyName("shortDescription")]
    [JsonInclude]
    public string? ShortDescription { get; set; }

    [JsonPropertyName("version")]
    [JsonInclude]
    public string? Version { get; set; }

    [JsonPropertyName("dependencies")]
    [JsonInclude]
    public EntityPluginDependency[]? Dependencies { get; set; }
}