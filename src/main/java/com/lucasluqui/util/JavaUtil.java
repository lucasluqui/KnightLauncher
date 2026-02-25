package com.lucasluqui.util;

import com.google.inject.Inject;
import com.lucasluqui.launcher.LauncherGlobals;
import com.lucasluqui.launcher.flamingo.FlamingoManager;
import sun.misc.Launcher;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static com.lucasluqui.util.Log.log;

public class JavaUtil
{
  @Inject private static FlamingoManager _flamingoManager;

  public static String getJVMVersionOutput (String path)
  {
    String output = "";
    if (SystemUtil.isWindows()) {
      output = ProcessUtil.runAndCapture(new String[] { "cmd.exe", "/C", path, "-version" })[1];
    } else {
      output = ProcessUtil.runAndCapture(new String[] { "/bin/bash", "-c", "\"" + path + "\" -version" })[1];
    }
    return output;
  }

  public static int getJVMArch (String path)
  {
    String output = getJVMVersionOutput(path);

    // We got no output, can't do any checks.
    if (output.isEmpty()) return 0;

    // Matches a 64-bit JVM.
    if (output.contains("64-Bit") || output.contains("PE32+")) return 64;

    // No results matched. We assume it's 32-bit.
    return 32;
  }

  public static int getJVMVersion (String path)
  {
    String output = getJVMVersionOutput(path);
    int version = 0;

    try {
      output = output.split("\"")[1];
      version = output.startsWith("1.") ? Integer.parseInt(output.split("\\.")[1]) : Integer.parseInt(output.split("\\.")[0]);
    } catch (Exception e) {
      log.error(e, "output", output);
    }

    return version;
  }

  public static String getGameJVMData ()
  {
    String path = getGameJVMDirPath() + "/release";
    String version = "";
    String osArch = "";

    if (FileUtil.fileExists(path)) {
      Properties releaseFile = new Properties();
      try {
        releaseFile.load(Files.newInputStream(new File(path).toPath()));
      } catch (IOException e) {
        log.error(e);
      }

      version = releaseFile.getProperty("JAVA_VERSION");
      osArch = releaseFile.getProperty("OS_ARCH");
    } else {
      String output = "";
      try {
        output = getJVMVersionOutput(getGameJVMExePath());
        version = output.split("\"")[1];
        osArch = String.valueOf(getJVMArch(getGameJVMExePath()));
      } catch (Exception e) {
        log.error(e, "output", output);
      }
    }

    if (osArch == null) {
      osArch = SystemUtil.is64Bit() ? "64-bit" : "32-bit";
    }

    if (version.isEmpty()) {
      return "Unknown Java VM";
    }

    return (version + ", " + osArch).replace("\"", "");
  }

  public static String getReadableGameJVMData ()
  {
    String rawJavaVMData = getGameJVMData();

    if (rawJavaVMData.contains("Unknown")) {
      return "Unknown, probably 32-bit";
    }

    String javaMajorVersion = "unknown";
    String javaMinorVersion = "unknown";
    String javaArch = "unknown";

    boolean postJava8Versioning = !rawJavaVMData.startsWith("1.");

    try {
      if (postJava8Versioning) {
        // versioning for Java 10 onwards. e.g. "15.0.2"
        javaMinorVersion = rawJavaVMData.split(",")[0];
        javaMajorVersion = javaMinorVersion.split("\\.")[0];
      } else {
        // versioning for Java 8 and prior. e.g. "1.8.0_251"
        javaMajorVersion = rawJavaVMData.split("\\.")[1];
        javaMinorVersion = rawJavaVMData.split("_")[1].split(",")[0];
      }
      javaArch = JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()) == 64 ? "64-bit" : "32-bit";
    } catch (Exception e) {
      log.error(e);
    }

    if (javaMajorVersion.equalsIgnoreCase("unknown")
      || javaMinorVersion.equalsIgnoreCase("unknown")
      || javaArch.equalsIgnoreCase("unknown")) {
      return "Unknown, probably 32-bit";
    }

    if (postJava8Versioning) {
      return "Java " + javaMajorVersion + " (" + javaMinorVersion + "), " + javaArch;
    }

