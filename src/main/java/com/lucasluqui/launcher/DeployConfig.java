package com.lucasluqui.launcher;

import com.samskivert.util.Config;

public class DeployConfig
{
  protected static Config _deploy = new Config("deploy");

  public DeployConfig ()
  {
    // empty.
  }

  public static String getEnv ()
  {
    return _deploy.getValue("env", "dev");
  }

  public static boolean isDev ()
  {
    return getEnv().equalsIgnoreCase("dev");
  }

  public static boolean isProd ()
  {
    return getEnv().equalsIgnoreCase("prod");
  }

  public static String getFlamingoAddress ()
  {
    return _deploy.getValue(getEnv() + ".flamingo.addr", "127.0.0.1");
  }

  public static int getFlamingoPort ()
  {
    return _deploy.getValue(getEnv() + ".flamingo.port", 6060);
  }
}
