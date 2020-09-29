package com.lucasallegri.launcher;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.json.JSONObject;

import com.lucasallegri.dialog.DialogError;
import com.lucasallegri.discord.DiscordInstance;
import com.lucasallegri.launcher.mods.ModList;
import com.lucasallegri.launcher.mods.ModLoader;
import com.lucasallegri.launcher.settings.Settings;
import com.lucasallegri.launcher.settings.SettingsProperties;
import com.lucasallegri.logging.Logging;
import com.lucasallegri.util.DesktopUtil;
import com.lucasallegri.util.FileUtil;
import com.lucasallegri.util.INetUtil;
import com.lucasallegri.util.ImageUtil;
import com.lucasallegri.util.SteamUtil;
import com.lucasallegri.util.SystemUtil;

import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.JMarsDarkTheme;
import mdlaf.themes.MaterialLiteTheme;
import net.sf.image4j.codec.ico.ICOEncoder;

public class BootManager {
	
	public static void onBootStart() {
		
		checkStartLocation();
		
		try {
			Logging.setup();
		} catch (IOException ex) {
			Logging.logException(ex);
		}
		
		SettingsProperties.setup();
		SettingsProperties.loadFromProp();
		setupLauncherStyle();
		setupHTTPSProtocol();
		LanguageManager.setup();
		FontManager.setup();
		DiscordInstance.start();
		KeyboardController.start();
		checkDirectories();
		checkShortcut();
	}
	
	public static void onBootEnd() {
		
		ModLoader.checkInstalled();
		if(Settings.doRebuilds && ModLoader.rebuildRequired) ModLoader.startFileRebuild();
		if(Settings.useIngameRPC) Modules.setupIngameRPC();
		if(!Settings.ucpSetup) Modules.setupUCP();
		if(!FileUtil.fileExists(LauncherConstants.USER_DIR + "/KnightLauncher/modules/safeguard/bundle.zip")) Modules.setupSafeguard();
		
		DiscordInstance.setPresence(LanguageManager.getValue("presence.launch_ready", String.valueOf(ModList.installedMods.size())));
		
		loadOnlineAssets();
	}
	
	private static void checkDirectories() {
		FileUtil.createDir("mods");
		FileUtil.createDir("KnightLauncher/logs/");
		FileUtil.createDir("KnightLauncher/images/");
		FileUtil.createDir("KnightLauncher/modules/");
	}
	
	// Checking if we're being ran inside the game's directory, "getdown-pro.jar" should always be present if so.
	private static void checkStartLocation() {
		if(!FileUtil.fileExists("getdown-pro.jar")) {
			DialogError.push("The .jar file seems to be placed in the wrong directory."
					+ System.lineSeparator() + "Try using the Batch (.bat) file for Windows or the Shell (.sh) file for Linux/MacOS."
					+ System.lineSeparator() + "Detected Steam path: " + SteamUtil.getGamePathWindows());
			DesktopUtil.openDir(SteamUtil.getGamePathWindows());
			System.exit(1);
		}
	}
	
	// Create a shortcut to the application if there's none.
	private static void checkShortcut() {
		if(SystemUtil.isWindows() && Settings.createShortcut
				&& !FileUtil.fileExists(DesktopUtil.getPathToDesktop() + "/" + LauncherConstants.SHORTCUT_FILE_NAME)) {
			
			BufferedImage bimg = ImageUtil.loadImageWithinJar("/img/icon-128.png");
			try {
				ICOEncoder.write(bimg, new File(LauncherConstants.USER_DIR + "/KnightLauncher/images/icon-128.ico"));
			} catch (IOException e) {
				Logging.logException(e);
			}
			
			DesktopUtil.createShellLink(System.getProperty("java.home") + "\\bin\\javaw.exe", 
										"-jar \"" + LauncherConstants.USER_DIR + "\\KnightLauncher.jar\"", 
										LauncherConstants.USER_DIR, 
										LauncherConstants.USER_DIR + "\\KnightLauncher\\images\\icon-128.ico", 
										"Start KnightLauncher", 
										LauncherConstants.SHORTCUT_FILE_NAME
			);
		}
	}
	
