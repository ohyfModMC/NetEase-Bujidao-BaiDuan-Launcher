using System;
using System.Threading;
using System.Threading.Tasks;
using Nirvana.WPFLauncher.Entities.WPFLauncher;
using Nirvana.WPFLauncher.Http;
using Serilog;

namespace Nirvana.WPFLauncher.Protocol;

public static class InterConn {
    /// <summary>最近一次进入游戏的 gameId(网易 game-play 会话约1分钟过期, code=10 时用它重建会话)</summary>
    public static string? LastGameId { get; private set; }
    private static readonly SemaphoreSlim RefreshGate = new(1, 1);
    private static long _lastRefreshTicks; // 会话重建节流, 避免 80 个并发查询同时重新登录

    private static async Task LoginStart()
    {
        Log.Debug("LoginStart response: {0}", await X19Extensions.Core1.ApiAsync<string>("/interconn/web/game-play-v2/login-start", "{\"strict_mode\":true}"));
    }

    private static async Task GameStart(string gameId)
    {
        Log.Debug("GameStart response: {0}", await X19Extensions.Core1.ApiAsync<string>("/interconn/web/game-play-v2/start", new InterConnGameStart {
            GameId = gameId,
            ItemList = ["10000"]
        }));
    }

    public static async Task LoginStartAndGameStart(string gameId)
    {
        await LoginStart();
        await GameStart(gameId);
        LastGameId = gameId;
    }

    /// <summary>
    /// Gateway API 返回 code=10(请先登录) 时调用: 重建 game-play 会话。
    /// 多线程并发触发时只重建一次(30 秒节流)。
    /// </summary>
    public static async Task EnsureSessionAsync()
    {
        var gameId = LastGameId;
        if (string.IsNullOrEmpty(gameId)) return;
        await RefreshGate.WaitAsync();
        try {
            var since = DateTime.UtcNow.Ticks - Interlocked.Read(ref _lastRefreshTicks);
            if (since < TimeSpan.FromSeconds(30).Ticks) return; // 30 秒内刚重建过
            Log.Warning("[InterConn] gateway session expired (code=10), re-login game-play for gameId={0}", gameId);
            await LoginStart();
            await GameStart(gameId);
            Interlocked.Exchange(ref _lastRefreshTicks, DateTime.UtcNow.Ticks);
        } finally {
            RefreshGate.Release();
        }
    }
}