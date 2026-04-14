package com.lucasluqui.launcher;

import com.google.inject.Inject;
import com.lucasluqui.launcher.ui.LauncherUI;
import com.lucasluqui.launcher.ui.ModListUI;
import com.lucasluqui.launcher.ui.UINames;
import com.lucasluqui.util.ThreadingUtil;

import javax.swing.*;

public class ProgressBar
{
  public ProgressBar ()
  {
    // empty.
  }

  public void setState (String newState)
  {
    ((LauncherUI) _ctx.getApp().getUI(UINames.UI_ID_LAUNCHER)).launchState.setText(newState);
    ((ModListUI) _ctx.getApp().getUI(UINames.UI_ID_MODLIST)).refreshProgressBar.setString(newState);
  }

  public void startTask ()
  {
    activeTasks++;
    lastTaskStartedAt = System.currentTimeMillis();
    setBarValue(0);
    showBar(true);
  }

  public void finishTask ()
  {
    activeTasks--;
    if (activeTasks == 0) {
      setState("Finished");
      ((LauncherUI) _ctx.getApp().getUI(UINames.UI_ID_LAUNCHER)).launchState.setIcon(null);
      Thread finishDelayThread = new Thread(() -> {
        long now = System.currentTimeMillis();
        if (now - lastTaskStartedAt > HIDE_BAR_TIME) {
          showBar(false);
        }
      });
      ThreadingUtil.executeWithDelay(finishDelayThread, HIDE_BAR_TIME);
    }
  }

  public void setBarValue (int n)
  {
    ((LauncherUI) _ctx.getApp().getUI(UINames.UI_ID_LAUNCHER)).launchProgressBar.setValue(n);
    ((ModListUI) _ctx.getApp().getUI(UINames.UI_ID_MODLIST)).refreshProgressBar.setValue(n);
  }

  public void setBarMax (int n)
  {
    ((LauncherUI) _ctx.getApp().getUI(UINames.UI_ID_LAUNCHER)).launchProgressBar.setMaximum(n);
    ((ModListUI) _ctx.getApp().getUI(UINames.UI_ID_MODLIST)).refreshProgressBar.setMaximum(n);
  }

  private void showBar (boolean show)
  {
    LauncherUI launcherUI = _ctx.getApp().getUI(UINames.UI_ID_LAUNCHER);
    ModListUI modListUI = _ctx.getApp().getUI(UINames.UI_ID_MODLIST);

    launcherUI.launchBackground.setVisible(show);
    launcherUI.launchState.setVisible(show);
    launcherUI.launchProgressBar.setVisible(show);
    modListUI.refreshProgressBar.setVisible(show);

    if (show) {
      launcherUI.launchState.setIcon(new ImageIcon(this.getClass().getResource("/rsrc/img/loading.gif")));
    }
  }

  @Inject protected LauncherContext _ctx;

  private final long HIDE_BAR_TIME = 8000;
  private int activeTasks = 0;
  private long lastTaskStartedAt = 0;
}
