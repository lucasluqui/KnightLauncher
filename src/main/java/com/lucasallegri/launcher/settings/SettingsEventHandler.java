package com.lucasallegri.launcher.settings;

import com.lucasallegri.dialog.DialogWarning;
import com.lucasallegri.launcher.Locale;
import com.lucasallegri.launcher.LauncherGlobals;
import com.lucasallegri.launcher.mods.ModLoader;
import com.lucasallegri.util.ProcessUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;

public class SettingsEventHandler {

  public static void platformChangeEvent(ItemEvent event) {
    Settings.gamePlatform = (String) SettingsGUI.choicePlatform.getSelectedItem();
    SettingsProperties.setValue("game.platform", (String) SettingsGUI.choicePlatform.getSelectedItem());
  }

  public static void rebuildsChangeEvent(ActionEvent event) {
    Settings.doRebuilds = SettingsGUI.switchCleaning.isSelected();
    SettingsProperties.setValue("launcher.rebuilds", SettingsGUI.switchCleaning.isSelected() ? "true" : "false");
  }

  public static void keepOpenChangeEvent(ActionEvent event) {
    Settings.keepOpen = SettingsGUI.switchKeepOpen.isSelected();
    SettingsProperties.setValue("launcher.keepOpen", SettingsGUI.switchKeepOpen.isSelected() ? "true" : "false");
  }

  public static void forceRebuildEvent() {
    ModLoader.mountRequired = true;
    ModLoader.startFileRebuild();
  }

  public static void createShortcutChangeEvent(ActionEvent event) {
    Settings.createShortcut = SettingsGUI.switchShortcut.isSelected();
    SettingsProperties.setValue("launcher.createShortcut", SettingsGUI.switchShortcut.isSelected() ? "true" : "false");
  }

  public static void languageChangeEvent(ItemEvent event) {
    Settings.lang = Locale.getLangCode((String) SettingsGUI.choiceLanguage.getSelectedItem());
    SettingsProperties.setValue("launcher.lang", Locale.getLangCode((String) SettingsGUI.choiceLanguage.getSelectedItem()));
    DialogWarning.pushTranslated(Locale.getValue("m.prompt_restart_required"));
  }

  public static void useStringDeduplicationChangeEvent(ActionEvent action) {
    Settings.gameUseStringDeduplication = SettingsGUI.switchStringDedup.isSelected();
    SettingsProperties.setValue("game.useStringDeduplication", SettingsGUI.switchStringDedup.isSelected() ? "true" : "false");
  }

  public static void customGCChangeEvent(ActionEvent action) {
    Settings.gameUseCustomGC = SettingsGUI.switchUseCustomGC.isSelected();
    SettingsProperties.setValue("game.useCustomGC", SettingsGUI.switchUseCustomGC.isSelected() ? "true" : "false");
  }

  public static void choiceGCChangeEvent(ItemEvent event) {
    switch (SettingsGUI.choiceGC.getSelectedIndex()) {
      case 0:
        Settings.gameGarbageCollector = "ParallelOld";
        SettingsProperties.setValue("game.garbageCollector", "ParallelOld");
        break;
      case 1:
        Settings.gameGarbageCollector = "Serial";
        SettingsProperties.setValue("game.garbageCollector", "Serial");
        break;
      case 2:
        Settings.gameGarbageCollector = "G1";
        SettingsProperties.setValue("game.garbageCollector", "G1");
        break;
    }
  }

  public static void disableExplicitGCChangeEvent(ActionEvent action) {
    Settings.gameDisableExplicitGC = SettingsGUI.switchExplicitGC.isSelected();
    SettingsProperties.setValue("game.disableExplicitGC", SettingsGUI.switchExplicitGC.isSelected() ? "true" : "false");
  }

  public static void saveAdditionalArgs() {
    Settings.gameAdditionalArgs = SettingsGUI.argumentsPane.getText();
    SettingsProperties.setValue("game.additionalArgs", SettingsGUI.argumentsPane.getText());
  }

