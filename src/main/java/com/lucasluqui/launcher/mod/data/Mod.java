package com.lucasluqui.launcher.mod.data;

import com.lucasluqui.util.ImageUtil;
import com.lucasluqui.util.ZipUtil;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;

import static com.lucasluqui.launcher.mod.Log.log;

public abstract class Mod
{
  /**
   * Holds metadata information about this mod in JSON format. Retrieved from the {@code mod.json} file
   * within each mod's zip/jar/modpack file.
   */
  protected JSONObject metadata;

  /**
   * The mod's name to display to the end user.
   */
  protected String displayName;

  /**
   * A brief description of this mod to display to the end user.
   */
  protected String description;

  /**
   * The mod's author (or authors) to be displayed to the end user.
   */
  protected String authorName;

  /**
   * The mod's version. For the moment we don't make version checks,
   * it only serves for modders to organize themselves.
   */
  protected String version;

  /**
   * Project X version this mod is intended for. Most mods do not need to worry about this.
   * Used to verify when mounting 'class' type ZipMods or JarMods, as we need to make sure they're compatible
   * with the current game version.
   */
  protected String pxVersion;

  /**
   * Whether it's compatible with the current game version. Set at parse time.
   */
  protected boolean pxCompatible;

  /**
   * The mod's file name. In case no display name is set, we fall back to this.
   */
  protected String fileName;

  /**
   * The absolute path leading to this mod's zip/jar/modpack file.
   */
  protected String absolutePath;

  /**
   * Determines whether this mod must be mounted or not when the mounting process is called.
   */
  protected Boolean isEnabled;

  /**
   * A cover image for the mod. This is sourced from either a base64 string in {@code mod.json} within the key
   * {@code image}, or from a {@code mod.png} file within the mod's zip/jar/modpack file.
   * <p>
   * There's no set lower or upper pixel limit, but it's recommended to use a generously sized image, say 512x512.
   * It will be resized as needed when used.
   */
  protected String image;

  /**
   * Any warning message related to this mod that we should display to the user.
   */
  protected String warningMessage = "";

  protected Mod ()
  {
    this.description = "No description found";
    this.authorName = "Unknown";
    this.version = "1.0";
    this.pxVersion = "0";
    this.pxCompatible = false;
    this.isEnabled = true;
  }

  public abstract void mount ();

  public abstract void wasAdded ();

  /**
   * Parses metadata information about this mod contained in the {@code mod.json} file.
   * This includes information like mod display name, description, author, version, etc.
   * <p>
   * We will only parse the most crucial bits, specific mod implementations like {@code JarMod}
   * or {@code ZipMod} can implement additional parsing.
   */
  public void parseMetadata ()
  {
    log.info("Parsing mod metadata...", "fileName", this.fileName);

    try {
      String jsonString = ZipUtil.readFileInsideZip(this.getAbsolutePath(), "mod.json");
      JSONObject jsonObject = new JSONObject(jsonString).getJSONObject("mod");
      this.setMetadata(jsonObject);
    } catch (IOException e) {
      log.error("Could not parse mod metadata from mod.json file. File probably does not exist");
      log.error(e);
      this.setMetadata(null);
    } catch (JSONException e) {
      log.error("Failed parsing mod metadata from mod.json file");
      log.error(e);
      this.setMetadata(null);
    }

    if (this.metadata != null) {
      this.setDisplayName(this.metadata.getString("name"));
      this.setDescription(this.metadata.getString("description"));
      this.setAuthor(this.metadata.getString("author"));
      this.setVersion(this.metadata.getString("version"));
      String pxVersion = this.metadata.has("pxVersion") ? this.metadata.getString("pxVersion") : "0";
      this.setPXVersion(pxVersion);

      try {
        this.setImage(this.metadata.getString("image"));
      } catch (Exception e) {
        try {
          InputStream imageIs = ZipUtil.getISFromFileInsideZip(this.getAbsolutePath(), "mod.png");
          this.setImage(ImageUtil.imageToBase64(ImageIO.read(imageIs)));
          imageIs.close();
        } catch (Exception e2) {
          this.setImage(null);
        }
      }
    }
  }

  public String getWarningMessage ()
  {
    return this.warningMessage;
  }

  public void addWarningMessage (String warning)
  {
    this.warningMessage += warningMessage.isEmpty() ? warning : "\n\n" + warning;
  }

