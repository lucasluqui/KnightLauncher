package com.lucasallegri.launcher;

import com.lucasallegri.launcher.settings.SettingsProperties;
import com.lucasallegri.util.Compressor;
import com.lucasallegri.util.FileUtil;
import com.lucasallegri.util.SystemUtil;

import java.io.File;
import java.io.IOException;

import static com.lucasallegri.launcher.Log.log;

public class Modules {

  public static void setupIngameRPC() {
    if (SystemUtil.isWindows() && SystemUtil.is64Bit()) {
      try {
        FileUtil.extractFileWithinJar(LauncherGlobals.USER_DIR + "/modules/skdiscordrpc/bundle.zip", LauncherGlobals.USER_DIR + "KnightLauncher/modules/skdiscordrpc/bundle.zip");
        Compressor.unzip(LauncherGlobals.USER_DIR + "KnightLauncher/modules/skdiscordrpc/bundle.zip", LauncherGlobals.USER_DIR + "KnightLauncher/modules/skdiscordrpc/", false);
        new File(LauncherGlobals.USER_DIR + "KnightLauncher/modules/skdiscordrpc/bundle.zip").delete();
        SettingsProperties.setValue("launcher.ingameRPCSetup", "true");
      } catch (IOException e) {
        log.error(e);
      }
    } else {
      SettingsProperties.setValue("launcher.ingameRPCSetup", "true");
      SettingsProperties.setValue("launcher.useIngameRPC", "false");
    }
  }

}
