package xyz.lucasallegri.launcher;

import java.io.File;

public class LauncherConstants {

	public static final String VERSION = "1.0.0-PRE5";
	public static final String ROOT_FOLDER_NAME = "KnightLauncher";
	public static final String[] STANDALONE_CLIENT_ARGS = {"java","-Dsun.java2d.d3d=false", "-Dcheck_unpacked=true", "-jar",System.getProperty("user.dir") + File.separator + "getdown-pro.jar",".","client"};
	
}
