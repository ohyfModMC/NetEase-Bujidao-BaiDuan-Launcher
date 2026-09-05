using System.Text.Json.Serialization;

namespace Nirvana.Public.Entities.Nirvana;

public class EntityDependence {
    [JsonPropertyName("data")]
    public required EntityDependence2[] Data { get; set; }
}