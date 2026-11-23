package com.lucasluqui.discord;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lucasluqui.util.BuildConfig;
import com.lucasluqui.util.ThreadingUtil;
import com.lucasluqui.launcher.LocaleManager;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

import static com.lucasluqui.discord.Log.log;

@Singleton
public class DiscordPresenceClient
{
  public DiscordPresenceClient ()
  {
    // empty.
  }

  public void init (String clientId, boolean stub)
  {
    this._stub = stub;
    if (stub) return;

    this._clientId = clientId;

    ThreadingUtil.start("DiscordPresence", () -> {
      DiscordRPC.discordInitialize(this._clientId, this.getEventHandlers(), true);
      synchronized (this) {
        this._running = true;
        // Details set while we were starting up are newer than the "starting" ones.
        updatePresenceDetails(_details != null ? _details : _localeManager.getValue("presence.starting"));
      }
      log.debug("Discord presence client is now running");
    });
  }

  public synchronized void setDetails (String details)
  {
    if (_stub) return;

    this._details = details;

    // Keep the details around, they'll be sent once the client is resumed.
    if (_running) updatePresenceDetails(details);
  }

  @SuppressWarnings("unused")
  public String getDetails ()
  {
    return this._details;
  }

  private void updatePresenceDetails (String details)
  {
    DiscordRichPresence.Builder presence = new DiscordRichPresence.Builder(_localeManager.getValue("presence.using"));
    presence.setDetails(details);
    presence.setBigImage("icon-512", "Knight Launcher " + BuildConfig.getVersion());
    DiscordRPC.discordUpdatePresence(presence.build());
    log.debug("Updating discord presence detail", "detail", details);
  }

  public synchronized void stop ()
  {
    if (_stub || !_running) return;
    DiscordRPC.discordShutdown();
    this._running = false;
    log.debug("Discord presence client stopped");
  }

  /**
   * Starts the client back up after being stopped, restoring the last known details.
   */
  public synchronized void resume ()
  {
    if (_stub || _running || _clientId == null) return;
    DiscordRPC.discordInitialize(this._clientId, this.getEventHandlers(), true);
    this._running = true;
    if (_details != null) updatePresenceDetails(_details);
    log.debug("Discord presence client resumed");
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
    if (this._stub) return null;

    if (this._eventHandlers == null) {
      this._eventHandlers = new DiscordEventHandlers.Builder()
        .setErroredEventHandler((errCode, err) -> log.error("Discord presence error " + errCode + ": " + err))
        .setReadyEventHandler((user) -> log.info("Discord presence registered for user: " + user.username))
        .build();
    }

    return (DiscordEventHandlers) this._eventHandlers;
  }

  /** To get localized presence messages. */
  @Inject protected LocaleManager _localeManager;

  /**
   * Client ID to use with this Discord RPC instance.
   * For example, "123456789123456". It has to be a string.
   */
  private String _clientId;

  /** Presence's current details field. */
  private String _details;

  /**
   * Used to mark this object as "stub" for when requested by ARM and Mac,
   * so that it doesn't attempt to follow through with any of the calls.
   */
  private boolean _stub;

  /** Whether the Discord RPC connection is currently initialized. */
  private volatile boolean _running;

  /**
   * The event handlers which bother with all the Discord RPC callback bits.
   * Abstracted to Object to avoid injecting the actual class on ARM or Mac.
   */
  private Object _eventHandlers;
}
