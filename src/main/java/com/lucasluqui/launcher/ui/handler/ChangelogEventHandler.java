package com.lucasluqui.launcher.ui.handler;

import com.google.inject.Inject;
import com.lucasluqui.launcher.LocaleManager;
import com.lucasluqui.launcher.ui.ChangelogUI;

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

  @Inject private ChangelogUI ui;

  protected LocaleManager _localeManager;
}
