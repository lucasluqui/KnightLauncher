package xyz.lucasallegri.util;

import java.awt.Desktop;
import java.net.URI;

public class SteamUtil {

    public static void startGameById(String id) throws Exception
    {
        Desktop desktop = Desktop.getDesktop();
        URI steamProtocol = new URI("steam://run/" + id);
        desktop.browse(steamProtocol);
    }

}
