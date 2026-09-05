using System;
using System.Text.Json.Serialization;
using Nirvana.Common.Utils.CodeTools;
using Nirvana.Public.Manager;
using Nirvana.WPFLauncher.Entities.WPFLauncher.NetGame.GameSkin;
using Nirvana.WPFLauncher.Protocol;

namespace Nirvana.Public.Entities.NEL;

public class EntitySkinDetail {
    public EntitySkinDetail(string id)
    {
        Set(NPFLauncher.GetSkinDetailsAsync(id).GetAwaiter().GetResult());
    }

    [JsonPropertyName("entity_id")]
    [JsonInclude]
    public string? EntityId { get; set; }

    [JsonPropertyName("brief_summary")]
    [JsonInclude]
    public string? BriefSummary { get; set; }

    [JsonPropertyName("name")]
    [JsonInclude]
    public string? Name { get; set; }

    [JsonPropertyName("title_image_url")]
    [JsonInclude]
    public string? TitleImageUrl { get; set; }

    [JsonPropertyName("like_num")]
    [JsonInclude]
    public int? LikeNum { get; set; }

    [JsonPropertyName("developer_name")]
    [JsonInclude]
    public string? DeveloperName { get; set; }

    [JsonPropertyName("publish_time")]
    [JsonInclude]
    public string? PublishTime { get; set; }

    [JsonPropertyName("download_num")]
    [JsonInclude]
    public long? DownloadNum { get; set; }

    private void Set(EntityQueryNetSkinItem? item)
    {
        if (item == null) {
            throw new ErrorCodeException(ErrorCode.IdError);
        }

        CacheManager.ClearCacheImage(item);
        DeveloperName = item.DeveloperName;
        // // unix 时间戳 转换为 文本
        PublishTime = DateTimeOffset.FromUnixTimeSeconds(item.PublishTime).ToString("yyyy-MM-dd");
        DownloadNum = item.DownloadNum;
        EntityId = item.EntityId;
        BriefSummary = item.BriefSummary;
        Name = item.Name;
        TitleImageUrl = item.TitleImageUrl;
        LikeNum = item.LikeNum;
    }
}