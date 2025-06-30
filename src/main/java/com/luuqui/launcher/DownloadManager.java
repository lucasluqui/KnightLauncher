package com.luuqui.launcher;

import com.google.inject.Singleton;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.luuqui.launcher.Log.log;

@Singleton
public class DownloadManager
{
  /**
   * Map of items in the download queue.
   * The first value is the URL to the download in question,
   * and the second is the local path where we'll write it to.
   * TODO: Support for downloads through torrent instead of just reading from an URL.
   */
  private HashMap<URL, File> queue = new HashMap<>();

  /**
   * Map of items that went through the download queue.
   * Their value will be true if downloaded correctly, and false if they failed.
   */
  private HashMap<URL, Boolean> downloadedStatusMap = new HashMap<>();

  /**
   * Maximum number of retries to try to download an item in queue.
   */
  private final int MAX_ATTEMPTS = 3;

  /**
   * Time in milliseconds until we time out a download attempt during initial connection.
   */
  private final int CONNECTION_TIMEOUT = 0;

  /**
   * Time in milliseconds until we time out a download attempt during read.
   */
  private final int READ_TIMEOUT = 0;

  public DownloadManager ()
  {
    // empty.
  }

  public void init ()
  {
    // empty.
  }

  /**
   * Adds an item to the download queue.
   *
   * @param url
   * The URL to the item we have to download.
   *
   * @param localFile
   * The local file where the downloaded item will be written to.
   */
  public void add (URL url, File localFile)
  {
    this.queue.put(url, localFile);
    log.info("Added to download queue", "url", url.toString());
  }

  /**
   * Starts processing all items queued.
   */
  public void processQueue ()
  {
    for (Map.Entry<URL, File> item : queue.entrySet()) {
      boolean downloadCompleted = false;
      int downloadAttempts = 0;

      URL url = item.getKey();
      File localFile = item.getValue();

      while (downloadAttempts <= MAX_ATTEMPTS && !downloadCompleted) {
        downloadAttempts++;
        log.info("Starting download of item in queue", "url", url, "localFile", localFile, "attempts", downloadAttempts);

        try {
          FileUtils.copyURLToFile(
              url,
              localFile,
              CONNECTION_TIMEOUT,
              READ_TIMEOUT
          );
          downloadCompleted = true;
          this.downloadedStatusMap.put(url, true);
        } catch (IOException e) {
          // Keep retrying.
          log.error(e);
        }
      }

      if (downloadCompleted) {
        log.info("Downloaded item in queue", "url", url, "localFile", localFile, "attempts", downloadAttempts);
      } else {
        this.downloadedStatusMap.put(url, false);
        log.error("Failed to download item in queue", "url", url, "localFile", localFile, "attempts", downloadAttempts);
      }
    }

    // Already processed all items in queue, so now we clear it.
    this.clear();
  }

  public boolean getDownloadedStatus (URL url)
  {
    return this.downloadedStatusMap.get(url);
  }

  /**
   * Clears the queue of items to download.
   */
  private void clear ()
  {
    this.queue.clear();
    log.info("Cleared download queue");
  }

}
