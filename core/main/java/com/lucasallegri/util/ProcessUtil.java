package com.lucasallegri.util;

import java.io.IOException;

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

}
