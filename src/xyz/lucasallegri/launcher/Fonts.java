package xyz.lucasallegri.launcher;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

import xyz.lucasallegri.launcher.settings.Settings;
import xyz.lucasallegri.logging.KnightLog;

public class Fonts {
	
	private static final String fontPathRegular = "/fonts/GoogleSans-Regular.ttf";
	private static final String fontPathMedium = "/fonts/GoogleSans-Medium.ttf";
	private static final String fontPathRegularJP = "/fonts/NotoSansJP-Regular.otf";
	private static final String fontPathMediumJP = "/fonts/NotoSansJP-Medium.otf";
	public static Font fontReg = null;
	public static Font fontRegBig = null;
	public static Font fontMed = null;
	public static Font fontMedBig = null;
	public static Font fontMedGiant = null;

	public static void setup() {
		
		InputStream fontRegIs;
		InputStream fontRegBigIs;
		InputStream fontMedIs;
		InputStream fontMedBigIs;
		InputStream fontMedGiantIs;
		
		if(Settings.lang.contains("jp")) {
			fontRegIs = LauncherGUI.class.getResourceAsStream(fontPathRegularJP);
			fontRegBigIs = LauncherGUI.class.getResourceAsStream(fontPathRegularJP);
			fontMedIs = LauncherGUI.class.getResourceAsStream(fontPathMediumJP);
			fontMedBigIs = LauncherGUI.class.getResourceAsStream(fontPathMediumJP);
			fontMedGiantIs = LauncherGUI.class.getResourceAsStream(fontPathMediumJP);
		} else {
			fontRegIs = LauncherGUI.class.getResourceAsStream(fontPathRegular);
			fontRegBigIs = LauncherGUI.class.getResourceAsStream(fontPathRegular);
			fontMedIs = LauncherGUI.class.getResourceAsStream(fontPathMedium);
			fontMedBigIs = LauncherGUI.class.getResourceAsStream(fontPathMedium);
			fontMedGiantIs = LauncherGUI.class.getResourceAsStream(fontPathMedium);
		}
		
		try {
			
			fontReg = Font.createFont(Font.TRUETYPE_FONT, fontRegIs);
			fontReg = fontReg.deriveFont(11.0f);
			fontReg = fontReg.deriveFont(Font.ITALIC);
			
			fontRegBig = Font.createFont(Font.TRUETYPE_FONT, fontRegBigIs);
			fontRegBig = fontRegBig.deriveFont(14.0f);
			fontRegBig = fontRegBig.deriveFont(Font.ITALIC);
			
			fontMed = Font.createFont(Font.TRUETYPE_FONT, fontMedIs);
			fontMed = fontMed.deriveFont(11.0f);
			
			fontMedBig = Font.createFont(Font.TRUETYPE_FONT, fontMedBigIs);
			fontMedBig = fontMedBig.deriveFont(14.0f);

			fontMedGiant = Font.createFont(Font.TRUETYPE_FONT, fontMedGiantIs);
			fontMedGiant = fontMedGiant.deriveFont(40.0f);
			
		} catch (FontFormatException | IOException e) {
			KnightLog.logException(e);
		}
		
	}
	
}
