package com.luuqui.util;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.luuqui.util.Log.log;

public class ProcessUtil {

  public static void run(String[] command, boolean keepAlive) {
    Process process = null;
    try {
      process = Runtime.getRuntime().exec(command);
    } catch (IOException e) {
      log.error(e);
    } finally {

      // No need to keep the process alive.
      if (process != null && !keepAlive) process.destroy();
    }
  }

  public static void runFromDirectory(String[] command, String workDir, boolean keepAlive) {
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.directory(new File(workDir));
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

  public static String[] runAndCapture(String[] command) {
    Process process = null;
    try {
      process = new ProcessBuilder(command).start();

      // Capture both streams and send them back. Ideally we'd only pass stdout but Java likes
      // sending important stuff through stderr like -version.
      String stdout = IOUtils.toString(process.getInputStream(), Charset.defaultCharset());
      String stderr = IOUtils.toString(process.getErrorStream(), Charset.defaultCharset());
      return new String[] {stdout, stderr};
    } catch (IOException e) {
      log.error(e);
    } finally {

      // No need to keep the process active.
      if(process != null) process.destroy();
    }
    return new String[1];
  }

  public static boolean isGameRunningByTitle(String processTitle) {
    String output = ProcessUtil.runAndCapture(new String[]{ "cmd.exe", "/C", "tasklist /v /fo csv /fi \"imagename eq java.exe\"" })[0];
    log.info("isGameRunningByTitle", "output", output.replace("\r", "").replace("\n", "|"));
    return output.contains(processTitle);
  }

}
