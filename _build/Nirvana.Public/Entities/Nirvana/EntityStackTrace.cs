using System.Diagnostics;
using System.Text.Json;
using System.Text.Json.Nodes;
using System.Text.Json.Serialization;

namespace Nirvana.Public.Entities.Nirvana;

public class EntityStackTrace {
    public EntityStackTrace(StackFrame stackTrace)
    {
        var method = stackTrace.GetMethod();
        if (method == null) {
            return;
        }

        Method = method.Name;
        File = stackTrace.GetFileName() ?? string.Empty;
        Line = stackTrace.GetFileLineNumber();
    }

    [JsonPropertyName("method")]
    [JsonInclude]
    private string Method { get; set; } = "Method Not Found";

    [JsonPropertyName("file")]
    [JsonInclude]
    public string File { get; set; } = "File Not Found";

    [JsonPropertyName("line")]
    [JsonInclude]
    private int Line { get; set; } = -1;

    public override string ToString()
    {
        return JsonSerializer.Serialize(this);
    }

    private JsonDocument ToJsonDocument()
    {
        return JsonDocument.Parse(ToString());
    }

    // 是否是不重要的
    public bool IsIgnore()
    {
        // (string.IsNullOrEmpty(File) && Line == 0) || 
        return Method.Length < 5;
    }

    public void ToAdd(JsonArray array)
    {
        if (Line != -1) {
            array.Add(ToJsonDocument());
        }
    }
}