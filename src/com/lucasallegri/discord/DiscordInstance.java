package com.lucasallegri.discord;

import com.lucasallegri.launcher.LanguageManager;
import com.lucasallegri.launcher.LauncherConstants;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

import static com.lucasallegri.discord.Log.log;

public class DiscordInstance {
	
	private static final String CLIENT_ID = "626524043209867274";
	private static final DiscordEventHandlers EVENT_HANDLER = new DiscordEventHandlers();
	
	public static void start() {
		DiscordRPC.discordInitialize(CLIENT_ID, EVENT_HANDLER, true);
		setPresence(LanguageManager.getValue("presence.starting"));
		log.info("DiscordInstance is now running.");
	}
	
	public static void setPresence(String detail) {
		DiscordRichPresence.Builder presence = new DiscordRichPresence.Builder(LanguageManager.getValue("presence.using"));
		presence.setDetails(detail);
		presence.setBigImage("icon-512", LanguageManager.getValue("presence.image_desc", LauncherConstants.VERSION));
		DiscordRPC.discordUpdatePresence(presence.build());
		log.info("Updating presence detail", new Object[] {"detail", detail});
	}
	
	public static void stop() {
		DiscordRPC.discordShutdown();
	}

}
