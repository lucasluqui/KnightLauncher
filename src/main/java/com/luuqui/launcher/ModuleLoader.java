package com.luuqui.launcher;

import com.luuqui.launcher.settings.SettingsProperties;
import com.luuqui.util.Compressor;
import com.luuqui.util.FileUtil;
import com.luuqui.util.JavaUtil;
import com.luuqui.util.SystemUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.luuqui.launcher.Log.log;

public class ModuleLoader {

  public static void loadModules() {
    Thread moduleThread = new Thread(() -> {
      loadIngameRPC();
      loadJarCommandLine();
      loadSpiralview();
    });
    moduleThread.start();
  }

  protected static void loadIngameRPC() {
    if (SystemUtil.isWindows() && SystemUtil.is64Bit()) {
      try {
        FileUtil.extractFileWithinJar("/modules/skdiscordrpc/bundle.zip", LauncherGlobals.USER_DIR + "\\KnightLauncher\\modules\\skdiscordrpc\\bundle.zip");
        Compressor.unzip(LauncherGlobals.USER_DIR + "\\KnightLauncher\\modules\\skdiscordrpc\\bundle.zip", LauncherGlobals.USER_DIR + "\\KnightLauncher\\modules\\skdiscordrpc\\", false);
        FileUtil.deleteFile(LauncherGlobals.USER_DIR + "\\KnightLauncher\\modules\\skdiscordrpc\\bundle.zip");
        SettingsProperties.setValue("launcher.ingameRPCSetup", "true");
      } catch (IOException e) {
        log.error(e);
      }
    } else {
      SettingsProperties.setValue("launcher.ingameRPCSetup", "true");
      SettingsProperties.setValue("launcher.useIngameRPC", "false");
    }
  }

  protected static void loadJarCommandLine() {
    try {
      int vmArch = JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath());
      if (SystemUtil.isWindows() && !FileUtil.fileExists(JavaUtil.getGameJavaDirPath() + "/bin/jar.exe")) {
        FileUtil.extractFileWithinJar("/modules/jarcmd/jar-" + vmArch + ".exe", JavaUtil.getGameJavaDirPath() + "/bin/jar.exe");
      } else if (!FileUtil.fileExists(JavaUtil.getGameJavaDirPath() + "/bin/jar")) {
        FileUtil.extractFileWithinJar(vmArch == 64 ? "/modules/jarcmd/jar-amd64" : "/modules/jarcmd/jar-i386", JavaUtil.getGameJavaDirPath() + "/bin/jar");
      }
    } catch (IOException e) {
      log.error(e);
    }
  }

  protected static void loadSpiralview() {
    try {
      FileUtil.extractFileWithinJar("/modules/spiralview/spiralview.jar",
        LauncherGlobals.USER_DIR + "/KnightLauncher/modules/spiralview/spiralview.jar");
    } catch (IOException e) {
      log.error(e);
    }
  }

}
