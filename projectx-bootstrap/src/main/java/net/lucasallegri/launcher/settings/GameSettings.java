package com.lucasallegri.launcher.settings;

import com.lucasallegri.launcher.LauncherGlobals;
import com.lucasallegri.launcher.Locale;
import com.lucasallegri.launcher.ProgressBar;
import com.lucasallegri.util.FileUtil;
import com.lucasallegri.util.ProcessUtil;
import com.lucasallegri.util.SystemUtil;

import java.io.*;
import java.nio.file.Files;
import java.util.Properties;

import static com.lucasallegri.launcher.settings.Log.log;

public class GameSettings {

  public static void load() {
    try {

      ProgressBar.startTask();
      ProgressBar.setBarMax(1);
      ProgressBar.setBarValue(0);
      ProgressBar.setState(Locale.getValue("m.apply"));

      /**
       * Back up the current extra.txt if there's no back up already.
       * This is useful if an user installs Knight Launcher and had already
       * made it's own extra.txt, this way it won't get deleted forever, just renamed.
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

      ProgressBar.setBarValue(1);
      ProgressBar.finishTask();
    } catch (FileNotFoundException | UnsupportedEncodingException e) {
      log.error(e);
    }
  }

  /**
   * @see com.lucasallegri.bootstrap.ProjectXBootstrap
   * @deprecated No longer use the way of modifying files.
   */
  @Deprecated
  private static void loadConnectionSettings() {
    try {
      FileUtil.extractFileWithinJar("/config/deployment.properties", LauncherGlobals.USER_DIR + "\\deployment.properties");
    } catch (IOException e) {
      log.error(e);
    }
    Properties properties = new Properties();
    try {
      properties.load(Files.newInputStream(new File(LauncherGlobals.USER_DIR + "\\deployment.properties").toPath()));
    } catch (IOException e) {
      log.error(e);
    }

    properties.setProperty("server_host", Settings.gameEndpoint);
    properties.setProperty("server_ports", String.valueOf(Settings.gamePort));
    properties.setProperty("datagram_ports", String.valueOf(Settings.gamePort));
    properties.setProperty("key.public", Settings.gamePublicKey);
    properties.setProperty("client_root_url", Settings.gameGetdownURL);

    try {
      properties.store(Files.newOutputStream(new File(LauncherGlobals.USER_DIR + "\\deployment.properties").toPath()), null);
    } catch (IOException e) {
      log.error(e);
    }

    String[] outputCapture = null;
    if(SystemUtil.isWindows()) {
      outputCapture = ProcessUtil.runAndCapture(new String[]{ "cmd.exe", "/C", LauncherGlobals.USER_DIR + "\\java_vm\\bin\\jar.exe", "uf", "code\\config.jar", "deployment.properties" });
    } else {
      outputCapture = ProcessUtil.runAndCapture(new String[]{ "/bin/bash", "-c", LauncherGlobals.USER_DIR + "\\java\\bin\\jar", "uf", "code\\config.jar", "deployment.properties" });
    }
    log.debug("Connection settings capture, stdout=", outputCapture[0], "stderr=", outputCapture[1]);
    FileUtil.deleteFile(LauncherGlobals.USER_DIR + "\\deployment.properties");
  }

}
