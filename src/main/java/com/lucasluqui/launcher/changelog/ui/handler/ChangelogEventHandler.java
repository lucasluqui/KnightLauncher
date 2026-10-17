package com.lucasluqui.launcher.changelog.ui.handler;

import com.google.inject.Inject;
import com.lucasluqui.launcher.LauncherGlobals;
import com.lucasluqui.launcher.LocaleManager;
import com.lucasluqui.launcher.changelog.ui.ChangelogUI;
import com.lucasluqui.util.DesktopUtil;

import java.awt.event.ActionEvent;

public class ChangelogEventHandler
{
  @Inject
  public ChangelogEventHandler (LocaleManager localeManager)
  {
    this._localeManager = localeManager;
  }

  public void selectedServerChanged ()
  {

  }

  public void openLink (String url)
  {
    DesktopUtil.openWebpage(url);
  }

  public void openReleasesEvent (ActionEvent action)
  {
    DesktopUtil.openWebpage(LauncherGlobals.URL_RELEASES);
  }

  @Inject private ChangelogUI ui;

  protected LocaleManager _localeManager;
}
