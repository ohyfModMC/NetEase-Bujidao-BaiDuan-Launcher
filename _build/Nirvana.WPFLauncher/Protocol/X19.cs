using System;
using Nirvana.Common.Utils.CodeTools;
using Nirvana.WPFLauncher.Http;

namespace Nirvana.WPFLauncher.Protocol;

public static class X19 {
    public const string Channel = "netease";

    // CRC盐值
    public static string? CrcSalt;

    // 最新盒子版本号
    public static string GameVersion => GetLatestVersion();

    public static string GetCrcSalt()
    {
        return CrcSalt ?? throw new ErrorCodeException(ErrorCode.CrcSaltNotSet);
    }

    /**
     * @return 最新盒子版本号
     */
    private static string GetLatestVersion()
    {
        var content = X19Extensions.UpdateNetease.Api<string>("/pl/x19_java_patchlist");
        ArgumentException.ThrowIfNullOrEmpty(content);

        const string size = "\":{\"size\":";

        var pos = content.LastIndexOf(size, StringComparison.Ordinal);
        if (pos == -1) {
            throw new ErrorCodeException(ErrorCode.NotVersionByLauncher);
        }

        content = content[..pos];
        pos = content.LastIndexOf('\"');

        return pos >= 0 ? content[(pos + 1)..] : throw new ErrorCodeException(ErrorCode.NotVersionByLauncher);
    }
}