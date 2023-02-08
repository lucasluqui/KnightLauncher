package com.lucasallegri.bootstrap;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipFile;

/**
 * Bootstraps instead of {@literal com.threerings.projectx.client.ProjectXApp} for loading mods and language packs.
 * <p>
 * Makes sure the "META-INF/MANIFEST.MF" is included in each mod jars,
 * and the main class must be specified.
 *
 * @author Leego Yih
 */
public class ProjectXBootstrap {
  private static final String USER_DIR = System.getProperty("user.dir");
  private static final String CODE_MODS_DIR = USER_DIR + "/code-mods/";
  private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
  private static final String MAIN_CLASS_KEY = "Main-Class:";
  private static final String NAME_KEY = "Name:";

  public static void main(String[] args) throws Exception {
    System.setProperty("com.threerings.io.enumPolicy", "ORDINAL");
    // ak.gm()
    if ((boolean) invokeMethod("com.samskivert.util.ak", "gm", null, new Object[0])) {
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    }
    // X.dM("projectx.log");
    invokeMethod("com.threerings.util.X", "dM", null, new Object[]{"projectx.log"});

    loadJarMods();

    String ticket = null;
    String password;
    for (int i = 0; i < args.length; ++i) {
      if ((password = args[i]).startsWith("+connect=")) {
        ticket = password;
        // com.samskivert.util.c.b(args, i, 1);
        args = (String[]) invokeMethod("com.samskivert.util.c", "b", null, new Object[]{args, i, 1});
        break;
      }
    }
    String username = args.length > 0 ? args[0] : System.getProperty("username");
    password = args.length > 1 ? args[1] : System.getProperty("password");
    boolean encrypted = Boolean.getBoolean("encrypted");
    String knight = args.length > 2 ? args[2] : System.getProperty("knight");
    String action = args.length > 3 ? args[3] : System.getProperty("action");
    String arg = args.length > 4 ? args[4] : System.getProperty("arg");
    String sessionKey = System.getProperty("sessionKey");

    Constructor<?> constructor = Class.forName("com.threerings.projectx.client.ProjectXApp")
        .getDeclaredConstructor(String.class, String.class, boolean.class, String.class, String.class, String.class, String.class, String.class);
    constructor.setAccessible(true);
    Object app = constructor.newInstance(username, password, encrypted, knight, action, arg, sessionKey, ticket);
    invokeMethod("com.threerings.projectx.client.ProjectXApp", "startup", app, new Object[0]);
  }

  static void loadJarMods() {
    // Read disabled jar mods from KnightLauncher.properties
    Set<String> disabledJarMods = new HashSet<>();
    String disabledJarModsString = getConfigValue("modloader.disabledMods");
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

  static void loadJars(List<File> jars) {
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
        System.out.println("Loaded jar '" + jar.getName() + "'");
      } catch (Exception e) {
        System.out.println("Failed to load jar '" + jar.getName() + "'");
        e.printStackTrace();
      }
    }
    method.setAccessible(accessible);
  }

  static void loadClasses(List<File> jars) {
    for (File jar : jars) {
      String manifest = readZip(jar, MANIFEST_PATH);
      if (manifest == null || manifest.length() == 0) {
        System.out.println("Failed to read '" + MANIFEST_PATH + "' from '" + jar.getName() + "'");
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
        System.out.println("Failed to read 'Main-Class' from '" + jar.getName() + "'");
        continue;
      }
      if (modName == null) {
        modName = jar.getName();
      }
      System.out.println("Mod '" + modName + "' initializing");
      try {
        Class.forName(className);
        System.out.println("Mod '" + modName + "' initialized");
      } catch (Exception e) {
        System.out.println("Failed to load mod '" + modName + "'");
        e.printStackTrace();
      }
    }
  }

  static String readZip(File file, String entry) {
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
      System.out.println("Failed to read '" + file.getName() + "'");
      e.printStackTrace();
      return null;
    }
  }

  static String getConfigValue(String key) {
    Properties _prop = new Properties();
    String value;
    try (InputStream is = Files.newInputStream(Paths.get(System.getProperty("user.dir") + File.separator + "KnightLauncher.properties"))) {
      _prop.load(is);
      value = _prop.getProperty(key);
      return value;
    } catch (IOException ignored) {
    }
    return null;
  }

  static Object invokeMethod(String className, String methodName, Object object, Object[] args) throws Exception {
    Class<?> clazz = Class.forName(className);
    Method[] methods = clazz.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      Method method = methods[i];
      if (method.getName().equals(methodName)) {
        method.setAccessible(true);
        return method.invoke(object, args);
      }
    }
    methods = clazz.getMethods();
    for (int i = 0; i < methods.length; i++) {
      Method method = methods[i];
      if (method.getName().equals(methodName)) {
        method.setAccessible(true);
        return method.invoke(object, args);
      }
    }
    throw new NoSuchMethodException(methodName);
  }
}
