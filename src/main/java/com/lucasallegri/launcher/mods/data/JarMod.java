package com.lucasallegri.launcher.mods.data;

import static com.lucasallegri.launcher.mods.Log.log;

public class JarMod extends Mod {

  public JarMod () {
    super();
  }

  public JarMod (String fileName) {
    super();
    this.displayName = fileName;
    this.fileName = fileName;
  }

  public void mount () {}

  public void wasAdded() {
    log.info("A jar mod was added", "object", this.toString());
  }

}
