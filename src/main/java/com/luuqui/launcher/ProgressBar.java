package com.luuqui.launcher;

import com.google.inject.Inject;
import com.luuqui.util.ThreadingUtil;

import javax.swing.*;

import static com.luuqui.launcher.Log.log;

public class ProgressBar
{
  @Inject protected LauncherContext _launcherCtx;

  private int activeTasks = 0;

  public ProgressBar ()
  {
    // empty.
  }

  public void setState (String newState)
  {
    _launcherCtx.launcherGUI.launchState.setText(newState);
    _launcherCtx.modListGUI.refreshProgressBar.setString(newState);
    //log.info(newState);
  }

  public void startTask ()
  {
    activeTasks++;
    showBar(true);
  }

  public void finishTask ()
  {
    activeTasks--;
    if(activeTasks == 0) {
      setState("Finished");
      _launcherCtx.launcherGUI.launchState.setIcon(null);
      Thread finishDelayThread = new Thread(() -> {
        showBar(false);
      });
      ThreadingUtil.executeWithDelay(finishDelayThread, 8000);
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
      _launcherCtx.launcherGUI.launchState.setIcon(new ImageIcon(this.getClass().getResource("/img/loading.gif")));
    }
  }

}
