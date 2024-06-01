package com.luuqui.util;

import org.json.JSONObject;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

public class SteamUtil {

  public static void startGameById(int id) throws Exception {
    if (SystemUtil.isUnix()) {
      ProcessUtil.run(new String[] {"steam", "steam://rungameid/" + id}, true);
      return;
    }
    Desktop desktop = Desktop.getDesktop();
    URI steamProtocol = new URI("steam://run/" + id);
    desktop.browse(steamProtocol);
  }

  public static String getGamePathWindows() {
    try {
      return WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, "Software\\Valve\\Steam", "SteamPath", 0)
              + "/steamapps/common/Spiral Knights";
    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static int getCurrentPlayers(String id) {
    String rawResponse = INetUtil.getWebpageContent("https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?appid=" + id);
    if (rawResponse == null) return 0;
    JSONObject jsonResponse = new JSONObject(rawResponse);
    String currentPlayers = String.valueOf(jsonResponse.getJSONObject("response").getInt("player_count"));
    return Integer.parseInt(currentPlayers);
  }

  public static boolean isRunningInSteamapps() {
    return System.getProperty("user.dir").contains("steamapps");
  }

}
