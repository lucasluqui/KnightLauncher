package com.luuqui.launcher.mod.data;

import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.util.Compressor;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.luuqui.launcher.mod.Log.log;

public class ZipMod extends Mod
{
  private final List<LocaleChange> localeChangeList = new ArrayList<>();

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
    log.info("Mounting Zip mod", "mod", this.displayName);
    Compressor.unzip(rootDir + "/mods/" + this.fileName, rootDir + "/rsrc/", false, Settings.fileProtection, LauncherGlobals.FILTER_LIST);
    log.info("Zip mod mounted successfully", "mod", this.displayName);
  }

  public void parseMetadata ()
  {
    super.parseMetadata();
    if (this.metadata != null && this.metadata.has("locale")) {
      JSONObject localeJson = this.metadata.getJSONObject("locale");
      for (String bundle : localeJson.keySet()) {
        JSONObject bundleJson = localeJson.getJSONObject(bundle);
        for (String key : bundleJson.keySet()) {
          localeChangeList.add(new LocaleChange(bundle, key, bundleJson.getString(key)));
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

  public void wasAdded ()
  {
    log.info("Zip mod was added", "object", this.toString());
  }

}
