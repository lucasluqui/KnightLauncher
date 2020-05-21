package xyz.lucasallegri.launcher;

import java.io.File;

public class LauncherConstants {

	public static final String VERSION = "1.2.8";
	public static final String LNK_FILE_NAME = "Knight Launcher";
	public static final String EVENT_QUERY_URL = "https://upsilonapi.lucasallegri.xyz/event";
	public static final String VERSION_QUERY_URL = "https://upsilonapi.lucasallegri.xyz/knightlauncher/version.txt";
	public static final String RELEASES_URL = "https://upsilonapi.lucasallegri.xyz/knightlauncher/releases.html";
	public static final String TWEETS_URL = "https://upsilonapi.lucasallegri.xyz/knightlauncher/tweets.txt";
	public static final String BUG_REPORT_URL = "https://upsilonapi.lucasallegri.xyz/knightlauncher/bug-report.html";
	public static final String DISCORD_URL = "https://upsilonapi.lucasallegri.xyz/knightlauncher/discord.html";
	public static final String GET_MODS_URL = "https://upsilonapi.lucasallegri.xyz/get-mods.html";
	public static final String USER_DIR = System.getProperty("user.dir");
	
	
	public static final String[] STANDALONE_LAUNCHER_ARGS = {"java",
			"-Dsun.java2d.d3d=false",
			"-Dcheck_unpacked=true",
			"-jar",
			USER_DIR + File.separator + "getdown-pro.jar",
			".",
			"client"
	};
	
	public static final String[] STANDALONE_CLIENT_ARGS = {"java",
			"-classpath",
			USER_DIR + File.separator + "./code/config.jar;" +
			USER_DIR + File.separator + "./code/projectx-config.jar;" +
			USER_DIR + File.separator + "./code/projectx-pcode.jar;" +
			USER_DIR + File.separator + "./code/lwjgl.jar;" +
			USER_DIR + File.separator + "./code/lwjgl_util.jar;" +
			USER_DIR + File.separator + "./code/jinput.jar;" +
			USER_DIR + File.separator + "./code/jutils.jar;" +
			USER_DIR + File.separator + "./code/jshortcut.jar;" +
			USER_DIR + File.separator + "./code/commons-beanutils.jar;" +
			USER_DIR + File.separator + "./code/commons-digester.jar;" +
			USER_DIR + File.separator + "./code/commons-logging.jar;",
			"-Dcom.threerings.getdown=true",
			"-Xmx512M",
			"-XX:+AggressiveOpts",
			"-XX:SoftRefLRUPolicyMSPerMB=10",
			"-Djava.library.path=" + USER_DIR + File.separator + "./native",
			"-Dorg.lwjgl.util.NoChecks=true",
			"-Dsun.java2d.d3d=false",
			"-Dappdir=" + USER_DIR + File.separator + ".",
			"-Dresource_dir=" + USER_DIR + File.separator + "./rsrc",
			"com.threerings.projectx.client.ProjectXApp",
	};
	
}
