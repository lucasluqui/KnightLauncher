package com.lucasluqui.launcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lucasluqui.download.DownloadManager;
import com.lucasluqui.download.data.URLDownloadQueue;
import com.lucasluqui.util.FileUtil;
import com.lucasluqui.util.ImageUtil;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static com.lucasluqui.launcher.Log.log;

@Singleton
public class CacheManager
{
  @Inject protected DownloadManager _downloadManager;

  public String CACHE_PATH = LauncherGlobals.USER_DIR + File.separator + "KnightLauncher" + File.separator + "cache" + File.separator;

  public CacheManager ()
  {
    // empty.
  }

  public void init ()
  {
    // Stores all cache resources.
    FileUtil.createDir(CACHE_PATH);
  }

  public BufferedImage fetchImage (String url, int width, int height)
  {
    String localPath = getLocalPath(url);
    BufferedImage bufferedImage = null;
    URL localURL;

    if (FileUtil.fileExists(localPath)) {
      log.info("Loading image from cache", "localPath", localPath);
      try {
        localURL = new File(localPath).toURI().toURL();
        bufferedImage = ImageUtil.toBufferedImage(ImageUtil.getImageFromURL(localURL, width, height));
      } catch (MalformedURLException e) {
        log.error(e);
      }
    } else {
      bufferedImage = ImageUtil.toBufferedImage(ImageUtil.getImageFromURL(url, width, height));
      log.info("Saving image to cache", "localPath", localPath);
      File cacheFile = new File(localPath);
      cacheFile.getParentFile().mkdirs();
      try {
        ImageIO.write(bufferedImage, FilenameUtils.getExtension(localPath), cacheFile);
      } catch (IOException e) {
        log.error(e);
      }
    }

    return bufferedImage;
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
    String localPath = CACHE_PATH + url.split("https://")[1];
    localPath = localPath.replace("/", File.separator);
    return localPath;
  }

}
