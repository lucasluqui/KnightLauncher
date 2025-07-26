package com.luuqui.launcher.mod.data;

import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.util.*;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import static com.luuqui.launcher.mod.Log.log;

public class ZipMod extends Mod
{
  private final List<LocaleChange> localeChangeList = new ArrayList<>();

  protected String type;

  @SuppressWarnings("unused")
  public ZipMod ()
  {
    super();
  }

  public ZipMod (String rootDir, String fileName)
  {
    super();
    this.displayName = fileName;
    this.fileName = fileName;
    this.setAbsolutePath(rootDir + fileName);
    parseMetadata();
  }

  public void mount () { }

  public void mount (String rootDir)
  {
    if (this.type == null) {
      log.info("Mounting Zip mod", "mod", this.displayName);
      Compressor.unzip(rootDir + "/mods/" + this.fileName, rootDir + "/rsrc/", false, Settings.fileProtection, LauncherGlobals.FILTER_LIST);
      log.info("Zip mod mounted successfully", "mod", this.displayName);
    } else if (this.type.equalsIgnoreCase("class")) {
      log.info("Mounting Zip mod with class type", "mod", this.displayName);
      try {
        ZipFile config = new ZipFile(rootDir + "/code/config.jar");
        FileUtil.unpackJar(config, new File(rootDir + "/code/class-changes/"), false);
        config.close();

        Compressor.unzip(rootDir + "/mods/" + this.fileName, rootDir + "/code/class-changes/", false, Settings.fileProtection, LauncherGlobals.FILTER_LIST);
        FileUtils.delete(new File(rootDir + "/code/class-changes/mod.json"));

        // Turn the class changes into a jar file.
        String[] outputCapture;
        if (SystemUtil.isWindows()) {
          outputCapture = ProcessUtil.runAndCapture(new String[] { "cmd.exe", "/C", JavaUtil.getGameJVMDirPath() + "/bin/jar.exe", "cvf", "code/config-new.jar", "-C", "code/class-changes/", "." });
        } else {
          outputCapture = ProcessUtil.runAndCapture(new String[] { "/bin/bash", "-c", JavaUtil.getGameJVMDirPath() + "/bin/jar", "cvf", "code/config-new.jar", "-C", "code/class-changes/", "." });
        }
        log.debug("Class changes capture, stdout=", outputCapture[0], "stderr=", outputCapture[1]);

        // Delete the temporary directory used to store class changes.
        FileUtils.deleteDirectory(new File(rootDir + "/code/class-changes"));

        // Rename the current config to old and the new one to its original name.
        FileUtils.moveFile(new File(rootDir + "/code/config.jar"), new File(rootDir + "/code/config-old.jar"));
        FileUtils.moveFile(new File(rootDir + "/code/config-new.jar"), new File(rootDir + "/code/config.jar"));

        // And finally, remove the old one. We don't need to store it as we'll fetch the original from getdown
        // when a rebuild is triggered.
        FileUtils.delete(new File(rootDir + "/code/config-old.jar"));
      } catch (IOException e) {
        log.error(e);
      }
      log.info("Zip mod with class type mounted successfully", "mod", this.displayName);
    }
  }

  public void wasAdded ()
  {
    log.info("Zip mod was added", "object", this.toString());
  }

  public void parseMetadata ()
  {
    super.parseMetadata();
    if (this.metadata != null) {
      if (this.metadata.has("type")) {
        this.setType(this.metadata.getString("type"));
      }

      if (this.metadata.has("locale")) {
        JSONObject localeJson = this.metadata.getJSONObject("locale");
        for (String bundle : localeJson.keySet()) {
          JSONObject bundleJson = localeJson.getJSONObject(bundle);
          for (String key : bundleJson.keySet()) {
            localeChangeList.add(new LocaleChange(bundle, key, bundleJson.getString(key)));
          }
        }
      }
    }
  }

  public boolean hasLocaleChanges ()
  {
    return !this.localeChangeList.isEmpty();
  }

  public List<LocaleChange> getLocaleChanges ()
  {
    return this.localeChangeList;
  }

  public String getType ()
  {
    return this.type;
  }

  public void setType (String type)
  {
    this.type = type;
  }

}
