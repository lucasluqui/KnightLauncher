package com.lucasallegri.launcher;

import java.awt.event.ActionEvent;

import com.lucasallegri.util.SteamUtil;

public class EventHandler {
	
	public static void launchEvent(ActionEvent action) {
		try {
			SteamUtil.startGameById("99900");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
