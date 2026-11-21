package com.lucasluqui.launcher.mod.data;

import com.lucasluqui.launcher.CustomColors;

import java.awt.*;

/**
 * All the groups a mod can be assigned to based on its qualities.
 */
public enum ModGroup
{
  CLASS("m.modlist_group_class", CustomColors.INTERFACE_MODLIST_BADGE_CLASS_BACKGROUND),
  CODE("m.modlist_group_code", CustomColors.INTERFACE_MODLIST_BADGE_CODE_BACKGROUND),
  MODPACK("m.modlist_group_modpack", CustomColors.INTERFACE_MODLIST_BADGE_PACK_BACKGROUND),
  RESOURCE("m.modlist_group_resource", CustomColors.INTERFACE_MODLIST_BADGE_RESOURCE_BACKGROUND);

  ModGroup (String titleKey, Color color)
  {
    this.titleKey = titleKey;
    this.color = color;
  }

  public static ModGroup of (Mod mod)
  {
    if (mod instanceof ZipMod && ((ZipMod) mod).isClassType()) return CLASS;
    if (mod instanceof JarMod) return CODE;
    if (mod instanceof Modpack) return MODPACK;
    return RESOURCE;
  }

  public String getTitleKey ()
  {
    return this.titleKey;
  }

  public Color getColor ()
  {
    return this.color;
  }

  private final String titleKey;
  private final Color color;
}
