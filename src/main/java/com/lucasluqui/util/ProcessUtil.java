package com.lucasluqui.util;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import static com.lucasluqui.util.Log.log;

public class ProcessUtil
{

  public static void run (String[] command, boolean keepAlive)
  {
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
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.directory(new File(workDir));

    // Get rid of _JAVA_OPTIONS, usually just contains garbage.
    pb.environment().remove("_JAVA_OPTIONS");

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

  public static boolean isProcessRunning (String processExecutable, String processTitle)
  {
    String[] output = ProcessUtil.runAndCapture(new String[]{"cmd.exe", "/C", "tasklist /v /fo csv /fi \"imagename eq " + processExecutable + "\""});
    log.info("isProcessRunning", "output (stdout)", output[0].replace("\r", "").replace("\n", "|"), "output (stderr)", output[1]);

    // We return true if we find either the process title requested or an "ERROR" string due to some
    // systems not having a properly functioning "tasklist" command or some antivirus blocking it.
    return output[0].contains(processTitle) || output[1].contains("ERROR");
  }

}
