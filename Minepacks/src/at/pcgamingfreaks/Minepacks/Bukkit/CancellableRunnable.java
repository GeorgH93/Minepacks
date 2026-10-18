package at.pcgamingfreaks.Minepacks.Bukkit;

import at.pcgf.libs.com.tcoded.folialib.impl.PlatformScheduler;
import at.pcgf.libs.com.tcoded.folialib.wrapper.task.WrappedTask;

public abstract class CancellableRunnable {
    protected WrappedTask task = null;

    public abstract void run();
    public abstract void schedule();

    public void cancel() {
        if (task != null) {
            task.cancel();
        }
    }

    protected PlatformScheduler getScheduler() {
        return Minepacks.getScheduler();
    }
}