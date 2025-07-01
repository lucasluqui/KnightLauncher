package com.luuqui.download.data;

public abstract class DownloadQueue
{

  /**
   * An identifying name for this download queue.
   */
  private final String name;

  protected DownloadQueue (String name)
  {
    this.name = name;
  }

  public String getName ()
  {
    return this.name;
  }

}