  public Boolean showWarningMessage ()
  {
    return !this.warningMessage.isEmpty();
  }

  public String getDisplayName ()
  {
    return this.displayName;
  }

  public void setDisplayName (String displayName)
  {
    this.displayName = displayName;
  }

  public String getDescription ()
  {
    return this.description;
  }

  public void setDescription (String description)
  {
    this.description = description;
  }

  public String getAuthor ()
  {
    return this.authorName;
  }

  public void setAuthor (String author)
  {
    this.authorName = author;
  }

  public String getVersion ()
  {
    return this.version;
  }

  public void setVersion (String version)
  {
    this.version = version;
  }

  public String getFileName ()
  {
    return this.fileName;
  }

  @SuppressWarnings("unused")
  public void setFileName (String fileName)
  {
    this.fileName = fileName;
  }

  public String getAbsolutePath ()
  {
    return this.absolutePath;
  }

  @SuppressWarnings("unused")
  public void setAbsolutePath (String absolutePath)
  {
    this.absolutePath = absolutePath;
  }

  public Boolean isEnabled ()
  {
    return this.isEnabled;
  }

  public void setEnabled (boolean enabled)
  {
    this.isEnabled = enabled;
  }

  public String getImage ()
  {
    return this.image;
  }

  public void setImage (String image)
  {
    this.image = image;
  }

  public String getPXVersion ()
  {
    return this.pxVersion;
  }

  public void setPXVersion (String pxVersion)
  {
    this.pxVersion = pxVersion;
  }

  public boolean isPXCompatible ()
  {
    return this.pxCompatible;
  }

  public void setPXCompatible (boolean pxCompatible)
  {
    this.pxCompatible = pxCompatible;
  }

  public JSONObject getMetadata ()
  {
    return this.metadata;
  }

  public void setMetadata (JSONObject metadata)
  {
    this.metadata = metadata;
  }

  @Override
  public String toString ()
  {
    return "[Mod displayName=" + this.displayName + ",description=" + this.description + ",author=" + this.authorName + ",version=" + this.version + ",fileName=" + this.fileName + ",isEnabled=" + this.isEnabled + "]";
  }

  protected final String[] FORCED_FILTER_LIST = new String[]{
    "item/live/statue/model.dat",
    "world/dynamic/switch/button/model.dat",
    "world/dynamic/switch/button/model_pressure.dat",
    "world/dynamic/switch/button/model_pressure_onetime.dat",
    "world/dynamic/switch/button/model_pressure_statue.dat",
    "world/dynamic/switch/button/model_whitespace.dat",
    "world/dynamic/switch/button/parts/animation_down.dat",
    "world/dynamic/switch/button/parts/animation_hide.dat",
    "world/dynamic/switch/button/parts/animation_show.dat",
    "world/dynamic/switch/button/parts/animation_up.dat",
    "world/dynamic/switch/button/parts/animation_whitespace_down.dat",
    "world/dynamic/switch/button/parts/animation_whitespace_up.dat",
    "world/dynamic/switch/button/parts/fx_down.dat",
    "world/dynamic/switch/button_large/fx_whitespace-hit.dat",
    "world/dynamic/switch/button_large/fx_whitespace.dat",
    "world/dynamic/switch/button_large/model.dat",
    "world/dynamic/switch/button_large/model_horde.dat",
    "world/dynamic/switch/button_large/model_whitespace.dat",
    "world/dynamic/switch/clockwork_button/glow.dat",
    "world/dynamic/switch/clockwork_button/model.dat",
    "world/dynamic/switch/clockwork_button/animation/state_down.dat",
    "world/dynamic/switch/clockwork_button/animation/state_up.dat",
    "world/dynamic/switch/multistate/model.dat",
    "world/dynamic/switch/multistate/parts/animation_disabled.dat",
    "world/dynamic/switch/multistate/parts/animation_green.dat",
    "world/dynamic/switch/multistate/parts/animation_red.dat",
    "world/dynamic/switch/multistate/parts/animation_violet.dat",
    "world/dynamic/switch/multistate/parts/animation_yellow.dat",
    "world/dynamic/switch/toggle_lever/animation_off.dat",
    "world/dynamic/switch/toggle_lever/animation_on.dat",
    "world/dynamic/switch/toggle_lever/model.dat"
  };
}
