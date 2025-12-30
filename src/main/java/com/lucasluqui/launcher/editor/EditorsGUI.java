package com.lucasluqui.launcher.editor;

import com.google.inject.Inject;
import com.jhlabs.image.GaussianFilter;
import com.jhlabs.image.GrayscaleFilter;
import com.lucasluqui.launcher.BaseGUI;
import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.Fonts;
import com.lucasluqui.launcher.LocaleManager;
import com.lucasluqui.launcher.editor.data.Editor;
import com.lucasluqui.launcher.setting.Settings;
import com.lucasluqui.swing.SmoothScrollPane;
import com.lucasluqui.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static com.lucasluqui.launcher.mod.Log.log;

public class EditorsGUI extends BaseGUI
{
  @Inject public EditorsEventHandler eventHandler;

  @Inject protected LocaleManager _localeManager;

  @Inject
  public EditorsGUI ()
  {
    super(385, 460, false);
  }

  public void init ()
  {
    compose();
    setupEditors();
  }

  private void compose ()
  {
    guiFrame.setVisible(false);
    guiFrame.setTitle(_localeManager.getValue("t.editors"));
    guiFrame.setBounds(100, 100, this.width, this.height);
    guiFrame.setResizable(false);
    guiFrame.setUndecorated(true);
    guiFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    guiFrame.getContentPane().setLayout(null);
    editorsPanel = (JPanel) guiFrame.getContentPane();

    editorLaunchState = new JLabel(_localeManager.getValue("m.editor_loading"));
    editorLaunchState.setIcon(new ImageIcon(this.getClass().getResource("/rsrc/img/loading.gif")));
    editorLaunchState.setHorizontalAlignment(SwingConstants.CENTER);
    editorLaunchState.setBounds(192, 190, 385, 25);
    editorLaunchState.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    editorLaunchState.setVisible(false);
    guiFrame.getContentPane().add(editorLaunchState);

    editorLaunchFakeProgressBar = new JProgressBar(0, 150);
    editorLaunchFakeProgressBar.setBounds(192, 220, 385, 25);
    editorLaunchFakeProgressBar.setVisible(false);
    guiFrame.getContentPane().add(editorLaunchFakeProgressBar);

    editorListPane = new JPanel();
    editorListPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    GridLayout layout = new GridLayout(4, 1);
    layout.setVgap(10);
    editorListPane.setLayout(layout);
    editorListPane.setPreferredSize(new Dimension(740, 440));

    editorListPaneScroll = new SmoothScrollPane(editorListPane);
    editorListPaneScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    editorListPaneScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    editorListPaneScroll.setBounds(30, 0, 760, 444);
    editorListPaneScroll.setBorder(null);
    editorListPaneScroll.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    editorListPaneScroll.setForeground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    guiFrame.getContentPane().add(editorListPaneScroll);

    footerLabel = new JLabel();
    footerLabel.setBounds(30, 449, 740, 15);
    footerLabel.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    footerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    footerLabel.setForeground(CustomColors.INTERFACE_MAINPANE_FOOTNOTE);
    footerLabel.setVisible(false);
    guiFrame.getContentPane().add(footerLabel);

  }

  @SuppressWarnings("all")
  protected void startFakeProgress ()
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
    BufferedImage sceneEditorImageFocused = null;
    BufferedImage sceneEditorImageUnfocused = null;

    if (Settings.showLegacySceneEditor) {
      sceneEditorImageFocused = ImageUtil.resizeImage(ImageUtil.loadImageWithinJar("/rsrc/img/editor-scene.png"), 740, 100);
      sceneEditorImageFocused = (BufferedImage) ImageUtil.addRoundedCorners(new GaussianFilter(25f).filter(sceneEditorImageFocused, null), 25);
      sceneEditorImageUnfocused = new GrayscaleFilter().filter(sceneEditorImageFocused, null);
    }

    BufferedImage crucibleEditorImageFocused = ImageUtil.resizeImage(ImageUtil.loadImageWithinJar("/rsrc/img/editor-crucible.png"), 740, 100);
    crucibleEditorImageFocused = (BufferedImage) ImageUtil.addRoundedCorners(new GaussianFilter(25f).filter(crucibleEditorImageFocused, null), 25);
    BufferedImage crucibleEditorImageUnfocused = new GrayscaleFilter().filter(crucibleEditorImageFocused, null);

    BufferedImage modelViewerImageFocused = ImageUtil.resizeImage(ImageUtil.loadImageWithinJar("/rsrc/img/editor-model.png"), 740, 100);
    modelViewerImageFocused = (BufferedImage) ImageUtil.addRoundedCorners(new GaussianFilter(25f).filter(modelViewerImageFocused, null), 25);
    BufferedImage modelViewerImageUnfocused = new GrayscaleFilter().filter(modelViewerImageFocused, null);

