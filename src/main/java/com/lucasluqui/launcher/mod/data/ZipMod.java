package com.lucasluqui.launcher.mod.data;

import com.lucasluqui.util.*;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.lucasluqui.launcher.mod.Log.log;

public class ZipMod extends Mod
{
  private final List<LocaleChange> localeChanges = new ArrayList<>();
  private final FileHeaderData fileHeaderData = new FileHeaderData();
  protected String type;
  protected Boolean hasInvalidFileHeaders;

  @SuppressWarnings("unused")
  public ZipMod ()
  {
    super();
  }

  public ZipMod (String rootDir, String fileName, String[] filter, Properties stamps)
  {
    super();
    this.displayName = fileName;
    this.fileName = fileName;
    this.setAbsolutePath(rootDir + fileName);

    this.hasInvalidFileHeaders = false;
    parseFileHeaderData(filter, stamps);
    parseMetadata();
  }

  public void mount () { }

  public void mount (String rootDir, String[] filter, Properties stamps)
  {
    ZipFile zipFile = new ZipFile(this.getAbsolutePath());

    if (this.type == null) {
      log.info("Mounting Zip mod", "mod", this.displayName);

      for (Map.Entry<FileHeader, Integer> entry : fileHeaderData.getFileHeaders().entrySet()) {
        if (entry.getValue() < 1) ZipUtil.unzipFileHeader(
            zipFile, entry.getKey(), rootDir + "/rsrc/", false);
        ZipUtil.closeZip(zipFile);
      }

      log.info("Zip mod mounted successfully", "mod", this.displayName);
    } else {
      log.info("Mounting Zip mod with " + this.type + " type", "mod", this.displayName);
      if (this.type.equalsIgnoreCase("class")) {
        for (Map.Entry<FileHeader, Integer> entry : fileHeaderData.getFileHeaders().entrySet()) {
          if (entry.getValue() < 1) ZipUtil.unzipFileHeader(
              zipFile, entry.getKey(), rootDir + "/code/class-changes/", false);
          ZipUtil.closeZip(zipFile);
        }
      }
      log.info("Zip mod with " + this.type + " type mounted successfully", "mod", this.displayName);
    }
  }

  public void wasAdded ()
  {
    log.info("Zip mod was added", "object", this.toString());
  }

  private void parseFileHeaderData (String[] filter, Properties stamps)
  {
    log.info("Parsing mod file headers...", "fileName", this.getFileName());
    List<FileHeader> fileHeaders = ZipUtil.getZipFileHeaders(this.getAbsolutePath());

    for (FileHeader fileHeader : fileHeaders) {
      int validState = 0;
      String fileHeaderFileName = fileHeader.getFileName();

      // no extension, and we don't want to extract directories.
      if (!fileHeaderFileName.contains(".")) continue;

      // ignore metadata file.
      if (fileHeaderFileName.contains("mod.json")) continue;

      if (filter != null) {
        for (String filterFileName : filter) {

          // File is inside the filter list we got passed.
          if (fileHeaderFileName.equalsIgnoreCase(filterFileName)) {
            validState = 1;
            this.hasInvalidFileHeaders = true;
            log.info(
                "Ignored file header found in filter list",
                "fileName", this.getFileName(), "header", fileHeaderFileName);
          }
        }
      }

      for (String forcedFilterFileName : this.FORCED_FILTER_LIST) {

        // File is inside the forced filter list.
        if (fileHeaderFileName.equalsIgnoreCase(forcedFilterFileName)) {
          validState = 2;
          this.hasInvalidFileHeaders = true;
          log.info(
              "Ignored file header found in forced filter list",
              "fileName", this.getFileName(), "header", fileHeaderFileName);
        }
      }

      if (stamps.containsKey(fileHeaderFileName)) {
        long vanillaStamp = Long.parseLong(stamps.getProperty(fileHeaderFileName));
        long modStamp = fileHeader.getLastModifiedTimeEpoch();

        // File is older than the vanilla counterpart.
        if (modStamp < vanillaStamp) {
          validState = 3;
          this.hasInvalidFileHeaders = true;
          log.info(
              "Ignored file header older than vanilla counterpart",
              "fileName", this.getFileName(),
              "header", fileHeaderFileName,
              "modStamp", modStamp,
              "vanillaStamp", vanillaStamp
          );
        }
      }

      this.fileHeaderData.addFileHeader(fileHeader, validState);
    }
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

  public FileHeaderData getFileHeaderData ()
  {
    return this.fileHeaderData;
  }

  public Boolean hasInvalidFileHeaders ()
  {
    return this.hasInvalidFileHeaders;
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
