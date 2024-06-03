package com.luuqui.launcher;

import com.luuqui.launcher.settings.SettingsProperties;
import com.luuqui.util.Compressor;
import com.luuqui.util.FileUtil;
import com.luuqui.util.JavaUtil;
import com.luuqui.util.SystemUtil;

import java.io.IOException;

import static com.luuqui.launcher.Log.log;

public class ModuleLoader {

  public static void loadIngameRPC() {
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

  public static void loadJarCommandLine() {
    try {
      int vmArch = JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath());
      if (SystemUtil.isWindows()) {
        FileUtil.extractFileWithinJar("/modules/jarcmd/jar-" + vmArch + ".exe", LauncherGlobals.USER_DIR + "/java_vm/bin/jar.exe");
      } else {
        FileUtil.extractFileWithinJar(vmArch == 64 ? "/modules/jarcmd/jar-amd64" : "/modules/jarcmd/jar-i386", LauncherGlobals.USER_DIR + "/java_vm/bin/jar");
      }
    } catch (IOException e) {
      log.error(e);
    }
  }

}
