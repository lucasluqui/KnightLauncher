package com.luuqui.launcher;

import com.luuqui.util.JavaUtil;

import java.io.File;

public class LauncherGlobals {

  public static final String LAUNCHER_VERSION = "2.0.42";

  public static final String LAUNCHER_NAME = "Knight Launcher";

  public static final String GITHUB_API = "https://api.github.com/";
  public static final String GITHUB_AUTHOR = "lucasluqui";
  public static final String GITHUB_REPO = "KnightLauncher";
  public static final String GITHUB_BRANCH = "main";

  public static final String URL_CDN = "https://cdn.luuqui.com/knightlauncher/v2/";
  public static final String URL_JAVA_REDIST = URL_CDN + "java/windows/{version}/redist.zip";
  public static final String URL_DISCORD = "https://discord.gg/RAf499a";
  public static final String URL_GET_MODS = "https://discord.gg/fAR8qtrat2";
  public static final String URL_DONATE = "https://ko-fi.com/lucasallegri";
  public static final String URL_BUG_REPORT = "https://github.com/lucasluqui/KnightLauncher/issues";

  public static final String USER_DIR = System.getProperty("user.dir");

  public static final String RPC_CLIENT_ID = "626524043209867274";

  public static final String BUNDLED_SPIRALVIEW_VERSION = "2.0.9";

  public static final String[] GETDOWN_ARGS;
  public static final String[] GETDOWN_ARGS_WIN;
  public static final String[] ALT_CLIENT_ARGS;

  static {

    String javaPath = JavaUtil.getGameJVMExePath();
    String fileSeparator = File.separator;
    String javaSeparator = JavaUtil.getJavaVMCommandLineSeparator();

    GETDOWN_ARGS = new String[]{
        javaPath,
        "-Dsun.java2d.d3d=false",
        "-Dcheck_unpacked=true",
        "-jar",
        "./getdown-pro.jar",
        ".",
        "client"
    };

    GETDOWN_ARGS_WIN = new String[]{
        javaPath,
        "-Dsun.java2d.d3d=false",
        "-Dcheck_unpacked=true",
        "-jar",
        USER_DIR + fileSeparator + "getdown-pro.jar",
        ".",
        "client"
    };

    ALT_CLIENT_ARGS = new String[]{
        javaPath,
        "-classpath",
        USER_DIR + fileSeparator + "./code/config.jar" + javaSeparator +
        USER_DIR + fileSeparator + "./code/projectx-config.jar" + javaSeparator +
        USER_DIR + fileSeparator + "./code/projectx-pcode.jar" + javaSeparator +
        USER_DIR + fileSeparator + "./code/lwjgl.jar" + javaSeparator +
        USER_DIR + fileSeparator + "./code/lwjgl_util.jar" + javaSeparator +
        USER_DIR + fileSeparator + "./code/jinput.jar" + javaSeparator +
        USER_DIR + fileSeparator + "./code/jutils.jar" + javaSeparator +
        USER_DIR + fileSeparator + "./code/jshortcut.jar" + javaSeparator +
        USER_DIR + fileSeparator + "./code/commons-beanutils.jar" + javaSeparator +
        USER_DIR + fileSeparator + "./code/commons-digester.jar" + javaSeparator +
        USER_DIR + fileSeparator + "./code/commons-logging.jar" + javaSeparator,
        "-Dcom.threerings.getdown=false",
        "-Xms256M",
        "-Xmx512M",
        "-XX:+AggressiveOpts",
        "-XX:SoftRefLRUPolicyMSPerMB=10",
        "-Djava.library.path=" + USER_DIR + fileSeparator + "./native",
        "-Dorg.lwjgl.util.NoChecks=true",
        "-Dsun.java2d.d3d=false",
        "-Dappdir=" + USER_DIR + fileSeparator + ".",
        "-Dresource_dir=" + USER_DIR + fileSeparator + "./rsrc",
        "-XX:+UseStringDeduplication",
        "com.threerings.projectx.client.ProjectXApp",
    };
  }

}
