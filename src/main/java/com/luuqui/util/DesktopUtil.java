package com.luuqui.util;

import mslinks.ShellLink;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static com.luuqui.util.Log.log;

public class DesktopUtil {

  public static void openDir(String path) {
    try {
      Desktop.getDesktop().open(new File(path));
    } catch (IOException e) {
      log.error(e);
    }
  }

  public static void openWebpage(URI uri) {
    if (SystemUtil.isUnix()) {
      ProcessUtil.run(new String[] {"xdg-open", uri.toString()}, true);
      return;
    }
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(uri);
      } catch (Exception e) {
        log.error(e);
      }
    }
  }

  public static void openWebpage(String url) {
    try {
      openWebpage(new URL(url).toURI());
    } catch (URISyntaxException | MalformedURLException e) {
      log.error(e);
    }
  }

  public static String getPathToDesktop() {
    return System.getProperty("user.home") + File.separator + "Desktop";
  }

  public static void createShellLink(String target, String args, String workDir, String ico, String hover, String name) {
    ShellLink sl = ShellLink.createLink(target)
            .setCMDArgs(args)
            .setWorkingDir(workDir)
            .setIconLocation(ico)
            .setName(hover);
    try {
      sl.saveTo(getPathToDesktop() + "/" + name + ".lnk");
    } catch (IOException e) {
      log.error(e);
    }
  }

}
