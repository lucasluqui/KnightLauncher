package com.lucasluqui.launcher.mod.data;

public class LocaleChange
{
  /**
   * The localization bundle the key is in.
   */
  private final String bundle;

  /**
   * Key to look for within that bundle, or to create as new.
   */
  private final String key;

  /**
   * The new value for said key.
   */
  private final String value;

  /**
   * The object which will hold the information for a single locale change.
   *
   * @param bundle The localization bundle that governs this KV.
   * @param key    Key to add or modify.
   * @param value  New value to set the key to.
   */
  public LocaleChange (String bundle, String key, String value)
  {
    this.bundle = bundle;
    this.key = key;
    this.value = value;
  }

  public String getBundle ()
  {
    return this.bundle;
  }

  public String getKey ()
  {
    return this.key;
  }

  public String getValue ()
  {
    return this.value;
  }
}
