package com.lucasluqui.util;

import com.lucasluqui.launcher.setting.Settings;
import com.samskivert.util.Logger;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.lucasluqui.util.Log.log;

public class ZipUtil
{
  private static final int BUFFER_SIZE = 4096;

  public static void unzip (String source, String dest)
  {
    doUnzip(new ZipFile(source), dest);
  }

  public static void unzipFileHeader (ZipFile zipFile, FileHeader fileHeader, String dest, boolean close)
  {
    try {
      zipFile.extractFile(fileHeader, dest);
      if (close) closeZip(zipFile);
    } catch (ZipException e) {
      log.error(e);
    }
  }

  public static Boolean controlledUnzip (String source, String dest, String[] forcedFilterList, String[] filter, Properties stamps)
  {
    ZipFile zipFile = new ZipFile(source);

    boolean clean = true;
    try {
      for (FileHeader fileHeader : new ArrayList<>(zipFile.getFileHeaders())) {
        boolean extract = true;
        String fileHeaderFileName = fileHeader.getFileName();

        // no extension, and we don't want to extract directories.
        if (!fileHeaderFileName.contains(".")) continue;

        if (filter != null) {
          for (String filterFileName : filter) {

            // File is inside the filter list we got passed.
            if (fileHeaderFileName.equalsIgnoreCase(filterFileName)) {
              clean = false;
              extract = false;
              log.info(
                  "Ignored file found in filter list",
                  "zip", zipFile.getFile().getName(), "file", fileHeaderFileName);
            }
          }
        }

        for (String forcedFilterFileName : forcedFilterList) {

          // File is inside the forced filter list.
          if (fileHeaderFileName.equalsIgnoreCase(forcedFilterFileName)) {
            clean = false;
            extract = false;
            log.info(
                "Ignored file found in forced filter list",
                "zip", zipFile.getFile().getName(), "file", fileHeaderFileName);
          }
        }

        if (stamps.containsKey(fileHeaderFileName)) {
          long stamp = Long.parseLong(stamps.getProperty(fileHeaderFileName));

          // File is older than the vanilla counterpart.
          if (fileHeader.getLastModifiedTime() < stamp) {
            clean = false;
            extract = false;
            log.info(
                "Ignored file older than vanilla counterpart",
                "zip", zipFile.getFile().getName(), "file", fileHeaderFileName);
          }
        }

        if (extract) zipFile.extractFile(fileHeader, dest);
      }
      closeZip(zipFile);
    } catch (IOException e) {
      log.error(e);
    }
    return clean;
  }

  private static void doUnzip (ZipFile zipFile, String dest)
  {
    try {
      zipFile.extractAll(dest);
      closeZip(zipFile); // Try to close the stream after we're done.
    } catch (IOException e) {
      log.error(e);
    }
  }

  public static void closeZip (ZipFile zipFile)
  {
    try {
      zipFile.close();
    } catch (IOException e) {
      log.error("Failed to close zip file");
      log.error(e);
    }
  }

  public static List<FileHeader> getZipFileHeaders (String source)
  {
    ZipFile zipFile = new ZipFile(source);
    List<FileHeader> fileHeaders = new ArrayList<>();

    try {
      fileHeaders = zipFile.getFileHeaders();
      closeZip(zipFile);
    } catch (ZipException e) {
      log.error(e);
    }

    return fileHeaders;
  }

  @Deprecated
  private static void createZipFileBackup (ZipFile zipFile)
  {
    String absolutePath = zipFile.getFile().getAbsolutePath();
    if (!FileUtil.fileExists(absolutePath + ".bak")) {
      try {
        FileUtils.copyFile(zipFile.getFile(), new File(absolutePath + ".bak"));
      } catch (IOException e) {
        log.error(e);
      }
    }
  }

  @Deprecated
  public static void unzipCustom (String zipFilePath, String destDirectory, String[] forcedFilterList)
      throws IOException
  {
    unzipCustom(zipFilePath, destDirectory, forcedFilterList, null, null);
  }

