package com.lucasallegri.util;

import org.junit.*;

import static org.junit.Assert.*;

public class JavaUtilTest {

  private final String PATH = "\"C:\\Program Files (x86)\\Steam\\steamapps\\common\\Spiral Knights\\java_vm\\bin\\java.exe\"";

  @Test
  public void determineJVMArch() throws Exception {
    int arch = JavaUtil.determineJVMArch(PATH);
    System.out.println(arch);
    assertNotEquals("Cannot determine Java VM architecture", 0, arch);
  }
}
