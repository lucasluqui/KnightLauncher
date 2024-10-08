package com.luuqui.util;

import com.luuqui.launcher.setting.Settings;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.luuqui.util.Log.log;

public class Compressor {

  private static final int BUFFER_SIZE = 4096;


  public static void unzip(String source, String dest, Boolean force4j) {
    try {
      if(force4j) {
        unzip4j(source, dest);
        return;
      }

      switch (Settings.compressorUnzipMethod) {
        case "custom":
          unzipCustom(source, dest);
          break;
        case "4j":
          unzip4j(source, dest);
          break;
        default:
          unzip4j(source, dest);
          break;
      }
    } catch (IOException e) {
      log.error(e);
    }
  }


  public static void unzip4j(String source, String dest) throws ZipException {
    ZipFile zipFile = new ZipFile(source);
    zipFile.extractAll(dest);
  }


  public static void unzipCustom(String zipFilePath, String destDirectory) throws IOException {
    FileUtil.createDir(destDirectory);
    ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
    ZipEntry entry = zipIn.getNextEntry();
    // iterates over entries in the zip file
    while (entry != null) {
      String filePath = destDirectory + File.separator + entry.getName();
      if (entry.getName().contains(".json")) {
        zipIn.closeEntry();
        entry = zipIn.getNextEntry();
        continue;
      }
      if (!entry.isDirectory()) {
        // if the entry is a file, extracts it
        extractFileSafe(zipIn, filePath);
      } else {
        // if the entry is a directory, make the directory
        FileUtil.createDir(filePath);
      }
      zipIn.closeEntry();
      entry = zipIn.getNextEntry();
    }
    zipIn.close();
  }


  private static void extractFileSafe(ZipInputStream zipIn, String filePath) throws IOException {
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
    byte[] bytesIn = new byte[Settings.compressorExtractBuffer];
    int read = 0;
    while ((read = zipIn.read(bytesIn)) != -1) {
      bos.write(bytesIn, 0, read);
    }
    bos.close();
  }


  public static String readFileInsideZip(String zip, String pathInZip) {
    ZipFile zipFile = new ZipFile(zip);
    FileHeader fileHeader;
    String content = null;
    try {
      fileHeader = zipFile.getFileHeader(pathInZip);
      InputStream inputStream = zipFile.getInputStream(fileHeader);
      content = FileUtil.convertInputStreamToString(inputStream);
    } catch (IOException e) {
      log.error(e);
    }
    return content;
  }

  public static InputStream getISFromFileInsideZip(String zip, String pathInZip) {
    ZipFile zipFile = new ZipFile(zip);
    FileHeader fileHeader;
    InputStream inputStream = null;
    try {
      fileHeader = zipFile.getFileHeader(pathInZip);
      inputStream = zipFile.getInputStream(fileHeader);
    } catch (IOException e) {
      if(e instanceof ZipException) {
        // ignore
      } else {
        log.error(e);
      }
    }
    return inputStream;
  }


  @SuppressWarnings("unused")
  public static String getZipHash(String source) {
    InputStream file = null;
    String hash = null;
    try {
      file = new FileInputStream(source);
    } catch (FileNotFoundException e) {
      log.error(e);
    }
    ZipInputStream stream = new ZipInputStream(file);
    try {
      ZipEntry entry;
      while ((entry = stream.getNextEntry()) != null) {
        MessageDigest md = MessageDigest.getInstance("MD5");
        DigestInputStream dis = new DigestInputStream(stream, md);
        byte[] buffer = new byte[BUFFER_SIZE];
        int read = dis.read(buffer);
        while (read > -1) {
          read = dis.read(buffer);
        }
        hash = new String(dis.getMessageDigest().digest());
      }
    } catch (NoSuchAlgorithmException | IOException e) {
      log.error(e);
    } finally {
      try {
        stream.close();
      } catch (IOException e) {
        log.error(e);
      }
    }
    return hash;
  }

  public static List<String> getFileListFromZip(String zipPath) {
    java.util.zip.ZipFile zipFile = null;
    try {
      zipFile = new java.util.zip.ZipFile(zipPath);
    } catch (IOException e) {
      log.error(e);
    }

    Enumeration zipEntries = zipFile.entries();

    List<String> fileList = new ArrayList();

    while (zipEntries.hasMoreElements()) {
      fileList.add(((ZipEntry) zipEntries.nextElement()).getName());
    }

    return fileList;
  }

  // source: https://stackoverflow.com/questions/51833423/how-to-zip-the-content-of-a-directory-in-java
  public static void zipFolderContents(File srcFolder, File destZipFile, String zipFileName) throws Exception {
    try (FileOutputStream fileWriter = new FileOutputStream(destZipFile);
         ZipOutputStream zip = new ZipOutputStream(fileWriter)) {
      addFolderToZip(srcFolder, srcFolder, zip, zipFileName);
      fileWriter.close();
      zip.close();
    }
  }

  private static void addFileToZip(File rootPath, File srcFile, ZipOutputStream zip, String zipFileName) throws Exception {
    if (srcFile.isDirectory()) {
      addFolderToZip(rootPath, srcFile, zip, zipFileName);
    } else if(srcFile.getName().equalsIgnoreCase(zipFileName)) {
      // do nothing
    } else {
      byte[] buf = new byte[BUFFER_SIZE];
      int len;
      try (FileInputStream in = new FileInputStream(srcFile)) {
        String name = srcFile.getPath();
        name = name.replace(rootPath.getPath(), "");
        name = name.substring(1);
        zip.putNextEntry(new ZipEntry(name));
        while ((len = in.read(buf)) > 0) {
          zip.write(buf, 0, len);
        }
      }
    }
  }

  private static void addFolderToZip(File rootPath, File srcFolder, ZipOutputStream zip, String zipFileName) throws Exception {
    for (File fileName : Objects.requireNonNull(srcFolder.listFiles())) {
      addFileToZip(rootPath, fileName, zip, zipFileName);
    }
  }
}
