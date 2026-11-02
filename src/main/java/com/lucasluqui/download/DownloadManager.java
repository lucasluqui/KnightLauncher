package com.lucasluqui.download;

import com.google.inject.Singleton;
import com.lucasluqui.download.data.URLDownloadQueue;
import com.lucasluqui.util.HttpUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lucasluqui.download.Log.log;

@Singleton
public class DownloadManager
{
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
   * @param downloadQueue The URL download queue object we'll have to store for further processing.
   */
  public synchronized void add (URLDownloadQueue downloadQueue)
  {
    this.storedQueues.add(downloadQueue);
    log.info("Added download queue", "queue", downloadQueue.getName());
  }

  /**
   * Starts processing all download queues stored, on the calling thread. Callers are expected to be off the
   * event dispatch thread, so the UI keeps responding while things download.
   *
   * Synchronized, so queues from different threads still download one at a time, as they always have.
   */
  public synchronized void processQueues ()
  {
    this.queuesInProcess += storedQueues.size();

    for (URLDownloadQueue downloadQueue : storedQueues) {
      try {
        processQueue(downloadQueue);
      } catch (Exception e) {
        log.error(e);
      }
    }

    this.clear();
  }

  /**
   * Processes a single download queue, iterating through all its items and downloading them.
   *
   * @param downloadQueue The download queue to process.
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
          if (HttpUtil.isHttp(url)) {
            HttpUtil.download(HttpUtil.toURI(url), localFile.toPath());
          } else {
            // Not something to fetch over the network, like a local file.
            FileUtils.copyURLToFile(url, localFile);
          }
          downloadCompleted = true;
        } catch (HttpUtil.HttpStatusException e) {
          log.error(e);
          // Things like a 404 won't fix themselves by trying again.
          if (!e.isRetryable()) break;
          waitBeforeRetry(downloadAttempts);
        } catch (InterruptedIOException e) {
          log.error(e);
          break;
        } catch (IOException e) {
          // Keep retrying.
          log.error(e);
          waitBeforeRetry(downloadAttempts);
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

  private void waitBeforeRetry (int attempts)
  {
    if (attempts > MAX_ATTEMPTS) return;
    try {
      Thread.sleep(RETRY_DELAY * attempts);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Gets the status of a download queue.
   *
   * @param downloadQueue The download queue object to search for.
   * @return Returns true if this queue was downloaded correctly or false if failed or not found.
   */
  public synchronized boolean getQueueStatus (URLDownloadQueue downloadQueue)
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
   * Time in milliseconds to wait before retrying, multiplied by the number of attempts so far.
   * Timeouts themselves are handled by {@link HttpUtil}.
   */
  @SuppressWarnings("all")
  private final long RETRY_DELAY = 1000;
}
