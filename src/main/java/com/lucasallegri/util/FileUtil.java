//
// Parts of the following code were stripped off Three Rings Getdown's repository:
// In the most part, JAR handling (extracting) code.
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2018 Getdown authors
// https://github.com/threerings/getdown/blob/master/LICENSE

package com.lucasallegri.util;

import com.samskivert.util.Logger;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.lucasallegri.util.Log.log;

public class FileUtil {

  public static void createDir(String path) {
    new File(path).mkdirs();
  }

  public static boolean createFile(String path) {
    File file = new File(path);
    try {
      return file.createNewFile();
    } catch (IOException e) {
      log.error(e);
    }
    return false;
  }

  public static boolean deleteFile(String path) {
    File file = new File(path);
    return file.delete();
  }

  public static boolean recreateFile(String path) {
    File file = new File(path);
    file.delete();
    try {
      return file.createNewFile();
    } catch (IOException e) {
      log.error(e);
    }
    return false;
  }

  public static void rename(File old, File dest) {
    old.renameTo(dest);
  }

  public static List<String> fileNamesInDirectory(String dir) {

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

  public static List<String> fileNamesInDirectory(String dir, String ext) {

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

  public static List<File> filesInDirectory(String dir, String ext) {

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

  public static boolean fileExists(String path) {
    File file = new File(path);
    return file.exists();
  }

  public static String readFile(String path) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, StandardCharsets.UTF_8);
  }

  public static void writeFile(String path, String content) {
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
  public static String convertInputStreamToString(InputStream is) {

    BufferedReader br = null;
    StringBuilder sbContent = new StringBuilder();

    try {
      /*
       * Create BufferedReader from InputStreamReader
       */
      br = new BufferedReader(new InputStreamReader(is));

      /*
       * read line by line and append content to
       * StringBuilder
       */
      String strLine = null;
      boolean isFirstLine = true;

      while ((strLine = br.readLine()) != null) {
        if (isFirstLine) {
          sbContent.append(strLine);
        } else {
          sbContent.append("\n").append(strLine);
        }

        /*
         * Flag to make sure we don't append new line
         * before the first line.
         */
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

    //convert StringBuilder to String and return
    return sbContent.toString();
  }

  public static void extractFileWithinJar(String pathInside, String pathOutside) throws IOException {
    URL filePath = FileUtil.class.getResource(pathInside);
    File destPath = new File(pathOutside);
    FileUtils.copyURLToFile(filePath, destPath);
  }

  /**
   * Unpacks the specified jar file into the specified target directory.
   *
   * @param cleanExistingDirs if true, all files in all directories contained in {@code jar} will
   *                          be deleted prior to unpacking the jar.
   */
  public static void unpackJar(ZipFile jar, File target, boolean cleanExistingDirs)
          throws IOException {
    if (cleanExistingDirs) {
      Enumeration<? extends ZipEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (entry.isDirectory()) {
          File efile = new File(target, entry.getName());
          if (efile.exists()) {
            for (File f : efile.listFiles()) {
              if (!f.isDirectory())
                f.delete();
            }
          }
        }
      }
    }

    Enumeration<? extends ZipEntry> entries = jar.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      File efile = new File(target, entry.getName());

      // if we're unpacking a normal jar file, it will have special path
      // entries that allow us to create our directories first
      if (entry.isDirectory()) {
        if (!efile.exists() && !efile.mkdir()) {
          log.warning("Failed to create jar entry path", "jar", jar, "entry", entry);
        }
        continue;
      }

      // but some do not, so we want to ensure that our directories exist
      // prior to getting down and funky
      File parent = new File(efile.getParent());
      if (!parent.exists() && !parent.mkdirs()) {
        log.warning("Failed to create jar entry parent", "jar", jar, "parent", parent);
        continue;
      }

      try (BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(efile));
           InputStream jin = jar.getInputStream(entry)) {
        StreamUtil.copy(jin, fout);
      } catch (Exception e) {
        throw new IOException(
                Logger.format("Failure unpacking", "jar", jar, "entry", efile), e);
      }
    }
  }

  /**
   * Unpacks a pack200 packed jar file from {@code packedJar} into {@code target}.
   * If {@code packedJar} has a {@code .gz} extension, it will be gunzipped first.
   */
  @SuppressWarnings({"removal", "deprecation"})
  public static void unpackPacked200Jar(File packedJar, File target) throws IOException {
    try (InputStream packJarIn = new FileInputStream(packedJar);
         JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(target))) {
      boolean gz = (packedJar.getName().endsWith(".gz") ||
              packedJar.getName().endsWith(".gz_new"));
      try (InputStream packJarIn2 = (gz ? new GZIPInputStream(packJarIn) : packJarIn)) {
        Pack200.Unpacker unpacker = Pack200.newUnpacker();
        unpacker.unpack(packJarIn2, jarOut);
      }
    }
  }

}
