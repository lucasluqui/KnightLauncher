package com.luuqui.launcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.luuqui.discord.DiscordPresenceClient;
import com.luuqui.launcher.editor.EditorsGUI;
import com.luuqui.launcher.mod.ModListGUI;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.launcher.setting.SettingsGUI;

@Singleton
public class LauncherContext
{

  public LauncherGUI launcherGUI;
  public SettingsGUI settingsGUI;
  public ModListGUI modListGUI;
  public EditorsGUI editorsGUI;
  public JVMPatcher jvmPatcher;
  public Updater updater;

  @Inject private DiscordPresenceClient _discordPresenceClient;
  @Inject public ProgressBar _progressBar = new ProgressBar();

  public LauncherContext ()
  {

  }

  public void init ()
  {

  }

  public void exit ()
  {
    _discordPresenceClient.stop();
    if (!Settings.keepOpen) {
      launcherGUI.guiFrame.dispose();
      System.exit(1);
    }
  }
}
