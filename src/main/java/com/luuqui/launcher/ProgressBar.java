package com.luuqui.launcher;

import com.google.inject.Inject;
import com.luuqui.util.ThreadingUtil;

import javax.swing.*;

import static com.luuqui.launcher.Log.log;

public class ProgressBar
{
  @Inject protected LauncherContext _launcherCtx;
  private int activeTasks = 0;
  private long lastTaskStartedAt = 0;
  private final long HIDE_BAR_TIME = 8000;

  public ProgressBar ()
  {
    // empty.
  }

  public void setState (String newState)
  {
    _launcherCtx.launcherGUI.launchState.setText(newState);
    _launcherCtx.modListGUI.refreshProgressBar.setString(newState);
  }

  public void startTask ()
  {
    activeTasks++;
    lastTaskStartedAt = System.currentTimeMillis();
    showBar(true);
  }

  public void finishTask ()
  {
    activeTasks--;
    if (activeTasks == 0) {
      setState("Finished");
      _launcherCtx.launcherGUI.launchState.setIcon(null);
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
    _launcherCtx.launcherGUI.launchProgressBar.setValue(n);
    _launcherCtx.modListGUI.refreshProgressBar.setValue(n);
  }

  public void setBarMax (int n)
  {
    _launcherCtx.launcherGUI.launchProgressBar.setMaximum(n);
    _launcherCtx.modListGUI.refreshProgressBar.setMaximum(n);
  }

  private void showBar (boolean show)
  {
    _launcherCtx.launcherGUI.launchBackground.setVisible(show);
    _launcherCtx.launcherGUI.launchState.setVisible(show);
    _launcherCtx.launcherGUI.launchProgressBar.setVisible(show);
    _launcherCtx.modListGUI.refreshProgressBar.setVisible(show);

    if (show) {
      _launcherCtx.launcherGUI.launchState.setIcon(new ImageIcon(this.getClass().getResource("/rsrc/img/loading.gif")));
    }
  }

}
