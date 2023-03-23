package com.lucasallegri.util;

import com.lucasallegri.launcher.LauncherGlobals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Properties;

import static com.lucasallegri.launcher.settings.Log.log;

public class JavaUtil {

  public static int getJVMArch(String path) {
    String[] output;
    if (SystemUtil.isWindows()) {
      output = ProcessUtil.runAndCapture(new String[]{ "cmd.exe", "/C", path, "-version" });
    } else {
      output = ProcessUtil.runAndCapture(new String[]{ "/bin/bash", "-c", path, "-version" });
    }

    // We got no output, so we can't do any checks.
    if(output[1].isEmpty()) return 0;

    // Matches a 64-bit '-version' output.
    if(output[1].contains("64-Bit Server VM")) return 64;

    // No results matched. We assume it's 32-bit.
    return 32;
  }

  public static String getGameJVMData() {
    String path = System.getProperty("user.dir") + (SystemUtil.isWindows() ? "\\java_vm\\release" : "\\java\\release");
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

}
