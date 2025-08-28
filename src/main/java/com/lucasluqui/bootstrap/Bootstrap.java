package com.lucasluqui.bootstrap;

import com.lucasluqui.launcher.LauncherGlobals;
import com.lucasluqui.util.FileUtil;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

/**
 * Bootstraps instead of {@literal com.threerings.projectx.client.ProjectXApp} for loading mods and language packs.
 * <p>
 * Makes sure the "META-INF/MANIFEST.MF" is included in each code mod,
 * and the main class must be specified.
 *
 * @author Leego Yih
 */
@SuppressWarnings("all")
public class Bootstrap
{
  private static final String USER_DIR = System.getProperty("user.dir");
  private static final String CODE_MODS_DIR = USER_DIR + "/mods/";
  private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
  private static final String MAIN_CLASS_KEY = "Main-Class:";
  private static final String NAME_KEY = "Name:";
  private static final Properties configs = new Properties();
  private static final Logger logger = Logger.getLogger(Bootstrap.class.getName());

  static {
    try {
      FileHandler fileHandler = new FileHandler(USER_DIR + File.separator + "bootstrap.log");
      fileHandler.setFormatter(new LogFormatter());
      for (Handler handler : logger.getHandlers()) {
        logger.removeHandler(handler);
      }
      logger.addHandler(fileHandler);
      logger.setLevel(Level.ALL);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main (String[] args) throws Exception
  {
    loadJarMods();
    checkSteamAppIdFile();
    com.threerings.projectx.client.ProjectXApp.main(args);
  }

  static void loadJarMods ()
  {
    // Read disabled jar mods from KnightLauncher.properties
    Set<String> disabledJarMods = new HashSet<>();
    String disabledJarModsString = configs.getProperty("modloader.disabledMods");
    if (disabledJarModsString != null && disabledJarModsString.length() > 0) {
      for (String disabledJarMod : disabledJarModsString.split(",")) {
        disabledJarMod = disabledJarMod.trim();
        if (disabledJarMod.length() > 0) {
          disabledJarMods.add(disabledJarMod);
        }
      }
    }
    // Obtain the mod files in the "/code-mods/" directory
    File codeModsDir = new File(CODE_MODS_DIR);
    if (!codeModsDir.exists()) {
      return;
    }
    File[] files = codeModsDir.listFiles();
    if (files == null || files.length == 0) {
      return;
    }
    List<File> jars = new ArrayList<File>(files.length);
    for (File file : files) {
      String filename = file.getName();
      if (filename.endsWith(".jar")
          && !disabledJarMods.contains(filename)) {
        jars.add(file);
      }
    }
    if (jars.isEmpty()) {
      return;
    }
    loadJars(jars);
    loadClasses(jars);
  }

  static void loadJars (List<File> jars)
  {
    // TODO Compatible with more versions of the JDK
    Method method;
    try {
      method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
    URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    boolean accessible = method.isAccessible();
    method.setAccessible(true);
    for (File jar : jars) {
      try {
        method.invoke(classLoader, jar.toURI().toURL());
        logger.info("Loaded jar '" + jar.getName() + "'");
      } catch (Exception e) {
        logger.warning("Failed to load jar '" + jar.getName() + "'");
        e.printStackTrace();
      }
    }
    method.setAccessible(accessible);
  }

  static void loadClasses (List<File> jars)
  {
    Map<String, Class<?>> classes = new LinkedHashMap<>();
    for (File jar : jars) {
      String manifest = readZip(jar, MANIFEST_PATH);
      if (manifest == null || manifest.length() == 0) {
        logger.warning("Failed to read '" + MANIFEST_PATH + "' from '" + jar.getName() + "'");
        continue;
      }
      String className = null;
      String modName = null;
      for (String item : manifest.split("\n")) {
        if (item.startsWith(MAIN_CLASS_KEY)) {
          className = item.replace(MAIN_CLASS_KEY, "").trim();
        } else if (item.startsWith(NAME_KEY)) {
          modName = item.replace(NAME_KEY, "").trim();
        }
      }
      if (className == null || className.length() == 0) {
        logger.warning("Failed to read 'Main-Class' from '" + jar.getName() + "'");
        continue;
      }
      if (modName == null) {
        modName = jar.getName();
      }
      try {
        Class<?> clazz = Class.forName(className);
        classes.put(modName, clazz);
        logger.info("Loaded class '" + className + "' from '" + jar.getName() + "'");
      } catch (Exception e) {
        logger.warning("Failed to load class '" + className + "' from '" + jar.getName() + "'");
        e.printStackTrace();
      }
    }
    if (!classes.isEmpty()) {
      mountMods(classes);
    }
  }

  static void mountMods (Map<String, Class<?>> classes)
  {
    for (Map.Entry<String, Class<?>> entry : classes.entrySet()) {
      String modName = entry.getKey();
      Class<?> clazz = entry.getValue();
      try {
        logger.info("Mounting mod '" + modName + "'");
        Method method = clazz.getDeclaredMethod("mount");
        method.setAccessible(true);
        method.invoke(null);
        logger.info("Mounted mod '" + modName + "'");
      } catch (NoSuchMethodException e) {
        logger.warning("Failed to mount mod '" + modName + "', it does not define `mount` method");
      } catch (IllegalAccessException | InvocationTargetException e) {
        logger.warning("Failed to mount mod '" + modName + "': " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  static String readZip (File file, String entry)
  {
    StringBuilder sb = new StringBuilder();
    try {
      ZipFile zip = new ZipFile(file);
      InputStream is = zip.getInputStream(zip.getEntry(entry));
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      String s;
      while ((s = reader.readLine()) != null) {
        sb.append(s).append("\n");
      }
      reader.close();
      zip.close();
      return sb.toString();
    } catch (Exception e) {
      logger.warning("Failed to read '" + file.getName() + "'");
      e.printStackTrace();
      return null;
    }
  }

  static void checkSteamAppIdFile ()
  {
    String steamAppIdFilePath = LauncherGlobals.USER_DIR + File.separator + "steam_appid.txt";

    if (!FileUtil.fileExists(steamAppIdFilePath)) {
      // File doesn't exist and we're running on a steam path.
      if (LauncherGlobals.USER_DIR.contains("steamapps")) {
        try {
          FileUtils.writeStringToFile(
              new File(steamAppIdFilePath), "99900");
        } catch (IOException e) {
          logger.warning("Failed to create steam_appid.txt file for steam install");
          e.printStackTrace();
        }
      }
    } else {
      // File exists and we're running outside of a steam path.
      if (!LauncherGlobals.USER_DIR.contains("steamapps")) {
        FileUtil.deleteFile(steamAppIdFilePath);
      }
    }
  }

  static class LogFormatter extends Formatter
  {
    private static final String format = "%1$tY/%1$tm/%1$td/%1$tH:%1$tM:%1$tS %2$s[%4$s]\t%5$s%6$s%n";

    private final Date dat = new Date();

    public synchronized String format (LogRecord record)
    {
      dat.setTime(record.getMillis());
      String source;
      if (record.getSourceClassName() != null) {
        source = record.getSourceClassName();
        if (record.getSourceMethodName() != null) {
          source += " " + record.getSourceMethodName();
        }
      } else {
        source = record.getLoggerName();
      }
      String message = formatMessage(record);
      String throwable = "";
      if (record.getThrown() != null) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        record.getThrown().printStackTrace(pw);
        pw.close();
        throwable = sw.toString();
      }
      return String.format(format,
          dat,
          source,
          record.getLoggerName(),
          //record.getLevel().getLocalizedLevelName(),
          record.getLevel().getName(),
          message,
          throwable);
    }
  }

}
