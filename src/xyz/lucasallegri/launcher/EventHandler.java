package xyz.lucasallegri.launcher;

import xyz.lucasallegri.util.SteamUtil;
import java.awt.event.ActionEvent;

public class EventHandler {
	
	public static void launchEvent(ActionEvent action) {
		try {
			SteamUtil.startGameById("99900");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
