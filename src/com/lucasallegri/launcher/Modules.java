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
        FileUtil.extractFileWithinJar("/modules/skdiscordrpc/bundle.zip", "KnightLauncher/modules/skdiscordrpc/bundle.zip");
        Compressor.unzip("KnightLauncher/modules/skdiscordrpc/bundle.zip", "KnightLauncher/modules/skdiscordrpc/", false);
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

  public static void setupSafeguard() {
    try {
      FileUtil.extractFileWithinJar("/modules/safeguard/bundle.zip", "KnightLauncher/modules/safeguard/bundle.zip");
      Compressor.unzip("KnightLauncher/modules/safeguard/bundle.zip", "rsrc/", false);
    } catch (IOException e) {
      log.error(e);
    }

  }

  public static void setupUCP() {
    try {
      FileUtil.rename(new File("getdown.txt"), new File("getdown_unpatched.txt"));
      FileUtil.extractFileWithinJar("/modules/ucp/getdown.txt", "getdown.txt");
      SettingsProperties.setValue("launcher.ucpSetup", "true");
      log.info("Successfully detoured base getdown to UCP.");
    } catch (IOException e) {
      FileUtil.rename(new File("getdown_unpatched.txt"), new File("getdown.txt"));
      SettingsProperties.setValue("launcher.ucpSetup", "false");
      log.error(e);
    }
  }

}
