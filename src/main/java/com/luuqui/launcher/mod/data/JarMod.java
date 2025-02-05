package com.luuqui.launcher.mod.data;

import com.luuqui.launcher.LauncherGlobals;

import static com.luuqui.launcher.mod.Log.log;

public class JarMod extends Mod {

  private int minJDKVersion;

  public JarMod () {
    super();
  }

  public JarMod (String fileName) {
    super();
    this.displayName = fileName;
    this.fileName = fileName;
  }

  public int getMinJDKVersion() {
    return this.minJDKVersion;
  }

  public void setMinJDKVersion(int minJDKVersion) {
    this.minJDKVersion = minJDKVersion;
  }

  public void mount () {
    log.info("Code mod mounted successfully", "mod", this.displayName);
  }

  public void wasAdded() {
    log.info("Code mod was added", "object", this.toString());
  }

}
