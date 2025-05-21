package com.luuqui.launcher.mod.data;

import com.luuqui.launcher.LauncherGlobals;

import static com.luuqui.launcher.mod.Log.log;

public class JarMod extends Mod {

  private int minJDKVersion;
  private int maxJDKVersion;
  private boolean meetsJDKRequirements;

  public JarMod () {
    super();
  }

  public JarMod (String fileName) {
    super();
    this.displayName = fileName;
    this.fileName = fileName;
    this.minJDKVersion = 11;
    this.maxJDKVersion = 11;
    this.meetsJDKRequirements = true;
  }

  public int getMinJDKVersion() {
    return this.minJDKVersion;
  }

  public void setMinJDKVersion(int minJDKVersion) {
    this.minJDKVersion = minJDKVersion;
  }

  public int getMaxJDKVersion() {
    return this.maxJDKVersion;
  }

  public void setMaxJDKVersion(int maxJDKVersion) {
    this.maxJDKVersion = maxJDKVersion;
  }

  public boolean getMeetsJDKRequirements() {
    return this.meetsJDKRequirements;
  }

  public void setMeetsJDKRequirements(boolean meetsJDKRequirements) {
    this.meetsJDKRequirements = meetsJDKRequirements;
  }

  public void mount () {
    log.info("Code mod mounted successfully", "mod", this.displayName);
  }

  public void wasAdded() {
    log.info("Code mod was added", "object", this.toString());
  }

  @Override
  public String toString() {
    return super.toString() + ", [JarMod minJDKVersion=" + this.minJDKVersion + ",maxJDKVersion=" + this.maxJDKVersion + "]";
  }

}
