package com.lucasallegri.util;

import java.util.ArrayList;

public class JavaUtil {

  public static int determineJVMArch(String path) {
    String output = ProcessUtil.runAndCapture(new String[]{"cmd.exe", "/C", path, "-version"});

    // We got no output, so we can't do any checks.
    if(output == null) return 0;

    // Matches a 64-bit '-version' output.
    if(output.contains("64-Bit Server VM")) return 64;

    // No results matched. We assume it's 32-bit.
    return 32;
  }

}
