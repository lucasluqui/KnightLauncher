package com.lucasluqui.download.data;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

public class URLDownloadQueue extends DownloadQueue
{
  /**
   * Map of items in the download queue.
   * The first value is the URL to the download in question,
   * and the second is the local path where we'll write it to.
   */
  private final HashMap<URL, File> queue;

  public URLDownloadQueue (String name)
  {
    super(name);
    this.queue = new HashMap<>();
  }

  public URLDownloadQueue (String name, URL url, File localPath)
  {
    super(name);
    this.queue = new HashMap<>();
    this.queue.put(url, localPath);
  }

  public URLDownloadQueue (String name, HashMap<URL, File> queue)
  {
    super(name);
    this.queue = queue;
  }

  public void addToQueue (URL url, File localPath)
  {
    this.queue.put(url, localPath);
  }

  public HashMap<URL, File> getQueue ()
  {
    return this.queue;
  }

}
