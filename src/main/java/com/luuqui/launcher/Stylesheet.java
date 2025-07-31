package com.luuqui.launcher;

import com.formdev.flatlaf.FlatDarkLaf;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;

import static com.luuqui.launcher.Log.log;

public class Stylesheet
{

  public static void setup ()
  {
    System.setProperty("awt.useSystemAAFontSettings", "on");
    System.setProperty("swing.aatext", "true");

    IconFontSwing.register(FontAwesome.getIconFont());

    try {
      UIManager.setLookAndFeel(new FlatDarkLaf());
    } catch (UnsupportedLookAndFeelException e) {
      log.error(e);
    }

    UIManager.put("TabbedPane.underlineColor", CustomColors.LAUNCHER);
    UIManager.put("TabbedPane.inactiveUnderlineColor", CustomColors.LAUNCHER);

    UIManager.put("Slider.thumbColor", CustomColors.INTERFACE_DEFAULT);

    UIManager.put("ComboBox.selectionBackground", CustomColors.INTERFACE_DEFAULT_FOCUS);

    UIManager.put("TextField.selectionBackground", CustomColors.INTERFACE_DEFAULT_FOCUS);

    UIManager.put("TextArea.selectionBackground", CustomColors.INTERFACE_DEFAULT_FOCUS);

    UIManager.put("EditorPane.selectionBackground", CustomColors.INTERFACE_DEFAULT_FOCUS);

    UIManager.put("ScrollPane.smoothScrolling", true);

    UIManager.put("ScrollBar.background", CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    UIManager.put("ScrollBar.trackArc", 100);
    UIManager.put("ScrollBar.thumbArc", 100);
    UIManager.put("ScrollBar.width", 10);

    UIManager.put("Button.default.hoverBorderColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
    UIManager.put("Button.hoverBorderColor", CustomColors.INTERFACE_COMPONENT_FOCUS);

    UIManager.put("CheckBox.icon.focusedBorderColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
    UIManager.put("CheckBox.icon.focusColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
    UIManager.put("CheckBox.icon.hoverBorderColor", CustomColors.INTERFACE_COMPONENT_FOCUS);

    UIManager.put("Component.focusedBorderColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
    UIManager.put("Component.focusColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
    UIManager.put("Component.hoverBorderColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
  }

}
