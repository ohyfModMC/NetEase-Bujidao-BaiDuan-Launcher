using System.Text.Json.Serialization;
using Nirvana.Common.Utils;
using Nirvana.WPFLauncher.Utils;

namespace Nirvana.WPFLauncher.Entities.WPFLauncher.Login;

public class EntityAuthenticationDetail {
    [JsonPropertyName("os_name")]
    [JsonInclude]
    public string OsName { get; set; } = "windows";

    [JsonPropertyName("os_ver")]
    [JsonInclude]
    public string OsVer { get; set; } = "Microsoft Windows 11 专业版";

    [JsonPropertyName("mac_addr")]
    [JsonInclude]
    public string MacAddr { get; set; } = StringGenerator.GenerateRandomMacAddress("");

    [JsonPropertyName("udid")]
    [JsonInclude]
    public required string Udid { get; set; }

    [JsonPropertyName("app_ver")]
    [JsonInclude]
    public required string AppVersion { get; set; }

    [JsonPropertyName("sdk_ver")]
    [JsonInclude]
    public string SdkVersion { get; set; } = string.Empty;

    [JsonPropertyName("network")]
    [JsonInclude]
    public string Network { get; set; } = string.Empty;

    [JsonPropertyName("disk")]
    [JsonInclude]
    public required string Disk { get; set; }

    [JsonPropertyName("is64bit")]
    [JsonInclude]
    public string Is64Bit { get; set; } = "1";

    [JsonPropertyName("video_card1")]
    [JsonInclude]
    public string VideoCard1 { get; set; } = "Microsoft Hyper-V 视频";

    [JsonPropertyName("video_card2")]
    [JsonInclude]
    public string VideoCard2 { get; set; } = "Microsoft Remote Display Adapter";

    [JsonPropertyName("video_card3")]
    [JsonInclude]
    public string VideoCard3 { get; set; } = string.Empty;

    [JsonPropertyName("video_card4")]
    [JsonInclude]
    public string VideoCard4 { get; set; } = string.Empty;

    [JsonPropertyName("launcher_type")]
    [JsonInclude]
    public string LauncherType { get; set; } = "PC_java";

    [JsonPropertyName("pay_channel")]
    [JsonInclude]
    public required string PayChannel { get; set; }

    [JsonPropertyName("dotnet_ver")]
    [JsonInclude]
    public string DotnetVersion { get; set; } = "4.8.0";

    [JsonPropertyName("cpu_type")]
    [JsonInclude]
    public string CpuType { get; set; } = Tools.GetRandomCpuType();

    [JsonPropertyName("ram_size")]
    [JsonInclude]
    public string RamSize { get; set; } = Tools.GetRandomRamSize();

    [JsonPropertyName("device_width")]
    [JsonInclude]
    public string DeviceWidth { get; set; } = "1920";

    [JsonPropertyName("device_height")]
    [JsonInclude]
    public string DeviceHeight { get; set; } = "1080";

    [JsonPropertyName("os_detail")]
    [JsonInclude]
    public string OsDetail { get; set; } = "10.0.26100";
}