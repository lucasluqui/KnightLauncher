package xyz.lucasallegri.util;

import xyz.lucasallegri.launcher.ConstantsKL;
import java.awt.Desktop;
import java.net.URI;

public class SteamUtil {

    public static void startGameById(String id) throws Exception
    {
    	
        if (ConstantsKL.USE_STEAM_PROTOCOL) {
        	
            Desktop desktop = Desktop.getDesktop();
            URI steamProtocol = new URI("steam://run/" + id);
            desktop.browse(steamProtocol);
            
        }
        
    }

}