    return "Java " + javaMajorVersion + " (" + javaMinorVersion + "), " + javaArch;
  }

  public static String getGameJVMDirPath ()
  {
    // Third party servers.
    if (_flamingoManager != null && _flamingoManager.getSelectedServer() != null) {
      if (!_flamingoManager.getSelectedServer().isOfficial()) {
        String path = LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + _flamingoManager.getSelectedServer().getSanitizedName();
        return new File(path, "java_vm").getAbsolutePath();
      }
    }

    // Official (steam)
    if (SteamUtil.isRunningInSteamapps()) {
      return LauncherGlobals.USER_DIR + File.separator + "java_vm";
    }

    // Official (standalone)
    return LauncherGlobals.USER_DIR.split("Spiral Knights")[0] + "Spiral Knights" + File.separator + "runtime";

    /*
      Exclude linux users from possibly matching a java_vm directory.
      They might have installed from Steam which downloads Windows files
      which also have a java_vm folder, but meant to be run with Proton...
     */

    /*File javaVMDir = new File(startingDirPath, "java_vm");
    if (javaVMDir.exists() && javaVMDir.isDirectory() && !SystemUtil.isUnix()) {
      return javaVMDir.getAbsolutePath();
    }

    File javaDir = new File(startingDirPath, "java");
    if (javaDir.exists() && javaDir.isDirectory()) {
      return javaDir.getAbsolutePath();
    }*/

    //return "";
  }

  public static String getGameJVMExePath ()
  {
    String javaDir = getGameJVMDirPath();

    if (FileUtil.fileExists(javaDir + File.separator + "bin" + File.separator + "javaw.exe")) {
      return javaDir + File.separator + "bin" + File.separator + "javaw.exe";
    }

    if (FileUtil.fileExists(javaDir + File.separator + "bin" + File.separator + "javaw")) {
      return javaDir + File.separator + "bin" + File.separator + "javaw";
    }

    if (FileUtil.fileExists(javaDir + File.separator + "bin" + File.separator + "java")) {
      return javaDir + File.separator + "bin" + File.separator + "java";
    }

    log.error("Cannot locate local java executable");
    return "javaw";
  }

  public static boolean isLegacy ()
  {
    int gameJVMVersion = 8;
    try {
      gameJVMVersion = JavaUtil.getJVMVersion(JavaUtil.getGameJVMExePath());
    } catch (Exception e) {
      log.error("Failed to get game Java VM version", e);
    }

    return gameJVMVersion <= 8;
  }

  public static String getJavaVMCommandLineSeparator ()
  {
    return SystemUtil.isWindows() ? ";" : ":";
  }

  public static synchronized void loadLibrary (File jar)
  {
    try {
      // We are using reflection here to circumvent encapsulation; addURL is not public
      URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
      URL url = jar.toURI().toURL();

      // Disallow if already loaded
      for (URL it : loader.getURLs()) {
        if (it.equals(url)) {
          return;
        }
      }

      Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      method.setAccessible(true);
      method.invoke(loader, url);
    } catch (final NoSuchMethodException |
                   IllegalAccessException |
                   MalformedURLException |
                   InvocationTargetException e) {
      log.error(e);
    }
  }

  public static void addToLibraryPath (String... path)
  {
    String cmdLineSeparator = getJavaVMCommandLineSeparator();

    for (String p : path) {
      System.setProperty("java.library.path", p + cmdLineSeparator + System.getProperty("java.library.path"));
    }

    try {
      Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
      fieldSysPath.setAccessible(true);
      fieldSysPath.set(null, null);
    } catch (Exception e) {
      log.error(e);
    }
  }

  public static void createJar (Path source, Path output, String mainClass)
    throws IOException
  {
    final int BUFFER_SIZE = 8192;

    if (!Files.exists(source) || !Files.isDirectory(source)) {
      throw new IllegalArgumentException("Source must be an existing directory");
    }

    Manifest manifest = new Manifest();
    Attributes attributes = manifest.getMainAttributes();
    attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");

    if (mainClass != null && !mainClass.isEmpty()) {
      attributes.put(Attributes.Name.MAIN_CLASS, mainClass);
    }

    // Warning: Manifest is not passed through.
    try (JarOutputStream jarOutputStream =
           new JarOutputStream(Files.newOutputStream(output))) {

      Files.walk(source)
        .filter(path -> !Files.isDirectory(path))
        .forEach(path -> {
          String entryName = source.relativize(path)
            .toString()
            .replace("\\", "/");

          try (InputStream inputStream =
                 new BufferedInputStream(new FileInputStream(path.toFile()))) {

            JarEntry jarEntry = new JarEntry(entryName);
            jarOutputStream.putNextEntry(jarEntry);

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
              jarOutputStream.write(buffer, 0, bytesRead);
            }

            jarOutputStream.closeEntry();

          } catch (IOException e) {
            throw new RuntimeException("Error adding file to jar: " + path, e);
          }
        });
    }
  }

  public static void clearJavaOptions ()
  {
    if (SystemUtil.isWindows()) {
      ProcessUtil.run(new String[] { "cmd.exe", "/C", "setx _JAVA_OPTIONS \"\"" }, true);
    }
    // TODO: Linux and macOS support for clearing _JAVA_OPTIONS?
  }
}