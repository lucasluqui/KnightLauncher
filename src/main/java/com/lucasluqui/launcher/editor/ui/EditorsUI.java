package com.lucasluqui.launcher.editor.ui;

import com.lucasluqui.swing.Scale;
import com.formdev.flatlaf.FlatClientProperties;
import com.google.inject.Inject;
import com.lucasluqui.launcher.ui.BaseUI;
import com.lucasluqui.launcher.editor.ui.handler.EditorsEventHandler;
import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.Fonts;
import com.lucasluqui.launcher.LocaleManager;
import com.lucasluqui.launcher.editor.data.Editor;
import com.lucasluqui.launcher.setting.Settings;
import com.lucasluqui.swing.SmoothProgressBar;
import com.lucasluqui.swing.SmoothScrollPane;
import com.lucasluqui.util.BuildConfig;
import com.lucasluqui.util.ImageUtil;
import com.lucasluqui.swing.LoadingIcon;
import com.lucasluqui.swing.SlimProgressBarUI;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static com.lucasluqui.launcher.editor.ui.Log.log;

public class EditorsUI extends BaseUI
{
  @Inject
  public EditorsUI ()
  {
    super(385, 460, false);
  }

  public void init ()
  {
    super.init();
    compose();
    initFinished();
  }

  public void initFinished ()
  {
    super.initFinished();
    setupEditors();
  }

  @Override
  public void loadOnline ()
  {

  }

