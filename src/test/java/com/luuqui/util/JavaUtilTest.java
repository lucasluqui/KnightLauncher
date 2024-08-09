package com.luuqui.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class JavaUtilTest {

  private final String PATH = System.getProperty("sun.boot.library.path") + "\\java.exe";

  @Test
  public void getJVMArch() throws Exception {
    assertNotEquals(0, JavaUtil.getJVMArch(PATH), "Cannot determine Java VM architecture");
  }
}
