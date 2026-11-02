package com.lucasluqui.launcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lucasluqui.download.DownloadManager;
import com.lucasluqui.download.data.URLDownloadQueue;
import com.lucasluqui.util.FileUtil;
import com.lucasluqui.util.HttpUtil;
import com.lucasluqui.util.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.lucasluqui.launcher.Log.log;

@Singleton
public class CacheManager
{
  @Inject protected DownloadManager _downloadManager;

  public String CACHE_PATH = LauncherGlobals.USER_DIR + File.separator + "KnightLauncher" + File.separator + "cache" + File.separator;

  /**
   * Images are kept apart from other cached files. Older versions cached them already resized, at the old paths.
   */
  private static final String IMAGE_CACHE_DIR = "images" + File.separator;

  public CacheManager ()
  {
    // empty.
  }

  public void init ()
  {
    // Stores all cache resources.
    FileUtil.createDir(CACHE_PATH);
  }

  /**
   * Fetches an image, from the cache if it's been fetched before, resized to the given size.
   * The cache keeps the image as it was downloaded, so asking for it at a bigger size later on (like after
   * raising the UI scale) still gets a sharp result.
   *
   * @return The resized image, or null if it couldn't be fetched or read.
   */
  public BufferedImage fetchImage (String url, int width, int height)
  {
    BufferedImage original = fetchOriginalImage(url);
    if (original == null) return null;
    return ImageUtil.resizeImagePreserveTransparency(original, width, height);
  }

  private BufferedImage fetchOriginalImage (String url)
  {
    Path cacheFile = Paths.get(getLocalPath(url, IMAGE_CACHE_DIR));

    try {
      if (Files.exists(cacheFile)) {
        log.info("Loading image from cache", "localPath", cacheFile);
        BufferedImage image = ImageIO.read(cacheFile.toFile());
        if (image != null) return image;
        // Not readable anymore, fetch it again.
        Files.deleteIfExists(cacheFile);
      }

      byte[] data = HttpUtil.getBytes(HttpUtil.toURI(new URL(url)));
      BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
      if (image == null) {
        log.error("Not a readable image", "url", url);
        return null;
      }

      // Saved as downloaded rather than written back out, which could lose quality or animation.
      log.info("Saving image to cache", "localPath", cacheFile);
      Files.createDirectories(cacheFile.getParent());
      Files.write(cacheFile, data);
      return image;
    } catch (IOException | RuntimeException e) {
      log.error("Failed to fetch image", "url", url, e);
      return null;
    }
  }

  public File fetchFile (String url)
  {
    String localPath = getLocalPath(url);

    if (FileUtil.fileExists(localPath)) {
      log.info("Loading file from cache", "localPath", localPath);
      return new File(localPath);
    } else {
      try {
        File localFile = new File(localPath);
        _downloadManager.add(new URLDownloadQueue(localFile.getName(), new URL(url), localFile));
        _downloadManager.processQueues();
        log.info("Saving file to cache", "localPath", localPath);
        return localFile;
      } catch (IOException e) {
        log.error(e);
      }
    }

    return null;
  }

  private String getLocalPath (String url)
  {
    return getLocalPath(url, "");
  }

  private String getLocalPath (String url, String subDir)
  {
    String localPath = CACHE_PATH + subDir + url.split("https://")[1];
    localPath = localPath.replace("/", File.separator);
    return localPath;
  }

}
