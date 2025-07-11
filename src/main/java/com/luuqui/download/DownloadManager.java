package com.luuqui.download;

import com.google.inject.Singleton;
import com.luuqui.download.data.URLDownloadQueue;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.luuqui.download.Log.log;

@Singleton
public class DownloadManager
{

  /**
   * The list of all stored download queues that are pending processing.
   */
  private final List<URLDownloadQueue> storedQueues = new ArrayList<>();

  /**
   * Map of download queues and their status.
   * Their value will be true if all items downloaded correctly, and false if any of them failed.
   */
  private final HashMap<URLDownloadQueue, Boolean> queueStatusMap = new HashMap<>();

  /**
   * N. Of queues currently being processed.
   */
  private int queuesInProcess = 0;

  /**
   * Maximum number of retries to try to download an item in queue.
   */
  @SuppressWarnings("all")
  private final int MAX_ATTEMPTS = 3;

  /**
   * Time in milliseconds until we time out a download attempt during initial connection.
   */
  @SuppressWarnings("all")
  private final int CONNECTION_TIMEOUT = 0;

  /**
   * Time in milliseconds until we time out a download attempt during read.
   */
  @SuppressWarnings("all")
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
   * Adds an URL download queue to store.
   *
   * @param downloadQueue
   * The URL download queue object we'll have to store for further processing.
   */
  public void add (URLDownloadQueue downloadQueue)
  {
    this.storedQueues.add(downloadQueue);
    log.info("Added download queue", "queue", downloadQueue.getName());
  }

  /**
   * Starts processing all download queues stored.
   */
  public void processQueues ()
  {
    this.queuesInProcess += storedQueues.size();

    for (URLDownloadQueue downloadQueue : storedQueues)
    {
      try {
        EventQueue.invokeAndWait(() -> processQueue(downloadQueue));
      } catch (Exception e) {
        log.error(e);
      }
    }

    this.clear();
  }

  /**
   * Processes a single download queue, iterating through all its items and downloading them.
   *
   * @param downloadQueue
   * The download queue to process.
   */
  private void processQueue (URLDownloadQueue downloadQueue)
  {
    String queueName = downloadQueue.getName();

    for (Map.Entry<URL, File> item : downloadQueue.getQueue().entrySet()) {
      boolean downloadCompleted = false;
      int downloadAttempts = 0;

      URL url = item.getKey();
      File localFile = item.getValue();

      while (downloadAttempts <= MAX_ATTEMPTS && !downloadCompleted) {
        downloadAttempts++;
        log.info("Starting download attempt of item in queue", "queue", queueName, "url", url, "localFile", localFile, "attempts", downloadAttempts);

        try {
          FileUtils.copyURLToFile(
              url,
              localFile,
              CONNECTION_TIMEOUT,
              READ_TIMEOUT
          );
          downloadCompleted = true;
        } catch (IOException e) {
          // Keep retrying.
          log.error(e);
        }
      }

      if (downloadCompleted) {
        log.info("Downloaded item in queue", "queue", queueName, "url", url, "localFile", localFile, "attempts", downloadAttempts);
      } else {
        log.error("Failed to download item in queue", "queue", queueName, "url", url, "localFile", localFile, "attempts", downloadAttempts);
      }

      // Set the status map for this queue if none exists so far
      // or update it to its latest value.
      // Once it's set to false, it can't be set to true again, indicating at least one of them failed.
      if (this.queueStatusMap.getOrDefault(downloadQueue, true)) {
        this.queueStatusMap.put(downloadQueue, downloadCompleted);
      }
    }
    this.queuesInProcess -= 1;
  }

  /**
   * Gets the status of a download queue.
   *
   * @param downloadQueue
   * The download queue object to search for.
   *
   * @return
   * Returns true if this queue was downloaded correctly or false if failed or not found.
   */
  public boolean getQueueStatus (URLDownloadQueue downloadQueue)
  {
    return this.queueStatusMap.getOrDefault(downloadQueue, false);
  }

  /**
   * Clears all stored download queues.
   */
  private void clear ()
  {
    this.storedQueues.clear();
    log.info("Cleared stored download queues");
  }

}
