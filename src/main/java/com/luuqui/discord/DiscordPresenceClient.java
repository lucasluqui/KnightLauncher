package com.luuqui.discord;

import com.luuqui.launcher.Locale;
import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.util.SystemUtil;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

import static com.luuqui.discord.Log.log;

public class DiscordPresenceClient {

  /**
   * Client ID of this DiscordRPC Instance.
   * Example: "123456789123456". Must be a String.
   */
  private final String clientId;

  /**
   * Presence's current details field.
   */
  private String details;

  /**
   * Used to mark this object as "stub" for when requested by ARM and macOS systems,
   * so that it doesn't attempt to follow through with any of the calls.
   */
  private boolean stub;

  /**
   * The event handler which... handles all the Discord bits.
   */
  private final DiscordEventHandlers eventHandler;

  /**
   * Our not-so-beautiful presence client.
   */
  private static DiscordPresenceClient _instance = null;

  public DiscordPresenceClient(String clientId) {
    this.clientId = clientId;
    this.eventHandler = new DiscordEventHandlers();
  }

  public DiscordPresenceClient(String clientId, boolean stub) {
    this.clientId = clientId;
    this.stub = stub;
    this.eventHandler = null;
  }

  public void start() {
    if (stub) return;
    DiscordRPC.discordInitialize(this.clientId, this.eventHandler, true);
    setDetails(Locale.getValue("presence.starting"));
    log.info("Discord RPC Instance is now running.");
  }

  public void setDetails(String details) {
    if (stub) return;
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
    if (stub) return;
    DiscordRPC.discordShutdown();
  }

  public static DiscordPresenceClient getInstance() {
    if (_instance == null) {
      // Give ARM and macOS users a stub version of the DiscordPresenceClient object
      // so that it knows not to do anything when prompted.
      if (SystemUtil.isARM() || SystemUtil.isMac()) {
        _instance = new DiscordPresenceClient("0", true);
        return _instance;
      }

      try {
        _instance = new DiscordPresenceClient(LauncherGlobals.RPC_CLIENT_ID);
      } catch (UnsatisfiedLinkError e) {
        log.error(e);
        SystemUtil.fixTempDir(LauncherGlobals.USER_DIR + "/KnightLauncher/temp/");
        _instance = new DiscordPresenceClient(LauncherGlobals.RPC_CLIENT_ID);
      }
    }
    return _instance;
  }

}
