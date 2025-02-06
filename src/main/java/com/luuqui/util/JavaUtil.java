package com.luuqui.util;

import com.luuqui.launcher.LauncherApp;
import com.luuqui.launcher.LauncherGlobals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Properties;

import static com.luuqui.util.Log.log;

public class JavaUtil {

  public static String getJVMVersionOutput(String path) {
    String output;
    if (SystemUtil.isWindows()) {
      output = ProcessUtil.runAndCapture(new String[]{ "cmd.exe", "/C", path, "-version" })[1];
    } else {
      output = ProcessUtil.runAndCapture(new String[]{ "/bin/bash", "-c", path + " -version" })[1];
    }
    return output;
  }

  public static int getJVMArch(String path) {
    String output = getJVMVersionOutput(path);

    // We got no output, can't do any checks.
    if(output.isEmpty()) return 0;

    // Matches a 64-bit JVM.
    if(output.contains("64-Bit") || output.contains("PE32+")) return 64;

    // No results matched. We assume it's 32-bit.
    return 32;
  }

  public static int getJVMVersion(String path) {
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

  public static String getGameJVMData() {
    String path = getGameJVMDirPath() + "/release";
    String version = "";
    String osArch = "";

    if(FileUtil.fileExists(path)) {
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

    if(version.isEmpty() || osArch.isEmpty()) {
      return "Unknown Java VM";
    }

    return (version + ", " + osArch).replace("\"", "");
  }

  public static String getReadableGameJVMData() {
    String rawJavaVMData = getGameJVMData();

    if(rawJavaVMData.contains("Unknown")) {
      return "Unknown, probably 32-bit";
    }

    String javaMajorVersion = "unknown";
    String javaMinorVersion = "unknown";
    String javaArch = "unknown";

    boolean postJava8Versioning = !rawJavaVMData.startsWith("1.");

    try {
      if(postJava8Versioning) {
        // versioning for Java 10 onwards. e.g. "15.0.2"
        javaMajorVersion = rawJavaVMData;
        javaMinorVersion = rawJavaVMData;
      } else {
        // versioning for Java 8 and prior. e.g. "1.8.0_251"
        javaMajorVersion = rawJavaVMData.split("\\.")[1];
        javaMinorVersion = rawJavaVMData.split("_")[1].split(",")[0];
      }
      javaArch = JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()) == 64 ? "64-bit" : "32-bit";
    } catch (Exception e) {
      log.error(e);
    }

    if(javaMajorVersion.equalsIgnoreCase("unknown")
      || javaMinorVersion.equalsIgnoreCase("unknown")
      || javaArch.equalsIgnoreCase("unknown")) {
      return "Unknown, probably 32-bit";
    }

    if(postJava8Versioning) {
      return "Java " + javaMajorVersion + ", " + javaArch;
    }

    return "Java " + javaMajorVersion + " (" + javaMinorVersion + "), " + javaArch;
  }

  public static String getGameJVMDirPath() {
    String startingDirPath = LauncherGlobals.USER_DIR;

    if(LauncherApp.selectedServer != null) {
      if(!LauncherApp.selectedServer.name.equalsIgnoreCase("Official")) {
        startingDirPath += File.separator + "thirdparty" + File.separator + LauncherApp.selectedServer.getSanitizedName();
      }
    }

    File javaVMDir = new File(startingDirPath, "/java_vm");
    if (javaVMDir.exists() && javaVMDir.isDirectory() && SystemUtil.isWindows()) {
     return javaVMDir.getAbsolutePath();
    }
    File javaDir = new File(startingDirPath, "/java");
    if (javaDir.exists() && javaDir.isDirectory()) {
      return javaDir.getAbsolutePath();
    }
    return "";
  }

  public static String getGameJVMExePath() {
    String javaDir = getGameJVMDirPath();
    if (FileUtil.fileExists(javaDir + "/bin/java.exe")) {
      return javaDir + "/bin/java.exe";
    }
    if (FileUtil.fileExists(javaDir + "/bin/java")) {
      return javaDir + "/bin/java";
    }
    log.error("Cannot locate local java executable");
    return "java";
  }

  public static String getJavaVMCommandLineSeparator() {
    return SystemUtil.isWindows() ? ";" : ":";
  }

  public static synchronized void loadLibrary(File jar) {
    try {
      // We are using reflection here to circumvent encapsulation; addURL is not public
      URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
      URL url = jar.toURI().toURL();

      // Disallow if already loaded
      for (URL it : Arrays.asList(loader.getURLs())) {
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

  public static void addToLibraryPath(String... path) {
    String cmdLineSeparator = getJavaVMCommandLineSeparator();

    for(String p : path) {
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

}
