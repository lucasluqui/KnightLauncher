package com.luuqui.launcher.mod.data;

import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.util.Compressor;

import static com.luuqui.launcher.mod.Log.log;

public class ZipMod extends Mod
{

  @SuppressWarnings("unused")
  public ZipMod ()
  {
    super();
  }

  public ZipMod (String fileName)
  {
    super();
    this.displayName = fileName;
    this.fileName = fileName;
  }

  public void mount () { }

  public void mount (String rootDir)
  {
    log.info("Mounting Zip mod", "mod", this.displayName);
    Compressor.unzip(rootDir + "/mods/" + this.fileName, rootDir + "/rsrc/", false, Settings.fileProtection, LauncherGlobals.FILTER_LIST);
    log.info("Zip mod mounted successfully", "mod", this.displayName);
  }

  public void wasAdded ()
  {
    log.info("Zip mod was added", "object", this.toString());
  }

}
