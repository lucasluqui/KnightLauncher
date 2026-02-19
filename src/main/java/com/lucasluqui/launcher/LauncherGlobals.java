package com.lucasluqui.launcher;

import com.lucasluqui.util.JavaUtil;

import java.io.File;

public class LauncherGlobals
{
  public static final String GITHUB_API = "https://api.github.com/";
  public static final String GITHUB_AUTHOR = "lucasluqui";
  public static final String GITHUB_REPO = "KnightLauncher";
  public static final String GITHUB_BRANCH = "main";

  public static final String CDN_ENDPOINT = "cdn.knightlauncher.com";
  public static final String CDN_VERSION = "2";

  public static final String URL_CDN = "https://" + CDN_ENDPOINT + "/knightlauncher/v" + CDN_VERSION + "/";
  public static final String URL_JAVA_REDIST = URL_CDN + "java/windows/{version}/redist.zip";
  public static final String URL_DISCORD = "https://discord.gg/8rE4GeAMza";
  public static final String URL_GET_MODS = "https://discord.gg/fAR8qtrat2";
  public static final String URL_DONATE = "https://ko-fi.com/lucasallegri";
  public static final String URL_BUG_REPORT = "https://github.com/lucasluqui/KnightLauncher/issues";

  public static final String USER_DIR = System.getProperty("user.dir");

  public static final String RPC_CLIENT_ID = "626524043209867274";

  public static final String[] GETDOWN_ARGS;
  public static final String[] GETDOWN_ARGS_MAC;
  public static final String[] GETDOWN_ARGS_WIN;
  public static final String[] ALT_CLIENT_ARGS;

  static {
    final String javaPath = JavaUtil.getGameJVMExePath();
    final String javaSeparator = JavaUtil.getJavaVMCommandLineSeparator();

    GETDOWN_ARGS = new String[] {
      javaPath,
      "-Dsun.java2d.d3d=false",
      "-Dcheck_unpacked=true",
      "-Dsilent=noupdate",
      "-jar",
      "./getdown-pro.jar",
      ".",
      "client"
    };

    GETDOWN_ARGS_MAC = new String[] {
      "java",
      "-Dsun.java2d.d3d=false",
      "-Dcheck_unpacked=true",
      "-Dsilent=noupdate",
      "-jar",
      "./getdown-pro.jar",
      ".",
      "client"
    };

    GETDOWN_ARGS_WIN = new String[] {
      javaPath,
      "-Dsun.java2d.d3d=false",
      "-Dcheck_unpacked=true",
      "-Dsilent=noupdate",
      "-jar",
      USER_DIR + File.separator + "getdown-pro.jar",
      ".",
      "client"
    };

    ALT_CLIENT_ARGS = new String[] {
      javaPath,
      "-classpath",
      USER_DIR + File.separator + "./code/config.jar" + javaSeparator +
      USER_DIR + File.separator + "./code/projectx-config.jar" + javaSeparator +
      USER_DIR + File.separator + "./code/projectx-pcode.jar" + javaSeparator +
      USER_DIR + File.separator + "./code/lwjgl.jar" + javaSeparator +
      USER_DIR + File.separator + "./code/lwjgl_util.jar" + javaSeparator +
      USER_DIR + File.separator + "./code/jinput.jar" + javaSeparator +
      USER_DIR + File.separator + "./code/jutils.jar" + javaSeparator +
      USER_DIR + File.separator + "./code/jshortcut.jar" + javaSeparator +
      USER_DIR + File.separator + "./code/commons-beanutils.jar" + javaSeparator +
      USER_DIR + File.separator + "./code/commons-digester.jar" + javaSeparator +
      USER_DIR + File.separator + "./code/commons-logging.jar" + javaSeparator +
      USER_DIR + File.separator + "./code/discord-game-sdk4j.jar" + javaSeparator +
      USER_DIR + File.separator + "./code/gson.jar" + javaSeparator,
      "-Dcom.threerings.getdown=false",
      "-Xms512M",
      "-Xmx512M",
      "-XX:+UseSerialGC",
      "-XX:SoftRefLRUPolicyMSPerMB=10",
      "-Djava.library.path=" + USER_DIR + File.separator + "./native",
      "-Dorg.lwjgl.util.NoChecks=true",
      "-Dsun.java2d.d3d=false",
      "--add-opens=java.base/java.lang=ALL-UNNAMED",
      "--add-opens=java.base/java.util=ALL-UNNAMED",
      "--enable-native-access=ALL-UNNAMED",
      "-Dappdir=" + USER_DIR + File.separator + ".",
      "-Dresource_dir=" + USER_DIR + File.separator + "./rsrc",
      "-Dcrucible.dir=" + USER_DIR + File.separator + "./crucible",
      "com.threerings.projectx.client.ProjectXApp",
    };
  }

}
