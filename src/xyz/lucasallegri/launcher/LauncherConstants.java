package xyz.lucasallegri.launcher;

import java.io.File;

public class LauncherConstants {

	public static final String VERSION = "1.3.4";
	public static final String LNK_FILE_NAME = "Knight Launcher";
	public static final String EVENT_QUERY_URL = "https://upsilonapi.lucasallegri.xyz/event";
	public static final String VERSION_QUERY_URL = "https://upsilonapi.lucasallegri.xyz/knightlauncher/version.txt";
	public static final String RELEASES_URL = "https://upsilonapi.lucasallegri.xyz/knightlauncher/releases.html";
	public static final String TWEETS_URL = "https://upsilonapi.lucasallegri.xyz/knightlauncher/tweets.txt";
	public static final String BUG_REPORT_URL = "https://upsilonapi.lucasallegri.xyz/knightlauncher/bug-report.html";
	public static final String DISCORD_URL = "https://upsilonapi.lucasallegri.xyz/knightlauncher/discord.html";
	public static final String GET_MODS_URL = "https://upsilonapi.lucasallegri.xyz/knightlauncher/get-mods.html";
	public static final String JVM_DOWNLOAD_URL = "https://upsilonapi.lucasallegri.xyz/knightlauncher/jvm-download.txt";
	public static final String KOFI_URL = "https://ko-fi.com/lucasallegri";
	public static final String USER_DIR = System.getProperty("user.dir");
	
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
