package com.lucasallegri.launcher;

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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static com.lucasallegri.launcher.Log.log;

public class PostInitRoutine {

  public PostInitRoutine(LauncherApp app) {
    ModLoader.checkInstalled();
    if (Settings.doRebuilds && ModLoader.rebuildRequired) ModLoader.startFileRebuild();
    if (Settings.useIngameRPC) Modules.setupIngameRPC();
    if (!FileUtil.fileExists(LauncherGlobals.USER_DIR + "/KnightLauncher/modules/safeguard/bundle.zip")) {
      ModLoader.extractSafeguard();
    }

    LauncherApp.getRPC().setDetails(Locale.getValue("presence.launch_ready", String.valueOf(ModList.installedMods.size())));
    loadOnlineAssets();
  }

  private static void loadOnlineAssets() {
    Thread oassetsThread = new Thread(() -> {

      checkVersion();
      getProjectXVersion();

      int steamPlayers = SteamUtil.getCurrentPlayers("99900");
      if (steamPlayers == 0) {
        LauncherGUI.playerCountLabel.setText(Locale.getValue("error.get_player_count"));
      } else {
        int approximateTotalPlayers = Math.round(steamPlayers * 1.6f);
        LauncherGUI.playerCountLabel.setText(Locale.getValue("m.player_count", new String[]{
                String.valueOf(approximateTotalPlayers), String.valueOf(steamPlayers)
        }));
      }

      String tweets;
      tweets = INetUtil.getWebpageContent(LauncherGlobals.CDN_URL + "tweets.html");
      if (tweets == null) {
        LauncherGUI.tweetsContainer.setText(Locale.getValue("error.tweets_retrieve"));
      } else {
        String styledTweets = tweets.replaceFirst("FONT_FAMILY", LauncherGUI.tweetsContainer.getFont().getFamily())
                .replaceFirst("COLOR", Settings.launcherStyle.equals("dark") ? "#ffffff" : "#000000");
        LauncherGUI.tweetsContainer.setContentType("text/html");
        LauncherGUI.tweetsContainer.setText(styledTweets);
      }

      Image eventImage;
      String eventImageLang = Settings.lang.startsWith("es") ? "es" : "en";
      eventImage = ImageUtil.getImageFromURL(LauncherGlobals.CDN_URL + "event_" + eventImageLang + ".png", 525, 305);
      if (eventImage == null) {
        LauncherGUI.imageContainer.setText(Locale.getValue("error.event_image_missing"));
      } else {
        eventImage = ImageUtil.addRoundedCorners(eventImage, 25);
        LauncherGUI.imageContainer.setText("");
        LauncherGUI.imageContainer.setIcon(new ImageIcon(eventImage));
      }
    });
    oassetsThread.start();
  }

  private static void checkVersion() {

    String rawResponseReleases = INetUtil.getWebpageContent(
            LauncherGlobals.GITHUB_API
                    + "repos/"
                    + LauncherGlobals.GITHUB_AUTHOR + "/"
                    + LauncherGlobals.GITHUB_REPO + "/"
                    + "releases/"
                    + "latest"
    );

    if(rawResponseReleases != null) {
      JSONObject jsonReleases = new JSONObject(rawResponseReleases);

      String latestRelease = jsonReleases.getString("tag_name");
      if (latestRelease.equalsIgnoreCase(LauncherGlobals.VERSION)) {
        Settings.isOutdated = true;
        LauncherGUI.updateButton.setVisible(true);
      }
    } else {
      log.error("Received no response from GitHub. Possible downtime?");
    }
  }

  private static void getProjectXVersion() {
    InputStream stream = new ByteArrayInputStream((
            INetUtil.getWebpageContent("http://gamemedia2.spiralknights.com/spiral/client/getdown.txt"))
            .getBytes(StandardCharsets.UTF_8));
    Properties prop = new Properties();
    try {
      prop.load(stream);
    } catch (IOException e) {
      log.error(e);
    }

    LauncherApp.projectXVersion = prop.getProperty("version");
    log.info("Latest ProjectX version updated", LauncherApp.projectXVersion);
  }

}
