package xyz.lucasallegri.launcher;

import java.io.File;

public class LauncherConstants {

	public static final String VERSION = "1.2.5";
	public static final String GET_MODS_URL = "https://sites.google.com/view/spiralknights-mods/mods";
	public static final String LNK_FILE_NAME = "Knight Launcher";
	public static final String EVENT_QUERY_URL = "https://aegis.lucasallegri.xyz/event";
	public static final String VERSION_QUERY_URL = "https://aegis.lucasallegri.xyz/knightlauncher/version.txt";
	public static final String RELEASES_URL = "https://github.com/lucas-allegri/KnightLauncher/releases";
	public static final String TWEETS_URL = "https://aegis.lucasallegri.xyz/knightlauncher/tweets.txt";
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
