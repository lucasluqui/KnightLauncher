package com.luuqui.launcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.luuqui.launcher.setting.SettingsManager;
import com.luuqui.util.Compressor;
import com.luuqui.util.FileUtil;
import com.luuqui.util.JavaUtil;
import com.luuqui.util.SystemUtil;

import java.io.IOException;

import static com.luuqui.launcher.Log.log;

@Singleton
public class ModuleManager
{
  @Inject protected SettingsManager _settingsManager;

  public ModuleManager ()
  {

  }

  public void init ()
  {

  }

  public void loadModules ()
  {
    Thread moduleThread = new Thread(() -> {
      loadIngameRPC();
      loadJarCommandLine();
    });
    moduleThread.start();
  }

  protected void loadIngameRPC ()
  {
    if (SystemUtil.isWindows() && SystemUtil.is64Bit()) {
      try {
        FileUtil.extractFileWithinJar("/modules/skdiscordrpc/bundle.zip", LauncherGlobals.USER_DIR + "\\KnightLauncher\\modules\\skdiscordrpc\\bundle.zip");
        Compressor.unzip(LauncherGlobals.USER_DIR + "\\KnightLauncher\\modules\\skdiscordrpc\\bundle.zip", LauncherGlobals.USER_DIR + "\\KnightLauncher\\modules\\skdiscordrpc\\", false);
        FileUtil.deleteFile(LauncherGlobals.USER_DIR + "\\KnightLauncher\\modules\\skdiscordrpc\\bundle.zip");
        _settingsManager.setValue("launcher.ingameRPCSetup", "true");
      } catch (IOException e) {
        log.error(e);
      }
    } else {
      _settingsManager.setValue("launcher.ingameRPCSetup", "true");
      _settingsManager.setValue("launcher.useIngameRPC", "false");
    }
  }

  protected void loadJarCommandLine ()
  {
    try {
      int vmArch = JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath());
      if (SystemUtil.isWindows() && !FileUtil.fileExists(JavaUtil.getGameJVMDirPath() + "/bin/jar.exe")) {
        FileUtil.extractFileWithinJar("/modules/jarcmd/jar-" + vmArch + ".exe", JavaUtil.getGameJVMDirPath() + "/bin/jar.exe");
      } else if (!FileUtil.fileExists(JavaUtil.getGameJVMDirPath() + "/bin/jar")) {
        FileUtil.extractFileWithinJar(vmArch == 64 ? "/modules/jarcmd/jar-amd64" : "/modules/jarcmd/jar-i386", JavaUtil.getGameJVMDirPath() + "/bin/jar");
      }
    } catch (IOException e) {
      log.error(e);
    }
  }

  public void loadSpiralview ()
  {
    try {
      FileUtil.extractFileWithinJar("/modules/spiralview/spiralview.jar",
        LauncherGlobals.USER_DIR + "/KnightLauncher/modules/spiralview/spiralview.jar");
    } catch (IOException e) {
      log.error(e);
    }
  }

}
