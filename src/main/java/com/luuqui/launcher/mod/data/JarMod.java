package com.luuqui.launcher.mod.data;

import com.luuqui.launcher.LauncherGlobals;

import static com.luuqui.launcher.mod.Log.log;

public class JarMod extends Mod {

  public JarMod () {
    super();
  }

  public JarMod (String fileName) {
    super();
    this.displayName = fileName;
    this.fileName = fileName;
  }

  public void mount () {
    log.info("Jar Mod mounted successfully", "mod", this.displayName);
  }

  public String getAbsolutePath() {
    return LauncherGlobals.USER_DIR + "/code-mods/" + this.fileName;
  }

  public void wasAdded() {
    log.info("A jar mod was added", "object", this.toString());
  }

}
