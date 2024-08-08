package com.luuqui.launcher;

import javax.swing.*;

public class Stylesheet {

  public static void load() {
    UIManager.put("TabbedPane.underlineColor", CustomColors.KL);
    UIManager.put("TabbedPane.inactiveUnderlineColor", CustomColors.KL);

    UIManager.put("Slider.thumbColor", CustomColors.KL);

    UIManager.put("ComboBox.selectionBackground", CustomColors.INTERFACE_DEFAULT_FOCUS);

    UIManager.put("TextField.selectionBackground", CustomColors.INTERFACE_DEFAULT_FOCUS);

    UIManager.put("EditorPane.selectionBackground", CustomColors.INTERFACE_DEFAULT_FOCUS);

    UIManager.put("ScrollPane.smoothScrolling", true);

    UIManager.put("Button.default.focusedBorderColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
    UIManager.put("Button.default.focusColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
    UIManager.put("Button.default.hoverBorderColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
    UIManager.put("Button.focusedBorderColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
    UIManager.put("Button.focusColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
    UIManager.put("Button.hoverBorderColor", CustomColors.INTERFACE_COMPONENT_FOCUS);

    UIManager.put("CheckBox.icon.focusedBorderColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
    UIManager.put("CheckBox.icon.focusColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
    UIManager.put("CheckBox.icon.hoverBorderColor", CustomColors.INTERFACE_COMPONENT_FOCUS);

    UIManager.put("Component.focusedBorderColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
    UIManager.put("Component.focusColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
    UIManager.put("Component.hoverBorderColor", CustomColors.INTERFACE_COMPONENT_FOCUS);
  }

}
