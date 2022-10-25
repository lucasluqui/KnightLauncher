package com.lucasallegri.util;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static com.lucasallegri.util.Log.log;

public class ProcessUtil {

  @SuppressWarnings("unused")
  public static void startApplication(String commandLine) {
    try {
      final Process p = Runtime.getRuntime().exec(commandLine);
    } catch (IOException e) {
      log.error(e);
    }
  }

  @SuppressWarnings("unused")
  public static void startApplication(String[] args) {
    try {
      final Process p = Runtime.getRuntime().exec(args);
    } catch (IOException e) {
      log.error(e);
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

}
