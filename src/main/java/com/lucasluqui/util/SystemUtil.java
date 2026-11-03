package com.lucasluqui.util;

import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

  /**
   * Identifies this machine by its motherboard: the system UUID set in the firmware, along with the board's
   * serial number. Some manufacturers leave the UUID at a placeholder shared by many machines, which the serial
   * number makes up for when it's set.
   *
   * @return The machine ID, or null if the hardware doesn't tell us anything usable.
   */
  private static String getMachineId ()
  {
    try {
      ComputerSystem computerSystem = new SystemInfo().getHardware().getComputerSystem();
      String uuid = usableHardwareValue(computerSystem.getHardwareUUID());
      String serial = usableHardwareValue(computerSystem.getBaseboard().getSerialNumber());
      if (uuid != null || serial != null) return (uuid == null ? "" : uuid) + "|" + (serial == null ? "" : serial);
    } catch (Throwable e) {
      log.error("Failed to get machine id", e);
    }

    // On Linux the firmware's IDs are only readable as root, but the OS keeps an ID of its own anyone can read.
    if (isUnix()) {
      try {
        String osId = usableHardwareValue(Files.readString(Paths.get("/etc/machine-id")));
        if (osId != null) return "|" + osId;
      } catch (Exception e) {
        log.error("Failed to read /etc/machine-id", e);
      }
    }
    return null;
  }

  /**
   * @return The value if it identifies something, or null for blanks and the placeholders firmware tends to use.
   */
  private static String usableHardwareValue (String value)
  {
    if (value == null) return null;
    value = value.trim();
    if (value.isEmpty() || value.equalsIgnoreCase("unknown")
      || value.equalsIgnoreCase("Default string") || value.equalsIgnoreCase("To be filled by O.E.M.")
      || value.equalsIgnoreCase("03000200-0400-0500-0006-000700080009")) {
      return null;
    }
    // All the same digit, like 00000000-0000-... or FFFFFFFF-FFFF-...
    if (value.replace("-", "").chars().distinct().count() == 1) return null;
    return value;
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
}
