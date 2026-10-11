package com.lucasluqui.util;

import org.apache.commons.io.IOUtils;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
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

  public static void runFromDirectory (String[] command, String workDir, boolean keepAlive)
  {
    runFromDirectory(command, workDir, keepAlive, false);
  }

  public static void runFromDirectory (String[] command, String workDir, boolean keepAlive, boolean silent)
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
