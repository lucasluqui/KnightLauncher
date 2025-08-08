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
import java.util.Properties;

import static com.luuqui.launcher.mod.Log.log;

public class ZipMod extends Mod
{
  private final List<LocaleChange> localeChanges = new ArrayList<>();

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

  public void mount (String rootDir, Properties stamps)
  {
    if (this.type == null) {
      log.info("Mounting Zip mod", "mod", this.displayName);
      ZipUtil.controlledUnzip(rootDir + "/mods/" + this.fileName, rootDir + "/rsrc/", Settings.fileProtection ? LauncherGlobals.FILTER_LIST : null, stamps);
      log.info("Zip mod mounted successfully", "mod", this.displayName);
    } else {
      log.info("Mounting Zip mod with " + this.type + " type", "mod", this.displayName);
      if (this.type.equalsIgnoreCase("class")) {
        try {
          ZipUtil.controlledUnzip(rootDir + "/mods/" + this.fileName, rootDir + "/code/class-changes/", Settings.fileProtection ? LauncherGlobals.FILTER_LIST : null, stamps);
          FileUtils.delete(new File(rootDir + "/code/class-changes/mod.json"));
        } catch (IOException e) {
          log.error(e);
        }
      }
      log.info("Zip mod with " + this.type + " type mounted successfully", "mod", this.displayName);
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
            localeChanges.add(new LocaleChange(bundle, key, bundleJson.getString(key)));
          }
        }
      }
    }
  }

  public boolean hasLocaleChanges ()
  {
    return !this.localeChanges.isEmpty();
  }

  public List<LocaleChange> getLocaleChanges ()
  {
    return this.localeChanges;
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
