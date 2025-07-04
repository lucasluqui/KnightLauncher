package com.luuqui.launcher.mod.data;

import static com.luuqui.launcher.mod.Log.log;

public class JarMod extends Mod
{
  private int minJDKVersion;
  private int maxJDKVersion;
  private String pxVersion;

  private boolean jdkCompatible;
  private boolean pxCompatible;

  @SuppressWarnings("unused")
  public JarMod ()
  {
    super();
  }

  public JarMod (String fileName)
  {
    super();
    this.displayName = fileName;
    this.fileName = fileName;
    this.minJDKVersion = 8;
    this.maxJDKVersion = 8;
    this.pxVersion = "0";
    this.jdkCompatible = true;
    this.pxCompatible = true;
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

  public String getPXVersion ()
  {
    return this.pxVersion;
  }

  public void setPXVersion (String pxVersion)
  {
    this.pxVersion = pxVersion;
  }

  public boolean isPXCompatible ()
  {
    return this.pxCompatible;
  }

  public void setPXCompatible (boolean pxCompatible)
  {
    this.pxCompatible = pxCompatible;
  }

  public void mount ()
  {
    log.info("Code mod mounted successfully", "mod", this.displayName);
  }

  public void wasAdded ()
  {
    log.info("Code mod was added", "object", this.toString());
  }

  @Override
  public String toString ()
  {
    return super.toString() + ", [JarMod minJDKVersion=" + this.minJDKVersion + ",maxJDKVersion=" + this.maxJDKVersion + ",pxVersion=" + this.pxVersion + "]";
  }

}
