package com.luuqui.launcher.mods.data;

import static com.luuqui.launcher.mods.Log.log;

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

  public void wasAdded() {
    log.info("A jar mod was added", "object", this.toString());
  }

}
