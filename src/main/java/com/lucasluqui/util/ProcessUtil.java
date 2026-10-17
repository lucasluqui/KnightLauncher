package com.lucasluqui.util;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import org.apache.commons.io.IOUtils;
import oshi.SystemInfo;
import oshi.software.os.OSDesktopWindow;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static com.lucasluqui.util.Log.log;

public class ProcessUtil
{
  public static void run (String[] command, boolean keepAlive)
  {
    run(command, keepAlive, false);
  }

  public static void run (String[] command, boolean keepAlive, boolean silent)
  {
    if (!silent) {
      log.info("Running process command",
        "keepAlive", keepAlive,
        "command", String.join(" ", command)
      );
    }

    Process process = null;
    try {
      Map<String, String> env = System.getenv();

      String[] cleanEnv = env.entrySet().stream()
        .filter(e -> !e.getKey().equals("_JAVA_OPTIONS"))
        .map(e -> e.getKey() + "=" + e.getValue())
        .toArray(String[]::new);

      process = Runtime.getRuntime().exec(command, cleanEnv);
    } catch (IOException e) {
      log.error(e);
    } finally {
      // No need to keep the process alive.
      if (process != null && !keepAlive) process.destroy();
    }
  }

  public static Process runFromDirectory (String[] command, String workDir, boolean keepAlive)
  {
    return runFromDirectory(command, workDir, keepAlive, false);
  }

  /**
   * @return The started process, or null if it failed to start or wasn't kept alive.
   */
  public static Process runFromDirectory (String[] command, String workDir, boolean keepAlive, boolean silent)
  {
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.directory(new File(workDir));

    // Get rid of _JAVA_OPTIONS, usually just contains garbage.
    pb.environment().remove("_JAVA_OPTIONS");

    if (!silent) {
      log.info("Running process command from directory",
        "directory", workDir,
        "command", String.join(" ", command)
      );
    }

    Process process = null;
    try {
      process = pb.start();
    } catch (IOException e) {
      log.error(e);
    } finally {
      // No need to keep the process alive.
      if (process != null && !keepAlive) process.destroy();
    }
    return keepAlive ? process : null;
  }

  public static String[] runAndCapture (String[] command)
  {
    return runAndCapture(command, false);
  }

  public static String[] runAndCapture (String[] command, boolean silent)
  {
    if (!silent) {
      log.info("Running process command and capturing",
        "command", String.join(" ", command)
      );
    }

    Process process = null;
    try {
      ProcessBuilder processBuilder = new ProcessBuilder(command);

      // Get rid of _JAVA_OPTIONS, usually just contains garbage.
      processBuilder.environment().remove("_JAVA_OPTIONS");

      // Start the process.
      process = processBuilder.start();

      // Capture both streams and send them back. Ideally we'd only pass stdout but Java likes
      // sending important stuff through stderr like -version.
      String stdout = IOUtils.toString(process.getInputStream(), Charset.defaultCharset());
      String stderr = IOUtils.toString(process.getErrorStream(), Charset.defaultCharset());
      return new String[]{stdout, stderr};
    } catch (IOException e) {
      log.error(e);
    } finally {
      // No need to keep the process active.
      if (process != null) process.destroy();
    }
    return new String[1];
  }

  /**
   * Reads and throws away everything a process writes to its output streams in the background.
   * Needed when holding on to a process for a while, as it'd block once the pipe buffers fill up.
   */
  public static void discardOutput (Process process)
  {
    for (InputStream stream : new InputStream[] { process.getInputStream(), process.getErrorStream() }) {
      Thread drainer = new Thread(() -> {
        byte[] buffer = new byte[8192];
        try (InputStream in = stream) {
          while (in.read(buffer) != -1) ;
        } catch (IOException ignored) {
          // The process went away, nothing left to read.
        }
      }, "ProcessOutputDrainer");
      drainer.setDaemon(true);
      drainer.start();
    }
  }

