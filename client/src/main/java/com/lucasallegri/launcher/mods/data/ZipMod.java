package com.lucasallegri.launcher.mods.data;

import com.lucasallegri.launcher.LauncherGlobals;
import com.lucasallegri.util.Compressor;
import com.lucasallegri.util.SystemUtil;

import static com.lucasallegri.launcher.mods.Log.log;

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
    log.info("Zip Mod mounted successfully", "mod", this.displayName);
  }

  public void wasAdded() {
    log.info("A zip mod was added", "object", this.toString());
  }

}
