using System.Text.Json.Serialization;
using Nirvana.Development;

namespace Nirvana.Public.Entities.NEL;

public class RunningProxy : EntityProxyBase {
    public readonly Interceptor Interceptor;

    public RunningProxy(Interceptor interceptor)
    {
        Interceptor = interceptor;
        IsRental = Interceptor.CurrentConfig.IsRental;
        NickName = Interceptor.CurrentConfig.NickName;
        LocalPort = Interceptor.CurrentConfig.LocalPort;
        ServerName = Interceptor.CurrentConfig.ServerName;
        LocalAddress = Interceptor.CurrentConfig.LocalAddress;
    }

    [JsonPropertyName("is_rental")]
    [JsonInclude]
    public bool IsRental { get; init; }

    [JsonPropertyName("local_address")]
    [JsonInclude]
    public string LocalAddress { get; init; }

    [JsonPropertyName("local_port")]
    [JsonInclude]
    public int LocalPort { get; init; }

    [JsonPropertyName("nick_name")]
    [JsonInclude]
    private string NickName { get; init; }

    [JsonPropertyName("server_name")]
    [JsonInclude]
    public string ServerName { get; init; }

    /**
     * 关闭服务
     */
    public void Shutdown()
    {
        Interceptor.ShutdownAsync();
    }

    /**
     * 获取游戏昵称
     */
    public override string GetNickName()
    {
        return NickName;
    }
}