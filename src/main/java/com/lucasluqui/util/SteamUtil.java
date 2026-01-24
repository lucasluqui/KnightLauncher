package com.lucasluqui.util;

import org.json.JSONObject;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import static com.lucasluqui.util.Log.log;

public class SteamUtil
{

  public static void runApp (int id, boolean dialog)
  {
    // Special procedure for unix systems where we might not be able to get the darn desktop.
    if (SystemUtil.isUnix()) {
      ProcessUtil.run(new String[] { "steam", "steam://rungameid/" + id }, true);
      return;
    }

    String steamProtocolString = dialog ? "steam://launch/" + id + "/dialog" : "steam://run/" + id;
    ProcessUtil.run(new String[] { "start", steamProtocolString }, true);
    //Desktop desktop = Desktop.getDesktop();
    //URI steamProtocol = new URI(steamProtocolString);
    //desktop.browse(steamProtocol);
  }

  public static String getSteamPath ()
  {
    try {
      return WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, "Software\\Valve\\Steam", "SteamPath", 0);
    } catch (Exception e) {
      log.error(e);
    }
    return null;
  }

  public static String getGamePathWindows ()
  {
    String steamPath = getSteamPath();
    return steamPath != null ? steamPath + "/steamapps/common/Spiral Knights" : null;
  }

  public static int getCurrentPlayers (String id)
  {
    String rawResponse = INetUtil.getWebpageContent("https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?appid=" + id);
    if (rawResponse == null) return 0;
    JSONObject jsonResponse = new JSONObject(rawResponse);
    String currentPlayers = String.valueOf(jsonResponse.getJSONObject("response").getInt("player_count"));
    return Integer.parseInt(currentPlayers);
  }

  public static boolean isRunningInSteamapps ()
  {
    return System.getProperty("user.dir").contains("steamapps");
  }

}
