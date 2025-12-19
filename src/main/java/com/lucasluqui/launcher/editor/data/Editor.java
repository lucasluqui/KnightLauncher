package com.lucasluqui.launcher.editor.data;

import java.awt.image.BufferedImage;

public class Editor
{
  public String name;
  public BufferedImage splashImage;
  public BufferedImage splashImageUnfocused;
  public BufferedImage currentSplashImage;
  public String className;
  public String classNameThirdParty;
  public String arg;
  public int stallTime;

  public Editor (
    String name,
    BufferedImage splashImage,
    BufferedImage splashImageUnfocused,
    String className,
    String classNameThirdParty,
    String arg,
    int stallTime
  )
  {
    this.name = name;
    this.splashImage = splashImage;
    this.splashImageUnfocused = splashImageUnfocused;
    this.currentSplashImage = splashImageUnfocused;
    this.className = className;
    this.classNameThirdParty = classNameThirdParty;
    this.arg = arg;
    this.stallTime = stallTime;
  }
}
