package com.luuqui.launcher.editor;

import com.luuqui.dialog.Dialog;
import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.util.JavaUtil;
import com.luuqui.util.ProcessUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

import static com.luuqui.launcher.editor.Log.log;

public class EditorsEventHandler {

  public static boolean isBooting = false;

  private static void startEditor(String editor, String arg) {
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
        Dialog.push("No compatible Java VM to start the editor with.", "Editors", JOptionPane.ERROR_MESSAGE);
      }

      String[] editorCmdLine = new String[] {
        javaVMPath,
        "-classpath",
        LauncherGlobals.USER_DIR + File.separator + "./KnightLauncher/modules/spiralview/spiralview.jar" + libSeparator +
          LauncherGlobals.USER_DIR + File.separator + "./code/projectx-config.jar" + libSeparator +
          LauncherGlobals.USER_DIR + File.separator + "./code/projectx-pcode.jar" + libSeparator +
          LauncherGlobals.USER_DIR + File.separator + "./code/lwjgl_util.jar" + libSeparator +
          LauncherGlobals.USER_DIR + File.separator + "./code/lwjgl.jar",
        "-Xms2G",
        "-Xmx2G",
        "-Dappdir=" + LauncherGlobals.USER_DIR + File.separator + "./",
        "-Dresource_dir=" + LauncherGlobals.USER_DIR + File.separator + "./rsrc",
        "-Djava.library.path=" + LauncherGlobals.USER_DIR + File.separator + "./native",
        editor,
        arg
      };

      ProcessUtil.runFromDirectory(editorCmdLine, LauncherGlobals.USER_DIR, true);
    }
  }

  public static void startModelViewer(ActionEvent actionEvent) {
    EditorsGUI.editorLaunchFakeProgressBar.setMaximum(150);
    startEditor("com.luuqui.spiralview.ModelViewerHook", "rsrc/character/pc/model.dat");
  }

  public static void startSceneEditor(ActionEvent actionEvent) {
    EditorsGUI.editorLaunchFakeProgressBar.setMaximum(155);
    startEditor("com.luuqui.spiralview.SceneEditorHook", "");
  }

  public static void startInterfaceTester(ActionEvent actionEvent) {
    EditorsGUI.editorLaunchFakeProgressBar.setMaximum(110);
    startEditor("com.luuqui.spiralview.InterfaceTesterHook", "");
  }

  public static void startParticleEditor(ActionEvent actionEvent) {
    EditorsGUI.editorLaunchFakeProgressBar.setMaximum(125);
    startEditor("com.luuqui.spiralview.ParticleEditorHook", "");
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

}
