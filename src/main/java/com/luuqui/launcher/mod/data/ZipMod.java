package com.luuqui.launcher.mod.data;

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
    Compressor.unzip(LauncherGlobals.USER_DIR + "/mods/" + this.fileName, LauncherGlobals.USER_DIR + "/rsrc/", SystemUtil.isMac());
    log.info("Zip mod mounted successfully", "mod", this.displayName);
  }

  public String getAbsolutePath() {
    return LauncherGlobals.USER_DIR + "/mods/" + this.fileName;
  }

  public void wasAdded() {
    log.info("Zip mod was added", "object", this.toString());
  }

}
