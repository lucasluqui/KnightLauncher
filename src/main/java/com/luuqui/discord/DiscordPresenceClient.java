package com.luuqui.discord;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.luuqui.launcher.LocaleManager;
import com.luuqui.launcher.LauncherGlobals;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

import static com.luuqui.discord.Log.log;

@Singleton
public class DiscordPresenceClient
{

  /**
   * Client ID to use with this Discord RPC instance.
   * For example, "123456789123456". It has to be a string.
   */
  private String clientId;

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
   * The event handler which handles all the Discord bits.
   */
  private DiscordEventHandlers eventHandler;

  /**
   * The locale manager to get localized presence messages.
   */
  @Inject protected LocaleManager _localeManager;

  public DiscordPresenceClient ()
  {
    // empty.
  }

  public DiscordPresenceClient (String clientId, boolean stub)
  {
    this.clientId = clientId;

    // Give ARM and macOS users a stub version of the DiscordPresenceClient object
    // so that it knows not to do anything when prompted.
    this.stub = stub;
    this.eventHandler = stub ? null : new DiscordEventHandlers();
  }

  public void init (String clientId, boolean stub)
  {
    this.clientId = clientId;
    this.stub = stub;
    if (stub) return;

    this.eventHandler = new DiscordEventHandlers();
    DiscordRPC.discordInitialize(this.clientId, this.eventHandler, true);
    setDetails(_localeManager.getValue("presence.starting"));
    log.info("Discord presence client is now running.");
  }

  public void setDetails (String details)
  {
    if (stub) return;
    this.details = details;
    updatePresenceDetails(details);
  }

  @SuppressWarnings("unused")
  public String getDetails ()
  {
    return this.details;
  }

  private void updatePresenceDetails (String details)
  {
    DiscordRichPresence.Builder presence = new DiscordRichPresence.Builder(_localeManager.getValue("presence.using"));
    presence.setDetails(details);
    presence.setBigImage("icon-512", _localeManager.getValue("presence.image_desc", LauncherGlobals.LAUNCHER_VERSION));
    DiscordRPC.discordUpdatePresence(presence.build());
    log.info("Updating discord presence detail", "detail", details);
  }

  public void stop ()
  {
    if (stub) return;
    DiscordRPC.discordShutdown();
  }

}
