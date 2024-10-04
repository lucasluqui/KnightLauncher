package com.luuqui.launcher.mod.data;

import com.luuqui.launcher.LauncherApp;
import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.util.Compressor;
import com.luuqui.util.SystemUtil;

import static com.luuqui.launcher.mod.Log.log;

public class ZipMod extends Mod {

  public ZipMod () {
    super();
  }

  public ZipMod (String fileName) {
    super();
    this.displayName = fileName;
    this.fileName = fileName;
  }

  public void mount() {
    String rootDir = LauncherGlobals.USER_DIR;
    if(LauncherApp.selectedServer != null) {
      rootDir = LauncherApp.selectedServer.getRootDirectory();
    }
    Compressor.unzip(rootDir + "/mods/" + this.fileName, rootDir + "/rsrc/", SystemUtil.isMac());
    log.info("Zip mod mounted successfully", "mod", this.displayName);
  }

  public void wasAdded() {
    log.info("Zip mod was added", "object", this.toString());
  }

}
