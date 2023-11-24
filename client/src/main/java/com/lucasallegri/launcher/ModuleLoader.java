package com.lucasallegri.launcher;

import com.lucasallegri.launcher.settings.SettingsProperties;
import com.lucasallegri.util.Compressor;
import com.lucasallegri.util.FileUtil;
import com.lucasallegri.util.JavaUtil;
import com.lucasallegri.util.SystemUtil;

import java.io.IOException;

import static com.lucasallegri.launcher.Log.log;

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
      int vmArch = JavaUtil.getJVMArch(SystemUtil.isWindows() ? LauncherGlobals.USER_DIR + "\\java_vm\\bin\\java.exe" : LauncherGlobals.USER_DIR + "\\java\\bin\\java");
      if (SystemUtil.isWindows()) {
        FileUtil.extractFileWithinJar("/modules/jarcmd/jar-" + vmArch + ".exe", LauncherGlobals.USER_DIR + "\\java_vm\\bin\\jar.exe");
      } else {
          FileUtil.extractFileWithinJar(vmArch == 64 ? "/modules/jarcmd/jar-amd64" : "/modules/jarcmd/jar-i386", LauncherGlobals.USER_DIR + "\\java\\bin\\jar");
      }
    } catch (IOException e) {
      log.error(e);
    }
  }

}
