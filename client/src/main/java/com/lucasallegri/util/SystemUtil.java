package com.lucasallegri.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.lucasallegri.util.Log.log;

public class SystemUtil {

  private static final String OS = System.getProperty("os.name").toLowerCase();

  public static boolean isWindows() {
    return OS.contains("win");
  }

  public static boolean isMac() {
    return OS.contains("mac");
  }

  public static boolean isUnix() {
    return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
  }

  public static boolean is64Bit() {
    boolean is64Bit;
    if (isWindows()) {
      is64Bit = (System.getenv("ProgramFiles(x86)") != null);
    } else {
      is64Bit = (System.getProperty("os.arch").indexOf("64") != -1);
    }
    return is64Bit;
  }

  public static boolean isARM() {
    boolean isARM = false;
    if (System.getProperty("os.arch").contains("arm")) {
      isARM = true;
    }
    return isARM;
  }

  @Deprecated
  public static boolean hasValidJavaHome() {
    return System.getProperty("java.home").contains("1.6") ||
            System.getProperty("java.home").contains("1.7") ||
            System.getProperty("java.home").contains("1.8");
  }

  public static String getMachineId() {
    String machineId = null;

    if(isWindows()) {
      try {
        String command = "wmic csproduct get UUID";
        StringBuffer output = new StringBuffer();

        Process SerNumProcess = Runtime.getRuntime().exec(command);
        BufferedReader sNumReader = new BufferedReader(new InputStreamReader(SerNumProcess.getInputStream()));

        String line = "";
        while ((line = sNumReader.readLine()) != null) {
          output.append(line + "\n");
        }
        machineId = output.toString().substring(output.indexOf("\n"), output.length()).trim();

        SerNumProcess.waitFor();
        sNumReader.close();
      } catch (Exception e) {
        log.error(e);
      }
    }

    if(isUnix()) {
      try {
        String command = "echo \"$(fdisk --list)$(lshw -short)\" | md5sum | cut --delimiter=' ' --fields=1";
        StringBuffer output = new StringBuffer();

        Process SerNumProcess = Runtime.getRuntime().exec(command);
        BufferedReader sNumReader = new BufferedReader(new InputStreamReader(SerNumProcess.getInputStream()));

        String line = "";
        while ((line = sNumReader.readLine()) != null) {
          output.append(line + "\n");
        }

        machineId = output.toString().trim();

        SerNumProcess.waitFor();
        sNumReader.close();
      } catch (Exception e) {
        log.error(e);
      }
    }

    if(isMac()) {
      try {
        String command = "system_profiler SPHardwareDataType | awk '/UUID/ { print $3; }'";
        StringBuffer output = new StringBuffer();

        Process SerNumProcess = Runtime.getRuntime().exec(command);
        BufferedReader sNumReader = new BufferedReader(new InputStreamReader(SerNumProcess.getInputStream()));

        String line = "";
        while ((line = sNumReader.readLine()) != null) {
          output.append(line + "\n");
        }

        machineId = output.toString().substring(output.indexOf("UUID: "), output.length()).replace("UUID: ", "");

        SerNumProcess.waitFor();
        sNumReader.close();
      } catch (Exception e) {
        log.error(e);
      }
    }

    return machineId;
  }

}