  public static void memoryChangeEvent(ItemEvent event) {
    Settings.gameMemory = SettingsGUI.parseSelectedMemoryAsInt();
    SettingsProperties.setValue("game.memory", String.valueOf(SettingsGUI.parseSelectedMemoryAsInt()));
    SettingsGUI.choiceMemory.setToolTipText((String) SettingsGUI.choiceMemory.getSelectedItem());
  }

  public static void styleChangeEvent(ItemEvent event) {
    SettingsProperties.setValue("launcher.style", SettingsGUI.choiceStyle.getSelectedIndex() == 0 ? "dark" : "light");
    DialogWarning.pushTranslated(Locale.getValue("m.prompt_restart_required"));
  }

  public static void ingameRPCChangeEvent(ActionEvent action) {
    Settings.useIngameRPC = SettingsGUI.switchUseIngameRPC.isSelected();
    SettingsProperties.setValue("launcher.useIngameRPC", SettingsGUI.switchUseIngameRPC.isSelected() ? "true" : "false");
  }

  public static void jvmPatchEvent(ActionEvent action) {
    SettingsProperties.setValue("launcher.jvm_patched", "false");
    ProcessUtil.run(new String[] { "java", "-jar", LauncherGlobals.USER_DIR + File.pathSeparator + "KnightLauncher.jar" }, true);
    SettingsGUI.settingsGUIFrame.dispose();
    System.exit(1);
  }

  public static void saveConnectionSettings() {
    Settings.gameEndpoint = SettingsGUI.serverAddressTextField.getText();
    Settings.gamePort = Integer.parseInt(SettingsGUI.portTextField.getText());
    Settings.gamePublicKey = SettingsGUI.publicKeyTextField.getText();
    Settings.gameGetdownFullURL = SettingsGUI.getdownURLTextField.getText();
    Settings.gameGetdownURL = "http://" + SettingsGUI.getdownURLTextField.getText().split("://")[1].split("/")[0];
    SettingsProperties.setValue("game.endpoint", Settings.gameEndpoint);
    SettingsProperties.setValue("game.port", String.valueOf(Settings.gamePort));
    SettingsProperties.setValue("game.publicKey", Settings.gamePublicKey);
    SettingsProperties.setValue("game.getdownURL", Settings.gameGetdownURL);
    SettingsProperties.setValue("game.getdownFullURL", Settings.gameGetdownFullURL);
  }

  public static void understoodCheckBoxChangeEvent(ActionEvent action) {
    if(SettingsGUI.understoodCheckBox.isSelected()) {
      Settings.connectionOverwriteAgreed = true;
      SettingsProperties.setValue("launcher.connectionOverwriteAgreed", "true");

      SettingsGUI.understoodCheckBox.setEnabled(false);
      SettingsGUI.understoodCheckBox.setVisible(false);
      SettingsGUI.serverAddressTextField.setEnabled(true);
      SettingsGUI.portTextField.setEnabled(true);
      SettingsGUI.publicKeyTextField.setEnabled(true);
      SettingsGUI.getdownURLTextField.setEnabled(true);
    }
  }

  public static void resetButtonEvent(ActionEvent action) {
    SettingsGUI.serverAddressTextField.setText("game.spiralknights.com");
    SettingsGUI.portTextField.setText("47624");
    SettingsGUI.publicKeyTextField.setText("a5ed0dc3892b9472cfb668e236064e989e95945dad18f3d7e7d8e474d6e03de38bc044c3429b9ca649d0881d601c0eb8ffebc3756f0503f73a8ca1760943ea0e8921ad6f8102026586db3133844bbadbcfcfc666d23982d7684511fbf6cd8bb1d02a14270d0854098d16fe88f99c05825b0fe1b6fd497709106f2c418796aaf7aab7c92f26fcd9fbb3c43df48075fed8dd931273a7b0a333c8de5967797874c1944aed65b47f0792b273a529ac22a2dce08dad04eeebeeff67c7bc99b97682bff488038b28e24f4b5eea77ed966caede52f2c1ecf2b403110a9765daa81ddf718129a040823bead3a0bdca70ef6d08f483757a6d3b6e01fbbcb32006b7872bcd#10001");
    SettingsGUI.getdownURLTextField.setText("http://gamemedia2.spiralknights.com/spiral/client/");
  }
}