  @Deprecated
  public static void unzipCustom (String zipFilePath, String destDirectory, String[] forcedFilterList, String[] filterList, Properties stamps)
      throws IOException
  {
    FileUtil.createDir(destDirectory);
    ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)));
    ZipEntry entry = zipIn.getNextEntry();

    // iterates over entries in the zip file
    while (entry != null) {
      String filePath = destDirectory + File.separator + entry.getName();
      try {
        boolean illegalFile = false;

        // Check if we've been given a filter list and in that case, iterate through it.
        if (filterList != null) {
          for (String fileName : filterList) {
            if (filePath.equalsIgnoreCase(fileName)) {
              log.info("Filter found illegal file", "source", zipFilePath, "file", fileName);
              illegalFile = true;
              break;
            }
          }
        }

        // Also, check whether any of the files matches the forced filter list.
        for (String fileName : forcedFilterList) {
          if (filePath.equalsIgnoreCase(fileName)) {
            log.info("Filter found illegal file. This is a forced filter thus filter value will be ignored.",
                "source", zipFilePath, "file", fileName);
            illegalFile = true;
            break;
          }
        }

        if (stamps.containsKey(filePath)) {
          long stamp = Long.parseLong(stamps.getProperty(filePath));

          // File is older than the vanilla counterpart.
          if (entry.getTime() < stamp) {
            illegalFile = true;
          }
        }

        if (illegalFile) {
          zipIn.closeEntry();
          entry = zipIn.getNextEntry();
          continue;
        }
      } catch (Exception e) {
        log.error(e);
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

  @Deprecated
  private static void extractFileSafe (ZipInputStream zipIn, String filePath)
      throws IOException
  {
    BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(filePath)));
    byte[] bytesIn = new byte[Settings.compressorExtractBuffer];
    int read = 0;
    while ((read = zipIn.read(bytesIn)) != -1) {
      bos.write(bytesIn, 0, read);
    }
    bos.close();
  }

  public static void extractFileWithinJar (String pathInside, String pathOutside)
      throws IOException
  {
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
  public static void unpackJar (java.util.zip.ZipFile jar, File target, boolean cleanExistingDirs)
      throws IOException
  {
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
  public static void unpackPacked200Jar(File packedJar, File target)
      throws IOException
  {
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


  public static String readFileInsideZip (String zip, String pathInZip)
      throws IOException
  {
    ZipFile zipFile = new ZipFile(zip);
    FileHeader fileHeader = zipFile.getFileHeader(pathInZip);
    InputStream inputStream = zipFile.getInputStream(fileHeader);
    String content = FileUtil.convertInputStreamToString(inputStream);
    zipFile.close();
    return content;
  }

  public static InputStream getISFromFileInsideZip (String zip, String pathInZip)
  {
    ZipFile zipFile = new ZipFile(zip);
    FileHeader fileHeader;
    InputStream inputStream = null;
    try {
      fileHeader = zipFile.getFileHeader(pathInZip);
      inputStream = zipFile.getInputStream(fileHeader);
    } catch (IOException e) {
      if (e instanceof ZipException) {
        // ignore
      } else {
        log.error(e);
      }
    }
    return inputStream;
  }


  @SuppressWarnings("unused")
  public static String getZipHash (String source)
  {
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

  public static List<String> getFileListFromZip (String zipPath)
  {
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
  public static void zipFolderContents (File srcFolder, File destZipFile, String zipFileName)
      throws Exception
  {
    try (FileOutputStream fileWriter = new FileOutputStream(destZipFile);
         ZipOutputStream zip = new ZipOutputStream(fileWriter)) {
      addFolderToZip(srcFolder, srcFolder, zip, zipFileName);
    }
  }

  private static void addFileToZip (File rootPath, File srcFile, ZipOutputStream zip, String zipFileName)
      throws Exception
  {
    if (srcFile.isDirectory()) {
      addFolderToZip(rootPath, srcFile, zip, zipFileName);
    } else if (srcFile.getName().equalsIgnoreCase(zipFileName)) {
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

  private static void addFolderToZip (File rootPath, File srcFolder, ZipOutputStream zip, String zipFileName)
      throws Exception
  {
    for (File fileName : Objects.requireNonNull(srcFolder.listFiles())) {
      addFileToZip(rootPath, fileName, zip, zipFileName);
    }
  }
}
