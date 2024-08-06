package com.luuqui.launcher;

import javax.swing.*;
import java.awt.*;

public class Stylesheet {

  public static void load() {
    UIManager.put("TabbedPane.underlineColor", CustomColors.KL);
    UIManager.put("TabbedPane.inactiveUnderlineColor", CustomColors.KL);
    UIManager.put("Slider.thumbColor", CustomColors.KL);
    UIManager.put("ComboBox.selectionBackground", CustomColors.INTERFACE_DEFAULT_FOCUS);
  }

}
