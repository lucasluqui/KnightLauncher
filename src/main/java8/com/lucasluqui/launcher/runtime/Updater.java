package com.lucasluqui.launcher.runtime;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Updates the launcher to its latest release.
 * <p>
 * Used by {@link com.lucasluqui.launcher.Main} as a last resort when it couldn't get onto a suitable Java.
 * <p>
 * The launcher's jar can't be replaced while we're running from it, so this works like
 * the launcher's regular updater: {@link #start} copies the jar elsewhere and starts it with {@link #UPDATE_PROPERTY}
 * set, then that copy does the actual update through {@link #run} and starts the updated launcher.
 * <p>
 * Compiled for Java 8 (see {@code src/main/java8}), so it can only use Java 8 APIs.
 */
public final class Updater
{
  private Updater ()
  {}

  /**
   * Starts updating the launcher if there's a newer release than this one.
   *
   * @param launcherJar The jar we're running from.
   * @return Whether the update was started, in which case we should exit right away.
   */
  public static boolean start (File launcherJar, String[] args)
  {
    if (Boolean.getBoolean(UPDATED_PROPERTY)) {
      log("Already updated once, not looking for another update");
      return false;
    }

    String currentVersion = RuntimeLocator.getLauncherVersion();
    String latestVersion;
    try {
      latestVersion = fetchLatestVersion();
    } catch (IOException e) {
      log("Couldn't look for launcher updates: " + e);
      return false;
    }

    if (latestVersion == null || latestVersion.equalsIgnoreCase(currentVersion)) {
      log("No launcher update available (current: " + currentVersion + ", latest: " + latestVersion + ")");
      return false;
    }

    log("Launcher update available: " + currentVersion + " -> " + latestVersion);

    File updaterJar = getUpdaterJar();
    try {
      RuntimeInstaller.mkdirs(updaterJar.getParentFile());
      Files.copy(launcherJar.toPath(), updaterJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      log("Couldn't copy " + launcherJar + " to " + updaterJar + ": " + e);
      return false;
    }

    return startJava(updaterJar, "-D" + UPDATE_PROPERTY + "=" + latestVersion, args);
  }

  /**
   * Updates the launcher to the given release, then starts it. Only to be called from the copy {@link #start} made.
   *
   * @return Whether it worked. If it didn't, the launcher was left as it was, or with only some files updated.
   */
  public static boolean run (String version, String[] args)
  {
    JFrame frame = showWindow(version);
    try {
      update(version);
    } catch (IOException e) {
      log("Couldn't update the launcher to " + version + ": " + e);
      return false;
    } finally {
      if (frame != null) SwingUtilities.invokeLater(frame::dispose);
    }

    return startJava(new File(RuntimeLocator.getRootDir(), LAUNCHER_JAR), "-D" + UPDATED_PROPERTY + "=true", args);
  }

  /**
   * Deletes the jar copy left behind by a previous update, unless that's what we're running from.
   */
  public static void cleanUp (File launcherJar)
  {
    File updaterJar = getUpdaterJar();
    if (updaterJar.isFile() && !RuntimeLocator.isInside(launcherJar, updaterJar)) {
      updaterJar.delete();
    }
  }

  private static void update (String version) throws IOException
  {
    File root = RuntimeLocator.getRootDir();
    File launcherDir = RuntimeLocator.getLauncherDir();
    RuntimeInstaller.mkdirs(launcherDir);

    long now = System.currentTimeMillis();
    File archive = new File(launcherDir, "update-" + now + ".zip");
    File tempDir = new File(launcherDir, "update-" + now);

    try {
      URL url = new URL(GITHUB_URL_REPO + "releases/download/" + version + "/KnightLauncher-" + version + ".zip");
      log("Downloading " + url);
      URLConnection connection = RuntimeInstaller.openFollowingRedirects(url);
      try (InputStream in = connection.getInputStream()) {
        Files.copy(in, archive.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }

      // Extract everything before touching the install, so a bad download doesn't leave it half updated.
      List<String> names = extractZip(archive, tempDir);
      if (!names.contains(LAUNCHER_JAR)) {
        throw new IOException("Release " + version + " has no " + LAUNCHER_JAR);
      }

      for (String name : names) {
        File target = RuntimeInstaller.resolveEntry(root, name);
        RuntimeInstaller.mkdirs(target.getParentFile());
        RuntimeInstaller.moveReplacing(new File(tempDir, name), target);
      }
      log("Updated the launcher to " + version);
    } finally {
      RuntimeInstaller.deleteRecursively(archive);
      RuntimeInstaller.deleteRecursively(tempDir);
    }
  }

  /**
   * @return The names of the files extracted, relative to the destination.
   */
  private static List<String> extractZip (File zip, File destination) throws IOException
  {
    RuntimeInstaller.mkdirs(destination);

    List<String> names = new ArrayList<>();
    byte[] buffer = new byte[64 * 1024];
    try (ZipInputStream in = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zip.toPath())))) {
      ZipEntry entry;
      while ((entry = in.getNextEntry()) != null) {
        if (entry.isDirectory()) continue;

        File file = RuntimeInstaller.resolveEntry(destination, entry.getName());
        RuntimeInstaller.mkdirs(file.getParentFile());
        try (OutputStream out = Files.newOutputStream(file.toPath())) {
          int read;
          while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
        }
        names.add(entry.getName().replace('\\', '/'));
      }
    }
    return names;
  }

  /**
   * @return The tag of the latest release, or null if there's none.
   */
  private static String fetchLatestVersion () throws IOException
  {
    // This endpoint already leaves out drafts and pre-releases.
    URLConnection connection = RuntimeInstaller.openFollowingRedirects(new URL(GITHUB_API_LATEST_RELEASE));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (InputStream in = connection.getInputStream()) {
      byte[] buffer = new byte[8192];
      int read;
      while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
    }

    // No JSON library to rely on this early, the tag is all we need anyway.
    Matcher matcher = TAG_NAME.matcher(new String(out.toByteArray(), StandardCharsets.UTF_8));
    return matcher.find() ? matcher.group(1) : null;
  }

  /**
   * Starts a jar on the Java we're running on right now, with the same arguments.
   */
  private static boolean startJava (File jar, String property, String[] args)
  {
    File javaHome = RuntimeLocator.getCurrentJavaHome();
    File java = RuntimeLocator.getJavaExecutable(javaHome, true);
    if (!java.isFile()) java = RuntimeLocator.getJavaExecutable(javaHome, false);

    List<String> command = new ArrayList<>();
    command.add(java.getAbsolutePath());
    command.add(property);
    command.add("-jar");
    command.add(jar.getAbsolutePath());
    command.addAll(Arrays.asList(args));

    ProcessBuilder pb = new ProcessBuilder(command);
    pb.directory(RuntimeLocator.getRootDir());
    pb.inheritIO();
    pb.environment().remove("_JAVA_OPTIONS");

    try {
      pb.start();
      return true;
    } catch (IOException e) {
      log("Failed to start " + jar + ": " + e);
      return false;
    }
  }

  private static JFrame showWindow (String version)
  {
    if (GraphicsEnvironment.isHeadless()) return null;

    JFrame frame = new JFrame("Knight Launcher");
    SwingUtilities.invokeLater(() -> {
      JProgressBar progressBar = new JProgressBar();
      progressBar.setIndeterminate(true);

      JPanel content = new JPanel(new BorderLayout(0, 10));
      content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
      content.add(new JLabel("Updating Knight Launcher to " + version + "..."), BorderLayout.NORTH);
      content.add(progressBar, BorderLayout.CENTER);

      frame.setContentPane(content);
      // The window can't be closed, it goes away by itself once done.
      frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      frame.setResizable(false);
      frame.setSize(420, 110);
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
    });
    return frame;
  }

  private static File getUpdaterJar ()
  {
    return new File(RuntimeLocator.getLauncherDir(), "self-updater.jar");
  }

  private static void log (String message)
  {
    System.err.println("[Knight Launcher] " + message);
  }

  private static final String LAUNCHER_JAR = "KnightLauncher.jar";

  private static final String GITHUB_URL_REPO = "https://github.com/lucasluqui/KnightLauncher/";
  private static final String GITHUB_API_LATEST_RELEASE =
    "https://api.github.com/repos/lucasluqui/KnightLauncher/releases/latest";

  private static final Pattern TAG_NAME = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");

  /** Set on the process doing the update, holds the release to update to. */
  public static final String UPDATE_PROPERTY = "knightlauncher.self_update";

  /**
   * Set on the launcher started after an update, and passed along when it restarts itself, so a release that
   * doesn't fix things doesn't make us update over and over.
   */
  public static final String UPDATED_PROPERTY = "knightlauncher.self_updated";
}
