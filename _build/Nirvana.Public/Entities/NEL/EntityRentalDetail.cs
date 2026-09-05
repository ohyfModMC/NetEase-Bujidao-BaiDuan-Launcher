using System;
using System.Collections.Generic;
using System.Text.Json.Serialization;
using System.Threading;
using Nirvana.Common.Utils.CodeTools;
using Nirvana.Public.Manager;
using Nirvana.WPFLauncher.Entities.WPFLauncher.RentalGame;
using Nirvana.WPFLauncher.Protocol;

namespace Nirvana.Public.Entities.NEL;

public class EntityRentalDetail {
    public EntityRentalDetail(string id)
    {
        Exception? exception = null;
        var threads = new List<Thread> {
            new(() => {
                try {
                    if (exception != null) {
                        return;
                    }

                    var item = NPFLauncher.GetRentalGameDetailsAsync(id).GetAwaiter().GetResult();
                    CacheManager.ClearCacheImage(item);
                    Set(item);
                } catch (Exception e) {
                    exception = e;
                }
            }),
            new(() => {
                try {
                    if (exception != null) {
                        return;
                    }

                    Set(NPFLauncher.GetGameRentalAddressAsync(id).GetAwaiter().GetResult());
                } catch (Exception e) {
                    exception = e;
                }
            })
        };
        foreach (var thread in threads) {
            thread.Start();
        }

        foreach (var thread in threads) {
            thread.Join();
        }

        if (exception != null) {
            throw exception;
        }
    }

    [JsonPropertyName("id")]
    [JsonInclude]
    public string? Id { get; set; }

    [JsonPropertyName("name")]
    [JsonInclude]
    public string? Name { get; set; }

    [JsonPropertyName("mc_version")]
    [JsonInclude]
    public string? McVersion { get; set; }

    [JsonPropertyName("image_url")]
    [JsonInclude]
    public string? ImageUrl { get; set; }

    [JsonPropertyName("player_count")]
    [JsonInclude]
    public uint PlayerCount { get; set; }

    [JsonPropertyName("capacity")]
    [JsonInclude]
    public uint Capacity { get; set; }

    [JsonPropertyName("brief_summary")]
    [JsonInclude]
    public string? BriefSummary { get; set; }

    [JsonPropertyName("address")]
    [JsonInclude]
    public string? Address { get; set; }

    [JsonPropertyName("server_type")]
    [JsonInclude]
    public string? ServerType { get; set; }

    private void Set(EntityRentalGameDetails entity)
    {
        if (entity == null) {
            throw new ErrorCodeException(ErrorCode.IdError);
        }

        Id = entity.EntityId;
        Name = entity.ServerName;
        ImageUrl = entity.ImageUrl;
        Capacity = entity.Capacity;
        McVersion = entity.McVersion;
        ServerType = entity.ServerType;
        PlayerCount = entity.PlayerCount;
        BriefSummary = entity.BriefSummary;
    }

    private void Set(EntityRentalGameServerAddress data)
    {
        if (data == null) {
            throw new ErrorCodeException(ErrorCode.AddressError);
        }

        Address = data.McServerHost;
        if (data.McServerPort != 25565) Address += $":{data.McServerPort}";
    }
}