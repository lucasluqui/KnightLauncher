package com.lucasluqui.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZipUtilTest
{
  @TempDir
  Path dir;

  private Path jar ()
    throws IOException
  {
    Path jar = dir.resolve("config.jar");
    try (OutputStream out = Files.newOutputStream(jar); ZipOutputStream zip = new ZipOutputStream(out)) {
      zip.putNextEntry(new ZipEntry("deployment.properties"));
      zip.write("from-jar".getBytes());
      zip.closeEntry();
      zip.putNextEntry(new ZipEntry("mod/Foo.class"));
      zip.write("stale-bytecode".getBytes());
      zip.closeEntry();
    }
    return jar;
  }

  /** The class changes of this mount are staged first; the previous config.jar is unpacked over them. */
  private Path staged ()
    throws IOException
  {
    Path target = dir.resolve("class-changes");
    Files.createDirectories(target.resolve("mod"));
    Files.writeString(target.resolve("mod/Foo.class"), "fresh-bytecode");
    return target;
  }

  @Test
  public void unpackJarOverwritesWhatIsAlreadyThere ()
    throws IOException
  {
    Path target = staged();
    try (ZipFile zip = new ZipFile(jar().toFile())) {
      ZipUtil.unpackJar(zip, target.toFile(), false);
    }
    assertEquals("stale-bytecode", Files.readString(target.resolve("mod/Foo.class")));
  }

  @Test
  public void unpackJarFilterKeepsFreshClassesAndStillUnpacksTheRest ()
    throws IOException
  {
    Path target = staged();
    try (ZipFile zip = new ZipFile(jar().toFile())) {
      ZipUtil.unpackJar(zip, target.toFile(), false, name -> !name.endsWith(".class"));
    }
    assertEquals("fresh-bytecode", Files.readString(target.resolve("mod/Foo.class")));
    assertEquals("from-jar", Files.readString(target.resolve("deployment.properties")));
  }

  @Test
  public void unpackJarFilterSkipsClassesThatWereNeverStaged ()
    throws IOException
  {
    Path target = dir.resolve("class-changes");
    Files.createDirectories(target);
    try (ZipFile zip = new ZipFile(jar().toFile())) {
      ZipUtil.unpackJar(zip, target.toFile(), false, name -> !name.endsWith(".class"));
    }
    assertEquals(false, Files.exists(target.resolve("mod/Foo.class")));
  }

  @Test
  public void unpackJarNeverWritesOutsideTheTarget ()
    throws IOException
  {
    Path evil = dir.resolve("evil.jar");
    try (OutputStream out = Files.newOutputStream(evil); ZipOutputStream zip = new ZipOutputStream(out)) {
      for (String name : new String[] {"../escaped.txt", "a/../../escaped2.txt", "/tmp/escaped3.txt", "ok/fine.txt"}) {
        zip.putNextEntry(new ZipEntry(name));
        zip.write("x".getBytes());
        zip.closeEntry();
      }
    }
    Path target = dir.resolve("out");
    Files.createDirectories(target);
    try (ZipFile zip = new ZipFile(evil.toFile())) {
      ZipUtil.unpackJar(zip, target.toFile(), false);
    }
    assertEquals("x", Files.readString(target.resolve("ok/fine.txt")));
    assertEquals(false, Files.exists(dir.resolve("escaped.txt")));
    assertEquals(false, Files.exists(dir.resolve("escaped2.txt")));
    assertEquals(false, Files.exists(Path.of("/tmp/escaped3.txt")));
  }

  @Test
  public void cleaningNeverDeletesOutsideTheTarget ()
    throws IOException
  {
    Path victim = dir.resolve("victim");
    Files.createDirectories(victim);
    Files.writeString(victim.resolve("keep.txt"), "keep");
    Path evil = dir.resolve("evil.jar");
    try (OutputStream out = Files.newOutputStream(evil); ZipOutputStream zip = new ZipOutputStream(out)) {
      zip.putNextEntry(new ZipEntry("../victim/"));
      zip.closeEntry();
    }
    Path target = dir.resolve("out");
    Files.createDirectories(target);
    try (ZipFile zip = new ZipFile(evil.toFile())) {
      ZipUtil.unpackJar(zip, target.toFile(), true);
    }
    assertEquals("keep", Files.readString(victim.resolve("keep.txt")));
  }
}
