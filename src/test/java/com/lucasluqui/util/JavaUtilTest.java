package com.lucasluqui.util;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class JavaUtilTest
{
  private final String PATH = System.getProperty("sun.boot.library.path");

  @Test
  public void getJVMArch ()
      throws Exception
  {
    String execPath = PATH + File.separator;
    if (SystemUtil.isWindows()) {
      execPath += "java.exe";
    } else if (SystemUtil.isMac()) {
      execPath += "../bin/java";
    } else {
      execPath += "java";
    }

    assertNotEquals(0, JavaUtil.getJVMArch(execPath), "Cannot determine Java VM architecture");
  }
}
