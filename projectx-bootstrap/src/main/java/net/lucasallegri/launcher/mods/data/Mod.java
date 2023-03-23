package com.lucasallegri.launcher.mods.data;

public abstract class Mod {

  protected String displayName;
  protected String description;
  protected String authorName;
  protected String version;
  protected String fileName;
  protected Boolean isEnabled;

  protected final String DEFAULT_DESCRIPTION = "No description found";
  protected final String DEFAULT_AUTHOR = "Someone";
  protected final String DEFAULT_VERSION = "Unknown";

  protected Mod () {
    this.description = DEFAULT_DESCRIPTION;
    this.authorName = DEFAULT_AUTHOR;
    this.version = DEFAULT_VERSION;
    this.isEnabled = true;
  }

  public String getDisplayName() {
    return this.displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAuthor() {
    return this.authorName;
  }

  public void setAuthor(String author) {
    this.authorName = author;
  }

  public String getVersion() {
    return this.version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getFileName() {
    return this.fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Boolean isEnabled() {
    return this.isEnabled;
  }

  public void setEnabled(boolean enabled) {
    this.isEnabled = enabled;
  }

  public abstract void mount();
  public abstract void wasAdded();

  @Override
  public String toString() {
    return "[Mod displayName=" + this.displayName + ",description=" + this.description + ",author=" + this.authorName + ",version=" + this.version + ",fileName=" + this.fileName + ",isEnabled=" + this.isEnabled + "]";
  }

}
