package xyz.lucasallegri.util;

import java.awt.Desktop;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

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

}
