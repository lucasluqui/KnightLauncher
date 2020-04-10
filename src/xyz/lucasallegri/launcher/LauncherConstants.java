package xyz.lucasallegri.launcher;

import java.io.File;

public class LauncherConstants {

	public static final String VERSION = "1.2.2";
	public static final String[] STANDALONE_CLIENT_ARGS = {"java", "-Dsun.java2d.d3d=false", "-Dcheck_unpacked=true", "-jar", System.getProperty("user.dir") + File.separator + "getdown-pro.jar", ".", "client"};
	public static final String GET_MODS_URL = "https://sites.google.com/view/spiralknights-mods/mods";
	public static final String LNK_FILE_NAME = "Knight Launcher";
	public static final String EVENT_QUERY_URL = "https://aegis.lucasallegri.xyz/event";
	public static final String VERSION_QUERY_URL = "https://aegis.lucasallegri.xyz/knightlauncher/version.txt";
	public static final String RELEASES_URL = "https://github.com/lucas-allegri/KnightLauncher/releases";
	public static final String TWEETS_URL = "https://aegis.lucasallegri.xyz/knightlauncher/tweets.txt";
	
}
