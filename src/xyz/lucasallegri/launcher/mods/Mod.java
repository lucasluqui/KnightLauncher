package xyz.lucasallegri.launcher.mods;

public class Mod {
	
	protected String displayName;
	protected String fileName;
	protected Boolean isEnabled;
	protected Boolean hasHash;
	
	public Mod() {
		this.displayName = null;
		this.fileName = null;
		this.isEnabled = true;
		this.hasHash = false;
	}
	
	public Mod(String displayName, String fileName) {
		this.displayName = displayName;
		this.fileName = fileName;
		this.isEnabled = true;
		this.hasHash = false;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public void setDisplayName(String _displayName) {
		this.displayName = _displayName;
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
	
	public Boolean hasHash() {
		return this.hasHash;
	}
	
	public void setHasHash(boolean _hasHash) {
		this.hasHash = _hasHash;
	}

}
