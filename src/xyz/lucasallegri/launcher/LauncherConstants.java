package xyz.lucasallegri.launcher;

import java.io.File;

public class LauncherConstants {

	public static final String VERSION = "1.0_A1";
	public static final String ROOT_FOLDER_NAME = "KnightLauncher";
	public static final String[] STANDALONE_CLIENT_ARGS = {"java.exe","-Dsun.java2d.d3d=false", "-Dcheck_unpacked=true", "-jar",System.getProperty("user.dir") + File.separator + "getdown-pro.jar",".","client"};
	
}
