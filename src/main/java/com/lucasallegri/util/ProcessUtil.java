package com.lucasallegri.util;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static com.lucasallegri.util.Log.log;

public class ProcessUtil {

  @SuppressWarnings("unused")
  public static void startApplication(String commandLine) {
    try {
      final Process p = Runtime.getRuntime().exec(commandLine);
    } catch (IOException e) {
      log.error(e);
    }
  }

  @SuppressWarnings("unused")
  public static void startApplication(String[] args) {
    try {
      final Process p = Runtime.getRuntime().exec(args);
    } catch (IOException e) {
      log.error(e);
    }
  }

  public static String runAndCapture(String[] command) {
    Process process = null;
    try {
      process = new ProcessBuilder(command).start();
      return IOUtils.toString(process.getErrorStream(), Charset.defaultCharset());
    } catch (IOException e) {
      log.error(e);
    } finally {
      if(process != null) process.destroy();
    }
    return null;
  }

  public static ArrayList<String> runAsBatchAndCapture(String command) {
    FileUtil.createFile("output-capture-aabb.bat");
    FileUtil.writeFile("output-capture-aabb.bat", command + " >output.tmp 2>&1");
    ArrayList<String> output = new ArrayList<>();
    Process process = null;
    try {
      process = new ProcessBuilder("output-capture-aabb.bat").start();
      System.out.println(FileUtil.readFile("output.tmp"));
      String stdout = IOUtils.toString(process.getInputStream(), Charset.defaultCharset());
      System.out.println(stdout);
      InputStream is = process.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));

      String line;
      while ((line = reader.readLine()) != null) {
        output.add(line);
      }
      process.waitFor();
    } catch (IOException | InterruptedException e) {
      log.error(e);
    } finally {
      if(process != null) process.destroy();
      //FileUtil.deleteFile("output-capture-aabb.bat");
      //FileUtil.deleteFile("output.tmp");
    }
    //FileUtil.deleteFile("output-capture-aabb.bat");
    //FileUtil.deleteFile("output.tmp");
    return output;
  }

}
