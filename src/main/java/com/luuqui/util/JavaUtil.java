package com.luuqui.util;

import com.luuqui.launcher.LauncherGlobals;
import com.samskivert.util.Folds;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import static com.luuqui.launcher.settings.Log.log;

public class JavaUtil {

  public static int getJVMArch(String path) {
    String[] output;
    if (SystemUtil.isWindows()) {
      output = ProcessUtil.runAndCapture(new String[]{ "cmd.exe", "/C", path, "-version" });
      // We got no output, so we can't do any checks.
      if(output[1].isEmpty()) return 0;

      // Matches a 64-bit '-version' output.
      if(output[1].contains("64-Bit Server VM")) return 64;
    } else {
      output = ProcessUtil.runAndCapture(new String[]{ "file", path });
      // We got no output, so we can't do any checks.
      if(output[0].isEmpty()) return 0;

      // Matches a 64-bit.
      if(output[0].contains("64-Bit") || output[0].contains("PE32+")) return 64;
    }

    // No results matched. We assume it's 32-bit.
    return 32;
  }

  public static String getGameJVMData() {
    String path = getGameJavaDirPath() + "/release";
    if(!FileUtil.fileExists(path)) {
      return "Unknown Java VM";
    }

    Properties releaseFile = new Properties();
    try {
      releaseFile.load(Files.newInputStream(new File(path).toPath()));
    } catch (IOException e) {
      log.error(e);
    }

    String version = releaseFile.getProperty("JAVA_VERSION");
    String osArch = releaseFile.getProperty("OS_ARCH");

    return (version + ", " + osArch).replace("\"", "");
  }

  public static String getGameJavaDirPath() {
    File javaVMDir = new File(LauncherGlobals.USER_DIR, "/java_vm");
    if (javaVMDir.exists() && javaVMDir.isDirectory()) {
     return javaVMDir.getAbsolutePath();
    }
    File javaDIR = new File(LauncherGlobals.USER_DIR, "/java");
    if (javaDIR.exists() && javaDIR.isDirectory()) {
      return javaDIR.getAbsolutePath();
    }
    return "";
  }

  public static String getGameJVMExePath() {
    String javaDir = getGameJavaDirPath();
    if (new File(javaDir, "bin/java.exe").exists()) {
      return javaDir + "/bin/java.exe";
    }
    if (new File(javaDir, "bin/java").exists()) {
      return javaDir + "/bin/java";
    }
    log.error("Cannot locate local java executable");
    return "java";
  }

}
