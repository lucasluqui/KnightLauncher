package com.lucasallegri.launcher.mods;

import com.lucasallegri.launcher.mods.data.Mod;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ModList {

  private static ModList instance = new ModList();
  private final LinkedList<Mod> installedMods;

  private ModList() {
    this.installedMods = new LinkedList<>();
  }

  private void addMod(Mod mod) {
    if(mod.isEnabled()) this.installedMods.add(mod);
  }

  private int getModCount() {
    return installedMods.size();
  }

  public static ModList getInstance() {
    if (instance == null) instance = new ModList();
    return instance;
  }

}
