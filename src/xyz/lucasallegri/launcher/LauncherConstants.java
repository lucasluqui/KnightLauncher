package xyz.lucasallegri.launcher;

import java.io.File;

public class LauncherConstants {

	public static final String VERSION = "1.0.0-PRE10_dev";
	public static final String[] STANDALONE_CLIENT_ARGS = {"java","-Dsun.java2d.d3d=false", "-Dcheck_unpacked=true", "-jar",System.getProperty("user.dir") + File.separator + "getdown-pro.jar",".","client"};
	public static final String GET_MODS_URL = "https://sites.google.com/view/spiralknights-mods/mods";
	public static final String LNK_FILE_NAME = "Knight Launcher";
	public static final String EVENT_QUERY_URL = "http://px-api.lucasallegri.xyz/event.png";
	
}
