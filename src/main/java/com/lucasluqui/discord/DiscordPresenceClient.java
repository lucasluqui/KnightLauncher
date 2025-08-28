package com.lucasluqui.discord;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lucasluqui.launcher.BuildConfig;
import com.lucasluqui.launcher.LocaleManager;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

import static com.lucasluqui.discord.Log.log;

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
   * Used to mark this object as "stub" for when requested by ARM and Mac,
   * so that it doesn't attempt to follow through with any of the calls.
   */
  private boolean stub;

  /**
   * The event handlers which bother with all the Discord RPC callback bits.
   * Abstracted to Object to avoid injecting the actual class on ARM or Mac.
   */
  private Object eventHandlers;

  /**
   * The locale manager to get localized presence messages.
   */
  @Inject protected LocaleManager _localeManager;

  public DiscordPresenceClient ()
  {
    // empty.
  }

  public void init (String clientId, boolean stub)
  {
    this.stub = stub;
    if (stub) return;

    this.clientId = clientId;
    DiscordRPC.discordInitialize(this.clientId, this.getEventHandlers(), true);
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
    presence.setBigImage("icon-512", "Knight Launcher " + BuildConfig.getVersion());
    DiscordRPC.discordUpdatePresence(presence.build());
    log.info("Updating discord presence detail", "detail", details);
  }

  public void stop ()
  {
    if (stub) return;
    DiscordRPC.discordShutdown();
  }

  /**
   * Return the actual event handlers to interface with Discord RPC callbacks.
   * We have to abstract it in the attributes to avoid issues when the class is injected
   * as we don't want that to happen in ARM or Mac.
   *
   * @return Discord RPC event handlers.
   */
  private DiscordEventHandlers getEventHandlers ()
  {
    if (this.stub) return null;

    if (this.eventHandlers == null) {
      this.eventHandlers = new DiscordEventHandlers.Builder()
          .setErroredEventHandler((errCode, err) -> log.error("Discord presence error " + errCode + ": " + err))
          .setReadyEventHandler((user) -> log.info("Discord presence registered for user: " + user.username))
          .build();
    }

    return (DiscordEventHandlers) this.eventHandlers;
  }

}
