package com.lucasallegri.launcher;

import java.io.File;

public class LauncherConstants {

	public static final String VERSION = "1.3.7";
	public static String LATEST_RELEASE;
	
	public static final String SHORTCUT_FILE_NAME = "Knight Launcher";
	
	public static final String GITHUB_API = "https://api.github.com/";
	public static final String GITHUB_AUTHOR = "lucas-allegri";
	public static final String GITHUB_REPO = "KnightLauncher";
	public static final String GITHUB_MAIN_BRANCH = "master";
	
	public static final String CDN_URL = "https://knightlauncher-cdn.lucasallegri.com/knightlauncher/v1/";
	public static final String LARGE_CDN_URL = "https://knightlauncher-large-cdn.lucasallegri.com/knightlauncher/v1/";
	public static final String DISCORD_URL = "https://discord.gg/RAf499a";
	public static final String GET_MODS_URL = "https://discord.gg/BZRyJrr";
	public static final String KOFI_URL = "https://ko-fi.com/lucasallegri";
	public static final String BUG_REPORT_URL = "https://github.com/lucas-allegri/KnightLauncher/issues";
	
	public static final String USER_DIR = System.getProperty("user.dir");
	
	
	public static final String[] GETDOWN_ARGS_WIN = {".\\java_vm\\bin\\java.exe",
			"-Dsun.java2d.d3d=false",
			"-Dcheck_unpacked=true",
			"-jar",
			USER_DIR + File.separator + "getdown-pro.jar",
			".",
			"client"
	};
	
	public static final String[] GETDOWN_ARGS = {"./java/bin/java",
			"-Dsun.java2d.d3d=false",
			"-Dcheck_unpacked=true",
			"-jar",
			"./getdown-pro.jar",
			".",
			"client"
	};
	
	public static final String[] ALT_CLIENT_WIN = {".\\java_vm\\bin\\java.exe",
			"-classpath code/config.jar;code/projectx-config.jar;code/projectx-pcode.jar;code/lwjgl.jar;code/lwjgl_util.jar;code/jinput.jar;code/jutils.jar;code/jshortcut.jar;code/commons-beanutils.jar;code/commons-digester.jar;code/commons-logging.jar",
			"-Dcom.threerings.getdown=false",
			"-XX:+AggressiveOpts",
			"-XX:SoftRefLRUPolicyMSPerMB=10",
			"-Djava.library.path=\\native",
			"-Dorg.lwjgl.util.NoChecks=true",
			"-Dsun.java2d.d3d=false",
			"-Dappdir=\\spiral",
			"-Dresource_dir=\\rsrc",
			"-Xms256M",
			"-Xmx512M",
			"-XX:+UseStringDeduplication",
			"com.threerings.projectx.client.ProjectXApp"
	};
	
	public static final String[] ALT_CLIENT = {"./java/bin/java",
			"-classpath ./code/config.jar:./code/projectx-config.jar:./code/projectx-pcode.jar:./code/lwjgl.jar:./code/lwjgl_util.jar:./code/jinput.jar:./code/jutils.jar:./code/jshortcut.jar:./code/commons-beanutils.jar:./code/commons-digester.jar:./code/commons-logging.jar",
			"-Dcom.threerings.getdown=false",
			"-XX:+AggressiveOpts",
			"-XX:SoftRefLRUPolicyMSPerMB=10",
			"-Djava.library.path=./native",
			"-Dorg.lwjgl.util.NoChecks=true",
			"-Dsun.java2d.d3d=false",
			"-Dappdir=./spiral",
			"-Dresource_dir=./rsrc",
			"-Xms256M",
			"-Xmx512M",
			"-XX:+UseStringDeduplication",
			"com.threerings.projectx.client.ProjectXApp"
	};
	
}
