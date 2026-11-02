package com.lucasluqui.launcher.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Finds the Java runtimes the launcher can run on: its own one under {@code KnightLauncher/runtime},
 * and the one the game ships with.
 * <p>
 * Compiled for Java 8 (see {@code src/main/java8}), so it can only use Java 8 APIs. It's used both by
 * {@link com.lucasluqui.launcher.Main} before the launcher is running, and by the launcher itself.
 */
public final class RuntimeLocator
{
  /**
   * The Java version the launcher is built for and needs to run.
   * Set through the launcher.java.version property in pom.xml.
   */
  public static synchronized int getRequiredMajorVersion ()
  {
    if (requiredMajorVersion > 0) return requiredMajorVersion;

    int version = parseMajorVersion(getBuildProperties().getProperty(BUILD_PROPERTIES_JAVA_VERSION));
    if (version <= 0) {
      throw new IllegalStateException("No valid " + BUILD_PROPERTIES_JAVA_VERSION + " in " + BUILD_PROPERTIES
        + ", was it built through Maven?");
    }

    requiredMajorVersion = version;
    return version;
  }

  /**
   * The launcher's own version, e.g. "2.8.9".
   */
  public static String getLauncherVersion ()
  {
    String version = getBuildProperties().getProperty(BUILD_PROPERTIES_VERSION);
    return version == null ? "0" : version.trim();
  }

  private static synchronized Properties getBuildProperties ()
  {
    if (buildProperties != null) return buildProperties;

    Properties properties = new Properties();
    try (InputStream in = RuntimeLocator.class.getResourceAsStream(BUILD_PROPERTIES)) {
      if (in == null) throw new IllegalStateException("Missing " + BUILD_PROPERTIES);
      properties.load(in);
    } catch (IOException e) {
      throw new IllegalStateException("Could not read " + BUILD_PROPERTIES, e);
    }

    buildProperties = properties;
    return properties;
  }

  private static final String RUNTIME_PROPERTIES_VERSION = "version";
  private static final String RUNTIME_PROPERTIES_CHECKSUM = "checksum";
  private static final String RUNTIME_PROPERTIES_DIR = "dir";

  private RuntimeLocator ()
  {
  }

  /**
   * The game directory, which is also where the launcher runs from.
   */
  public static File getRootDir ()
  {
    return new File(System.getProperty("user.dir"));
  }

  /**
   * The launcher's own directory, holding its runtime, modules, cache and so on.
   */
  public static File getLauncherDir ()
  {
    return new File(getRootDir(), LAUNCHER_DIR_NAME);
  }

  /**
   * Where launcher runtimes get installed, one directory per version.
   */
  public static File getRuntimesDir ()
  {
    return new File(getLauncherDir(), "runtime");
  }

  /**
   * Points to the runtime currently in use within {@link #getRuntimesDir()}.
   */
  public static File getRuntimePropertiesFile ()
  {
    return new File(getLauncherDir(), "runtime.properties");
  }

  /**
   * @return The home directory of the installed launcher runtime, or null if there's none, or it's unusable.
   */
  public static File getInstalledRuntimeDir ()
  {
    Properties properties = readProperties(getRuntimePropertiesFile());
    if (properties == null) return null;

    String dirName = properties.getProperty(RUNTIME_PROPERTIES_DIR);
    if (dirName == null || dirName.trim().isEmpty()) return null;

    File dir = new File(getRuntimesDir(), dirName.trim());
    return isValidRuntime(dir) ? dir : null;
  }

  /**
   * @return The version of the installed launcher runtime, or null if there's none.
   */
  public static String getInstalledRuntimeVersion ()
  {
    return getRuntimeProperty(RUNTIME_PROPERTIES_VERSION);
  }

  /**
   * @return The SHA-256 of the archive the installed launcher runtime came from, or null if there's none.
   */
  public static String getInstalledRuntimeChecksum ()
  {
    return getRuntimeProperty(RUNTIME_PROPERTIES_CHECKSUM);
  }

