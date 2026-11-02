package com.lucasluqui.launcher.runtime;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads and installs launcher runtimes into {@link RuntimeLocator#getRuntimesDir()}.
 * <p>
 * A runtime only becomes the one in use once it's been fully downloaded, verified, extracted and checked,
 * so an interrupted install never leaves a broken runtime behind. Each runtime goes into its own directory
 * since on Windows the files of a running runtime can't be replaced.
 * <p>
 * Compiled for Java 8 (see {@code src/main/java8}), so it can only use Java 8 APIs.
 */
public final class RuntimeInstaller
{
  private RuntimeInstaller ()
  {}

  public interface ProgressListener
  {
    /**
     * @param total Total bytes to download, or -1 if unknown.
     */
    void onDownloadProgress (long done, long total);

    void onStatus (String status);
  }

  /**
   * Downloads, verifies and installs a runtime, then marks it as the one in use.
   *
   * @param checksum  The SHA-256 the runtime archive should have. Fetched from the source if null.
   * @param listener  Notified of progress, may be null.
   * @return The home directory of the installed runtime.
   */
  public static File install (RuntimeSource source, String checksum, ProgressListener listener)
    throws IOException
  {
    File runtimesDir = RuntimeLocator.getRuntimesDir();
    if (!runtimesDir.isDirectory() && !runtimesDir.mkdirs()) {
      throw new IOException("Could not create " + runtimesDir);
    }

    long now = System.currentTimeMillis();
    File archive = new File(runtimesDir, "download-" + now + (source.tar ? ".tar.gz" : ".zip"));
    File tempDir = new File(runtimesDir, "extract-" + now);

    try {
      status(listener, "Downloading Java runtime...");
      if (checksum == null) checksum = source.fetchChecksum();

      String actualChecksum = download(source.archiveUrl, archive, listener);
      if (!actualChecksum.equalsIgnoreCase(checksum)) {
        throw new IOException("Runtime checksum mismatch, expected " + checksum + " but got " + actualChecksum);
      }

      status(listener, "Extracting Java runtime...");
      if (source.tar) {
        extractTarGz(archive, tempDir);
      } else {
        extractZip(archive, tempDir);
      }

      File home = findHome(tempDir);
      if (home == null) throw new IOException("Downloaded runtime has no Java executable");
      markExecutable(home);

      if (!RuntimeLocator.isValidRuntime(home)) {
        throw new IOException("Downloaded runtime isn't a usable Java " + RuntimeLocator.getRequiredMajorVersion() + "+ runtime");
      }

      // Move it into place, named after its version and checksum so updates of the same version get their own directory.
      String version = RuntimeLocator.getReleaseVersion(home);
      if (version == null) version = String.valueOf(RuntimeLocator.getRequiredMajorVersion());
      String dirName = sanitizeDirName(version) + "-" + checksum.substring(0, 8);

      File target = new File(runtimesDir, dirName);
      if (target.exists() && !deleteRecursively(target)) {
        // Probably in use, put it next to it instead.
        target = new File(runtimesDir, dirName + "-" + now);
      }
      moveReplacing(home, target);

      RuntimeLocator.setInstalledRuntime(version, checksum.toLowerCase(), target.getName());
      status(listener, "Java runtime " + version + " installed");
      return target;
    } finally {
      deleteRecursively(archive);
      deleteRecursively(tempDir);
    }
  }

  /**
   * Deletes runtimes that aren't in use anymore, along with leftovers from interrupted installs.
   * Runtimes that are still in use are left alone.
   */
  public static void pruneOldRuntimes ()
  {
    File runtimesDir = RuntimeLocator.getRuntimesDir();
    File[] children = runtimesDir.listFiles();
    if (children == null) return;

    File installed = RuntimeLocator.getInstalledRuntimeDir();
    File currentJavaHome = RuntimeLocator.getCurrentJavaHome();

    for (File child : children) {
      if (installed != null && RuntimeLocator.isInside(installed, child)) continue;
      if (RuntimeLocator.isInside(currentJavaHome, child)) continue;
      deleteRecursively(child);
    }
  }

  /**
   * Opens a connection, following redirects by hand since HttpURLConnection won't follow
   * the ones that switch protocols (e.g. http to https).
   */
  static URLConnection openFollowingRedirects (URL url) throws IOException
  {
    for (int redirects = 0; redirects <= MAX_REDIRECTS; redirects++) {
      URLConnection connection = url.openConnection();
      connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
      connection.setReadTimeout(READ_TIMEOUT_MS);
      connection.setRequestProperty("User-Agent", "KnightLauncher");

      if (!(connection instanceof HttpURLConnection)) return connection;

      HttpURLConnection http = (HttpURLConnection) connection;
      http.setInstanceFollowRedirects(false);
      int code = http.getResponseCode();

      if (code >= 300 && code < 400) {
        String location = http.getHeaderField("Location");
        http.disconnect();
        if (location == null) throw new IOException("Redirect without a location from " + url);
        url = new URL(url, location);
        continue;
      }

      if (code != HttpURLConnection.HTTP_OK) {
        http.disconnect();
        throw new IOException("Unexpected response " + code + " from " + url);
      }

      return http;
    }
    throw new IOException("Too many redirects from " + url);
  }

  /**
   * Moves a file or directory, replacing whatever is at the destination. Atomic where possible.
   */
  static void moveReplacing (File source, File target) throws IOException
  {
    try {
      Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException e) {
      Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * Turns a runtime version into something safe to use as a directory name, e.g. "25.0.5+8-LTS" into "25.0.5_8-LTS".
   * The start scripts read it too, so it must never need escaping.
   */
  static String sanitizeDirName (String version)
  {
    return version.replaceAll("[^A-Za-z0-9._-]", "_");
  }

  /**
   * @return The checksum (SHA-256) of what was downloaded, lowercase hex.
   */
  private static String download (URL url, File destination, ProgressListener listener)
    throws IOException
  {
    URLConnection connection = openFollowingRedirects(url);

    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IOException(e);
    }

    long total = connection.getContentLengthLong();
    long done = 0;
    byte[] buffer = new byte[BUFFER_SIZE];

    try (InputStream in = connection.getInputStream();
         OutputStream out = Files.newOutputStream(destination.toPath())) {
      int read;
      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
        digest.update(buffer, 0, read);
        done += read;
        if (listener != null) listener.onDownloadProgress(done, total);
      }
    } finally {
      if (connection instanceof HttpURLConnection) ((HttpURLConnection) connection).disconnect();
    }

    StringBuilder hex = new StringBuilder();
    for (byte b : digest.digest()) hex.append(String.format("%02x", b));
    return hex.toString();
  }

  private static void extractZip (File zip, File destination) throws IOException
  {
    if (!destination.mkdirs()) throw new IOException("Could not create " + destination);

    byte[] buffer = new byte[BUFFER_SIZE];
    try (ZipInputStream in = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zip.toPath()), BUFFER_SIZE))) {
      ZipEntry entry;
      while ((entry = in.getNextEntry()) != null) {
        File file = resolveEntry(destination, entry.getName());
        if (entry.isDirectory()) {
          mkdirs(file);
          continue;
        }

        mkdirs(file.getParentFile());
        try (OutputStream out = Files.newOutputStream(file.toPath())) {
          int read;
          while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
        }
      }
    }
  }

  /**
   * Extracts a gzipped tar archive. Java 8 can't read tar archives, so this handles the bits runtimes need:
   * files, directories and links in the ustar, GNU (long names) and pax formats. File permissions are kept.
   */
  private static void extractTarGz (File archive, File destination) throws IOException
  {
    if (!destination.mkdirs()) throw new IOException("Could not create " + destination);

    byte[] header = new byte[512];
    byte[] buffer = new byte[BUFFER_SIZE];
    String nextName = null;
    String nextLinkName = null;

    try (InputStream in = new BufferedInputStream(new GZIPInputStream(Files.newInputStream(archive.toPath()), BUFFER_SIZE), BUFFER_SIZE)) {
      while (true) {
        // Archives end with empty blocks, but don't insist on them.
        if (!readBlock(in, header) || isEmptyBlock(header)) break;

        String name = tarString(header, 0, 100);
        long mode = tarNumber(header, 100, 8);
        long size = tarNumber(header, 124, 12);
        char type = (char) header[156];
        String linkName = tarString(header, 157, 100);

        // POSIX ustar archives may split long names, GNU ones use the same space for other things.
        if (tarString(header, 257, 6).equals("ustar") && header[262] == 0) {
          String prefix = tarString(header, 345, 155);
          if (!prefix.isEmpty()) name = prefix + "/" + name;
        }

        // Entries that only describe the next entry.
        if (type == 'L' || type == 'K' || type == 'x') {
          byte[] data = readData(in, size);
          if (type == 'L') {
            nextName = tarString(data, 0, data.length);
          } else if (type == 'K') {
            nextLinkName = tarString(data, 0, data.length);
          } else {
            Map<String, String> pax = parsePaxHeaders(data);
            if (pax.containsKey("path")) nextName = pax.get("path");
            if (pax.containsKey("linkpath")) nextLinkName = pax.get("linkpath");
          }
          continue;
        }

        if (nextName != null) name = nextName;
        if (nextLinkName != null) linkName = nextLinkName;
        nextName = null;
        nextLinkName = null;

        switch (type) {
          case '0':
          case '\0':
          case '7': {
            File file = resolveEntry(destination, name);
            mkdirs(file.getParentFile());
            try (OutputStream out = Files.newOutputStream(file.toPath())) {
              long remaining = size;
              while (remaining > 0) {
                int read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (read == -1) throw new EOFException("Runtime archive ended unexpectedly");
                out.write(buffer, 0, read);
                remaining -= read;
              }
            }
            skipFully(in, padding(size));
            if ((mode & 0111) != 0) file.setExecutable(true, (mode & 0011) == 0);
            break;
          }
          case '5':
            mkdirs(resolveEntry(destination, name));
            skipFully(in, size + padding(size));
            break;
          case '2':
            createSymbolicLink(destination, name, linkName);
            skipFully(in, size + padding(size));
            break;
          case '1': {
            // Hard link to an earlier entry, a copy does just as well.
            File file = resolveEntry(destination, name);
            File target = resolveEntry(destination, linkName);
            mkdirs(file.getParentFile());
            if (target.isFile()) Files.copy(target.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            skipFully(in, size + padding(size));
            break;
          }
          default:
            // Global pax headers, devices, and anything else a runtime doesn't need.
            skipFully(in, size + padding(size));
            break;
        }
      }
    }
  }

  private static void createSymbolicLink (File destination, String name, String linkName) throws IOException
  {
    // Windows needs special privileges for these, and Windows runtimes don't come as tar archives anyway.
    if (RuntimeLocator.isWindows()) return;

    File link = resolveEntry(destination, name);
    mkdirs(link.getParentFile());

    // Don't let links point outside of the runtime.
    Path linkTarget = Paths.get(linkName);
    Path resolvedTarget = link.toPath().getParent().resolve(linkTarget).normalize();
    if (linkTarget.isAbsolute() || !resolvedTarget.startsWith(destination.toPath().normalize())) {
      throw new IOException("Runtime archive link points outside of the runtime: " + name + " -> " + linkName);
    }

    Files.deleteIfExists(link.toPath());
    Files.createSymbolicLink(link.toPath(), linkTarget);
  }

  /**
   * Where an archive entry goes, making sure it doesn't escape the destination directory.
   */
  static File resolveEntry (File destination, String name) throws IOException
  {
    File file = new File(destination, name);
    String destinationPath = destination.getCanonicalPath() + File.separator;
    String filePath = file.getCanonicalPath();
    if (!(filePath + File.separator).startsWith(destinationPath)) {
      throw new IOException("Archive entry is outside of the target directory: " + name);
    }
    return file;
  }

  static void mkdirs (File dir) throws IOException
  {
    if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("Could not create " + dir);
  }

  /**
   * @return False if the stream ended before the block started.
   */
  private static boolean readBlock (InputStream in, byte[] block) throws IOException
  {
    int done = 0;
    while (done < block.length) {
      int read = in.read(block, done, block.length - done);
      if (read == -1) {
        if (done == 0) return false;
        throw new EOFException("Runtime archive ended unexpectedly");
      }
      done += read;
    }
    return true;
  }

  private static boolean isEmptyBlock (byte[] block)
  {
    for (byte b : block) {
      if (b != 0) return false;
    }
    return true;
  }

  private static byte[] readData (InputStream in, long size) throws IOException
  {
    if (size < 0 || size > 1024 * 1024) throw new IOException("Unexpected runtime archive header size: " + size);
    byte[] data = new byte[(int) size];
    if (size > 0 && !readBlock(in, data)) throw new EOFException("Runtime archive ended unexpectedly");
    skipFully(in, padding(size));
    return data;
  }

  private static void skipFully (InputStream in, long count) throws IOException
  {
    while (count > 0) {
      long skipped = in.skip(count);
      if (skipped <= 0) {
        if (in.read() == -1) throw new EOFException("Runtime archive ended unexpectedly");
        skipped = 1;
      }
      count -= skipped;
    }
  }

  /**
   * Tar entries take up whole blocks of 512 bytes.
   */
  private static long padding (long size)
  {
    return (512 - (size % 512)) % 512;
  }

  private static String tarString (byte[] data, int offset, int length)
  {
    int end = offset;
    while (end < offset + length && data[end] != 0) end++;
    return new String(data, offset, end - offset, StandardCharsets.UTF_8);
  }

  /**
   * Tar numbers are octal text, or big-endian binary for big values (GNU) when the first byte's top bit is set.
   */
  private static long tarNumber (byte[] data, int offset, int length) throws IOException
  {
    if ((data[offset] & 0x80) != 0) {
      long value = data[offset] & 0x7f;
      for (int i = offset + 1; i < offset + length; i++) value = (value << 8) | (data[i] & 0xff);
      return value;
    }

    String text = tarString(data, offset, length).trim();
    if (text.isEmpty()) return 0;
    try {
      return Long.parseLong(text, 8);
    } catch (NumberFormatException e) {
      throw new IOException("Invalid number in runtime archive: " + text);
    }
  }

  /**
   * Pax headers are records like "{length} {key}={value}\n", where the length counts the whole record in bytes.
   */
  private static Map<String, String> parsePaxHeaders (byte[] data) throws IOException
  {
    Map<String, String> headers = new HashMap<>();
    int position = 0;
    while (position < data.length) {
      int space = position;
      while (space < data.length && data[space] != ' ') space++;
      if (space >= data.length) break;

      int length;
      try {
        length = Integer.parseInt(new String(data, position, space - position, StandardCharsets.US_ASCII));
      } catch (NumberFormatException e) {
        throw new IOException("Invalid pax header in runtime archive");
      }
      if (length <= 0 || position + length > data.length) throw new IOException("Invalid pax header in runtime archive");

      // Skip the trailing newline.
      String record = new String(data, space + 1, position + length - space - 2, StandardCharsets.UTF_8);
      int equals = record.indexOf('=');
      if (equals > 0) headers.put(record.substring(0, equals), record.substring(equals + 1));
      position += length;
    }
    return headers;
  }

  /**
   * Runtime archives might have everything inside a top level directory, or be a macOS
   * bundle layout ({@code Contents/Home}), so look around for the actual runtime home.
   */
  private static File findHome (File extracted)
  {
    if (hasJavaExecutable(extracted)) return extracted;

    File macHome = new File(new File(extracted, "Contents"), "Home");
    if (hasJavaExecutable(macHome)) return macHome;

    File[] children = extracted.listFiles(File::isDirectory);
    if (children == null) return null;

    for (File child : children) {
      if (hasJavaExecutable(child)) return child;

      macHome = new File(new File(child, "Contents"), "Home");
      if (hasJavaExecutable(macHome)) return macHome;
    }
    return null;
  }

  private static boolean hasJavaExecutable (File home)
  {
    return RuntimeLocator.getJavaExecutable(home, false).isFile();
  }

  /**
   * Zips don't keep file permissions, so make sure the executable bits are where they're needed.
   * Without jspawnhelper the runtime can't start other processes, like the game.
   */
  private static void markExecutable (File home)
  {
    if (RuntimeLocator.isWindows()) return;

    File[] binaries = new File(home, "bin").listFiles();
    if (binaries != null) {
      for (File binary : binaries) {
        if (binary.isFile()) binary.setExecutable(true, false);
      }
    }

    File lib = new File(home, "lib");
    for (String name : new String[] { "jspawnhelper", "jexec" }) {
      File file = new File(lib, name);
      if (file.isFile()) file.setExecutable(true, false);
    }
  }

  private static void status (ProgressListener listener, String status)
  {
    if (listener != null) listener.onStatus(status);
  }

  /**
   * @return Whether everything got deleted. Also true if there was nothing to delete.
   */
  static boolean deleteRecursively (File file)
  {
    // Links are deleted themselves, never followed.
    if (!file.exists() && !Files.isSymbolicLink(file.toPath())) return true;

    if (file.isDirectory() && !Files.isSymbolicLink(file.toPath())) {
      File[] children = file.listFiles();
      if (children != null) {
        for (File child : children) deleteRecursively(child);
      }
    }
    return file.delete();
  }

  private static final int CONNECT_TIMEOUT_MS = 10000;
  private static final int READ_TIMEOUT_MS = 30000;
  private static final int MAX_REDIRECTS = 5;
  private static final int BUFFER_SIZE = 64 * 1024;
}
