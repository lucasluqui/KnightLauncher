package xyz.lucasallegri.launcher;

import java.io.File;

public class LauncherConstants {

	public static final String VERSION = "1.3.4";
	
	public static final String LNK_FILE_NAME = "Knight Launcher";
	
	public static final String GITHUB_API = "https://api.github.com/";
	public static final String GITHUB_AUTHOR = "lucas-allegri";
	public static final String GITHUB_REPO = "KnightLauncher";
	public static final String GITHUB_MAIN_BRANCH = "master";
	
	public static final String API_URL = "https://upsilonapi.lucasallegri.xyz/";
	public static final String DISCORD_URL = "https://discord.gg/RAf499a";
	public static final String GET_MODS_URL = "https://sites.google.com/view/spiralknights-mods/home";
	public static final String KOFI_URL = "https://ko-fi.com/lucasallegri";
	
	public static final String USER_DIR = System.getProperty("user.dir");
	
	/*
	 * Constants below will be set after pulling the data from the Internet.
	 * Technically they're not contants but they'll be placed here temporarily,
	 * or until I find where to move them to.
	 */
	
	public static String LATEST_RELEASE = null;
	public static String BUG_REPORT_URL = null;
	
	public static final String[] STANDALONE_LAUNCHER_ARGS = {".\\java_vm\\bin\\java.exe",
			"-Dsun.java2d.d3d=false",
			"-Dcheck_unpacked=true",
			"-jar",
			USER_DIR + File.separator + "getdown-pro.jar",
			".",
			"client"
	};
	
	public static final String[] STANDALONE_LAUNCHER_ARGS_LINUX_MAC = {"./java/bin/java",
			"-Dsun.java2d.d3d=false",
			"-Dcheck_unpacked=true",
			"-jar",
			"./getdown-pro.jar",
			".",
			"client"
	};
	
}
