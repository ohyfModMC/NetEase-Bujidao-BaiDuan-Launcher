using System;

namespace Nirvana.Common.Utils.Progress;

public class SyncCallback<T>(Action<T> handler) : IProgress<T> {
    public void Report(T value)
    {
        handler(value);
    }
}

public class SyncCallback {
    public static SyncCallback<SyncProgressBarUtil.ProgressReport> Create(SyncCallback<SyncProgressBarUtil.ProgressReport>? progress)
    {
        return progress ?? Create();
    }

    public static SyncCallback<SyncProgressBarUtil.ProgressReport> Create()
    {
        var progressBar = new SyncProgressBarUtil.ProgressBar();
        return new SyncCallback<SyncProgressBarUtil.ProgressReport>(progressBar.Update);
    }
}