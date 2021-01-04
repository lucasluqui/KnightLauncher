package com.lucasallegri.launcher.mods;

public class Mod {

  protected String displayName;
  protected String description;
  protected String authorName;
  protected String version;
  protected String fileName;
  protected Boolean isEnabled;

  protected final String DEFAULT_DESCRIPTION = "No description found";
  protected final String DEFAULT_AUTHOR = "Someone";
  protected final String DEFAULT_VERSION = "Unknown";

  public Mod() {
    this.displayName = null;
    this.description = null;
    this.authorName = null;
    this.version = null;
    this.fileName = null;
    this.isEnabled = true;
  }

  public Mod(String fileName) {
    this.displayName = fileName;
    this.description = DEFAULT_DESCRIPTION;
    this.authorName = DEFAULT_AUTHOR;
    this.version = DEFAULT_VERSION;
    this.fileName = fileName;
    this.isEnabled = true;
  }

  public String getDisplayName() {
    return this.displayName;
  }

  public void setDisplayName(String _displayName) {
    this.displayName = _displayName;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String _description) {
    this.description = _description;
  }

  public String getAuthor() {
    return this.authorName;
  }

  public void setAuthor(String _author) {
    this.authorName = _author;
  }

  public String getVersion() {
    return this.version;
  }

  public void setVersion(String _version) {
    this.version = _version;
  }

  public String getFileName() {
    return this.fileName;
  }

  public void setFileName(String _fileName) {
    this.fileName = _fileName;
  }

  public Boolean isEnabled() {
    return this.isEnabled;
  }

  public void setEnabled(boolean enabled) {
    this.isEnabled = enabled;
  }

  @Override
  public String toString() {
    return "[Mod displayName=" + this.displayName + ",description=" + this.description + ",author=" + this.authorName + ",version=" + this.version + ",fileName=" + this.fileName + ",isEnabled=" + this.isEnabled + "]";
  }

}
