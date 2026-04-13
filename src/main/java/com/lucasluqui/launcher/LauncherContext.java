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

  public void init (LauncherApp app)
  {
    this._app = app;
  }

  public LauncherApp getApp ()
  {
    return this._app;
  }

  private LauncherApp _app;
  @Inject private DiscordPresenceClient _discordPresenceClient;
  @Inject public ProgressBar _progressBar = new ProgressBar();
}
