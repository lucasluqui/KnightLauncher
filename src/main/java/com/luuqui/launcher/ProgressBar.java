package com.luuqui.launcher;

import com.luuqui.launcher.mod.ModListGUI;
import com.luuqui.util.ThreadingUtil;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.luuqui.launcher.Log.log;

public class ProgressBar {

  private static int activeTasks = 0;

  public static void setState(String newState) {
    LauncherGUI.launchState.setText(newState);
    ModListGUI.refreshProgressBar.setString(newState);
    log.info(newState);
  }

  public static void startTask() {
    activeTasks++;
    showBar(true);
  }

  public static void finishTask() {
    activeTasks--;
    if(activeTasks == 0) {
      setState("Finished");
      Thread finishDelayThread = new Thread(() -> {
        showBar(false);
      });
      ThreadingUtil.executeWithDelay(finishDelayThread, 8000);
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

  private static void showBar(boolean show) {
    LauncherGUI.launchBackground.setVisible(show);
    LauncherGUI.launchState.setVisible(show);
    LauncherGUI.launchProgressBar.setVisible(show);
    ModListGUI.refreshProgressBar.setVisible(show);
  }

}
