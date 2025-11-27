package com.lucasluqui.launcher.editor;

import com.google.inject.Inject;
import com.jhlabs.image.GaussianFilter;
import com.jhlabs.image.GrayscaleFilter;
import com.lucasluqui.launcher.BaseGUI;
import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.Fonts;
import com.lucasluqui.launcher.LocaleManager;
import com.lucasluqui.launcher.setting.Settings;
import com.lucasluqui.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

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
    setupImages();
    compose();
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

    editorListPaneScroll = new JScrollPane(editorListPane);
    editorListPaneScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    editorListPaneScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    editorListPaneScroll.setBounds(30, 0, 740, 444);
    editorListPaneScroll.setBorder(null);
    editorListPaneScroll.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    editorListPaneScroll.setForeground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    editorListPaneScroll.getVerticalScrollBar().setUnitIncrement(16);
    guiFrame.getContentPane().add(editorListPaneScroll);

    if (Settings.showLegacySceneEditor) {
      JPanel sceneEditorPane = new JPanel()
      {
        @Override
        protected void paintComponent (Graphics g)
        {
          super.paintComponent(g);
          g.drawImage(sceneEditorImage, 0, 0, null);
        }
      };
      sceneEditorPane.setLayout(null);
      sceneEditorPane.setBounds(0, 0, 740, 100);
      sceneEditorPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

      JLabel sceneEditorLabel = new JLabel();
      sceneEditorLabel.setText(_localeManager.getValue("m.scene_editor"));
      sceneEditorLabel.setFont(Fonts.getFont("defaultMedium", 40.0f, Font.PLAIN));
      sceneEditorLabel.setHorizontalAlignment(SwingConstants.CENTER);
      sceneEditorLabel.setVerticalAlignment(SwingConstants.CENTER);
      sceneEditorLabel.setBounds(0, 0, 740, 100);
      sceneEditorPane.add(sceneEditorLabel);
      sceneEditorPane.addMouseListener(new MouseListener()
      {
        @Override
        public void mouseClicked (MouseEvent e)
        {
          eventHandler.startSceneEditor(null);
        }

        @Override
        public void mousePressed (MouseEvent e)
        {
          eventHandler.startSceneEditor(null);
        }

        @Override
        public void mouseReleased (MouseEvent e)
        {
          eventHandler.startSceneEditor(null);
        }

        @Override
        public void mouseEntered (MouseEvent e)
        {
          sceneEditorPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
          sceneEditorImage = sceneEditorImageFocused;
          sceneEditorPane.repaint();
        }

        @Override
        public void mouseExited (MouseEvent e)
        {
          sceneEditorPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          sceneEditorImage = sceneEditorImageUnfocused;
          sceneEditorPane.repaint();
        }
      });
      editorListPane.add(sceneEditorPane);
    } else {
      JPanel crucibleEditorPane = new JPanel()
      {
        @Override
        protected void paintComponent (Graphics g)
        {
          super.paintComponent(g);
          g.drawImage(crucibleEditorImage, 0, 0, null);
        }
      };
      crucibleEditorPane.setLayout(null);
      crucibleEditorPane.setBounds(0, 0, 740, 100);
      crucibleEditorPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

      JLabel crucibleEditorLabel = new JLabel();
      crucibleEditorLabel.setText(_localeManager.getValue("m.crucible_editor"));
      crucibleEditorLabel.setFont(Fonts.getFont("defaultMedium", 40.0f, Font.PLAIN));
      crucibleEditorLabel.setHorizontalAlignment(SwingConstants.CENTER);
      crucibleEditorLabel.setVerticalAlignment(SwingConstants.CENTER);
      crucibleEditorLabel.setBounds(0, 0, 740, 100);
      crucibleEditorPane.add(crucibleEditorLabel);
      crucibleEditorPane.addMouseListener(new MouseListener()
      {
        @Override
        public void mouseClicked (MouseEvent e)
        {
          eventHandler.startCrucibleEditor(null);
        }

        @Override
        public void mousePressed (MouseEvent e)
        {
          eventHandler.startCrucibleEditor(null);
        }

        @Override
        public void mouseReleased (MouseEvent e)
        {
          eventHandler.startCrucibleEditor(null);
        }

        @Override
        public void mouseEntered (MouseEvent e)
        {
          crucibleEditorPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
          crucibleEditorImage = crucibleEditorImageFocused;
          crucibleEditorPane.repaint();
        }

        @Override
        public void mouseExited (MouseEvent e)
        {
          crucibleEditorPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          crucibleEditorImage = crucibleEditorImageUnfocused;
          crucibleEditorPane.repaint();
        }
      });
      editorListPane.add(crucibleEditorPane);
    }

    JPanel modelViewerPane = new JPanel()
    {
      @Override
      protected void paintComponent (Graphics g)
      {
        super.paintComponent(g);
        g.drawImage(modelViewerImage, 0, 0, null);
      }
    };
    modelViewerPane.setLayout(null);
    modelViewerPane.setBounds(0, 0, 740, 100);
    modelViewerPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    JLabel modelViewerLabel = new JLabel();
    modelViewerLabel.setText(_localeManager.getValue("m.model_viewer"));
    modelViewerLabel.setFont(Fonts.getFont("defaultMedium", 40.0f, Font.PLAIN));
    modelViewerLabel.setHorizontalAlignment(SwingConstants.CENTER);
    modelViewerLabel.setVerticalAlignment(SwingConstants.CENTER);
    modelViewerLabel.setBounds(0, 0, 740, 100);
    modelViewerPane.add(modelViewerLabel);

    modelViewerPane.addMouseListener(new MouseListener()
    {
      @Override
      public void mouseClicked (MouseEvent e)
      {
        eventHandler.startModelViewer(null);
      }

      @Override
      public void mousePressed (MouseEvent e)
      {
        eventHandler.startModelViewer(null);
      }

      @Override
      public void mouseReleased (MouseEvent e)
      {
        eventHandler.startModelViewer(null);
      }

      @Override
      public void mouseEntered (MouseEvent e)
      {
        modelViewerPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
        modelViewerImage = modelViewerImageFocused;
        modelViewerPane.repaint();
      }

      @Override
      public void mouseExited (MouseEvent e)
      {
        modelViewerPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        modelViewerImage = modelViewerImageUnfocused;
        modelViewerPane.repaint();
      }
    });
    editorListPane.add(modelViewerPane);

    JPanel interfaceTesterPane = new JPanel()
    {
      @Override
      protected void paintComponent (Graphics g)
      {
        super.paintComponent(g);
        g.drawImage(interfaceTesterImage, 0, 0, null);
      }
    };
    interfaceTesterPane.setLayout(null);
    interfaceTesterPane.setBounds(0, 0, 740, 100);
    interfaceTesterPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    JLabel interfaceTesterLabel = new JLabel();
    interfaceTesterLabel.setText(_localeManager.getValue("m.interface_tester"));
    interfaceTesterLabel.setFont(Fonts.getFont("defaultMedium", 40.0f, Font.PLAIN));
    interfaceTesterLabel.setHorizontalAlignment(SwingConstants.CENTER);
    interfaceTesterLabel.setVerticalAlignment(SwingConstants.CENTER);
    interfaceTesterLabel.setBounds(0, 0, 740, 100);
    interfaceTesterPane.add(interfaceTesterLabel);

    interfaceTesterPane.addMouseListener(new MouseListener()
    {
      @Override
      public void mouseClicked (MouseEvent e)
      {
        eventHandler.startInterfaceTester(null);
      }

      @Override
      public void mousePressed (MouseEvent e)
      {
        eventHandler.startInterfaceTester(null);
      }

      @Override
      public void mouseReleased (MouseEvent e)
      {
        eventHandler.startInterfaceTester(null);
      }

      @Override
      public void mouseEntered (MouseEvent e)
      {
        interfaceTesterPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
        interfaceTesterImage = interfaceTesterImageFocused;
        interfaceTesterPane.repaint();
      }

      @Override
      public void mouseExited (MouseEvent e)
      {
        interfaceTesterPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        interfaceTesterImage = interfaceTesterImageUnfocused;
        interfaceTesterPane.repaint();
      }
    });
    editorListPane.add(interfaceTesterPane);

    JPanel particleEditorPane = new JPanel()
    {
      @Override
      protected void paintComponent (Graphics g)
      {
        super.paintComponent(g);
        g.drawImage(particleEditorImage, 0, 0, null);
      }
    };
    particleEditorPane.setLayout(null);
    particleEditorPane.setBounds(0, 0, 740, 100);
    particleEditorPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    JLabel particleEditorLabel = new JLabel();
    particleEditorLabel.setText(_localeManager.getValue("m.particle_editor"));
    particleEditorLabel.setFont(Fonts.getFont("defaultMedium", 40.0f, Font.PLAIN));
    particleEditorLabel.setHorizontalAlignment(SwingConstants.CENTER);
    particleEditorLabel.setVerticalAlignment(SwingConstants.CENTER);
    particleEditorLabel.setBounds(0, 0, 740, 100);
    particleEditorPane.add(particleEditorLabel);

    particleEditorPane.addMouseListener(new MouseListener()
    {
      @Override
      public void mouseClicked (MouseEvent e)
      {
        eventHandler.startParticleEditor(null);
      }

      @Override
      public void mousePressed (MouseEvent e)
      {
        eventHandler.startParticleEditor(null);
      }

      @Override
      public void mouseReleased (MouseEvent e)
      {
        eventHandler.startParticleEditor(null);
      }

      @Override
      public void mouseEntered (MouseEvent e)
      {
        particleEditorPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
        particleEditorImage = particleEditorImageFocused;
        particleEditorPane.repaint();
      }

      @Override
      public void mouseExited (MouseEvent e)
      {
        particleEditorPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        particleEditorImage = particleEditorImageUnfocused;
        particleEditorPane.repaint();
      }
    });
    editorListPane.add(particleEditorPane);

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

  protected void setupImages ()
  {
    if (Settings.showLegacySceneEditor) {
      sceneEditorImageFocused = ImageUtil.resizeImage(ImageUtil.loadImageWithinJar("/rsrc/img/editor-scene.png"), 740, 100);
      sceneEditorImageFocused = (BufferedImage) ImageUtil.addRoundedCorners(new GaussianFilter(25f).filter(sceneEditorImageFocused, null), 25);
      sceneEditorImageUnfocused = new GrayscaleFilter().filter(sceneEditorImageFocused, null);
    } else {
      crucibleEditorImageFocused = ImageUtil.resizeImage(ImageUtil.loadImageWithinJar("/rsrc/img/editor-crucible.png"), 740, 100);
      crucibleEditorImageFocused = (BufferedImage) ImageUtil.addRoundedCorners(new GaussianFilter(25f).filter(crucibleEditorImageFocused, null), 25);
      crucibleEditorImageUnfocused = new GrayscaleFilter().filter(crucibleEditorImageFocused, null);
    }

    modelViewerImageFocused = ImageUtil.resizeImage(ImageUtil.loadImageWithinJar("/rsrc/img/editor-model.png"), 740, 100);
    modelViewerImageFocused = (BufferedImage) ImageUtil.addRoundedCorners(new GaussianFilter(25f).filter(modelViewerImageFocused, null), 25);
    modelViewerImageUnfocused = new GrayscaleFilter().filter(modelViewerImageFocused, null);

    interfaceTesterImageFocused = ImageUtil.resizeImage(ImageUtil.loadImageWithinJar("/rsrc/img/editor-default.png"), 740, 100);
    interfaceTesterImageFocused = (BufferedImage) ImageUtil.addRoundedCorners(new GaussianFilter(25f).filter(interfaceTesterImageFocused, null), 25);
    interfaceTesterImageUnfocused = new GrayscaleFilter().filter(interfaceTesterImageFocused, null);

    particleEditorImageFocused = ImageUtil.resizeImage(ImageUtil.loadImageWithinJar("/rsrc/img/editor-default.png"), 740, 100);
    particleEditorImageFocused = (BufferedImage) ImageUtil.addRoundedCorners(new GaussianFilter(25f).filter(particleEditorImageFocused, null), 25);
    particleEditorImageUnfocused = new GrayscaleFilter().filter(particleEditorImageFocused, null);

    sceneEditorImage = sceneEditorImageUnfocused;
    crucibleEditorImage = crucibleEditorImageUnfocused;
    modelViewerImage = modelViewerImageUnfocused;
    interfaceTesterImage = interfaceTesterImageUnfocused;
    particleEditorImage = particleEditorImageUnfocused;
  }

  public JPanel editorsPanel;
  protected JPanel editorListPane = new JPanel();
  protected JScrollPane editorListPaneScroll = new JScrollPane();
  protected JLabel editorLaunchState;
  protected JProgressBar editorLaunchFakeProgressBar;
  protected JLabel footerLabel;

  protected BufferedImage sceneEditorImage = null;
  protected BufferedImage crucibleEditorImage = null;
  protected BufferedImage modelViewerImage = null;
  protected BufferedImage interfaceTesterImage = null;
  protected BufferedImage particleEditorImage = null;

  protected BufferedImage sceneEditorImageUnfocused = null;
  protected BufferedImage crucibleEditorImageUnfocused = null;
  protected BufferedImage modelViewerImageUnfocused = null;
  protected BufferedImage interfaceTesterImageUnfocused = null;
  protected BufferedImage particleEditorImageUnfocused = null;

  protected BufferedImage sceneEditorImageFocused = null;
  protected BufferedImage crucibleEditorImageFocused = null;
  protected BufferedImage modelViewerImageFocused = null;
  protected BufferedImage interfaceTesterImageFocused = null;
  protected BufferedImage particleEditorImageFocused = null;
}
