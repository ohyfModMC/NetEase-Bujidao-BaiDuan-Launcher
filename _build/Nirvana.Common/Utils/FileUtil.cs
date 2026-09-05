using System;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Threading.Tasks;
using Serilog;

namespace Nirvana.Common.Utils;

public static class FileUtil {
    public static string[] EnumerateFiles(string path, string? fileType = null)
    {
        if (string.IsNullOrWhiteSpace(path) || !Directory.Exists(path)) {
            return [];
        }

        var searchPattern = string.IsNullOrWhiteSpace(fileType) ? "*" : "*." + fileType.TrimStart('.').ToLowerInvariant();
        return Directory.EnumerateFiles(path, searchPattern, SearchOption.AllDirectories).ToArray();
    }

    /**
     * 计算文件的MD5哈希值
     * @param filePath 文件绝对路径
     * @return 文件的MD5哈希值（小写）
     */
    public static string ComputeMd5FromFile(string path)
    {
        if (string.IsNullOrWhiteSpace(path) || !File.Exists(path)) {
            return string.Empty;
        }

        try {
            using var inputStream = File.OpenRead(path);
            using var mD = MD5.Create();
            return Convert.ToHexString(mD.ComputeHash(inputStream)).ToUpperInvariant();
        } catch (IOException) {
            return string.Empty;
        } catch (UnauthorizedAccessException) {
            return string.Empty;
        }
    }

    public static bool EqualsMd5FromFile(string path, string md5)
    {
        return ComputeMd5FromFile(path).Equals(md5, StringComparison.OrdinalIgnoreCase);
    }

    public static void CleanDirectorySafe(string path)
    {
        if (string.IsNullOrWhiteSpace(path)) {
            return;
        }

        DeleteDirectorySafe(path);
        Directory.CreateDirectory(path);
    }

    public static void DeleteDirectorySafe(string path)
    {
        if (!Directory.Exists(path)) {
            return;
        }

        try {
            Directory.Delete(path, true);
        } catch (IOException) { } catch (UnauthorizedAccessException) { }
    }

    public static string[] GetFilesByDirectoryByFileSize(string path, int fileSize, string? searchPattern = null)
    {
        var files = EnumerateFiles(path, searchPattern);
        return files.Where(filePath => new FileInfo(filePath).Length > fileSize).ToArray();
    }

    public static bool CopyFileSafe(string sourcePath, string destPath)
    {
        if (string.IsNullOrWhiteSpace(sourcePath) || string.IsNullOrWhiteSpace(destPath)) {
            return false;
        }

        try {
            var directoryName = Path.GetDirectoryName(destPath);
            if (string.IsNullOrEmpty(directoryName)) {
                return false;
            }

            Directory.CreateDirectory(directoryName);
            File.Copy(sourcePath, destPath, true);
            return true;
        } catch (IOException) {
            return false;
        } catch (UnauthorizedAccessException) {
            return false;
        }
    }

    public static void CopyDirectory(string sourceDir, string destDir, bool overwrite = false)
    {
        // 创建目标目录
        Directory.CreateDirectory(sourceDir); // 防止源目录不存在导致异常
        Directory.CreateDirectory(destDir); // 防止目标目录不存在导致异常

        var dir = new DirectoryInfo(sourceDir);

        // 复制所有文件
        foreach (var file in dir.GetFiles()) {
            var targetFile = Path.Combine(destDir, file.Name);
            file.CopyTo(targetFile, overwrite);
        }

        // 递归复制所有子目录
        foreach (var subDir in dir.GetDirectories()) {
            var targetSubDir = Path.Combine(destDir, subDir.Name);
            CopyDirectory(subDir.FullName, targetSubDir, overwrite);
        }
    }

    public static bool DeleteFileSafe(string path)
    {
        try {
            if (!File.Exists(path)) {
                return true;
            }

            File.Delete(path);
            return true;
        } catch (Exception) {
            return false;
        }
    }

    /**
     * 设置文件权限
     * @param filePath 文件路径
     * @param requiredPermissions 所需权限，默认所有权限
     */
    public static void SetUnixFilePermissions(string filePath)
    {
        if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows)) {
            return;
        }

        try {
            var processStartInfo = new ProcessStartInfo("chmod", $"755 \"{filePath}\"");
            var process = Process.Start(processStartInfo);
            process?.WaitForExit();
        } catch (Exception e) {
            Log.Warning("警告：使用 chmod 设置 {0} 权限时出错: {1}", filePath, e.Message);
        }
    }

    public static bool IsFileReadable(string filePath)
    {
        try {
            using var fileStream = new FileStream(filePath, FileMode.Open, FileAccess.Read, FileShare.Read);
            return fileStream.Length > 0;
        } catch {
            return false;
        }
    }

    public static async Task WriteFileSafelyAsync(string filePath, byte[] buffer)
    {
        var tempFile = filePath + ".tmp";
        try {
            await File.WriteAllBytesAsync(tempFile, buffer);
            if (File.Exists(tempFile) && new FileInfo(tempFile).Length > 0) {
                if (File.Exists(filePath)) {
                    File.Delete(filePath);
                }

                File.Move(tempFile, filePath);
            }
        } catch {
            if (File.Exists(tempFile)) {
                File.Delete(tempFile);
            }

            throw;
        }
    }
}