package com.lucasluqui.launcher;

import com.lucasluqui.launcher.runtime.RuntimeInstaller;
import com.lucasluqui.launcher.runtime.RuntimeLocator;
import com.lucasluqui.launcher.runtime.RuntimeSource;
import com.lucasluqui.util.ThreadingUtil;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.File;

import static com.lucasluqui.launcher.Log.log;

/**
 * Keeps the launcher's own Java runtime (under {@code KnightLauncher/runtime}) installed and up to date.
 * <p>
 * {@link Main} only installs a runtime when there's none at all. On the other hand, if anything fails,
 * this manager will assess the situation and try to download missing runtime files in the background.
 */
@Singleton
public class RuntimeManager
{
  @Inject protected LauncherContext _ctx;

  public RuntimeManager ()
  {
    // empty.
  }

  public void init ()
  {
    log.info("Launcher runtime",
      "javaHome", RuntimeLocator.getCurrentJavaHome(),
      "ownRuntime", isRunningOnOwnRuntime(),
      "installedVersion", RuntimeLocator.getInstalledRuntimeVersion(),
      "installedChecksum", RuntimeLocator.getInstalledRuntimeChecksum()
    );
  }

  /**
   * Whether we're running on the launcher's own runtime, rather than the game's or some other Java install.
   */
  public boolean isRunningOnOwnRuntime ()
  {
    return RuntimeLocator.isInside(RuntimeLocator.getCurrentJavaHome(), RuntimeLocator.getRuntimesDir());
  }

  /**
   * Cleans up old runtimes, and installs the latest runtime if it isn't installed yet.
   * Runs in the background, newly installed runtimes get used from the next launch on.
   */
  public void maybeUpdateRuntimeInBackground ()
  {
    ThreadingUtil.startDaemon("RuntimeUpdater", this::maybeUpdateRuntime);
  }

  private void maybeUpdateRuntime ()
  {
    try {
      RuntimeInstaller.pruneOldRuntimes();

      RuntimeSource source = RuntimeSource.forCurrentPlatform();
      if (source == null) {
        log.info("No launcher runtime available for this platform",
          "os", System.getProperty("os.name"), "arch", System.getProperty("os.arch"));
        return;
      }

      String checksum = source.fetchChecksum();
      if (RuntimeLocator.getInstalledRuntimeDir() != null && checksum.equalsIgnoreCase(RuntimeLocator.getInstalledRuntimeChecksum())) {
        log.info("Launcher runtime is up to date");
        return;
      }

      log.info("Installing launcher runtime", "source", source, "checksum", checksum);
      File home = RuntimeInstaller.install(source, checksum, null);
      log.info("Installed launcher runtime, it'll be used from the next launch on",
        "dir", home.getName(), "version", RuntimeLocator.getInstalledRuntimeVersion());

      // Shortcuts point to a specific runtime, move them over to the new one.
      _ctx.getApp().refreshShortcut();
    } catch (Exception e) {
      log.error("Failed to update launcher runtime", e);
    }
  }
}
