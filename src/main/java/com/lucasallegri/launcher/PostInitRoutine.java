package com.lucasallegri.launcher;

import com.lucasallegri.discord.DiscordInstance;
import com.lucasallegri.launcher.mods.ModList;
import com.lucasallegri.launcher.mods.ModLoader;
import com.lucasallegri.launcher.settings.Settings;
import com.lucasallegri.util.FileUtil;
import com.lucasallegri.util.INetUtil;
import com.lucasallegri.util.ImageUtil;
import com.lucasallegri.util.SteamUtil;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;

public class PostInitRoutine {

  public PostInitRoutine(LauncherApp app) {
    ModLoader.checkInstalled();
    if (Settings.doRebuilds && ModLoader.rebuildRequired) ModLoader.startFileRebuild();
    if (Settings.useIngameRPC) Modules.setupIngameRPC();
    if (!FileUtil.fileExists(LauncherGlobals.USER_DIR + "/KnightLauncher/modules/safeguard/bundle.zip")) {
      ModLoader.extractSafeguard();
    }

    DiscordInstance.setPresence(LanguageManager.getValue("presence.launch_ready", String.valueOf(ModList.installedMods.size())));
    loadOnlineAssets();
  }

  private static void loadOnlineAssets() {
    Thread oassetsThread = new Thread(() -> {

      pullGithubData();
      checkVersion();

      int steamPlayers = SteamUtil.getCurrentPlayers("99900");
      if (steamPlayers == 0) {
        LauncherGUI.playerCountLabel.setText(LanguageManager.getValue("error.get_player_count"));
      } else {
        int approximateTotalPlayers = Math.round(steamPlayers * 1.6f);
        LauncherGUI.playerCountLabel.setText(LanguageManager.getValue("m.player_count", new String[]{
                String.valueOf(approximateTotalPlayers), String.valueOf(steamPlayers)
        }));
      }

      String tweets = null;
      tweets = INetUtil.getWebpageContent(LauncherGlobals.CDN_URL + "tweets.html");
      if (tweets == null) {
        LauncherGUI.tweetsContainer.setText(LanguageManager.getValue("error.tweets_retrieve"));
      } else {
        String styledTweets = tweets.replaceFirst("FONT_FAMILY", LauncherGUI.tweetsContainer.getFont().getFamily())
                .replaceFirst("COLOR", Settings.launcherStyle.equals("dark") ? "#ffffff" : "#000000");
        LauncherGUI.tweetsContainer.setContentType("text/html");
        LauncherGUI.tweetsContainer.setText(styledTweets);
      }

      Image eventImage = null;
      String eventImageLang = Settings.lang.startsWith("es") ? "es" : "en";
      eventImage = ImageUtil.getImageFromURL(LauncherGlobals.CDN_URL + "event_" + eventImageLang + ".png", 525, 305);
      if (eventImage == null) {
        LauncherGUI.imageContainer.setText(LanguageManager.getValue("error.event_image_missing"));
      } else {
        eventImage = ImageUtil.addRoundedCorners(eventImage, 25);
        LauncherGUI.imageContainer.setText("");
        LauncherGUI.imageContainer.setIcon(new ImageIcon(eventImage));
      }
    });
    oassetsThread.start();
  }

  private static void pullGithubData() {

    String rawResponseReleases = INetUtil.getWebpageContent(
            LauncherGlobals.GITHUB_API
                    + "repos/"
                    + LauncherGlobals.GITHUB_AUTHOR + "/"
                    + LauncherGlobals.GITHUB_REPO + "/"
                    + "releases/"
                    + "latest"
    );

    JSONObject jsonReleases = new JSONObject(rawResponseReleases);

    LauncherGlobals.latestRelease = jsonReleases.getString("tag_name");
  }

  private static void checkVersion() {
    if (!LauncherGlobals.latestRelease.equalsIgnoreCase(LauncherGlobals.VERSION)) {
      Settings.isOutdated = true;
      LauncherGUI.updateButton.setVisible(true);
    }
  }

}