  /**
   * Marks a runtime within {@link #getRuntimesDir()} as the one to use.
   *
   * @param checksum The SHA-256 of the archive it came from.
   */
  public static void setInstalledRuntime (String version, String checksum, String dirName) throws IOException
  {
    File file = getRuntimePropertiesFile();
    File parent = file.getParentFile();
    if (!parent.isDirectory() && !parent.mkdirs()) {
      throw new IOException("Could not create " + parent);
    }

    // Written by hand (no comments, no escaping) so the start scripts can read it too.
    // Values never need escaping, see RuntimeInstaller#sanitizeDirName.
    String content = RUNTIME_PROPERTIES_VERSION + "=" + version + "\n"
      + RUNTIME_PROPERTIES_CHECKSUM + "=" + checksum + "\n"
      + RUNTIME_PROPERTIES_DIR + "=" + dirName + "\n";

    File temp = new File(parent, file.getName() + ".tmp");
    try (OutputStream out = Files.newOutputStream(temp.toPath())) {
      out.write(content.getBytes(StandardCharsets.UTF_8));
    }
    RuntimeInstaller.moveReplacing(temp, file);
  }

  /**
   * Looks for a usable runtime shipped with the game.
   *
   * @return Its home directory, or null if there's none.
   */
  public static File findGameRuntime ()
  {
    File root = getRootDir();
    File[] candidates = new File[] {
      // Standalone Windows installs.
      new File(root.getParentFile() != null ? root.getParentFile() : root, "runtime"),
      // Steam installs, and most others.
      new File(root, "java_vm"),
      // macOS and Linux.
      new File(root, "java"),
    };

    for (File candidate : candidates) {
      if (isValidRuntime(candidate)) return candidate;
    }
    return null;
  }

  /**
   * Whether a runtime can run the launcher: it has a Java executable, is recent enough and is 64-bit.
   */
  public static boolean isValidRuntime (File home)
  {
    if (home == null || !home.isDirectory()) return false;
    if (!getJavaExecutable(home, false).isFile()) return false;

    String version = null;
    String arch = null;

    Properties release = readProperties(new File(home, "release"));
    if (release != null) {
      version = unquote(release.getProperty("JAVA_VERSION"));
      arch = unquote(release.getProperty("OS_ARCH"));
    }

    if (version == null) {
      // No release file to go by, ask the runtime itself.
      String output = runVersionCommand(getJavaExecutable(home, false));
      if (output == null) return false;
      version = parseQuotedVersion(output);
      arch = output.contains("64-Bit") ? "64" : "32";
    }

    return parseMajorVersion(version) >= getRequiredMajorVersion() && !is32BitArch(arch);
  }

  /**
   * The full version of a runtime according to its release file, e.g. "25.0.5+8-LTS".
   *
   * @return The version, or null if it has no release file.
   */
  public static String getReleaseVersion (File home)
  {
    Properties release = readProperties(new File(home, "release"));
    if (release == null) return null;

    String version = unquote(release.getProperty("JAVA_RUNTIME_VERSION"));
    if (version == null) version = unquote(release.getProperty("JAVA_VERSION"));
    return version;
  }

  /**
   * @param windowed Whether to prefer the executable that doesn't open a console window on Windows.
   */
  public static File getJavaExecutable (File home, boolean windowed)
  {
    File bin = new File(home, "bin");
    if (isWindows()) {
      return new File(bin, windowed ? "javaw.exe" : "java.exe");
    }
    return new File(bin, "java");
  }

  /**
   * The major version of the Java we're running on right now.
   */
  public static int getCurrentMajorVersion ()
  {
    return parseMajorVersion(System.getProperty("java.specification.version"));
  }

