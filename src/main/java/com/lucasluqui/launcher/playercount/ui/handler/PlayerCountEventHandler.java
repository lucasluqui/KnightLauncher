package com.lucasluqui.launcher.playercount.ui.handler;

import com.lucasluqui.launcher.LocaleManager;
import com.lucasluqui.launcher.flamingo.FlamingoManager;
import com.lucasluqui.launcher.playercount.ui.PlayerCountUI;
import com.lucasluqui.util.ThreadingUtil;

import com.google.inject.Inject;
import org.json.JSONObject;

import javax.swing.*;

public class PlayerCountEventHandler
{
  @Inject
  public PlayerCountEventHandler (LocaleManager localeManager)
  {
    this._localeManager = localeManager;
  }

  /**
   * Loads the history, unless it was already loaded, and we do not need to refresh yet.
   */
  public void loadHistory ()
  {
    long now = System.currentTimeMillis();
    if (now - _lastLoad < REFRESH_INTERVAL) return;
    _lastLoad = now;

    ui.showLoading();
    ThreadingUtil.start("PlayerCountLoader", () -> {
      JSONObject history = _flamingoManager.fetchPlayerCountHistory();
      SwingUtilities.invokeLater(() -> ui.showHistory(history));
    });
  }

  /**
   * Loads the history again while it's being looked at, swapping it in place once it's here.
   */
  public void refreshHistory ()
  {
    _lastLoad = System.currentTimeMillis();
    ThreadingUtil.start("PlayerCountRefresher", () -> {
      JSONObject history = _flamingoManager.fetchPlayerCountHistory();
      if (history != null) SwingUtilities.invokeLater(() -> ui.showHistory(history));
    });
  }

  public void selectedServerChanged ()
  {
    // empty.
  }

  @Inject private PlayerCountUI ui;
  @Inject protected FlamingoManager _flamingoManager;
  protected LocaleManager _localeManager;

  private long _lastLoad = 0;

  public static final long REFRESH_INTERVAL = 5 * 60 * 1000; // every 5 min.
}
