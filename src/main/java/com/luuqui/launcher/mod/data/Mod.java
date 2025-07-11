package com.luuqui.launcher.mod.data;

import com.luuqui.util.Compressor;
import com.luuqui.util.ImageUtil;
import org.json.JSONObject;

import javax.imageio.ImageIO;

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
   *
   * The cover image must not exceed the pixel size of 64x64.
   */
  protected String image;

  protected final String DEFAULT_DESCRIPTION = "No description found";
  protected final String DEFAULT_AUTHOR = "Unknown";
  protected final String DEFAULT_VERSION = "?";

  protected Mod ()
  {
    this.description = DEFAULT_DESCRIPTION;
    this.authorName = DEFAULT_AUTHOR;
    this.version = DEFAULT_VERSION;
    this.isEnabled = true;
  }

  public abstract void mount ();
  public abstract void wasAdded ();

  /**
   * Parses metadata information about this mod contained in the {@code mod.json} file.
   * This includes information like mod display name, description, author, version, etc.
   *
   * We will only parse the most crucial bits, specific mod implementations like {@code JarMod}
   * or {@code ZipMod} can implement additional parsing.
   */
  public void parseMetadata () {
    try {
      this.setMetadata(
          new JSONObject(
              Compressor.readFileInsideZip(this.getAbsolutePath(), "mod.json")
          ).getJSONObject("mod")
      );
    } catch (Exception e) {
      this.setMetadata(null);
    }

    if (this.metadata != null) {
      this.setDisplayName(this.metadata.getString("name"));
      this.setDescription(this.metadata.getString("description"));
      this.setAuthor(this.metadata.getString("author"));
      this.setVersion(this.metadata.getString("version"));

      try {
        this.setImage(this.metadata.getString("image"));
      } catch (Exception e) {
        try {
          this.setImage(ImageUtil.imageToBase64(ImageIO.read(Compressor.getISFromFileInsideZip(this.getAbsolutePath(), "mod.png"))));
        } catch (Exception e2) {
          this.setImage(null);
        }
      }
    }
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

}
