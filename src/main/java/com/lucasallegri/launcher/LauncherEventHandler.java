package com.lucasallegri.launcher;

import com.lucasallegri.launcher.mods.ModLoader;
import com.lucasallegri.launcher.settings.GameSettings;
import com.lucasallegri.launcher.settings.Settings;
import com.lucasallegri.util.ProcessUtil;
import com.lucasallegri.util.SteamUtil;
import com.lucasallegri.util.SystemUtil;

import static com.lucasallegri.launcher.Log.log;

public class LauncherEventHandler {

  private static final String[] RPC_COMMAND_LINE = new String[] { ".\\KnightLauncher\\modules\\skdiscordrpc\\SK-DiscordRPC.exe" };

  public static void launchGameEvent() {

    Thread launchThread = new Thread(() -> {

      if (ModLoader.mountRequired) ModLoader.mount();
      GameSettings.load();

      if (Settings.gamePlatform.startsWith("Steam")) {

        try {
          SteamUtil.startGameById(99900);
        } catch (Exception e) {
          log.error(e);
        }

      } else {

        if (SystemUtil.isWindows()) {
          ProcessUtil.run(LauncherGlobals.GETDOWN_ARGS_WIN, true);
        } else {
          ProcessUtil.run(LauncherGlobals.GETDOWN_ARGS, true);
        }

      }

      log.info("Starting game", "platform", Settings.gamePlatform);

      LauncherApp.getRPC().stop();
      if (Settings.useIngameRPC) ProcessUtil.run(RPC_COMMAND_LINE, true);
      if (!Settings.keepOpen) {
        LauncherGUI.launcherGUIFrame.dispose();
        System.exit(1);
      }

    });
    launchThread.start();

  }

  public static void launchGameAltEvent() {

    Thread launchAltThread = new Thread(() -> {

      if (!SystemUtil.isWindows()) {
        ProcessUtil.run(LauncherGlobals.ALT_CLIENT_ARGS, true);
      } else {
        ProcessUtil.run(LauncherGlobals.ALT_CLIENT_ARGS_WIN, true);
      }

      LauncherApp.getRPC().stop();

    });
    launchAltThread.start();

  }

}
