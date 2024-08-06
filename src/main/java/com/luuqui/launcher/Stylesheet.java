package com.luuqui.launcher;

import javax.swing.*;
import java.awt.*;

public class Stylesheet {

  public static void load() {
    UIManager.put("TabbedPane.underlineColor", CustomColors.KL);
    UIManager.put("TabbedPane.inactiveUnderlineColor", CustomColors.KL);

    UIManager.put("Slider.thumbColor", CustomColors.KL);

    UIManager.put("ComboBox.selectionBackground", CustomColors.INTERFACE_DEFAULT_FOCUS);

    UIManager.put("TextField.selectionBackground", CustomColors.INTERFACE_DEFAULT_FOCUS);

    UIManager.put("EditorPane.selectionBackground", CustomColors.INTERFACE_DEFAULT_FOCUS);
    UIManager.put("EditorPane.border", CustomColors.INTERFACE_DEFAULT_FOCUS);

    //UIManager.put("ScrollPane.contentMargins", new Insets(4,6,4,6));
    UIManager.put("ScrollPane.smoothScrolling", true);
  }

}
