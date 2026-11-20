package com.lucasluqui.launcher.mod.data;

import com.lucasluqui.launcher.setting.Settings;
import com.lucasluqui.util.ZipUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.lucasluqui.launcher.mod.Log.log;

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
    parseFiles();
  }

  public void mount ()
  {
  }

  @SuppressWarnings("all")
  public void mount (String rootDir, String[] filter, Properties stamps)
  {
    this.setAbsolutePath(rootDir + "/mods/" + getFileName());
    for (String fileInsideZip : ZipUtil.getFileListFromZip(this.getAbsolutePath())) {
      InputStream fileIs = ZipUtil.getISFromFileInsideZip(this.getAbsolutePath(), fileInsideZip);
      String pathOutside = rootDir + "/mods/" + fileInsideZip;
      File tempFile = new File(pathOutside);
      try {
        FileUtils.copyInputStreamToFile(fileIs, tempFile);
      } catch (IOException e) {
        log.error(e);
      }
      ZipUtil.controlledUnzip(pathOutside, rootDir + "/rsrc/", this.FORCED_FILTER_LIST, Settings.fileProtection ? filter : null, stamps);
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

  @Override
  public List<String> getFiles ()
  {
    return this.files;
  }

  /**
   * Gathers the files of every mod inside the pack.
   * Filters applied at mount time aren't taken into account.
   */
  private void parseFiles ()
  {
    Set<String> files = new TreeSet<>();
    try (ZipFile pack = new ZipFile(this.getAbsolutePath())) {
      for (ZipEntry modEntry : Collections.list(pack.entries())) {
        try (ZipInputStream mod = new ZipInputStream(pack.getInputStream(modEntry))) {
          for (ZipEntry entry = mod.getNextEntry(); entry != null; entry = mod.getNextEntry()) {
            String name = entry.getName();
            if (entry.isDirectory() || !name.contains(".") || name.equals("mod.json") || name.equals("mod.png")) continue;
            files.add(name);
          }
        }
      }
    } catch (IOException e) {
      log.error("Failed to list modpack files", "fileName", this.fileName, "error", e.getMessage());
    }
    this.files = new ArrayList<>(files);
  }

  private List<String> files = new ArrayList<>();

}