	private static void pullGithubData() {
		
//		String rawResponseRepo = INetUtil.getWebpageContent(
//				LauncherConstants.GITHUB_API
//				+ "repos/"
//				+ LauncherConstants.GITHUB_AUTHOR + "/"
//				+ LauncherConstants.GITHUB_REPO
//				);
		
		String rawResponseReleases = INetUtil.getWebpageContent(
				LauncherConstants.GITHUB_API
				+ "repos/"
				+ LauncherConstants.GITHUB_AUTHOR + "/"
				+ LauncherConstants.GITHUB_REPO + "/"
				+ "releases/"
				+ "latest"
				);
		
//		JSONObject jsonRepo = new JSONObject(rawResponseRepo);
		JSONObject jsonReleases = new JSONObject(rawResponseReleases);
		
		LauncherConstants.LATEST_RELEASE = jsonReleases.getString("tag_name");
	}
	
	private static void checkVersion() {
		if (!LauncherConstants.LATEST_RELEASE.equalsIgnoreCase(LauncherConstants.VERSION)) {
			Settings.isOutdated = true;
			LauncherGUI.updateButton.setVisible(true);
		}
	}
	
	private static void setupLauncherStyle() {
		IconFontSwing.register(FontAwesome.getIconFont());
		try {
			UIManager.setLookAndFeel(new MaterialLookAndFeel());
			
			switch(Settings.launcherStyle) {
			case "dark":
				MaterialLookAndFeel.changeTheme(new JMarsDarkTheme());
				break;
			case "light":
				MaterialLookAndFeel.changeTheme(new MaterialLiteTheme());
				break;
			default:
				MaterialLookAndFeel.changeTheme(new MaterialLiteTheme());
				break;
			}
		} catch (UnsupportedLookAndFeelException e) {
			Logging.logException(e);
		}
	}
	
	private static void setupHTTPSProtocol() {
		System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
		System.setProperty("http.agent", "Mozilla/5.0");
		System.setProperty("https.agent", "Mozilla/5.0");
	}
	
	private static void loadOnlineAssets() {
		Thread oassetsThread = new Thread(new Runnable() {
			public void run() {
				
				pullGithubData();
				checkVersion();
				
				int steamPlayers = SteamUtil.getCurrentPlayers("99900");
				if(steamPlayers == 0) {
					LauncherGUI.playerCountLabel.setText(LanguageManager.getValue("error.get_player_count"));
				} else {
					int approximateTotalPlayers = Math.round(steamPlayers * 1.6f);
					LauncherGUI.playerCountLabel.setText(LanguageManager.getValue("m.player_count", new String[] {
							String.valueOf(approximateTotalPlayers), String.valueOf(steamPlayers)
					}));
				}
				
				String tweets = null;
				tweets = INetUtil.getWebpageContent(LauncherConstants.CDN_URL + "tweets.html");
				if(tweets == null) {
					LauncherGUI.tweetsContainer.setText(LanguageManager.getValue("error.tweets_retrieve"));
				} else {
					String styledTweets = tweets.replaceFirst("FONT_FAMILY", LauncherGUI.tweetsContainer.getFont().getFamily())
							.replaceFirst("COLOR", Settings.launcherStyle.equals("dark") ? "#ffffff" : "#000000");
					LauncherGUI.tweetsContainer.setContentType("text/html");
					LauncherGUI.tweetsContainer.setText(styledTweets);
				}
				
				Image eventImage = null;
				String eventImageLang = Settings.lang.startsWith("es") ? "es" : "en";
				eventImage = ImageUtil.getImageFromURL(LauncherConstants.CDN_URL + "event_" + eventImageLang + ".png", 525, 305);
				if(eventImage == null) {
					LauncherGUI.imageContainer.setText(LanguageManager.getValue("error.event_image_missing"));
				} else {
					eventImage = ImageUtil.addRoundedCorners(eventImage, 25);
					LauncherGUI.imageContainer.setText("");
					LauncherGUI.imageContainer.setIcon(new ImageIcon(eventImage));
				}
			}
		});
		oassetsThread.start();
	}

}
