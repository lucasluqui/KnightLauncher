package com.lucasluqui.launcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lucasluqui.util.ImageUtil;

import java.awt.*;

import static com.lucasluqui.launcher.Log.log;

/**
 * Handles the launcher's icon in the system tray, shown while the launcher is hidden away.
 */
@Singleton
public class TrayManager
{
  @Inject
  public TrayManager (LocaleManager localeManager)
  {
    this._localeManager = localeManager;
  }

  /**
   * Adds the launcher's icon to the system tray.
   *
   * @param onOpen Called when the user asks to bring the launcher back up.
   * @param onExit Called when the user asks to exit the launcher from the tray.
   * @return Whether the icon is now in the system tray. False if the system has no tray to use.
   */
  public boolean show (Runnable onOpen, Runnable onExit)
  {
    if (_trayIcon != null) return true;
    if (!SystemTray.isSupported()) {
      log.info("System tray is not supported");
      return false;
    }

    try {
      PopupMenu menu = new PopupMenu();

      MenuItem openItem = new MenuItem(_localeManager.getValue("m.tray_open"));
      openItem.addActionListener(e -> onOpen.run());
      menu.add(openItem);

      menu.addSeparator();

      MenuItem exitItem = new MenuItem(_localeManager.getValue("m.tray_exit"));
      exitItem.addActionListener(e -> onExit.run());
      menu.add(exitItem);

      TrayIcon trayIcon = new TrayIcon(ImageUtil.loadImageWithinJar("/rsrc/img/icon-64.png"), _localeManager.getValue("m.tray_tooltip"), menu);
      trayIcon.setImageAutoSize(true);

      // Double-clicking the icon also brings the launcher back.
      trayIcon.addActionListener(e -> onOpen.run());

      SystemTray.getSystemTray().add(trayIcon);
      _trayIcon = trayIcon;
      return true;
    } catch (Exception e) {
      log.error("Failed to add tray icon", e);
      return false;
    }
  }

  /**
   * Removes the launcher's icon from the system tray, if it's there.
   */
  public void hide ()
  {
    if (_trayIcon == null) return;
    SystemTray.getSystemTray().remove(_trayIcon);
    _trayIcon = null;
  }

  protected LocaleManager _localeManager;

  private TrayIcon _trayIcon;
}
