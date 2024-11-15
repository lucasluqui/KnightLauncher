package com.luuqui.launcher.editor;

import com.luuqui.dialog.Dialog;
import com.luuqui.launcher.LauncherApp;
import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.launcher.Locale;
import com.luuqui.launcher.ModuleLoader;
import com.luuqui.util.JavaUtil;
import com.luuqui.util.ProcessUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.luuqui.launcher.editor.Log.log;

public class EditorsEventHandler {

  public static boolean isBooting = false;
  public static boolean spiralviewExtracted = false;

  private static void startEditor(String editor, String arg, boolean thirdparty) {
    if(!spiralviewExtracted) {
      ModuleLoader.loadSpiralview();
      spiralviewExtracted = true;
    }

    if(!isBooting) {
      isBooting = true;
      new Thread(EditorsEventHandler::startedBooting).start();

      String javaVMPath = JavaUtil.getGameJVMExePath();
      String javaVMVersion = JavaUtil.getGameJVMData();
      String libSeparator = JavaUtil.getJavaVMCommandLineSeparator();

      if(javaVMVersion.contains("1.7") || javaVMVersion.contains("1.8")) {
        log.info("Compatible game Java VM version found: " + javaVMVersion);
      } else if (System.getProperty("java.version").contains("1.7") || System.getProperty("java.version").contains("1.8")) {
        log.warning("Incompatible game Java VM version: " + javaVMVersion + ". Luckily we can rely on system's (" + System.getProperty("java.version") + ")");
        javaVMPath = "java";
      } else {
        Dialog.push(Locale.getValue("error.no_compatible_jvm"), Locale.getValue("b.editors"), JOptionPane.ERROR_MESSAGE);
      }

      String classpath = "";
      String rootDir = LauncherApp.selectedServer.getRootDirectory();
      if(!thirdparty) classpath += rootDir + File.separator + "./KnightLauncher/modules/spiralview/spiralview.jar" + libSeparator;
      if(thirdparty) classpath += LauncherGlobals.USER_DIR + File.separator + "./KnightLauncher.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/projectx-config.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/projectx-pcode.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/lwjgl_util.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/lwjgl.jar";

      List<String> editorCmdLine = new ArrayList<>();
      editorCmdLine.add(javaVMPath);
      editorCmdLine.add("-classpath");
      editorCmdLine.add(classpath);
      editorCmdLine.add("-Xms2G");
      editorCmdLine.add("-Xmx2G");
      if(thirdparty) {
        editorCmdLine.add("-Dcom.threerings.getdown=false");
        editorCmdLine.add("-XX:SoftRefLRUPolicyMSPerMB=10");
        editorCmdLine.add("-Dorg.lwjgl.util.NoChecks=true");
        editorCmdLine.add("-Dsun.java2d.d3d=false");
        editorCmdLine.add("-XX:+AggressiveOpts");
      }
      editorCmdLine.add("-Dappdir=" + rootDir + File.separator + "./");
      editorCmdLine.add("-Dresource_dir=" + rootDir + File.separator + "./rsrc");
      editorCmdLine.add("-Djava.library.path=" + rootDir + File.separator + "./native");
      editorCmdLine.add(editor);
      editorCmdLine.add(arg);

      ProcessUtil.runFromDirectory(editorCmdLine.toArray(new String[editorCmdLine.size()]), rootDir, true);
    }
  }

  public static void startModelViewer(ActionEvent actionEvent) {
    EditorsGUI.editorLaunchFakeProgressBar.setMaximum(150);
    if(LauncherApp.selectedServer.isOfficial()) {
      startEditor("com.luuqui.spiralview.ModelViewerHook", "rsrc/character/pc/model.dat", false);
    } else {
      startEditor("com.threerings.opengl.model.tools.ModelViewer", LauncherApp.selectedServer.getRootDirectory() + "rsrc/character/pc/model.dat", true);
    }
  }

  public static void startSceneEditor(ActionEvent actionEvent) {
    EditorsGUI.editorLaunchFakeProgressBar.setMaximum(155);
    if(LauncherApp.selectedServer.isOfficial()) {
      startEditor("com.luuqui.spiralview.SceneEditorHook", "", false);
    } else {
      startEditor("com.threerings.tudey.tools.SceneEditor", "", true);
    }
  }

  public static void startInterfaceTester(ActionEvent actionEvent) {
    EditorsGUI.editorLaunchFakeProgressBar.setMaximum(110);
    if(LauncherApp.selectedServer.isOfficial()) {
      startEditor("com.luuqui.spiralview.InterfaceTesterHook", "", false);
    } else {
      Dialog.push(Locale.getValue("error.not_supported"), Locale.getValue("t.error"), JOptionPane.ERROR_MESSAGE);
    }
  }

  public static void startParticleEditor(ActionEvent actionEvent) {
    EditorsGUI.editorLaunchFakeProgressBar.setMaximum(125);
    if(LauncherApp.selectedServer.isOfficial()) {
      startEditor("com.luuqui.spiralview.ParticleEditorHook", "", false);
    } else {
      startEditor("com.threerings.opengl.effect.tools.ParticleEditor", "", true);
    }
  }

  protected static void startedBooting() {
    isBooting = true;
    EditorsGUI.editorListPaneScroll.setVisible(false);
    EditorsGUI.editorLaunchState.setVisible(true);
    EditorsGUI.editorLaunchFakeProgressBar.setVisible(true);
    EditorsGUI.startFakeProgress();
  }

  protected static void finishedBooting() {
    isBooting = false;
    EditorsGUI.editorListPaneScroll.setVisible(true);
    EditorsGUI.editorLaunchState.setVisible(false);
    EditorsGUI.editorLaunchFakeProgressBar.setVisible(false);
  }

  public static void selectedServerChanged() {
    if(LauncherApp.selectedServer.isOfficial()) {
      EditorsGUI.footerLabel.setText(Locale.getValue("m.powered_by_spiralview", LauncherGlobals.BUNDLED_SPIRALVIEW_VERSION));
    } else {
      EditorsGUI.footerLabel.setText(Locale.getValue("m.viewing_editors", LauncherApp.selectedServer.name));
    }
  }

}
