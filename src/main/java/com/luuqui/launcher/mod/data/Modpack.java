package com.luuqui.launcher.mod.data;

import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.util.Compressor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.luuqui.launcher.mod.Log.log;

public class Modpack extends Mod
{

  @SuppressWarnings("unused")
  public Modpack ()
  {
    super();
  }

  public Modpack (String rootDir, String fileName)
  {
    super();
    this.displayName = fileName;
    this.fileName = fileName;
    this.setAbsolutePath(rootDir + fileName);
    parseMetadata();
  }

  public void mount () { }

  @SuppressWarnings("all")
  public void mount (String rootDir)
  {
    this.setAbsolutePath(rootDir + "/mods/" + getFileName());
    for (String fileInsideZip : Compressor.getFileListFromZip(this.getAbsolutePath())) {
      InputStream fileIs = Compressor.getISFromFileInsideZip(this.getAbsolutePath(), fileInsideZip);
      String pathOutside = rootDir + "/mods/" + fileInsideZip;
      File tempFile = new File(pathOutside);
      try {
        FileUtils.copyInputStreamToFile(fileIs, tempFile);
      } catch (IOException e) {
        log.error(e);
      }
      Compressor.unzip(pathOutside, rootDir + "/rsrc/", false, Settings.fileProtection, LauncherGlobals.FILTER_LIST);
      log.info("Mod from modpack mounted successfully", "pack", this.displayName, "mod", fileInsideZip);
      tempFile.delete();
    }
    log.info("Modpack mounted successfully", "pack", this.displayName);
  }

  public void wasAdded ()
  {
    log.info("Modpack was added", "object", this.toString());
  }

  public void parseMetadata ()
  {
    super.parseMetadata();
  }

}
