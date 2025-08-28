//
// Parts of the following code were stripped off Three Rings Getdown's repository:
// In the most part, JAR handling (extracting) code.
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/threerings/getdown/blob/master/LICENSE

package com.lucasluqui.util;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.lucasluqui.util.Log.log;

public class FileUtil
{

  public static void createDir (String path)
  {
    new File(path).mkdirs();
  }

  public static boolean createFile (String path)
  {
    File file = new File(path);
    try {
      return file.createNewFile();
    } catch (IOException e) {
      log.error(e);
    }
    return false;
  }

  public static boolean deleteFile (String path)
  {
    File file = new File(path);
    return file.delete();
  }

  public static boolean recreateFile (String path)
  {
    File file = new File(path);
    file.delete();
    try {
      return file.createNewFile();
    } catch (IOException e) {
      log.error(e);
    }
    return false;
  }

  public static void rename (File old, File dest)
  {
    old.renameTo(dest);
  }

  public static List<String> fileNamesInDirectory (String dir)
  {
    File folder = new File(dir);
    File[] fileList = folder.listFiles();
    List<String> fileNames = new ArrayList<String>();

    for (int i = 0; i < fileList.length; i++) {
      if (fileList[i].isDirectory() == false) {
        fileNames.add(fileList[i].getName());
      }
    }

    return fileNames;
  }

  public static List<String> fileNamesInDirectory (String dir, String ext)
  {
    File folder = new File(dir);
    File[] fileList = folder.listFiles();
    List<String> fileNames = new ArrayList<String>();

    for (int i = 0; i < fileList.length; i++) {
      if (fileList[i].isDirectory() == false && fileList[i].toString().endsWith(ext)) {
        fileNames.add(fileList[i].getName());
      }
    }

    return fileNames;
  }

  public static List<File> filesInDirectory (String dir, String ext)
  {
    File folder = new File(dir);
    File[] fileList = folder.listFiles();
    List<File> files = new ArrayList<File>();

    for (int i = 0; i < fileList.length; i++) {
      if (fileList[i].isDirectory() == false && fileList[i].toString().endsWith(ext)) {
        files.add(fileList[i]);
      }
    }

    return files;
  }

  public static List<File> filesAndDirectoriesInDirectory (String dir)
  {
    File folder = new File(dir);
    File[] fileList = folder.listFiles();
    List<File> files = new ArrayList<File>();

    for (int i = 0; i < fileList.length; i++) {
      files.add(fileList[i]);
    }

    return files;
  }

  public static boolean fileExists (String path)
  {
    File file = new File(path);
    return file.exists();
  }

  public static String readFile (String path)
      throws IOException
  {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, StandardCharsets.UTF_8);
  }

  public static void writeFile (String path, String content)
  {
    try (Writer writer = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(path), StandardCharsets.UTF_8))) {
      writer.write(content);
    } catch (IOException e) {
      log.error(e);
    }
  }

  /*
   * Method to convert InputStream to String
   */
  public static String convertInputStreamToString (InputStream is)
  {
    BufferedReader br = null;
    StringBuilder sbContent = new StringBuilder();

    try {
      // Create BufferedReader from InputStreamReader
      br = new BufferedReader(new InputStreamReader(is));

      // read line by line and append content to StringBuilder
      String strLine = null;
      boolean isFirstLine = true;

      while ((strLine = br.readLine()) != null) {
        if (isFirstLine) {
          sbContent.append(strLine);
        } else {
          sbContent.append("\n").append(strLine);
        }

        // Flag to make sure we don't append new line before the first line.
        isFirstLine = false;
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (br != null) br.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    // convert StringBuilder to String and return
    return sbContent.toString();
  }

  public static void copyFilesToClipboard (List<File> files)
  {
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
      new Transferable() {
        @Override
        public DataFlavor[] getTransferDataFlavors() {
          return new DataFlavor[] { DataFlavor.javaFileListFlavor };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
          return DataFlavor.javaFileListFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
          return files;
        }
      }, null
    );
  }

  public static void purgeDirectory (File dir, String[] exceptions)
  {
    for (File file : dir.listFiles()) {
      boolean skip = false;
      for (String except : exceptions) {
        if (file.getName().contains(except)) {
          skip = true;
          break;
        }
      }
      if (skip) continue;
      if (file.isDirectory())
        purgeDirectory(file, exceptions);
      file.delete();
    }
  }

}
