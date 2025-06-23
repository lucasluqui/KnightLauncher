package com.luuqui.launcher.editor;

import com.google.inject.Inject;
import com.luuqui.dialog.Dialog;
import com.luuqui.launcher.LauncherApp;
import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.launcher.LocaleManager;
import com.luuqui.launcher.ModuleManager;
import com.luuqui.launcher.flamingo.FlamingoManager;
import com.luuqui.util.JavaUtil;
import com.luuqui.util.ProcessUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.luuqui.launcher.editor.Log.log;

public class EditorsEventHandler
{
  @Inject private EditorsGUI gui;

  protected LocaleManager _localeManager;
  protected FlamingoManager _flamingoManager;
  protected ModuleManager _moduleManager;

  public boolean isBooting = false;
  public boolean spiralviewExtracted = false;

  @Inject
  public EditorsEventHandler (LocaleManager _localeManager,
                              FlamingoManager _flamingoManager,
                              ModuleManager _moduleManager)
  {
    this._localeManager = _localeManager;
    this._flamingoManager = _flamingoManager;
    this._moduleManager = _moduleManager;
  }

  @SuppressWarnings("unused")
  public void startModelViewer (ActionEvent actionEvent)
  {
    this.gui.editorLaunchFakeProgressBar.setMaximum(150);
    if(_flamingoManager.getSelectedServer().isOfficial()) {
      startEditor("com.luuqui.spiralview.ModelViewerHook", "rsrc/character/pc/model.dat", false);
    } else {
      startEditor("com.threerings.opengl.model.tools.ModelViewer", _flamingoManager.getSelectedServer().getRootDirectory() + "rsrc/character/pc/model.dat", true);
    }
  }

  @SuppressWarnings("unused")
  public void startSceneEditor (ActionEvent actionEvent)
  {
    this.gui.editorLaunchFakeProgressBar.setMaximum(155);
    if(_flamingoManager.getSelectedServer().isOfficial()) {
      startEditor("com.luuqui.spiralview.SceneEditorHook", "", false);
    } else {
      startEditor("com.threerings.tudey.tools.SceneEditor", "", true);
    }
  }

  @SuppressWarnings("unused")
  public void startInterfaceTester (ActionEvent actionEvent)
  {
    this.gui.editorLaunchFakeProgressBar.setMaximum(110);
    if(_flamingoManager.getSelectedServer().isOfficial()) {
      startEditor("com.luuqui.spiralview.InterfaceTesterHook", "", false);
    } else {
      Dialog.push(_localeManager.getValue("error.not_supported"), _localeManager.getValue("t.error"), JOptionPane.ERROR_MESSAGE);
    }
  }

  @SuppressWarnings("unused")
  public void startParticleEditor (ActionEvent actionEvent)
  {
    this.gui.editorLaunchFakeProgressBar.setMaximum(125);
    if(_flamingoManager.getSelectedServer().isOfficial()) {
      startEditor("com.luuqui.spiralview.ParticleEditorHook", "", false);
    } else {
      startEditor("com.threerings.opengl.effect.tools.ParticleEditor", "", true);
    }
  }

  @SuppressWarnings("all")
  private void startEditor (String editor, String arg, boolean thirdparty)
  {
    if(!spiralviewExtracted) {
      _moduleManager.loadSpiralview();
      spiralviewExtracted = true;
    }

    if(!isBooting) {
      isBooting = true;
      new Thread(this::startedBooting).start();

      String javaVMPath = JavaUtil.getGameJVMExePath();
      String javaVMVersion = JavaUtil.getGameJVMData();
      String libSeparator = JavaUtil.getJavaVMCommandLineSeparator();

      if(javaVMVersion.contains("1.7") || javaVMVersion.contains("1.8")) {
        log.info("Compatible game Java VM version found: " + javaVMVersion);
      } else if (System.getProperty("java.version").contains("1.7") || System.getProperty("java.version").contains("1.8")) {
        log.warning("Incompatible game Java VM version: " + javaVMVersion + ". Luckily we can rely on system's (" + System.getProperty("java.version") + ")");
        javaVMPath = "java";
      } else {
        Dialog.push(_localeManager.getValue("error.no_compatible_jvm"), _localeManager.getValue("b.editors"), JOptionPane.ERROR_MESSAGE);
      }

      String classpath = "";
      String rootDir = _flamingoManager.getSelectedServer().getRootDirectory();
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

  protected void startedBooting ()
  {
    isBooting = true;
    this.gui.editorListPaneScroll.setVisible(false);
    this.gui.editorLaunchState.setVisible(true);
    this.gui.editorLaunchFakeProgressBar.setVisible(true);
    this.gui.startFakeProgress();
  }

  protected void finishedBooting ()
  {
    isBooting = false;
    this.gui.editorListPaneScroll.setVisible(true);
    this.gui.editorLaunchState.setVisible(false);
    this.gui.editorLaunchFakeProgressBar.setVisible(false);
  }

  public void selectedServerChanged ()
  {
    if(_flamingoManager.getSelectedServer().isOfficial()) {
      this.gui.footerLabel.setText(_localeManager.getValue("m.powered_by_spiralview", LauncherGlobals.BUNDLED_SPIRALVIEW_VERSION));
    } else {
      this.gui.footerLabel.setText(_localeManager.getValue("m.viewing_editors", _flamingoManager.getSelectedServer().name));
    }
  }

}
