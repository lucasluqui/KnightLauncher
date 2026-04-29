package com.lucasluqui.launcher.editor.ui.handler;

import com.google.inject.Inject;
import com.lucasluqui.dialog.Dialog;
import com.lucasluqui.util.BuildConfig;
import com.lucasluqui.launcher.LauncherGlobals;
import com.lucasluqui.launcher.LocaleManager;
import com.lucasluqui.launcher.ModuleManager;
import com.lucasluqui.launcher.editor.data.Editor;
import com.lucasluqui.launcher.flamingo.FlamingoManager;
import com.lucasluqui.launcher.flamingo.data.Server;
import com.lucasluqui.launcher.editor.ui.EditorsUI;
import com.lucasluqui.util.JavaUtil;
import com.lucasluqui.util.ProcessUtil;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditorsEventHandler
{
  @Inject
  public EditorsEventHandler (LocaleManager localeManager,
                              FlamingoManager flamingoManager,
                              ModuleManager moduleManager)
  {
    this._localeManager = localeManager;
    this._flamingoManager = flamingoManager;
    this._moduleManager = moduleManager;
  }

  public void startEditor (Editor editor)
  {
    this.ui.editorLaunchFakeProgressBar.setMaximum(editor.stallTime);

    if (_flamingoManager.getSelectedServer().isOfficial()) {
      // Any attempts to start the legacy Scene Editor must first show a warning.
      if (editor.name.equalsIgnoreCase("editor_scene_legacy")) {
        boolean confirm = Dialog.pushWithConfirm(_localeManager.getValue("m.scene_editor_legacy_warning"), _localeManager.getValue("t.warning"), JOptionPane.WARNING_MESSAGE);
        if (confirm) {
          startEditor(editor.className, editor.arg, false);
        }
        return;
      }

      // Similar case for the Crucible Editor.
      if (editor.name.equalsIgnoreCase("editor_crucible")) {
        boolean confirm = Dialog.pushWithConfirm(_localeManager.getValue("m.crucible_editor_warning"), _localeManager.getValue("t.warning"), JOptionPane.WARNING_MESSAGE);
        if (confirm) {
          startCrucibleEditor();
        }
        return;
      }

      // Not any of them, move on.
      startEditor(editor.className, editor.arg, false);
    } else {
      // Check if this editor is supported by third parties first.
      if (editor.classNameThirdParty == null) {
        Dialog.push(_localeManager.getValue("error.not_supported"), _localeManager.getValue("t.error"), JOptionPane.ERROR_MESSAGE);
        return;
      }

      // All clear, move on.
      startEditor(editor.classNameThirdParty, editor.arg, true);
    }
  }

  @SuppressWarnings("all")
  private void startEditor (String editorClassName, String arg, boolean thirdparty)
  {
    if (!spiralviewExtracted) {
      _moduleManager.loadSpiralview();
      spiralviewExtracted = true;
    }

    if (!isBooting) {
      isBooting = true;
      new Thread(this::startedBooting).start();

      String libSeparator = JavaUtil.getJavaVMCommandLineSeparator();
      String classpath = "";
      String rootDir = _flamingoManager.getSelectedServer().getRootDirectory();
      if (!thirdparty)
        classpath += rootDir + File.separator + "./KnightLauncher/modules/spiralview/spiralview.jar" + libSeparator;
      if (thirdparty) classpath += LauncherGlobals.USER_DIR + File.separator + "./KnightLauncher.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/projectx-config.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/projectx-pcode.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/lwjgl_util.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/lwjgl.jar";

      List<String> editorCmdLine = new ArrayList<>();
      editorCmdLine.add(JavaUtil.getGameJVMExePath());
      editorCmdLine.add("-classpath");
      editorCmdLine.add(classpath);
      editorCmdLine.add("-Xms2G");
      editorCmdLine.add("-Xmx2G");
      editorCmdLine.add("-Dcom.threerings.getdown=false");
      editorCmdLine.add("-Dorg.lwjgl.util.NoChecks=true");
      editorCmdLine.add("-Dsun.java2d.d3d=false");
      editorCmdLine.add("--add-opens=java.base/java.lang=ALL-UNNAMED");
      editorCmdLine.add("--add-opens=java.base/java.util=ALL-UNNAMED");
      editorCmdLine.add("--enable-native-access=ALL-UNNAMED");
      if (thirdparty) {
        editorCmdLine.add("-XX:SoftRefLRUPolicyMSPerMB=10");
      }
      editorCmdLine.add("-Dappdir=" + rootDir + File.separator + "./");
      editorCmdLine.add("-Dresource_dir=" + rootDir + File.separator + "./rsrc");
      editorCmdLine.add("-Djava.library.path=" + rootDir + File.separator + "./native");
      editorCmdLine.add(editorClassName);
      editorCmdLine.add(arg);

      ProcessUtil.runFromDirectory(editorCmdLine.toArray(new String[editorCmdLine.size()]), rootDir, true);
      initEditorTask();
    }
  }

  private void startCrucibleEditor ()
  {
    if (!isBooting) {
      isBooting = true;
      new Thread(this::startedBooting).start();

      String libSeparator = JavaUtil.getJavaVMCommandLineSeparator();
      String classpath = "";
      String rootDir = LauncherGlobals.USER_DIR;
      classpath += rootDir + File.separator + "./code/config.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/projectx-config.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/projectx-pcode.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/lwjgl.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/lwjgl_util.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/jinput.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/jutils.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/jshortcut.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/commons-beanutils.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/commons-digester.jar" + libSeparator;
      classpath += rootDir + File.separator + "./code/commons-logging.jar" + libSeparator;

      List<String> editorCmdLine = new ArrayList<>();
      editorCmdLine.add(JavaUtil.getGameJVMExePath());
      editorCmdLine.add("-classpath");
      editorCmdLine.add(classpath);
      editorCmdLine.add("-Xms2G");
      editorCmdLine.add("-Xmx2G");
      editorCmdLine.add("-Dcom.threerings.getdown=false");
      editorCmdLine.add("-Dorg.lwjgl.util.NoChecks=true");
      editorCmdLine.add("-Dsun.java2d.d3d=false");
      editorCmdLine.add("--add-opens=java.base/java.lang=ALL-UNNAMED");
      editorCmdLine.add("--add-opens=java.base/java.util=ALL-UNNAMED");
      editorCmdLine.add("--enable-native-access=ALL-UNNAMED");
      editorCmdLine.add("-Dappdir=" + rootDir + File.separator + ".");
      editorCmdLine.add("-Dresource_dir=" + rootDir + File.separator + "./rsrc");
      editorCmdLine.add("-Dcrucible.dir=../crucible");
      editorCmdLine.add("-Djava.library.path=" + rootDir + File.separator + "./native");
      editorCmdLine.add("com.threerings.projectx.tools.CrucibleSceneEditor");

      ProcessUtil.runFromDirectory(editorCmdLine.toArray(new String[0]), rootDir, true);
      initEditorTask();
    }
  }

  protected void startedBooting ()
  {
    isBooting = true;
    this.ui.editorListPaneScroll.setVisible(false);
    this.ui.editorLaunchState.setVisible(true);
    this.ui.editorLaunchFakeProgressBar.setVisible(true);
    this.ui.startFakeProgress();
  }

  public void finishedBooting ()
  {
    isBooting = false;
    this.ui.editorListPaneScroll.setVisible(true);
    this.ui.editorLaunchState.setVisible(false);
    this.ui.editorLaunchFakeProgressBar.setVisible(false);
  }

  private void initEditorTask ()
  {
    // wip for discord integration.
  }

  public void selectedServerChanged ()
  {
    Server server = _flamingoManager.getSelectedServer();
    if (server != null) {
      if (server.isOfficial()) {
        this.ui.footerLabel.setText(_localeManager.getValue("m.powered_by_spiralview", BuildConfig.getSpiralviewVersion()));
      } else {
        this.ui.footerLabel.setText(_localeManager.getValue("m.viewing_editors", server.name));
      }
    }
  }

  @Inject private EditorsUI ui;

  protected LocaleManager _localeManager;
  protected FlamingoManager _flamingoManager;
  protected ModuleManager _moduleManager;

  public List<Editor> editors = new ArrayList<>();

  public boolean isBooting = false;
  public boolean spiralviewExtracted = false;
}