  /**
   * Gets the ID of a process we started. Java 8 has no way of asking for it, so this digs it out of the
   * platform's process implementation.
   *
   * @return The process ID, or -1 if it couldn't be found out.
   */
  public static long getProcessId (Process process)
  {
    try {
      // Java 9 onwards. Looked up on Process itself, as the platform's implementation class isn't
      // public and calling the method through it is refused.
      return (long) Process.class.getMethod("pid").invoke(process);
    } catch (NoSuchMethodException ignored) {
      // Java 8, move on.
    } catch (Throwable e) {
      log.error("Failed to get process id", e);
      return -1;
    }

    try {
      Class<?> clazz = process.getClass();
      if (SystemUtil.isWindows()) {
        // Windows only keeps a handle to the process around.
        Field handleField = clazz.getDeclaredField("handle");
        handleField.setAccessible(true);
        long handle = handleField.getLong(process);
        int pid = Kernel32.INSTANCE.GetProcessId(new WinNT.HANDLE(Pointer.createConstant(handle)));
        return pid != 0 ? pid : -1;
      } else {
        Field pidField = clazz.getDeclaredField("pid");
        pidField.setAccessible(true);
        return pidField.getInt(process);
      }
    } catch (Throwable e) {
      log.error("Failed to get process id", e);
      return -1;
    }
  }

  /**
   * Checks whether a process has any window showing on the desktop.
   *
   * @return Whether there's a visible window, or null if windows couldn't be listed.
   */
  public static Boolean hasVisibleWindow (long pid)
  {
    try {
      for (OSDesktopWindow window : getOperatingSystem().getDesktopWindows(true)) {
        if (window.getOwningProcessId() == pid) return true;
      }
      return false;
    } catch (Throwable e) {
      log.error("Failed to list windows", e);
      return null;
    }
  }

  /**
   * Finds the running Java processes whose command line contains {@code commandLineFragment}.
   *
   * @return The IDs of the matching processes, or null if processes couldn't be listed.
   */
  public static Set<Integer> findJavaProcesses (String commandLineFragment)
  {
    Map<Integer, String> processes = listJavaProcesses();
    if (processes == null) return null;
    return findProcesses(processes, commandLineFragment);
  }

  /**
   * Finds the running Java processes whose command line contains {@code commandLineFragment},
   * leaving out the ones whose command line also contains {@code excludedFragment}.
   *
   * @return The IDs of the matching processes, or null if processes couldn't be listed.
   */
  public static Set<Integer> findJavaProcesses (String commandLineFragment, String excludedFragment)
  {
    Map<Integer, String> processes = listJavaProcesses();
    if (processes == null) return null;
    return findProcesses(processes, commandLineFragment, excludedFragment);
  }

  /**
   * Picks the processes whose command line contains {@code commandLineFragment} but not {@code excludedFragment}
   * out of a {@link #listJavaProcesses()} listing.
   */
  public static Set<Integer> findProcesses (Map<Integer, String> processes, String commandLineFragment, String excludedFragment)
  {
    Set<Integer> pids = findProcesses(processes, commandLineFragment);
    pids.removeIf(pid -> processes.get(pid).contains(excludedFragment));
    return pids;
  }

  /**
   * Picks the processes whose command line contains {@code commandLineFragment} out of a
   * {@link #listJavaProcesses()} listing. Useful to look for several things with a single listing.
   */
  public static Set<Integer> findProcesses (Map<Integer, String> processes, String commandLineFragment)
  {
    Set<Integer> pids = new HashSet<>();
    for (Map.Entry<Integer, String> process : processes.entrySet()) {
      if (process.getValue().contains(commandLineFragment)) pids.add(process.getKey());
    }
    return pids;
  }

  /**
   * Lists the running Java processes.
   *
   * @return The command line of each process keyed by its ID, or null if processes couldn't be listed.
   */
  public static Map<Integer, String> listJavaProcesses ()
  {
    try {
      // Only look into the command line of Java processes, querying it for every process is costly.
      // macOS is the exception, as the game might be started through an app bundle with a different name.
      Predicate<OSProcess> filter = SystemUtil.isMac()
        ? p -> true
        : p -> p.getName().toLowerCase().contains("java");

      Map<Integer, String> processes = new HashMap<>();
      for (OSProcess process : getOperatingSystem().getProcesses(filter, OperatingSystem.ProcessSorting.NO_SORTING, 0)) {
        processes.put(process.getProcessID(), process.getCommandLine());
      }
      return processes;
    } catch (Throwable e) {
      log.error("Failed to list processes", e);
      return null;
    }
  }

  private static synchronized OperatingSystem getOperatingSystem ()
  {
    if (_operatingSystem == null) _operatingSystem = new SystemInfo().getOperatingSystem();
    return _operatingSystem;
  }

  private static OperatingSystem _operatingSystem;

}
