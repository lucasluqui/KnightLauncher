package com.luuqui.launcher;

import java.io.File;

public class LauncherGlobals {

  public static final String LAUNCHER_VERSION = "2.0-PRE5";

  public static final String LAUNCHER_NAME = "Knight Launcher";

  public static final String FLAMINGO_ENDPOINT = "http://flamingo.luuqui.com:6060";

  public static final String GITHUB_API = "https://api.github.com/";
  public static final String GITHUB_AUTHOR = "lucasluqui";
  public static final String GITHUB_REPO = "KnightLauncher";
  public static final String GITHUB_BRANCH = "master";

  public static final String URL_CDN_V1 = "https://lucasallegri.b-cdn.net/knightlauncher/v1/";
  public static final String URL_CDN_LARGE_V1 = "https://lucasallegri.b-cdn.net/knightlauncher/v1/";
  public static final String URL_CDN_V2 = "https://lucasallegri.b-cdn.net/knightlauncher/v2/";
  public static final String URL_JAVA_REDISTRIBUTABLES = "https://lucasallegri.b-cdn.net/knightlauncher/v2/java/windows/{version}/redist.zip";
  public static final String URL_DISCORD = "https://discord.gg/RAf499a";
  public static final String URL_GET_MODS = "https://discord.gg/fAR8qtrat2";
  public static final String URL_KOFI = "https://ko-fi.com/lucasallegri";
  public static final String URL_BUG_REPORT = "https://github.com/lucasluqui/KnightLauncher/issues";

  public static final String USER_DIR = System.getProperty("user.dir");

  public static final String RPC_CLIENT_ID = "626524043209867274";

  public static final String[] GETDOWN_ARGS;
  public static final String[] GETDOWN_ARGS_WIN;
  public static final String[] ALT_CLIENT_ARGS;
  public static final String[] ALT_CLIENT_ARGS_WIN;

  static {

    String javaPath;
    if (new File(USER_DIR + File.separator + "./java/bin/java").exists()) {
      javaPath = "./java/bin/java";
    } else {
      javaPath = "./java_vm/bin/java";
    }

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
        USER_DIR + File.separator + "getdown-pro.jar",
        ".",
        "client"
    };

    ALT_CLIENT_ARGS = new String[]{
        javaPath,
        "-classpath",
        USER_DIR + File.separator + "./code/config.jar:" +
            USER_DIR + File.separator + "./code/projectx-config.jar:" +
            USER_DIR + File.separator + "./code/projectx-pcode.jar:" +
            USER_DIR + File.separator + "./code/lwjgl.jar:" +
            USER_DIR + File.separator + "./code/lwjgl_util.jar:" +
            USER_DIR + File.separator + "./code/jinput.jar:" +
            USER_DIR + File.separator + "./code/jutils.jar:" +
            USER_DIR + File.separator + "./code/jshortcut.jar:" +
            USER_DIR + File.separator + "./code/commons-beanutils.jar:" +
            USER_DIR + File.separator + "./code/commons-digester.jar:" +
            USER_DIR + File.separator + "./code/commons-logging.jar:" +
            USER_DIR + File.separator + "./KnightLauncher.jar:",
        "-Dcom.threerings.getdown=false",
        "-Xms256M",
        "-Xmx512M",
        "-XX:+AggressiveOpts",
        "-XX:SoftRefLRUPolicyMSPerMB=10",
        "-Djava.library.path=" + USER_DIR + File.separator + "./native",
        "-Dorg.lwjgl.util.NoChecks=true",
        "-Dsun.java2d.d3d=false",
        "-Dappdir=" + USER_DIR + File.separator + ".",
        "-Dresource_dir=" + USER_DIR + File.separator + "./rsrc",
        "-XX:+UseStringDeduplication",
        "com.lucasallegri.bootstrap.ProjectXBootstrap",
    };

    ALT_CLIENT_ARGS_WIN = new String[]{
        javaPath,
        "-classpath",
        USER_DIR + File.separator + "./code/config.jar;" +
            USER_DIR + File.separator + "./code/projectx-config.jar;" +
            USER_DIR + File.separator + "./code/projectx-pcode.jar;" +
            USER_DIR + File.separator + "./code/lwjgl.jar;" +
            USER_DIR + File.separator + "./code/lwjgl_util.jar;" +
            USER_DIR + File.separator + "./code/jinput.jar;" +
            USER_DIR + File.separator + "./code/jutils.jar;" +
            USER_DIR + File.separator + "./code/jshortcut.jar;" +
            USER_DIR + File.separator + "./code/commons-beanutils.jar;" +
            USER_DIR + File.separator + "./code/commons-digester.jar;" +
            USER_DIR + File.separator + "./code/commons-logging.jar;" +
            USER_DIR + File.separator + "./KnightLauncher.jar;",
        "-Dcom.threerings.getdown=false",
        "-Xms256M",
        "-Xmx512M",
        "-XX:+AggressiveOpts",
        "-XX:SoftRefLRUPolicyMSPerMB=10",
        "-Djava.library.path=" + USER_DIR + File.separator + "./native",
        "-Dorg.lwjgl.util.NoChecks=true",
        "-Dsun.java2d.d3d=false",
        "-Dappdir=" + USER_DIR + File.separator + ".",
        "-Dresource_dir=" + USER_DIR + File.separator + "./rsrc",
        "-XX:+UseStringDeduplication",
        "com.lucasallegri.bootstrap.ProjectXBootstrap",
    };
  }

}
