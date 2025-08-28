package com.lucasluqui.launcher.mod.data;

import net.lingala.zip4j.model.FileHeader;

import java.util.HashMap;

public class FileHeaderData
{
  private final HashMap<FileHeader, Integer> fileHeaders;

  public FileHeaderData ()
  {
    this.fileHeaders = new HashMap<>();
  }

  public void addFileHeader (FileHeader fileHeader, int validState)
  {
    this.fileHeaders.put(fileHeader, validState);
  }

  public HashMap<FileHeader, Integer> getFileHeaders ()
  {
    return this.fileHeaders;
  }
}
