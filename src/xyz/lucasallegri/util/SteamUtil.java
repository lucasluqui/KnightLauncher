package xyz.lucasallegri.util;

import java.awt.Desktop;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.json.JSONObject;

public class SteamUtil {

    public static void startGameById(String id) throws Exception
    {
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
    
    public static String getCurrentPlayers(String id) {
    	String rawResponse = INetUtil.getWebpageContent("https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?appid="+id);
    	JSONObject jsonResponse = new JSONObject(rawResponse);
    	String currentPlayers = String.valueOf(jsonResponse.getJSONObject("response").getInt("player_count"));
    	return currentPlayers;
    }
    
    public static String getCurrentPlayersApproximateTotal(String id) {
    	String rawResponse = INetUtil.getWebpageContent("https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?appid="+id);
    	JSONObject jsonResponse = new JSONObject(rawResponse);
    	int currentPlayers = jsonResponse.getJSONObject("response").getInt("player_count");
    	String approximatePlayers = String.valueOf(Math.round(currentPlayers*1.6f));
    	return approximatePlayers;
    }

}
