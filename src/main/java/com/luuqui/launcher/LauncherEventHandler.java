package com.luuqui.launcher;

import com.luuqui.discord.DiscordRPC;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.mods.ModLoader;
import com.luuqui.launcher.settings.GameSettings;
import com.luuqui.launcher.settings.Settings;
import com.luuqui.launcher.settings.SettingsEventHandler;
import com.luuqui.util.ProcessUtil;
import com.luuqui.util.SteamUtil;
import com.luuqui.util.SystemUtil;

import java.util.List;

import static com.luuqui.launcher.Log.log;

public class LauncherEventHandler {

  private static final String[] RPC_COMMAND_LINE = new String[] { ".\\KnightLauncher\\modules\\skdiscordrpc\\SK-DiscordRPC.exe" };

  public static void launchGameEvent() {

    Thread launchThread = new Thread(() -> {

      if (ModLoader.mountRequired) ModLoader.mount();
      SettingsEventHandler.saveAdditionalArgs();
      SettingsEventHandler.saveConnectionSettings();
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

      DiscordRPC.getInstance().stop();
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

      DiscordRPC.getInstance().stop();

    });
    launchAltThread.start();

  }

  public static void updateServerList(List<Server> servers) {
    for(Server server : servers) {
      String name = server.name;
      if(server.beta == 1) name += " (Beta)";
      LauncherGUI.serverList.addItem(name);
    }

  }

}
