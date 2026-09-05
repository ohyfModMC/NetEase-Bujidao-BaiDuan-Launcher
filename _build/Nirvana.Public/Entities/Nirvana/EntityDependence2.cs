using System.Text.Json.Serialization;

namespace Nirvana.Public.Entities.Nirvana;

public class EntityDependence2 {
    [JsonPropertyName("id")]
    public required string Id { get; set; }
}