package com.lucasallegri.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * @author Leego Yih
 */
public class LauncherDigester {
  public static final String KL_JAR_PATH = "/KnightLauncher.jar";
  public static final String KL_JARV_PATH = "/KnightLauncher.jarv";
  public static final String GETDOWN_PATH = "/getdown.txt";
  public static final String DIGEST_PATH = "/digest.txt";
  public static final String MAGIC_HEAD = "# Customized by KnightLauncher";
  public static final String GETDOWN_PROJECTXAPP_CLASS = "class = com.threerings.projectx.client.ProjectXApp";
  public static final String GETDOWN_PROJECTXAPP_CLIENT_CLASS = "client.class = com.threerings.projectx.client.ProjectXApp";
  public static final String GETDOWN_BOOTSTRAP_CLASS = "class = com.lucasallegri.bootstrap.ProjectXBootstrap";
  public static final String GETDOWN_BOOTSTRAP_CLIENT_CLASS = "client.class = com.lucasallegri.bootstrap.ProjectXBootstrap";
  public static final String GETDOWN_KL_JAR = "code = KnightLauncher.jar";

  public static void doDigest() {
    try {
      // Guarantee that the files exists
      File klJarFile = new File(LauncherGlobals.USER_DIR + KL_JAR_PATH);
      File klJarvFile = new File(LauncherGlobals.USER_DIR + KL_JARV_PATH);
      File getdownFile = new File(LauncherGlobals.USER_DIR + GETDOWN_PATH);
      File digestFile = new File(LauncherGlobals.USER_DIR + DIGEST_PATH);
      String getdownContent = readFile(getdownFile).trim();
      String digestContent = readFile(digestFile).trim();
      // Build a new "getdown.txt" file if it has not been modified by KL
      if (!getdownContent.startsWith(MAGIC_HEAD)) {
        getdownFile.renameTo(new File(getdownFile.getAbsoluteFile() + ".bak"));
        getdownContent = getdownContent
            .replace("\n"+GETDOWN_PROJECTXAPP_CLIENT_CLASS, "\n#" + GETDOWN_PROJECTXAPP_CLIENT_CLASS)
            .replace("\n"+GETDOWN_PROJECTXAPP_CLASS, "\n#" + GETDOWN_PROJECTXAPP_CLASS);
        StringBuilder sb = new StringBuilder()
            .append(MAGIC_HEAD).append("\n")
            .append(getdownContent).append("\n\n")
            .append("# KnightLauncher resources").append("\n")
            .append(GETDOWN_KL_JAR).append("\n")
            .append(GETDOWN_BOOTSTRAP_CLASS).append("\n")
            .append(GETDOWN_BOOTSTRAP_CLIENT_CLASS).append("\n");
        writeFile(getdownFile, sb.toString());
      }
      // For Windows
      if (!klJarvFile.exists()) {
        klJarvFile.createNewFile();
      }
      // Calculate the MD5 of the files
      String klMD5 = md5(klJarFile.getAbsolutePath());
      String getdownMD5 = md5(getdownFile.getAbsolutePath());
      // Build a new "digest.txt" file from the original one
      digestContent = digestContent.trim();
      digestContent = digestContent.replaceFirst("digest\\.txt = \\S+", "");
      digestContent = digestContent.replaceFirst("getdown\\.txt = \\S+\n", "getdown.txt = " + getdownMD5 + "\n");
      if (digestContent.indexOf("KnightLauncher.jar") > 0) {
        digestContent = digestContent.replaceFirst("KnightLauncher\\.jar = \\S+\n", "KnightLauncher.jar = " + klMD5 + "\n");
      } else {
        digestContent = digestContent + "KnightLauncher.jar = " + klMD5 + "\n";
      }
      // Append the final MD5 to the end
      String digestMD5 = md5(digestContent.getBytes("UTF-8"));
      digestContent = digestContent + "digest.txt = " + digestMD5 + "\n";
      writeFile(digestFile, digestContent);
      Log.log.info(String.format("\nKnightLauncher.jar: %s\ngetdown.txt: %s\ndigest.txt: %s\n", klMD5, getdownMD5, digestMD5));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static String readFile(File file) throws IOException {
    StringBuilder sb = new StringBuilder();
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String s;
    while ((s = reader.readLine()) != null) {
      sb.append(s).append("\n");
    }
    reader.close();
    return sb.toString();
  }

  static void writeFile(File file, String s) throws IOException {
    FileWriter writer = new FileWriter(file);
    writer.write(s);
    writer.flush();
    writer.close();
  }

  static String md5(String path) throws Exception {
    File file = new File(path);
    byte[] data = new byte[(int) file.length()];
    FileInputStream fis = new FileInputStream(file);
    fis.read(data);
    fis.close();
    return md5(data);
  }

  static String md5(byte[] data) throws Exception {
    byte[] hash = MessageDigest.getInstance("MD5").digest(data);
    return encodeHex(hash);
  }

  /** Table for byte to hex string translation. */
  static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
  /** Table for HEX to DEC byte translation. */
  static final int[] DEC = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15};

  static int getDec(int index) {
    // Fast for correct values, slower for incorrect ones
    try {
      return DEC[index - 48];
    } catch (ArrayIndexOutOfBoundsException e) {
      return -1;
    }
  }

  static byte getHex(int index) {
    return (byte) HEX[index];
  }

  static String encodeHex(byte[] bytes) {
    if (null == bytes) {
      return null;
    }
    int i = 0;
    char[] chars = new char[bytes.length << 1];
    for (byte b : bytes) {
      chars[i++] = HEX[(b & 0xf0) >> 4];
      chars[i++] = HEX[b & 0x0f];
    }
    return new String(chars);
  }

  static byte[] decodeHex(String input) {
    if (input == null) {
      return null;
    }
    if ((input.length() & 1) == 1) {
      // Odd number of characters
      throw new IllegalArgumentException("Odd digits");
    }
    char[] inputChars = input.toCharArray();
    byte[] result = new byte[input.length() >> 1];
    for (int i = 0; i < result.length; i++) {
      int upperNibble = getDec(inputChars[2 * i]);
      int lowerNibble = getDec(inputChars[2 * i + 1]);
      if (upperNibble < 0 || lowerNibble < 0) {
        // Non hex character
        throw new IllegalArgumentException("Non hex");
      }
      result[i] = (byte) ((upperNibble << 4) + lowerNibble);
    }
    return result;
  }
}