    BufferedImage interfaceTesterImageFocused = ImageUtil.resizeImage(ImageUtil.loadImageWithinJar("/rsrc/img/editor-default.png"), 740, 100);
    interfaceTesterImageFocused = (BufferedImage) ImageUtil.addRoundedCorners(new GaussianFilter(25f).filter(interfaceTesterImageFocused, null), 25);
    BufferedImage interfaceTesterImageUnfocused = new GrayscaleFilter().filter(interfaceTesterImageFocused, null);

    BufferedImage particleEditorImageFocused = ImageUtil.resizeImage(ImageUtil.loadImageWithinJar("/rsrc/img/editor-default.png"), 740, 100);
    particleEditorImageFocused = (BufferedImage) ImageUtil.addRoundedCorners(new GaussianFilter(25f).filter(particleEditorImageFocused, null), 25);
    BufferedImage particleEditorImageUnfocused = new GrayscaleFilter().filter(particleEditorImageFocused, null);

    List<Editor> editors = new ArrayList<>();

    editors.add(new Editor(
      "editor_crucible",
      crucibleEditorImageFocused,
      crucibleEditorImageUnfocused,
      "",
      null,
      "",
      140
      )
    );

    if (Settings.showLegacySceneEditor) {
      editors.add(new Editor(
          "editor_scene_legacy",
          sceneEditorImageFocused,
          sceneEditorImageUnfocused,
          "com.lucasluqui.spiralview.SceneEditorHook",
          "com.threerings.tudey.tools.SceneEditor",
          "",
          155
        )
      );
    }

    editors.add(new Editor(
        "editor_model",
        modelViewerImageFocused,
        modelViewerImageUnfocused,
        "com.lucasluqui.spiralview.ModelViewerHook",
        "com.threerings.opengl.model.tools.ModelViewer",
        "rsrc/character/pc/model.dat",
        150
      )
    );

    editors.add(new Editor(
        "editor_interface",
        interfaceTesterImageFocused,
        interfaceTesterImageUnfocused,
        "com.lucasluqui.spiralview.InterfaceTesterHook",
        null,
        "",
        110
      )
    );

    editors.add(new Editor(
        "editor_particle",
        particleEditorImageFocused,
        particleEditorImageUnfocused,
        "com.lucasluqui.spiralview.ParticleEditorHook",
        "com.threerings.opengl.effect.tools.ParticleEditor",
        "",
        125
      )
    );

    eventHandler.editors = editors;

    for (Editor editor : eventHandler.editors) {
      JPanel editorPane = new JPanel()
      {
        @Override
        protected void paintComponent (Graphics g)
        {
          super.paintComponent(g);
          g.drawImage(editor.currentSplashImage, 0, 0, null);
        }
      };
      editorPane.setLayout(null);
      editorPane.setBounds(0, 0, 740, 100);
      editorPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

      JLabel editorLabel = new JLabel();
      editorLabel.setText(_localeManager.getValue("m." + editor.name));
      editorLabel.setFont(Fonts.getFont("defaultMedium", 40.0f, Font.PLAIN));
      editorLabel.setHorizontalAlignment(SwingConstants.CENTER);
      editorLabel.setVerticalAlignment(SwingConstants.CENTER);
      editorLabel.setBounds(0, 0, 740, 100);
      editorPane.add(editorLabel);
      editorPane.addMouseListener(new MouseListener()
      {
        @Override
        public void mouseClicked (MouseEvent e)
        {
          eventHandler.startEditor(editor);
        }

        @Override
        public void mousePressed (MouseEvent e)
        {
          eventHandler.startEditor(editor);
        }

        @Override
        public void mouseReleased (MouseEvent e)
        {
          eventHandler.startEditor(editor);
        }

        @Override
        public void mouseEntered (MouseEvent e)
        {
          editorPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
          editor.currentSplashImage = editor.splashImage;
          editorPane.repaint();
        }

        @Override
        public void mouseExited (MouseEvent e)
        {
          editorPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          editor.currentSplashImage = editor.splashImageUnfocused;
          editorPane.repaint();
        }
      });
      editorListPane.add(editorPane);
    }

    editorListPane.setPreferredSize(new Dimension(740, eventHandler.editors.size() * 110));
    GridLayout layout = new GridLayout(eventHandler.editors.size(), 1);
    layout.setVgap(10);
    editorListPane.setLayout(layout);

    editorListPaneScroll.setBounds(
      editorListPaneScroll.getX(),
      editorListPaneScroll.getY(),
      editorListPaneScroll.getWidth(),
      Math.min(440, eventHandler.editors.size() * 110)
    );

    editorListPane.updateUI();
  }

  public JPanel editorsPanel;
  protected JPanel editorListPane = new JPanel();
  protected SmoothScrollPane editorListPaneScroll = new SmoothScrollPane();
  protected JLabel editorLaunchState;
  protected JProgressBar editorLaunchFakeProgressBar;
  protected JLabel footerLabel;
}
