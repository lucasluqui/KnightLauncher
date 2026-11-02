package com.lucasluqui.launcher;

import com.lucasluqui.launcher.runtime.Updater;
import com.lucasluqui.launcher.runtime.RuntimeWindow;
import com.lucasluqui.launcher.runtime.RuntimeLocator;
import com.lucasluqui.launcher.runtime.RuntimeSource;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Launcher's entry point.
 * <p>
 * We will need a recent Java (see the launcher.java.version property in pom.xml), but it's often started with an older
 * one, e.g. the Java 8 installs users get from Java's website, or an older launcher restarting itself after an update.
 * <p>
 * This class is compiled for Java 8 (see {@code src/main/java8}) so it can always run, and if the current Java is too
 * old, restarts the launcher on a suitable one, in this order:
 * <ol>
 *   <li>The launcher's own runtime, under {@code KnightLauncher/runtime}.</li>
 *   <li>The launcher's own runtime, freshly downloaded.</li>
 *   <li>The runtime the game ships with.</li>
 * </ol>
 * If even that leaves us on an old Java, it updates the launcher to its latest release, in case that sorts it out.
 * <p>
 * It must never touch the launcher's other classes directly, as those can't even be loaded on Java 8.
 */
public class Main
{
  public static void main (String[] args) throws Throwable
  {
    // We're the copy of the launcher that the updater (from runtime package, not the ACTUAL updater...)
    // started to update the real one.
    String updateVersion = System.getProperty(Updater.UPDATE_PROPERTY);
    if (updateVersion != null) {
      if (Updater.run(updateVersion, args)) {
        System.exit(0);
        return;
      }
      fail("Knight Launcher couldn't be updated to version " + updateVersion + ".\n\n"
        + "Please download the latest version and install it again.\n"
        + "If this keeps happening, ask for help on our Discord: " + SUPPORT_URL);
      return;
    }

    Updater.cleanUp(getLauncherJar());

    if (RuntimeLocator.getCurrentMajorVersion() >= RuntimeLocator.getRequiredMajorVersion()) {
      startApp(args);
      return;
    }

    log("Running on Java " + System.getProperty("java.version") + ", looking for a Java "
      + RuntimeLocator.getRequiredMajorVersion() + "+ runtime");

    if (Boolean.getBoolean(RELAUNCHED_PROPERTY)) {
      // We already restarted once and still ended up on an old Java. Don't keep going, unless there's
      // a newer launcher out there that might do better.
      if (Updater.start(getLauncherJar(), args)) {
        System.exit(0);
        return;
      }
      fail("Knight Launcher was restarted on Java " + System.getProperty("java.version")
        + ", but it needs Java " + RuntimeLocator.getRequiredMajorVersion() + " or newer.");
      return;
    }

    // 1. Our own runtime, if it's installed already.
    File installed = RuntimeLocator.getInstalledRuntimeDir();
    if (installed != null && relaunch(installed, args)) {
      System.exit(0);
      return;
    }

    // 2. Our own runtime, downloading it now.
    File downloaded = downloadRuntime();
    if (downloaded != null && relaunch(downloaded, args)) {
      System.exit(0);
      return;
    }

    // 3. The game's runtime. The launcher will try getting its own runtime again in the background.
    File gameRuntime = RuntimeLocator.findGameRuntime();
    if (gameRuntime != null && relaunch(gameRuntime, args)) {
      System.exit(0);
      return;
    }

    fail("Knight Launcher needs Java " + RuntimeLocator.getRequiredMajorVersion() + " to run, but it couldn't be "
      + "downloaded and none was found in your game folder.\n\n"
      + "Check your internet connection and try again.\n"
      + "If this keeps happening, ask for help on our Discord: " + SUPPORT_URL);
  }

  /**
   * Hands over to the launcher itself. Only call this when running on a suitable Java.
   */
  private static void startApp (String[] args) throws Throwable
  {
    Method main = Class.forName(APP_MAIN_CLASS).getMethod("main", String[].class);
    try {
      main.invoke(null, (Object) args);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  private static File downloadRuntime ()
  {
    RuntimeSource source;
    try {
      source = RuntimeSource.forCurrentPlatform();
    } catch (Exception e) {
      log("Couldn't figure out which runtime to download: " + e);
      return null;
    }

    if (source == null) {
      log("No runtime download available for " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
      return null;
    }

    log("Downloading " + source);
    useSystemLookAndFeel();
    return RuntimeWindow.install(source);
  }

  /**
   * Starts the launcher again on the given runtime, with the same arguments.
   *
   * @return Whether it could be started.
   */
  private static boolean relaunch (File javaHome, String[] args)
  {
    File java = RuntimeLocator.getJavaExecutable(javaHome, true);
    if (!java.isFile()) java = RuntimeLocator.getJavaExecutable(javaHome, false);

    List<String> command = new ArrayList<>();
    command.add(java.getAbsolutePath());
    command.add("-D" + RELAUNCHED_PROPERTY + "=true");
    if (Boolean.getBoolean(Updater.UPDATED_PROPERTY)) {
      command.add("-D" + Updater.UPDATED_PROPERTY + "=true");
    }
    command.add("-jar");
    command.add(getLauncherJar().getAbsolutePath());
    command.addAll(Arrays.asList(args));

    log("Restarting on " + javaHome);

    ProcessBuilder pb = new ProcessBuilder(command);
    pb.directory(RuntimeLocator.getRootDir());
    // Inherit our output instead of piping it, as nobody would be reading the pipes once we exit.
    pb.inheritIO();
    // Get rid of _JAVA_OPTIONS, usually just contains garbage.
    pb.environment().remove("_JAVA_OPTIONS");

    try {
      pb.start();
      return true;
    } catch (IOException e) {
      log("Failed to restart on " + javaHome + ": " + e);
      return false;
    }
  }

  /**
   * The jar we're running from. It isn't always KnightLauncher.jar, e.g. the updater runs from a copy.
   */
  private static File getLauncherJar ()
  {
    try {
      File jar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      if (jar.isFile()) return jar;
    } catch (Exception ignored) {
      // Fall back to the usual location below.
    }
    return new File(RuntimeLocator.getRootDir(), "KnightLauncher.jar");
  }

  private static void fail (String message)
  {
    log(message);
    if (!GraphicsEnvironment.isHeadless()) {
      useSystemLookAndFeel();
      JOptionPane.showMessageDialog(null, message, "Knight Launcher", JOptionPane.ERROR_MESSAGE);
    }
    System.exit(1);
  }

  private static void useSystemLookAndFeel ()
  {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ignored) {
      // The default one will do.
    }
  }

  private static void log (String message)
  {
    System.err.println("[Knight Launcher] " + message);
  }

  private static final String APP_MAIN_CLASS = "com.lucasluqui.launcher.LauncherApp";

  /** Set on the restarted launcher so a runtime that turns out to be too old doesn't make us restart forever. */
  private static final String RELAUNCHED_PROPERTY = "knightlauncher.relaunched";

  private static final String SUPPORT_URL = "https://discord.gg/8rE4GeAMza";
}
