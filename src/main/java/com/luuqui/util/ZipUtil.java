package com.luuqui.util;

import com.luuqui.launcher.setting.Settings;
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

import static com.luuqui.util.Log.log;

public class ZipUtil
{
  private static final int BUFFER_SIZE = 4096;

  public static void normalUnzip (String source, String dest)
  {
    doUnzip(new ZipFile(source), dest);
  }

  public static void controlledUnzip (String source, String dest, String[] filter, Properties stamps)
  {
    ZipFile zipFile = new ZipFile(source);

    // Make sure a backup of this zip file exists before we, potentially, make any changes to it.
    checkZipFileBackup(zipFile);

    try {
      for (FileHeader fileHeader : zipFile.getFileHeaders()) {
        String fileHeaderFileName = fileHeader.getFileName();

        if (filter != null) {
          for (String filterFileName : filter) {

            // File is inside the filter list we got passed.
            if (fileHeaderFileName.equalsIgnoreCase(filterFileName)) {
              zipFile.removeFile(fileHeader);
              log.info(
                  "Removed file found in filter list",
                  "zip", zipFile.getFile().getName(), "file", fileHeaderFileName);
            }
          }
        }

        for (String forcedFilterFileName : FORCED_FILTER_LIST) {

          // File is inside the forced filter list.
          if (fileHeaderFileName.equalsIgnoreCase(forcedFilterFileName)) {
            zipFile.removeFile(fileHeader);
            log.info(
                "Removed file found in forced filter list",
                "zip", zipFile.getFile().getName(), "file", fileHeaderFileName);
          }
        }

        if (stamps.containsKey(fileHeaderFileName)) {
          long stamp = Long.parseLong(stamps.getProperty(fileHeaderFileName));

          // File is older than the vanilla counterpart.
          if (fileHeader.getLastModifiedTime() < stamp) {
            zipFile.removeFile(fileHeader);
            log.info(
                "Removed file older than vanilla counterpart",
                "zip", zipFile.getFile().getName(), "file", fileHeaderFileName);
          }
        }
      }
    } catch (IOException e) {
      log.error(e);
    }

    doUnzip(zipFile, dest);
  }

  private static void checkZipFileBackup (ZipFile zipFile)
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

  private static void doUnzip (ZipFile zipFile, String dest)
  {
    try {
      zipFile.extractAll(dest);
      zipFile.close(); // Try to close the stream after we're done.
    } catch (IOException e) {
      try {
        zipFile.close();
      } catch (IOException ex) {
        log.error("Could not close zip file");
        log.error(ex);
      }
      log.error(e);
    }
  }

  @Deprecated
  public static void unzipCustom (String zipFilePath, String destDirectory)
      throws IOException
  {
    unzipCustom(zipFilePath, destDirectory, null, null);
  }

  @Deprecated
  public static void unzipCustom (String zipFilePath, String destDirectory, String[] filterList, Properties stamps)
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
        for (String fileName : FORCED_FILTER_LIST) {
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

  private static final String[] FORCED_FILTER_LIST = new String[] {
      "item/live/statue/model.dat",
      "model/conditional/distance.dat",
      "world/dynamic/switch/button/model.dat",
      "world/dynamic/switch/button/model_pressure.dat",
      "world/dynamic/switch/button/model_pressure_onetime.dat",
      "world/dynamic/switch/button/model_pressure_statue.dat",
      "world/dynamic/switch/button/model_whitespace.dat",
      "world/dynamic/switch/button/parts/animation_down.dat",
      "world/dynamic/switch/button/parts/animation_hide.dat",
      "world/dynamic/switch/button/parts/animation_show.dat",
      "world/dynamic/switch/button/parts/animation_up.dat",
      "world/dynamic/switch/button/parts/animation_whitespace_down.dat",
      "world/dynamic/switch/button/parts/animation_whitespace_up.dat",
      "world/dynamic/switch/button/parts/fx_down.dat",
      "world/dynamic/switch/button_large/fx_whitespace-hit.dat",
      "world/dynamic/switch/button_large/fx_whitespace.dat",
      "world/dynamic/switch/button_large/model.dat",
      "world/dynamic/switch/button_large/model_horde.dat",
      "world/dynamic/switch/button_large/model_whitespace.dat",
      "world/dynamic/switch/clockwork_button/glow.dat",
      "world/dynamic/switch/clockwork_button/model.dat",
      "world/dynamic/switch/clockwork_button/animation/state_down.dat",
      "world/dynamic/switch/clockwork_button/animation/state_up.dat",
      "world/dynamic/switch/multistate/model.dat",
      "world/dynamic/switch/multistate/parts/animation_disabled.dat",
      "world/dynamic/switch/multistate/parts/animation_green.dat",
      "world/dynamic/switch/multistate/parts/animation_red.dat",
      "world/dynamic/switch/multistate/parts/animation_violet.dat",
      "world/dynamic/switch/multistate/parts/animation_yellow.dat",
      "world/dynamic/switch/toggle_lever/animation_off.dat",
      "world/dynamic/switch/toggle_lever/animation_on.dat",
      "world/dynamic/switch/toggle_lever/model.dat"
  };
}