  /**
   * Parses the major version out of either versioning scheme, e.g. "1.8.0_432" (8) or "25.0.2" (25).
   *
   * @return The major version, or 0 if it can't be parsed.
   */
  public static int parseMajorVersion (String version)
  {
    if (version == null) return 0;
    version = unquote(version.trim());
    if (version == null) return 0;

    if (version.startsWith("1.")) version = version.substring(2);

    int end = 0;
    while (end < version.length() && Character.isDigit(version.charAt(end))) end++;
    if (end == 0) return 0;

    try {
      return Integer.parseInt(version.substring(0, end));
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  /**
   * Whether Windows itself is 64-bit.
   * We might be running on a 32-bit Java on 64-bit Windows, so os.arch can't tell.
   */
  public static boolean isWindows64Bit ()
  {
    String arch = System.getenv("PROCESSOR_ARCHITEW6432");
    if (arch == null) arch = System.getenv("PROCESSOR_ARCHITECTURE");
    if (arch != null) {
      arch = arch.toUpperCase(Locale.ROOT);
      return arch.equals("AMD64") || arch.equals("ARM64") || arch.equals("IA64");
    }
    return System.getProperty("os.arch", "").contains("64");
  }

  /**
   * Whether this is a Mac with Apple silicon.
   * We might be running on an Intel Java through Rosetta, which reports
   * an Intel os.arch even on Apple silicon, so macOS gets asked when it's not obvious.
   */
  public static boolean isAppleSilicon ()
  {
    if (!isMac()) return false;

    String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
    if (arch.equals("aarch64") || arch.equals("arm64")) return true;

    String output = runCommand("/usr/sbin/sysctl", "-n", "hw.optional.arm64");
    return output != null && output.trim().equals("1");
  }

  /**
   * Whether {@code file} is {@code dir} itself or anywhere inside it.
   */
  public static boolean isInside (File file, File dir)
  {
    if (file == null || dir == null) return false;
    try {
      String filePath = file.getCanonicalPath();
      String dirPath = dir.getCanonicalPath();
      if (isWindows()) {
        filePath = filePath.toLowerCase(Locale.ROOT);
        dirPath = dirPath.toLowerCase(Locale.ROOT);
      }
      return filePath.equals(dirPath) || filePath.startsWith(dirPath + File.separator);
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * The home directory of the Java we're running on right now.
   */
  public static File getCurrentJavaHome ()
  {
    return new File(System.getProperty("java.home"));
  }

  public static boolean isWindows ()
  {
    return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows");
  }

  public static boolean isMac ()
  {
    String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
    return os.startsWith("mac") || os.startsWith("darwin");
  }

  private static boolean is32BitArch (String arch)
  {
    if (arch == null) return false;
    arch = arch.toLowerCase(Locale.ROOT);
    return arch.equals("x86") || arch.equals("i386") || arch.equals("i486") || arch.equals("i586")
      || arch.equals("i686") || arch.equals("arm") || arch.equals("32");
  }

  private static String parseQuotedVersion (String output)
  {
    int start = output.indexOf('"');
    if (start < 0) return null;
    int end = output.indexOf('"', start + 1);
    if (end < 0) return null;
    return output.substring(start + 1, end);
  }

  private static String runVersionCommand (File executable)
  {
    return runCommand(executable.getAbsolutePath(), "-version");
  }

  /**
   * @return Everything the command printed, or null if it couldn't be run.
   */
  private static String runCommand (String... command)
  {
    Process process = null;
    try {
      ProcessBuilder pb = new ProcessBuilder(command);
      pb.redirectErrorStream(true);
      pb.environment().remove("_JAVA_OPTIONS");
      process = pb.start();

      StringBuilder output = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) output.append(line).append('\n');
      }

      if (!process.waitFor(15, TimeUnit.SECONDS)) return null;
      return output.toString();
    } catch (IOException e) {
      return null;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    } finally {
      if (process != null) process.destroy();
    }
  }

  private static String getRuntimeProperty (String key)
  {
    Properties properties = readProperties(getRuntimePropertiesFile());
    if (properties == null) return null;
    String value = properties.getProperty(key);
    return value == null || value.trim().isEmpty() ? null : value.trim();
  }

  private static Properties readProperties (File file)
  {
    if (!file.isFile()) return null;
    Properties properties = new Properties();
    try (InputStream in = Files.newInputStream(file.toPath())) {
      properties.load(in);
      return properties;
    } catch (IOException e) {
      return null;
    }
  }

  private static String unquote (String value)
  {
    if (value == null) return null;
    value = value.trim();
    if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
      value = value.substring(1, value.length() - 1);
    }
    return value.isEmpty() ? null : value;
  }

  private static int requiredMajorVersion = 0;
  private static Properties buildProperties = null;

  /**
   * Where the build writes its details, including the Java version
   * (see the launcher.java.version property in pom.xml).
   */
  private static final String BUILD_PROPERTIES = "/build.properties";
  private static final String BUILD_PROPERTIES_JAVA_VERSION = "java_version";
  private static final String BUILD_PROPERTIES_VERSION = "version";

  /** The launcher's own directory within {@link #getRootDir()}. */
  private static final String LAUNCHER_DIR_NAME = "KnightLauncher";
}