  private void compose ()
  {
    guiFrame.setVisible(false);
    guiFrame.setTitle(_localeManager.getValue("t.editors"));
    guiFrame.setBounds(Scale.rect(100, 100, this.width, this.height));
    guiFrame.setResizable(false);
    guiFrame.setUndecorated(true);
    guiFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    guiFrame.getContentPane().setLayout(null);
    panel = (JPanel) guiFrame.getContentPane();

    editorLaunchState = new JLabel(_localeManager.getValue("m.editor_loading"));
    editorLaunchState.setIcon(new LoadingIcon());
    editorLaunchState.setHorizontalAlignment(SwingConstants.CENTER);
    editorLaunchState.setBounds(Scale.rect(192, 190, 385, 25));
    editorLaunchState.setFont(Fonts.getFont("default", 14.0f, Font.ITALIC));
    editorLaunchState.setVisible(false);
    guiFrame.getContentPane().add(editorLaunchState);

    // Bounds include the room the bar's glow needs around it.
    editorLaunchFakeProgressBar = new SmoothProgressBar();
    editorLaunchFakeProgressBar.setUI(new SlimProgressBarUI(CustomColors.LAUNCH));
    editorLaunchFakeProgressBar.setOpaque(false);
    editorLaunchFakeProgressBar.setBorder(null);
    editorLaunchFakeProgressBar.setBounds(Scale.rect(192 - SlimProgressBarUI.GLOW_MARGIN, 224, 385 + SlimProgressBarUI.GLOW_MARGIN * 2, 16));
    editorLaunchFakeProgressBar.setVisible(false);
    editorLaunchFakeProgressBar.setShouldPaintString(false);
    guiFrame.getContentPane().add(editorLaunchFakeProgressBar);

    editorListPane = new JPanel();
    editorListPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    GridLayout layout = new GridLayout(4, 1);
    layout.setVgap(Scale.px(10));
    editorListPane.setLayout(layout);
    editorListPane.setPreferredSize(Scale.dim(740, 440));
    // Keeps the cards clear of the scroll bar when there's one.
    editorListPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, Scale.px(20)));

    editorListPaneScroll = new SmoothScrollPane(editorListPane);
    editorListPaneScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    editorListPaneScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    editorListPaneScroll.setBounds(Scale.rect(30, 0, 760, 444));
    editorListPaneScroll.setBorder(null);
    editorListPaneScroll.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    editorListPaneScroll.setForeground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    guiFrame.getContentPane().add(editorListPaneScroll);

    footerLabel = new JLabel();
    footerLabel.setBounds(Scale.rect(30, 449, 740, 15));
    footerLabel.setFont(Fonts.getFont("default", 11.0f, Font.ITALIC));
    footerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    footerLabel.setForeground(CustomColors.INTERFACE_MAINPANE_FOOTNOTE);
    footerLabel.setVisible(false);
    guiFrame.getContentPane().add(footerLabel);
  }

  public void selectedServerChanged ()
  {
    this.eventHandler.selectedServerChanged();
  }

  @SuppressWarnings("all")
  public void startFakeProgress ()
  {
    this.editorLaunchState.setText(_localeManager.getValue("m.editor_loading"));
    for (int i = editorLaunchFakeProgressBar.getMinimum(); i <= editorLaunchFakeProgressBar.getMaximum(); i++) {
      final int percent = i;
      SwingUtilities.invokeLater(() -> editorLaunchFakeProgressBar.setValue(percent));
      rotateFakeLabel(i);
      try {
        Thread.sleep(25);
      } catch (InterruptedException e) {
        log.error(e);
      }
    }
    this.eventHandler.finishedBooting();
  }

  protected void rotateFakeLabel (int pot)
  {
    switch (pot) {
      case 30:
        this.editorLaunchState.setText(_localeManager.getValue("m.editor_fake_loading_1"));
        break;
      case 75:
        this.editorLaunchState.setText(_localeManager.getValue("m.editor_fake_loading_2"));
        break;
      case 100:
        this.editorLaunchState.setText(_localeManager.getValue("m.editor_fake_loading_3"));
        break;
    }
  }

  protected void setupEditors ()
  {
    BufferedImage sceneEditorImage = null;
    if (Settings.showLegacySceneEditor) {
      sceneEditorImage = loadSplashImage("/rsrc/img/editor-scene.png");
    }
    BufferedImage crucibleEditorImage = loadSplashImage("/rsrc/img/editor-crucible.png");
    BufferedImage modelViewerImage = loadSplashImage("/rsrc/img/editor-model.png");
    BufferedImage silverweaveImage = loadSplashImage("/rsrc/img/editor-silverweave.png");
    BufferedImage defaultEditorImage = loadSplashImage("/rsrc/img/editor-default.png");

    List<Editor> editors = new ArrayList<>();

    // wip stuff.
    if (BuildConfig.isSnapshot()) {
      editors.add(new Editor(
          "editor_silverweave",
          silverweaveImage,
          "com.lucasluqui.silverweave.SilverweaveTool",
          "",
          "",
          120
        )
      );
    }

    editors.add(new Editor(
      "editor_crucible",
      crucibleEditorImage,
      "",
      null,
      "",
      120
      )
    );

    if (Settings.showLegacySceneEditor) {
      editors.add(new Editor(
          "editor_scene_legacy",
          sceneEditorImage,
          "com.lucasluqui.spiralview.SceneEditorHook",
          "com.threerings.tudey.tools.SceneEditor",
          "",
          120
        )
      );
    }

    editors.add(new Editor(
        "editor_model",
        modelViewerImage,
        "com.lucasluqui.spiralview.ModelViewerHook",
        "com.threerings.opengl.model.tools.ModelViewer",
        "rsrc/character/pc/model.dat",
        120
      )
    );

    editors.add(new Editor(
        "editor_interface",
        defaultEditorImage,
        "com.lucasluqui.spiralview.InterfaceTesterHook",
        null,
        "",
        110
      )
    );

    editors.add(new Editor(
        "editor_particle",
        defaultEditorImage,
        "com.lucasluqui.spiralview.ParticleEditorHook",
        "com.threerings.opengl.effect.tools.ParticleEditor",
        "",
        125
      )
    );

    eventHandler.editors = editors;

    // Two columns, filling up rows from left to right. With an odd amount, the last one takes up
    // the whole row instead of leaving a gap next to it.
    int count = eventHandler.editors.size();
    int rows = (count + EDITOR_COLUMNS - 1) / EDITOR_COLUMNS;
    int height = rows * (EDITOR_CARD_HEIGHT + EDITOR_CARD_GAP) - EDITOR_CARD_GAP;
    editorListPane.setPreferredSize(Scale.dim(740, height));
    editorListPane.setLayout(new GridBagLayout());

    editorCards.clear();
    for (int i = 0; i < count; i++) {
      Editor editor = eventHandler.editors.get(i);

      // Not every editor has a description.
      String descriptionKey = "m." + editor.name + "_description";
      String description = _localeManager.getValue(descriptionKey);
      if (description.equals(descriptionKey)) description = null;

      // The legacy Scene Editor goes by its plain name, with a badge to set it apart.
      boolean legacy = editor.name.equals("editor_scene_legacy");
      String title = _localeManager.getValue(legacy ? "m.editor_scene" : "m." + editor.name);
      String badge = legacy ? _localeManager.getValue("m.editor_legacy") : null;

      EditorCard card = new EditorCard(title, badge, description,
        editor.splashImage, () -> eventHandler.startEditor(editor));
      editorCards.add(card);

      // Cards have no size of their own, the equal weights split the space evenly between them.
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.gridx = i % EDITOR_COLUMNS;
      constraints.gridy = i / EDITOR_COLUMNS;
      constraints.gridwidth = i == count - 1 ? EDITOR_COLUMNS - constraints.gridx : 1;
      constraints.weightx = 1;
      constraints.weighty = 1;
      constraints.fill = GridBagConstraints.BOTH;
      constraints.insets = new Insets(constraints.gridy > 0 ? Scale.px(EDITOR_CARD_GAP) : 0,
        constraints.gridx > 0 ? Scale.px(EDITOR_CARD_GAP) : 0, 0, 0);
      editorListPane.add(card, constraints);
    }
    updateEditorAvailability();

    editorListPaneScroll.setBounds(
      editorListPaneScroll.getX(),
      editorListPaneScroll.getY(),
      editorListPaneScroll.getWidth(),
      Scale.px(Math.min(440, height))
    );

    editorListPane.updateUI();
  }

  /**
   * Flags the editors that can't be launched on the selected server.
   *
   * @param official Whether the selected server is the official one.
   */
  public void setOfficialServerSelected (boolean official)
  {
    this.officialServerSelected = official;
    updateEditorAvailability();
  }

  private void updateEditorAvailability ()
  {
    for (int i = 0; i < editorCards.size(); i++) {
      // Third party servers can only run the editors that have a version for them.
      boolean available = officialServerSelected || eventHandler.editors.get(i).classNameThirdParty != null;
      editorCards.get(i).setUnavailableReason(available ? null : _localeManager.getValue("m.editor_official_only"));
    }
  }

  private static BufferedImage loadSplashImage (String path)
  {
    return ImageUtil.resizeImage(ImageUtil.loadImageWithinJar(path), Scale.px(740), Scale.px(100));
  }

  @Inject public EditorsEventHandler eventHandler;
  @Inject protected LocaleManager _localeManager;

  protected JPanel editorListPane = new JPanel();
  public SmoothScrollPane editorListPaneScroll = new SmoothScrollPane();
  public JLabel editorLaunchState;
  public SmoothProgressBar editorLaunchFakeProgressBar;
  public JLabel footerLabel;

  private final List<EditorCard> editorCards = new ArrayList<>();

  private static final int EDITOR_COLUMNS = 2;
  private static final int EDITOR_CARD_HEIGHT = 130;
  private static final int EDITOR_CARD_GAP = 10;
  private boolean officialServerSelected = true;
}
