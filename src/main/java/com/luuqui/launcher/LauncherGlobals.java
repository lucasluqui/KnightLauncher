package com.luuqui.launcher;

import com.luuqui.util.JavaUtil;

import java.io.File;

public class LauncherGlobals
{

  public static final String LAUNCHER_VERSION = "2.1.2";

  public static final String LAUNCHER_NAME = "Knight Launcher";

  public static final String GITHUB_API = "https://api.github.com/";
  public static final String GITHUB_AUTHOR = "lucasluqui";
  public static final String GITHUB_REPO = "KnightLauncher";
  public static final String GITHUB_BRANCH = "main";

  public static final String CDN_ENDPOINT = "cdn.knightlauncher.com";
  public static final String CDN_VERSION = "v2";

  public static final String URL_CDN = "https://" + CDN_ENDPOINT + "/knightlauncher/" + CDN_VERSION + "/";
  public static final String URL_JAVA_REDIST = URL_CDN + "java/windows/{version}/redist.zip";
  public static final String URL_DISCORD = "https://discord.gg/RAf499a";
  public static final String URL_GET_MODS = "https://discord.gg/fAR8qtrat2";
  public static final String URL_DONATE = "https://ko-fi.com/lucasallegri";
  public static final String URL_BUG_REPORT = "https://github.com/lucasluqui/KnightLauncher/issues";

  public static final String USER_DIR = System.getProperty("user.dir");

  public static final String RPC_CLIENT_ID = "626524043209867274";

  public static final String BUNDLED_SPIRALVIEW_VERSION = "2.0.10";

  public static final String[] GETDOWN_ARGS;
  public static final String[] GETDOWN_ARGS_WIN;
  public static final String[] ALT_CLIENT_ARGS;

  static {
    final String javaPath = JavaUtil.getGameJVMExePath();
    final String javaSeparator = JavaUtil.getJavaVMCommandLineSeparator();

    GETDOWN_ARGS = new String[] {
        javaPath,
        "-Dsun.java2d.d3d=false",
        "-Dcheck_unpacked=true",
        "-jar",
        "./getdown-pro.jar",
        ".",
        "client"
    };

    GETDOWN_ARGS_WIN = new String[] {
        javaPath,
        "-Dsun.java2d.d3d=false",
        "-Dcheck_unpacked=true",
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
        USER_DIR + File.separator + "./code/commons-logging.jar" + javaSeparator,
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

  public static final String[] FILTER_LIST = new String[] {
      "config/accessory.dat",
      "config/accessory.xml",
      "config/actor.dat",
      "config/actor.xml",
      "config/area.dat",
      "config/area.xml",
      "config/attack.dat",
      "config/attack.xml",
      "config/battle_sprite.dat",
      "config/battle_sprite.xml",
      //"config/behavior.dat",
      //"config/behavior.xml",
      "config/catalog.dat",
      "config/catalog.xml",
      "config/conversation.dat",
      "config/conversation.xml",
      "config/cursor.dat",
      "config/cursor.xml",
      "config/depot_catalog.dat",
      "config/depot_catalog.xml",
      "config/depth_scale.dat",
      "config/depth_scale.xml",
      "config/description.dat",
      "config/description.xml",
      "config/effect.dat",
      "config/effect.xml",
      "config/emote.dat",
      "config/emote.xml",
      "config/event.dat",
      "config/event.xml",
      "config/fire_action.dat",
      "config/fire_action.xml",
      "config/font.dat",
      "config/font.xml",
      "config/forge_property.dat",
      "config/forge_property.xml",
      "config/gift.dat",
      "config/gift.xml",
      "config/ground.dat",
      "config/ground.xml",
      "config/harness.dat",
      "config/harness.xml",
      "config/interact.dat",
      "config/interact.xml",
      "config/interface_script.dat",
      "config/interface_script.xml",
      "config/item.dat",
      "config/item.xml",
      "config/item_depth_weight.dat",
      "config/item_depth_weight.xml",
      "config/item_property.dat",
      "config/item_property.xml",
      "config/level_table.dat",
      "config/level_table.xml",
      "config/material.dat",
      "config/material.xml",
      "config/mission.dat",
      "config/mission.xml",
      "config/mission_group.dat",
      "config/mission_group.xml",
      "config/mission_property.dat",
      "config/mission_property.xml",
      "config/parameterized_handler.dat",
      "config/parameterized_handler.xml",
      "config/path.dat",
      "config/path.xml",
      "config/placeable.dat",
      "config/placeable.xml",
      "config/recipe.dat",
      "config/recipe.xml",
      "config/recipe_property.dat",
      "config/recipe_property.xml",
      "config/render_effect.dat",
      "config/render_effect.xml",
      "config/render_queue.dat",
      "config/render_queue.xml",
      "config/render_scheme.dat",
      "config/render_scheme.xml",
      "config/scene_global.dat",
      "config/scene_global.xml",
      "config/shader.dat",
      "config/shader.xml",
      "config/sounder.dat",
      "config/sounder.xml",
      //"config/spawn_table.dat",
      //"config/spawn_table.xml",
      "config/status_condition.dat",
      "config/status_condition.xml",
      "config/style.dat",
      "config/style.xml",
      "config/texture.dat",
      "config/texture.xml",
      "config/tile.dat",
      "config/tile.xml",
      "config/tile_replacement.dat",
      "config/tile_replacement.xml",
      "config/uplink.dat",
      "config/uplink.xml",
      "config/variant.dat",
      "config/variant.xml",
      //"config/variant_table.dat",
      //"config/variant_table.xml",
      "config/wall.dat",
      "config/wall.xml",
  };

}
