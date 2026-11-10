package com.lucasluqui.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class FileUtilTest
{
  @TempDir
  Path dir;

  @Test
  public void replaceFileSwapsInTheReplacementAndKeepsNoLeftovers ()
    throws IOException
  {
    Path target = dir.resolve("config.jar");
    Path replacement = dir.resolve("config-new.jar");
    Files.writeString(target, "old");
    Files.writeString(replacement, "new");

    FileUtil.replaceFile(replacement, target);

    assertEquals("new", Files.readString(target));
    assertFalse(Files.exists(replacement));
    assertEquals(1L, Files.list(dir).count());
  }

  @Test
  public void replaceFileWorksWhenThereIsNothingToReplace ()
    throws IOException
  {
    Path target = dir.resolve("config.jar");
    Path replacement = dir.resolve("config-new.jar");
    Files.writeString(replacement, "new");

    FileUtil.replaceFile(replacement, target);

    assertEquals("new", Files.readString(target));
  }
}
