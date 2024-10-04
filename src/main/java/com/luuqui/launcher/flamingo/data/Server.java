package com.luuqui.launcher.flamingo.data;

import com.luuqui.launcher.LauncherGlobals;

public class Server {

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

  public String fromCode;

  public int enabled;

  public Server() {}

  public Server(String name) {
    this.name = name;
    this.beta = 0;
    this.enabled = 1;
  }

  public String getSanitizedName() {
    if(this.name.equalsIgnoreCase("Official")) return "";
    return this.name.toLowerCase().replace(" ", "-")
      .replace("(", "").replace(")", "");
  }

  public String getRootDirectory() {
    if(this.name.equalsIgnoreCase("Official")) return LauncherGlobals.USER_DIR;
    return LauncherGlobals.USER_DIR + "/thirdparty/" + this.getSanitizedName() + "/";
  }

  public String getModsDirectory() {
    return this.getRootDirectory() + "/mods/";
  }

  public boolean isOfficial() {
    return this.name.equalsIgnoreCase("Official");
  }

}
