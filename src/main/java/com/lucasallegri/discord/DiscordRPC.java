package com.lucasallegri.discord;

import com.lucasallegri.launcher.Locale;
import com.lucasallegri.launcher.LauncherGlobals;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRichPresence;

import static com.lucasallegri.discord.Log.log;

public class DiscordRPC {

  /**
   * Client ID of this Discord RPC Instance.
   * Example: "123456789123456". Must be a String.
   */
  private final String clientId;

  /**
   * Presence's current details field.
   */
  private String details;

  private final DiscordEventHandlers EVENT_HANDLER = new DiscordEventHandlers();

  private static DiscordRPC _instance = null;

  public DiscordRPC(String clientId) {
    this.clientId = clientId;
  }

  public void start() {
    net.arikia.dev.drpc.DiscordRPC.discordInitialize(this.clientId, EVENT_HANDLER, true);
    setDetails(Locale.getValue("presence.starting"));
    log.info("Discord RPC Instance is now running.");
  }

  public void setDetails(String details) {
    this.details = details;
    updatePresenceDetails(details);
  }

  public String getDetails() {
    return this.details;
  }

  private void updatePresenceDetails(String details) {
    DiscordRichPresence.Builder presence = new DiscordRichPresence.Builder(Locale.getValue("presence.using"));
    presence.setDetails(details);
    presence.setBigImage("icon-512", Locale.getValue("presence.image_desc", LauncherGlobals.LAUNCHER_VERSION));
    net.arikia.dev.drpc.DiscordRPC.discordUpdatePresence(presence.build());
    log.info("Updating presence detail", "detail", details);
  }

  public void stop() {
    net.arikia.dev.drpc.DiscordRPC.discordShutdown();
  }

  public static DiscordRPC getInstance() {
    if (_instance == null) {
      _instance = new DiscordRPC(LauncherGlobals.RPC_CLIENT_ID);
    }
    return _instance;
  }

}
