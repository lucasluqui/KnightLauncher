package xyz.lucasallegri.launcher.settings;

public class Settings {
	
	public static String apiEndpoint = "px-api.lucasallegri.xyz:5500/v1/";
	public static String gamePlatform = "Steam";
	public static String lang = "en";
	public static Boolean doRebuilds = true;
	public static Boolean keepOpen = false;
	public static Boolean createShortcut = true;
	public static Boolean jvmPatched = false;
	public static String compressorUnzipMethod = "safe";
	public static Integer compressorExtractBuffer = 8196;
	public static Integer gameMemory = 512;
	public static Boolean gameUseStringDeduplication = false;
	public static Boolean gameUseG1GC = false;
	public static Boolean gameDisableExplicitGC = false;
	public static Boolean gameUndecoratedWindow = false;
	public static String gameAdditionalArgs = null;

}
