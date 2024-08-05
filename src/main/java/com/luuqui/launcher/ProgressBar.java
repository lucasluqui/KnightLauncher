package com.luuqui.launcher;

import com.luuqui.launcher.mods.ModListGUI;

import static com.luuqui.launcher.Log.log;

public class ProgressBar {

  private static int activeTasks = 0;

  public static void setState(String newState) {
    LauncherGUI.launchState.setText(newState);
    log.info(newState);
  }

  public static void startTask() {
    activeTasks++;
    showState(true);
    showBar(true);
  }

  public static void finishTask() {
    activeTasks--;
    if(activeTasks == 0) {
      showState(false);
      showBar(false);
    }
  }

  public static void setBarValue(int n) {
    LauncherGUI.launchProgressBar.setValue(n);
    ModListGUI.refreshProgressBar.setValue(n);
  }

  public static void setBarMax(int n) {
    LauncherGUI.launchProgressBar.setMaximum(n);
    ModListGUI.refreshProgressBar.setMaximum(n);
  }

  private static void showState(boolean show) {
    LauncherGUI.launchState.setVisible(show);
  }

  private static void showBar(boolean show) {
    LauncherGUI.launchProgressBar.setVisible(show);
    ModListGUI.refreshProgressBar.setVisible(show);
  }

}
