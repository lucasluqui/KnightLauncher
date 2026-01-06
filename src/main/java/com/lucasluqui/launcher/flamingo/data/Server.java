package com.lucasluqui.launcher.flamingo.data;

import com.lucasluqui.launcher.LauncherGlobals;
import com.lucasluqui.launcher.setting.Log;
import com.lucasluqui.util.FileUtil;
import com.lucasluqui.util.ZipUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.zip.ZipFile;

import static com.lucasluqui.launcher.flamingo.Log.log;

public class Server
{
  public String name;

  public String description;

  public String managedBy;

  public int beta;

  public String version;

  public int deployMethod;

  public String deployUrl;

  public String playerCountUrl;

  public String siteUrl;

  public String communityUrl;

  public String sourceCodeUrl;

  public String announceType;

  public String announceBanner;

  public String announceContent;

  public String announceBannerLink;

  public long announceBannerStartsAt;

  public long announceBannerEndsAt;

  public String fromCode;

  public String serverIcon;

  public int enabled;

  public Server ()
  {
  }

  public Server (String name)
  {
    this.name = name;
    this.beta = 0;
    this.enabled = 1;
  }

  public String getSanitizedName ()
  {
    if (this.name.equalsIgnoreCase("Official")) return "";
    return this.name.toLowerCase().replace(" ", "-")
      .replace("(", "").replace(")", "");
  }

  public String getRootDirectory ()
  {
    if (this.name.equalsIgnoreCase("Official")) return LauncherGlobals.USER_DIR;
    return LauncherGlobals.USER_DIR + "/thirdparty/" + this.getSanitizedName() + "/";
  }

  public String getModsDirectory ()
  {
    return this.getRootDirectory() + "/mods/";
  }

  public boolean isOfficial ()
  {
    return this.name.equalsIgnoreCase("Official");
  }

  public String getLocalVersion ()
  {
    // Invalid default local version.
    String localVersion = "-1";

    // Try reading local version from getdown.txt. This doesn't work on third party servers, mostly.
    if (isOfficial()) {
      try (InputStream is = Files.newInputStream(Paths.get(getRootDirectory() + File.separator + "getdown.txt"))) {
        Properties properties = new Properties();
        properties.load(is);
        localVersion = properties.getProperty("version");
        properties.clear();
        log.info("Successfully read local version from getdown.txt", "version", localVersion);
      } catch (IOException e) {
        log.error(e);
      }
    }

    // Try reading local version from inside config.jar, without extracting it.
    if (localVersion.equalsIgnoreCase("-1")) {
      try {
        String buildString = ZipUtil.readFileInsideZip(getRootDirectory() + File.separator + "code/config.jar", "build.properties");
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(buildString.getBytes(StandardCharsets.UTF_8)));
        localVersion = properties.getProperty("version");
        properties.clear();
        log.info("Successfully read local version from inside config.jar", "version", localVersion);
      } catch (Exception e) {
        log.error(e);
      }
    }

    // Looks like we were unable to get it without extracting... unfortunate. Let's try extracting then reading.
    if (localVersion.equalsIgnoreCase("-1")) {
      try {
        ZipUtil.unpackJar(
          new ZipFile(getRootDirectory() + File.separator + "code" + File.separator + "config.jar"),
          new File(getRootDirectory() + File.separator + "code" + File.separator + "config"),
          false
        );

        try (InputStream is = Files.newInputStream(Paths.get(getRootDirectory() + File.separator + "code" + File.separator + "config" + File.separator + "build.properties"))) {
          Properties properties = new Properties();
          properties.load(is);
          localVersion = properties.getProperty("version");
          properties.clear();
          log.info("Successfully read local version from extracting config.jar", "version", localVersion);
          FileUtil.deleteFile(getRootDirectory() + File.separator + "code" + File.separator + "config");
        } catch (IOException e) {
          log.error(e);
        }
      } catch (IOException e) {
        log.error(e);
      }
    }

    // Everything else failed, hope version.txt exists...
    if (localVersion.equalsIgnoreCase("-1")) {
      try {
        localVersion = FileUtil.readFile(getRootDirectory() + File.separator + "version.txt").trim();
        log.info("Successfully read local version from version.txt", "version", localVersion);
      } catch (Exception e) {
        log.error(e);
      }
    }

    // This is very likely a valid local version at this point.
    return localVersion;
  }

  @SuppressWarnings("all")
  public boolean isInstalled ()
  {
    return FileUtil.fileExists(this.getRootDirectory() + File.separator + "getdown.txt") || FileUtil.fileExists(this.getRootDirectory() + File.separator + "version.txt");
  }

  public boolean isOutdated ()
  {
    return !this.version.equalsIgnoreCase(getLocalVersion());
  }

  @Override
  public String toString ()
  {
    return "[Server " +
      "name='" + name + '\'' +
      ", description='" + description + '\'' +
      ", managedBy='" + managedBy + '\'' +
      ", beta=" + beta +
      ", version='" + version + '\'' +
      ", deployMethod=" + deployMethod +
      ", deployUrl='" + deployUrl + '\'' +
      ", playerCountUrl='" + playerCountUrl + '\'' +
      ", siteUrl='" + siteUrl + '\'' +
      ", communityUrl='" + communityUrl + '\'' +
      ", sourceCodeUrl='" + sourceCodeUrl + '\'' +
      ", announceType='" + announceType + '\'' +
      ", announceBanner='" + announceBanner + '\'' +
      ", announceContent='" + announceContent + '\'' +
      ", announceBannerLink='" + announceBannerLink + '\'' +
      ", announceBannerStartsAt=" + announceBannerStartsAt +
      ", announceBannerEndsAt=" + announceBannerEndsAt +
      ", fromCode='" + fromCode + '\'' +
      ", serverIcon='" + serverIcon + '\'' +
      ", enabled=" + enabled +
      ']';
  }
}
