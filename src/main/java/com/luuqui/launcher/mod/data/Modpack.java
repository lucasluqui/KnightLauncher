package com.luuqui.launcher.mod.data;

import com.luuqui.launcher.LauncherApp;
import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.launcher.mod.ModLoader;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.util.Compressor;
import com.luuqui.util.SystemUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.luuqui.launcher.mod.Log.log;

public class Modpack extends Mod {

  public Modpack() {
    super();
  }

  public Modpack(String fileName) {
    super();
    this.displayName = fileName;
    this.fileName = fileName;
  }

  public void mount() {
    String rootDir = LauncherGlobals.USER_DIR;
    if(LauncherApp.selectedServer != null) {
      rootDir = LauncherApp.selectedServer.getRootDirectory();
    }

    for(String fileInsideZip : Compressor.getFileListFromZip(this.getAbsolutePath())) {
      InputStream fileIs = Compressor.getISFromFileInsideZip(this.getAbsolutePath(), fileInsideZip);
      String pathOutside = rootDir + "/mods/" + fileInsideZip;
      File tempFile = new File(pathOutside);
      try {
        FileUtils.copyInputStreamToFile(fileIs, tempFile);
      } catch (IOException e) {
        log.error(e);
      }
      Compressor.unzip(pathOutside, rootDir + "/rsrc/", false, Settings.fileProtection, ModLoader.FILTER_LIST);
      log.info("Mod from modpack mounted successfully", "pack", this.displayName, "mod", fileInsideZip);
      tempFile.delete();
    }
    log.info("Modpack mounted successfully", "pack", this.displayName);
  }

  public void wasAdded() {
    log.info("Modpack was added", "object", this.toString());
  }

}
