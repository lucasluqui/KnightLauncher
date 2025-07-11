package com.luuqui.launcher.flamingo.data;

import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.util.FileUtil;

import java.io.File;
import java.io.IOException;

import static com.luuqui.launcher.flamingo.Log.log;

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

  public Server () {}

  public Server (String name)
  {
    this.name = name;
    this.beta = 0;
    this.enabled = 1;
  }

  public String getSanitizedName ()
  {
    if(this.name.equalsIgnoreCase("Official")) return "";
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

  private String getLocalVersion ()
  {
    try {
      return FileUtil.readFile(this.getRootDirectory() + File.separator + "version.txt");
    } catch (IOException e) {
      log.error(e);
    }
    return null;
  }

  @SuppressWarnings("all")
  public boolean isInstalled ()
  {
    return FileUtil.fileExists(this.getRootDirectory() + File.separator + "version.txt");
  }

  public boolean isOutdated ()
  {
    return !this.version.equalsIgnoreCase(getLocalVersion());
  }

  @Override
  public String toString() {
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
