package xyz.lucasallegri.launcher.mods;

public class Mod {
	
	protected String displayName;
	protected String fileName ;
	protected Boolean isEnabled;
	
	public Mod() {
		this.displayName = null;
		this.fileName = null;
		this.isEnabled = true;
	}
	
	public Mod(String displayName, String fileName) {
		this.displayName = displayName;
		this.fileName = fileName;
		this.isEnabled = true;
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

}
