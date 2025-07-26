package com.luuqui.launcher.mod.data;

import static com.luuqui.launcher.mod.Log.log;

public class JarMod extends Mod
{
  private int minJDKVersion;
  private int maxJDKVersion;
  private boolean jdkCompatible;

  @SuppressWarnings("unused")
  public JarMod ()
  {
    super();
  }

  public JarMod (String rootDir, String fileName)
  {
    super();
    this.displayName = fileName;
    this.fileName = fileName;
    this.minJDKVersion = 8;
    this.maxJDKVersion = 8;
    this.jdkCompatible = true;
    this.setAbsolutePath(rootDir + fileName);
    parseMetadata();
  }

  public void mount ()
  {
    log.info("Code mod mounted successfully", "mod", this.displayName);
  }

  public void wasAdded ()
  {
    log.info("Code mod was added", "object", this.toString());
  }

  public void parseMetadata ()
  {
    super.parseMetadata();
    if (this.metadata != null) {
      int minJDKVersion = !this.metadata.isNull("minJDKVersion") ? Integer.parseInt(this.metadata.getString("minJDKVersion")) : 8;
      int maxJDKVersion = !this.metadata.isNull("maxJDKVersion") ? Integer.parseInt(this.metadata.getString("maxJDKVersion")) : 8;
      this.setMinJDKVersion(minJDKVersion);
      this.setMaxJDKVersion(maxJDKVersion);
    }
  }

  public int getMinJDKVersion ()
  {
    return this.minJDKVersion;
  }

  public void setMinJDKVersion (int minJDKVersion)
  {
    this.minJDKVersion = minJDKVersion;
  }

  public int getMaxJDKVersion ()
  {
    return this.maxJDKVersion;
  }

  public void setMaxJDKVersion (int maxJDKVersion)
  {
    this.maxJDKVersion = maxJDKVersion;
  }

  public boolean isJDKCompatible ()
  {
    return this.jdkCompatible;
  }

  public void setJDKCompatible (boolean jdkCompatible)
  {
    this.jdkCompatible = jdkCompatible;
  }

  @Override
  public String toString ()
  {
    return super.toString() + ", [JarMod minJDKVersion=" + this.minJDKVersion + ",maxJDKVersion=" + this.maxJDKVersion + ",pxVersion=" + this.pxVersion + "]";
  }

}
