using System;
using System.Threading.Tasks;
using Downloader;
using Nirvana.Common.Utils.Progress;
using Serilog;

namespace Nirvana.Game.Launcher.Utils;

public static class DownloadUtil {
    public static async Task<bool> DownloadAsync(string url, string destinationPath, Action<double>? downloadProgress = null)
    {
        try {
            if (url.Contains("netease.com")) {
                Log.Information("Downloading \"{0}\" from {1}", destinationPath, url);
            }

            var tcs = new TaskCompletionSource<bool>();

            var downloadOpt = new DownloadConfiguration {
                // ChunkCount = 1, // 设置并发块数
                // MaxTryAgainOnFailure = 4, // 下载失败后重试次数
                // ParallelDownload = true, // 启用并行下载 [ChunkCount]
                EnableAutoResumeDownload = true // 启用自动续传功能
                // HttpClientTimeout = 300_000, // 5 分钟超时
            };

            await using var downloader = new DownloadService(downloadOpt);

            var lastPercentage = -1.0;

            // 注册进度更新事件
            downloader.DownloadProgressChanged += (_, e) => {
                if (Math.Abs(e.ProgressPercentage - lastPercentage) < 0.1) {
                    return;
                }

                lastPercentage = e.ProgressPercentage;
                downloadProgress?.Invoke(e.ProgressPercentage);
            };

            downloader.DownloadFileCompleted += (_, e) => {
                if (e.Error != null) {
                    Log.Error("Download failed for {0}\n{1}", url, e.Error);
                    tcs.TrySetException(e.Error);
                } else if (e.Cancelled) {
                    Log.Information("Download canceled: {0}", url);
                    tcs.TrySetCanceled();
                } else {
                    tcs.TrySetResult(true);
                }
            };

            await downloader.DownloadFileTaskAsync(url, destinationPath);
            return await tcs.Task;
        } catch (TaskCanceledException) {
            Log.Information("Download canceled: {0}", url);
            throw;
        } catch (Exception ex) {
            Log.Error("Download failed for {0}\n{1}", url, ex);
            throw;
        }
    }

    /**
     * 更新文件
     * @param url 下载地址
     * @param path 保存路径
     * @param name 下载名称
     */
    public static async Task DownloadAsync(string url, string path, string name, SyncCallback<SyncProgressBarUtil.ProgressReport>? progress = null)
    {
        var uiProgress = SyncCallback.Create(progress);
        await DownloadAsync(url, path, dp => {
            uiProgress.Report(new SyncProgressBarUtil.ProgressReport {
                Percent = dp,
                Message = $"Downloading {name}"
            });
        });
    }
}