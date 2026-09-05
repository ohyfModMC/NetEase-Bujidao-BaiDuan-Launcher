using System.Text.Json.Serialization;
using Nirvana.Common.Utils;

namespace Nirvana.Common.Entities;

public class EntityResponseBase {
    [JsonPropertyName("code")]
    public int? Code { get; set; }

    [JsonPropertyName("msg")]
    [JsonConverter(typeof(FirstStringConverter))]
    public string? Message { get; set; }
}

public class EntityResponse<T> : EntityResponseBase {
    [JsonPropertyName("data")]
    public T? Data { get; set; }
}

public class EntityInfo {
    [JsonPropertyName("update_versions")]
    [JsonInclude]
    public string? UpdateVersions { get; init; }

    [JsonPropertyName("versions")]
    [JsonInclude]
    public string[]? Versions { get; init; }

    [JsonPropertyName("ad1")]
    [JsonInclude]
    public Advertisement? Ad1 { get; init; }

    [JsonPropertyName("ad2")]
    [JsonInclude]
    public Advertisement? Ad2 { get; init; }

    [JsonPropertyName("ad3")]
    [JsonInclude]
    public Advertisement? Ad3 { get; init; }

    [JsonPropertyName("crcSalt")]
    [JsonInclude]
    public string? CrcSalt { get; init; }

    [JsonPropertyName("shopUrl")]
    [JsonInclude]
    public string? ShopUrl { get; init; }
}

public class Advertisement {
    [JsonPropertyName("name")]
    [JsonInclude]
    public string? Name { get; set; }

    [JsonPropertyName("text")]
    [JsonInclude]
    public string? Text { get; set; }
}