package com.lucasluqui.launcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lucasluqui.discord.DiscordPresenceClient;
import com.lucasluqui.launcher.editor.EditorsGUI;
import com.lucasluqui.launcher.mod.ModListGUI;
import com.lucasluqui.launcher.setting.Settings;
import com.lucasluqui.launcher.setting.SettingsGUI;

import static com.lucasluqui.launcher.Log.log;

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
    // empty.
  }

  public void init ()
  {
    // empty.
  }

  public void block ()
  {
    try {
      launcherGUI.eventHandler.updateServerSwitcher(true);
      launcherGUI.closeButton.setEnabled(false);
      launcherGUI.launchButton.setEnabled(false);
      launcherGUI.launchPopupMenuButton.setEnabled(false);
      launcherGUI.settingsButton.setEnabled(false);
      launcherGUI.modButton.setEnabled(false);
      launcherGUI.editorsButton.setEnabled(false);
      settingsGUI.forceRebuildButton.setEnabled(false);
    } catch (Exception ignored) {}
  }

  public void unblock ()
  {
    try {
      launcherGUI.eventHandler.updateServerSwitcher(false);
      launcherGUI.closeButton.setEnabled(true);
      launcherGUI.launchButton.setEnabled(true);
      launcherGUI.launchPopupMenuButton.setEnabled(true);
      launcherGUI.settingsButton.setEnabled(true);
      launcherGUI.modButton.setEnabled(true);
      launcherGUI.editorsButton.setEnabled(true);
      settingsGUI.forceRebuildButton.setEnabled(true);
    } catch (Exception ignored) {}
  }

  public void exit (boolean force)
  {
    _discordPresenceClient.stop();
    if (force || !Settings.keepOpen) {
      try {
        launcherGUI.guiFrame.dispose();
      } catch (NullPointerException e) {
        log.error("Failed to dispose main GUI Frame on exit");
      }
      System.exit(0);
    }
  }
}
