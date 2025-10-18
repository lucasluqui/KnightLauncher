package com.lucasluqui.launcher;

import com.formdev.flatlaf.FlatDarkLaf;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;

import static com.lucasluqui.launcher.Log.log;

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

    UIManager.put("ComboBox.background", CustomColors.INTERFACE_COMPONENT_BACKGROUND);
    UIManager.put("ComboBox.popupBackground", CustomColors.INTERFACE_COMPONENT_BACKGROUND);
    UIManager.put("ComboBox.buttonBackground", CustomColors.INTERFACE_COMPONENT_BACKGROUND);
    UIManager.put("ComboBox.selectionBackground", CustomColors.INTERFACE_COMPONENT_SELECTED_BACKGROUND);

    UIManager.put("TextField.background", CustomColors.INTERFACE_COMPONENT_BACKGROUND);
    UIManager.put("TextField.selectionBackground", CustomColors.INTERFACE_COMPONENT_SELECTED_BACKGROUND);
    UIManager.put("TextField.caretForeground", Color.WHITE);

    UIManager.put("TextArea.background", CustomColors.INTERFACE_COMPONENT_BACKGROUND);
    UIManager.put("TextArea.selectionBackground", CustomColors.INTERFACE_COMPONENT_SELECTED_BACKGROUND);
    UIManager.put("TextArea.caretForeground", Color.WHITE);

    UIManager.put("EditorPane.selectionBackground", CustomColors.INTERFACE_COMPONENT_SELECTED_BACKGROUND);

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
    UIManager.put("CheckBox.icon.background", CustomColors.INTERFACE_COMPONENT_BACKGROUND);
    UIManager.put("CheckBox.icon.selectedBackground", CustomColors.INTERFACE_COMPONENT_BACKGROUND);

    UIManager.put("Component.borderColor", CustomColors.INTERFACE_COMPONENT_BACKGROUND);
    UIManager.put("Component.focusedBorderColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
    UIManager.put("Component.focusColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
    UIManager.put("Component.hoverBorderColor", CustomColors.INTERFACE_COMPONENT_FOCUS);

    UIManager.put("ToolTip.font", Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    UIManager.put("Button.font", Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    UIManager.put("TitlePane.font", Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    UIManager.put("ProgressBar.font", Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
  }

}
