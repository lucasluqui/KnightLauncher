package com.luuqui.launcher.setting;

import com.luuqui.launcher.LauncherApp;
import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.launcher.Locale;
import com.luuqui.launcher.ProgressBar;
import com.luuqui.util.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.Properties;

import static com.luuqui.launcher.setting.Log.log;

public class GameSettings {

  public static void load() {
    try {

      ProgressBar.startTask();
      ProgressBar.setBarMax(1);
      ProgressBar.setBarValue(0);
      ProgressBar.setState(Locale.getValue("m.apply"));

      /**
       * Back up the current extra.txt if there's no back up already.
       * This is useful if a user installs Knight Launcher and had already
       * made its own extra.txt, this way it won't get deleted forever, just renamed.
       */
      if(!FileUtil.fileExists("old-extra.txt")) {
        FileUtil.rename(new File("extra.txt"), new File("old-extra.txt"));
      }

      PrintWriter writer = new PrintWriter("extra.txt", "UTF-8");

      if (Settings.gameUseStringDeduplication) writer.println("-XX:+UseStringDeduplication");
      if (Settings.gameDisableExplicitGC) writer.println("-XX:+DisableExplicitGC");

      if (Settings.gameUseCustomGC) {
        if (Settings.gameGarbageCollector.equals("ParallelOld")) {
          writer.println("-XX:+UseParallelGC");
          writer.println("-XX:+Use" + Settings.gameGarbageCollector + "GC");
        } else {
          writer.println("-XX:+Use" + Settings.gameGarbageCollector + "GC");
        }
      }

      if (Settings.gameUndecoratedWindow) writer.println("-Dorg.lwjgl.opengl.Window.undecorated=true");

      if (Settings.gameGarbageCollector.equals("G1")) {
        writer.println("-Xms" + Settings.gameMemory + "M");
        writer.println("-Xmx" + Settings.gameMemory + "M");
      } else {
        writer.println("-Xms512M");
        writer.println("-Xmx" + Settings.gameMemory + "M");
      }

      writer.println(Settings.gameAdditionalArgs);
      writer.close();

      if(LauncherApp.selectedServer.isOfficial()) loadConnectionSettings();

      ProgressBar.setBarValue(1);
      ProgressBar.finishTask();
    } catch (FileNotFoundException | UnsupportedEncodingException e) {
      log.error(e);
    }
  }

  public static void resetGetdown() {
    int downloadAttempts = 0;
    boolean downloadCompleted = false;

    while (downloadAttempts <= 3 && !downloadCompleted) {
      downloadAttempts++;
      log.info("Resetting Getdown", "attempts", downloadAttempts);
      try {
        FileUtils.copyURLToFile(
                new URL("http://gamemedia2.spiralknights.com/spiral/client/getdown.txt"),
                new File(LauncherGlobals.USER_DIR, "getdown.txt"),
                0,
                0
        );
        FileUtils.copyURLToFile(
                new URL("http://gamemedia2.spiralknights.com/spiral/client/digest.txt"),
                new File(LauncherGlobals.USER_DIR, "digest.txt"),
                0,
                0
        );
        downloadCompleted = true;
      } catch (IOException e) {
        // Just keep retrying.
        log.error(e);
      }
    }
  }

  private static void loadConnectionSettings() {
    try {
      FileUtil.extractFileWithinJar("/config/deployment.properties", LauncherGlobals.USER_DIR + "/deployment.properties");
    } catch (IOException e) {
      log.error(e);
    }
    Properties properties = new Properties();
    try {
      properties.load(Files.newInputStream(new File(LauncherGlobals.USER_DIR + "/deployment.properties").toPath()));
    } catch (IOException e) {
      log.error(e);
    }

    properties.setProperty("server_host", Settings.gameEndpoint);
    properties.setProperty("server_ports", String.valueOf(Settings.gamePort));
    properties.setProperty("datagram_ports", String.valueOf(Settings.gamePort));
    properties.setProperty("key.public", Settings.gamePublicKey);
    properties.setProperty("client_root_url", Settings.gameGetdownURL);

    try {
      properties.store(Files.newOutputStream(new File(LauncherGlobals.USER_DIR + "/deployment.properties").toPath()), null);
    } catch (IOException e) {
      log.error(e);
    }

    String[] outputCapture = null;
    if(SystemUtil.isWindows()) {
      outputCapture = ProcessUtil.runAndCapture(new String[]{ "cmd.exe", "/C", JavaUtil.getGameJVMDirPath() + "/bin/jar.exe", "uf", "code/config.jar", "deployment.properties" });
    } else {
      outputCapture = ProcessUtil.runAndCapture(new String[]{ "/bin/bash", "-c", JavaUtil.getGameJVMDirPath() + "/bin/jar", "uf", "code/config.jar", "deployment.properties" });
    }
    log.debug("Connection settings capture, stdout=", outputCapture[0], "stderr=", outputCapture[1]);
    FileUtil.deleteFile(LauncherGlobals.USER_DIR + "/deployment.properties");
  }

}
