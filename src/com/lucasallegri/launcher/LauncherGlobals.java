package com.lucasallegri.launcher;

import java.io.File;

public class LauncherGlobals {

  public static final String VERSION = "1.5";
  public static String _latestRelease;

  public static final String SHORTCUT_FILE_NAME = "Knight Launcher";

  public static final String GITHUB_API = "https://api.github.com/";
  public static final String GITHUB_AUTHOR = "lucas-allegri";
  public static final String GITHUB_REPO = "KnightLauncher";
  public static final String GITHUB_MAIN_BRANCH = "master";

  public static final String CDN_URL = "https://knightlauncher-cdn.lucasallegri.com/knightlauncher/v1/";
  public static final String LARGE_CDN_URL = "https://knightlauncher-large-cdn.lucasallegri.com/knightlauncher/v1/";
  public static final String DISCORD_URL = "https://discord.gg/RAf499a";
  public static final String GET_MODS_URL = "https://discord.gg/BZRyJrr";
  public static final String KOFI_URL = "https://ko-fi.com/lucasallegri";
  public static final String BUG_REPORT_URL = "https://github.com/lucas-allegri/KnightLauncher/issues";

  public static final String USER_DIR = System.getProperty("user.dir");


  public static final String[] GETDOWN_ARGS_WIN = {
          ".\\java_vm\\bin\\java.exe",
          "-Dsun.java2d.d3d=false",
          "-Dcheck_unpacked=true",
          "-jar",
          USER_DIR + File.separator + "getdown-pro.jar",
          ".",
          "client"
  };

  public static final String[] GETDOWN_ARGS = {
          "./java/bin/java",
          "-Dsun.java2d.d3d=false",
          "-Dcheck_unpacked=true",
          "-jar",
          "./getdown-pro.jar",
          ".",
          "client"
  };

  public static final String[] ALT_CLIENT_ARGS_WIN = {
          "./java_vm/bin/java",
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
                  USER_DIR + File.separator + "./code/commons-logging.jar;",
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
          "com.threerings.projectx.client.ProjectXApp",
  };

  public static final String[] ALT_CLIENT_ARGS = {
          "./java/bin/java",
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
                  USER_DIR + File.separator + "./code/commons-logging.jar:",
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
          "com.threerings.projectx.client.ProjectXApp",
  };

}
