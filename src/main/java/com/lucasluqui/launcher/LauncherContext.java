package com.lucasluqui.launcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lucasluqui.discord.DiscordPresenceClient;
import com.lucasluqui.launcher.ui.*;
import com.lucasluqui.launcher.setting.Settings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.lucasluqui.launcher.Log.log;

@Singleton
public class LauncherContext
{
  public LauncherContext ()
  {
    // empty.
  }

  public void init ()
  {
    // empty.
  }

  public void registerUI (String id, BaseUI ui)
  {
    _uiSet.put(id, ui);
    log.info("Registered UI", "id", id);
  }

  public <T extends BaseUI> T getUI (String id)
  {
    return (T) _uiSet.get(id);
  }

  public Map<String, BaseUI> getUISet ()
  {
    return _uiSet;
  }

  public void disposeUI (String id)
  {
    _uiSet.remove(id);
  }

  public void toggleElementsBlock (boolean block)
  {
    for (String id : _uiSet.keySet()) {
      _uiSet.get(id).toggleElementsBlock(block);
    }
  }

  public void selectedServerChanged ()
  {
    for (String id : _uiSet.keySet()) {
      // DO NOT call LauncherUI::selectedServerChanged, or it will loop infinitely.
      if (id.equalsIgnoreCase("launcher")) return;

      _uiSet.get(id).selectedServerChanged();
    }
  }

  public void exit (boolean force)
  {
    _discordPresenceClient.stop();
    if (force || !Settings.keepOpen) {
      try {
        getUI("launcher").guiFrame.dispose();
      } catch (NullPointerException e) {
        log.error("Failed to dispose frame on exit");
      }
      System.exit(0);
    }
  }

  private final Map<String, BaseUI> _uiSet = new HashMap<>();
  @Inject private DiscordPresenceClient _discordPresenceClient;
  @Inject public ProgressBar _progressBar = new ProgressBar();
}
