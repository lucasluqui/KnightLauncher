package com.lucasallegri.util;

import org.junit.*;

import static org.junit.Assert.*;

public class JavaUtilTest {

  private final String PATH = System.getProperty("sun.boot.library.path") + "\\java.exe";

  @Test
  public void determineJVMArch() throws Exception {
    assertNotEquals("Cannot determine Java VM architecture", 0, JavaUtil.determineJVMArch(PATH));
  }
}
