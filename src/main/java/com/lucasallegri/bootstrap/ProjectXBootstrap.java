package com.lucasallegri.bootstrap;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
  private static final Properties configs = new Properties();

  public static void main(String[] args) throws Exception {
    System.setProperty("com.threerings.io.enumPolicy", "ORDINAL");
    // ak.gm()
    if ((boolean) invokeMethod("com.samskivert.util.ak", "gm", null, new Object[0])) {
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    }
    // X.dM("projectx.log");
    invokeMethod("com.threerings.util.X", "dM", null, new Object[]{"projectx.log"});

    loadConfigs();

    loadConnectionSettings();

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

  static void loadConfigs() throws Exception {
    InputStream is = Files.newInputStream(Paths.get(USER_DIR + File.separator + "KnightLauncher.properties"));
    configs.load(is);
    is.close();
  }

  static void loadConnectionSettings() throws Exception {
    // com.threerings.projectx.util.DeploymentConfig
    Field configField = Class.forName("com.threerings.projectx.util.a").getDeclaredField("akf");
    configField.setAccessible(true);
    Object config = configField.get(null);
    // com.samskivert.util.Config
    Field propsField = Class.forName("com.samskivert.util.m").getDeclaredField("AQ");
    propsField.setAccessible(true);
    // deployment.properties
    Properties properties = (Properties) propsField.get(config);
    // Replace connection settings
    Map<String, String> mapping = new HashMap<>();
    mapping.put("server_host", "game.endpoint");
    mapping.put("server_ports", "game.port");
    mapping.put("datagram_ports", "game.port");
    mapping.put("key.public", "game.publicKey");
    mapping.put("client_root_url", "game.getdownURL");
    for (Map.Entry<String, String> e : mapping.entrySet()) {
      String newConf = configs.getProperty(e.getValue());
      if (newConf == null) {
        continue;
      }
      newConf = newConf.trim();
      String oldConf = properties.getProperty(e.getKey());
      if (newConf.length() > 0 && !newConf.equals(oldConf)) {
        properties.setProperty(e.getKey(), newConf);
        System.out.println("[deployment.properties] Replace [" + e.getKey() + "] '" + oldConf + "' -> '" + newConf + "'");
      } else {
        System.out.println("[deployment.properties] No change [" + e.getKey() + "] '" + oldConf + "'");
      }
    }
  }

  static void loadJarMods() {
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
      Class<?> clazz = null;
      try {
        clazz = Class.forName(className);
      } catch (Exception e) {
        System.out.println("Failed to load mod '" + modName + "'");
        e.printStackTrace();
      }
      if (clazz != null) {
        try {
          Method method = clazz.getDeclaredMethod("mount");
          method.setAccessible(true);
          method.invoke(null);
        } catch (Exception e) {
          System.out.println("Mod '" + modName + "' does not define `mount` method");
        }
      }
      System.out.println("Mod '" + modName + "' initialized");
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
