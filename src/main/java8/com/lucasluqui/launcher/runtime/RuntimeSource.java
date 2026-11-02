package com.lucasluqui.launcher.runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Where the launcher's Java runtime for this platform can be downloaded from.
 * <p>
 * Runtimes live on the CDN under {@code runtime/<platform>/<major version>/}, as {@code runtime.zip} (Windows) or
 * {@code runtime.tar.gz} (everything else), next to a {@code checksum.txt} holding the archive's SHA-256. The URLs stay
 * the same when a runtime gets updated, so the checksum is what tells runtimes apart.
 * <p>
 * Compiled for Java 8 (see {@code src/main/java8}), so it can only use Java 8 APIs.
 */
public final class RuntimeSource
{
  private RuntimeSource (String platform, boolean tar)
  {
    this.platform = platform;
    this.tar = tar;

    String base = URL_RUNTIMES + platform + "/" + RuntimeLocator.getRequiredMajorVersion() + "/";
    try {
      this.archiveUrl = new URL(base + (tar ? "runtime.tar.gz" : "runtime.zip"));
      this.checksumUrl = new URL(base + "checksum.txt");
    } catch (MalformedURLException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * The runtime for the platform we're running on.
   *
   * @return The runtime source, or null if there's no runtime for this platform.
   */
  public static RuntimeSource forCurrentPlatform ()
  {
    if (RuntimeLocator.isWindows()) {
      // Only 64-bit Windows. Windows on ARM runs it through its x64 emulation.
      return RuntimeLocator.isWindows64Bit() ? new RuntimeSource("windows", false) : null;
    }

    if (RuntimeLocator.isMac()) {
      // Only Apple silicon, Intel Macs aren't supported.
      return RuntimeLocator.isAppleSilicon() ? new RuntimeSource("macos", true) : null;
    }

    String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
    if (arch.equals("amd64") || arch.equals("x86_64")) {
      return new RuntimeSource("linux-x64", true);
    }
    if (arch.equals("aarch64") || arch.equals("arm64")) {
      return new RuntimeSource("linux-aarch64", true);
    }
    return null;
  }

  /**
   * Fetches the checksum of the runtime currently available for download.
   *
   * @return Checksum, lowercase hex.
   */
  public String fetchChecksum () throws IOException
  {
    byte[] content;
    try (InputStream in = RuntimeInstaller.openFollowingRedirects(checksumUrl).getInputStream()) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buffer = new byte[256];
      int read;
      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
        if (out.size() > MAX_CHECKSUM_SIZE) throw new IOException("Runtime checksum from " + checksumUrl + " is too big");
      }
      content = out.toByteArray();
    }

    // Might be formatted like sha256sum's output, "<checksum>  <file name>".
    String checksum = new String(content, StandardCharsets.US_ASCII).trim().split("\\s+")[0].toLowerCase(Locale.ROOT);
    if (!checksum.matches("[0-9a-f]{64}")) {
      throw new IOException("Runtime checksum from " + checksumUrl + " isn't a SHA-256");
    }
    return checksum;
  }

  @Override
  public String toString ()
  {
    return "RuntimeSource{platform=" + platform + ", url=" + archiveUrl + "}";
  }

  /** The platform the runtime is for, as it appears in its URL. e.g. "windows" or "linux-x64". */
  public final String platform;

  /** Whether the archive is a .tar.gz rather than a .zip. */
  public final boolean tar;

  /** Where to download the runtime archive from. */
  public final URL archiveUrl;

  /** Where to download the runtime archive's SHA-256 from. */
  public final URL checksumUrl;

  /** Keep in sync with LauncherGlobals.URL_CDN, which can't be used from here. */
  private static final String URL_RUNTIMES = "https://cdn.knightlauncher.com/knightlauncher/v2/runtime/";

  /** Checksum files are tiny, anything bigger isn't one. */
  private static final int MAX_CHECKSUM_SIZE = 1024;
}
