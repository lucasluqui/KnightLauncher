package com.lucasluqui.launcher;

import com.samskivert.util.Config;

public class BuildConfig
{
  protected static Config _build = new Config("build");

  public BuildConfig ()
  {
    // empty.
  }

  public static String getName ()
  {
    return _build.getValue("name", "Launcher");
  }

  public static String getVersion ()
  {
    return _build.getValue("version", "0");
  }

  public static String getSpiralviewVersion ()
  {
    return _build.getValue("spiralview_version", "0");
  }

  public static boolean isDev ()
  {
    return _build.getValue("dev", false);
  }

}