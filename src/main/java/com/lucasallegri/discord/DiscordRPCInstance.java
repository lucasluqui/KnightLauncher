package com.lucasallegri.discord;

import com.lucasallegri.launcher.Locale;
import com.lucasallegri.launcher.LauncherGlobals;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

import static com.lucasallegri.discord.Log.log;

public class DiscordRPCInstance {

    /**
     * Client ID of this Discord RPC Instance.
     * <br>Example: "123456789123456"
     *
     * <br>Must be a String.
     */
    private final String clientId;

    /**
     * Presence's current details field.
     */
    private String details;

    private final DiscordEventHandlers EVENT_HANDLER = new DiscordEventHandlers();

    public DiscordRPCInstance(String clientId) {
        this.clientId = clientId;
    }

    public void start() {
        DiscordRPC.discordInitialize(this.clientId, EVENT_HANDLER, true);
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
        presence.setBigImage("icon-512", Locale.getValue("presence.image_desc", LauncherGlobals.VERSION));
        DiscordRPC.discordUpdatePresence(presence.build());
        log.info("Updating presence detail", "detail", details);
    }

    public void stop() {
        DiscordRPC.discordShutdown();
    }

}
