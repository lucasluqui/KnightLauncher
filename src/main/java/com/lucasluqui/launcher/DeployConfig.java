package com.lucasluqui.launcher;

import com.samskivert.util.Config;

public class DeployConfig
{
  protected static Config _deploy = new Config("deploy");

  public DeployConfig ()
  {
    // empty.
  }

  public static String getFlamingoAddress ()
  {
    return _deploy.getValue("flamingo.addr", "127.0.0.1");
  }

  public static int getFlamingoPort ()
  {
    return _deploy.getValue("flamingo.port", 6060);
  }
}
