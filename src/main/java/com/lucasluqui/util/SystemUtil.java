package com.lucasluqui.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.lucasluqui.util.Log.log;

public class SystemUtil
{
  private static final String OS = System.getProperty("os.name").toLowerCase();
  private static final String ARCH = System.getProperty("os.arch").toLowerCase();

  private static int refreshRate = 75;

  public static boolean isWindows ()
  {
    return OS.contains("win");
  }

  public static boolean isMac ()
  {
    return OS.contains("mac");
  }

  public static boolean isUnix ()
  {
    return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
  }

  public static boolean is64Bit ()
  {
    boolean is64Bit;
    if (isWindows()) {
      is64Bit = (System.getenv("ProgramFiles(x86)") != null);
    } else {
      is64Bit = (ARCH.contains("64"));
    }
    return is64Bit;
  }

  public static boolean isARM ()
  {
    return ARCH.contains("arm")
      || ARCH.contains("aarch");
  }

  @Deprecated
  public static boolean hasValidJavaHome ()
  {
    return System.getProperty("java.home").contains("1.6") ||
      System.getProperty("java.home").contains("1.7") ||
      System.getProperty("java.home").contains("1.8");
  }

  private static String getMachineId ()
  {
    String machineId = null;

    if (isWindows()) {
      machineId = ProcessUtil.runAndCapture(new String[]{"cmd.exe", "/C", "wmic csproduct get UUID"})[0];

      try {
        machineId = machineId.substring(machineId.indexOf("\n")).trim();
      } catch (Exception e) {
        log.info("Unable to get UUID using WMIC, probably not installed. Thanks Microsoft.");
      }

      // Some lazy bios manufacturers won't bother setting a valid UUID for their BIOS and thus
      // creating a case where someone might share an invalid or default UUID with someone else
      // (the infamous 03000200-0400-0500-0006-000700080009 UUID!).
      // Assuming this could be the case, we gather Windows' software GUID and add it to the mix.
      String machineIdFailsafe = ProcessUtil.runAndCapture(new String[]{"cmd.exe", "/C", "reg query HKLM\\SOFTWARE\\Microsoft\\Cryptography /v MachineGuid"})[0];

      try {
        machineIdFailsafe = machineIdFailsafe.substring(machineIdFailsafe.indexOf("\n")).trim();
      } catch (Exception e) {
        log.error(e);
      }

      // Some systems return an empty string when trying to get the failsafe, so let's not add it when that's the case.
      if (!machineIdFailsafe.isEmpty()) {
        machineIdFailsafe = machineIdFailsafe.split("REG_SZ")[1].trim();
        machineId += machineIdFailsafe;
      } else {
        // Let's make sure they don't also have an invalid UUID before letting them through without the failsafe
        // If they do send them to the NullPointerException realm.
        if (machineId != null && (machineId.equalsIgnoreCase("03000200-0400-0500-0006-000700080009")
          || machineId.equalsIgnoreCase("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF")
          || machineId.equalsIgnoreCase("00000000-0000-0000-0000-000000000000"))) {
          machineId = null;
        }
      }
    }

    if (isUnix()) {
      machineId = ProcessUtil.runAndCapture(new String[]{"/bin/bash", "-c", "cat /etc/machine-id"})[0];
      machineId = machineId.replaceAll("\\r|\\n", "").trim();
    }

    // TODO: Verify this even works on Mac systems.
    if (isMac()) {
      try {
        String command = "system_profiler SPHardwareDataType | awk '/UUID/ { print $3; }'";
        StringBuffer output = new StringBuffer();

        Process SerNumProcess = Runtime.getRuntime().exec(command);
        BufferedReader sNumReader = new BufferedReader(new InputStreamReader(SerNumProcess.getInputStream()));

        String line = "";
        while ((line = sNumReader.readLine()) != null) {
          output.append(line + "\n");
        }

        machineId = output.substring(output.indexOf("UUID: "), output.length()).replace("UUID: ", "");

        SerNumProcess.waitFor();
        sNumReader.close();
      } catch (Exception e) {
        log.error(e);
      }
    }

    return machineId;
  }

  public static String getHashedMachineId ()
  {
    return getHashedMachineId("");
  }

  public static String getHashedMachineId (String additionalId)
  {
    String machineId = getMachineId() + additionalId;
    String hashedMachineId = null;

    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      messageDigest.update(machineId.getBytes(StandardCharsets.UTF_8));
      UUID uuid = UUID.nameUUIDFromBytes(messageDigest.digest());
      hashedMachineId = uuid.toString();
    } catch (Exception e) {
      log.error(e);
    }

    return hashedMachineId;
  }

  /**
   * Environment variables to override in every process we start, see {@link ProcessUtil}.
   * <p>
   * Our own environment can't be changed anymore on recent Java versions, so these are applied when starting processes instead.
   */
  private static final Map<String, String> ENV_OVERRIDES = new ConcurrentHashMap<>();

  public static void setEnv (Map<String, String> newenv)
  {
    ENV_OVERRIDES.putAll(newenv);
  }

  /**
   * @return The environment variables set through {@link #setEnv(Map)}.
   */
  public static Map<String, String> getEnvOverrides ()
  {
    return Collections.unmodifiableMap(ENV_OVERRIDES);
  }

  public static void fixTempDir (String newDir)
  {
    System.setProperty("java.io.tmpdir", newDir);
    Map<String, String> newEnv = new HashMap<>();
    newEnv.put("TEMP", newDir);
    SystemUtil.setEnv(newEnv);
    log.info("TEMP path changed to: " + newDir);
  }

  public static int getRefreshRate ()
  {
    return refreshRate;
  }

  public static void setRefreshRate (int newRate)
  {
    refreshRate = newRate;
  }
}
