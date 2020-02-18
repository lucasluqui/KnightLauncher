package xyz.lucasallegri.launcher;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

import xyz.lucasallegri.logging.KnightLog;

public class Fonts {
	
	public static Font fontReg = null;
	public static Font fontRegBig = null;
	public static Font fontMed = null;
	public static Font fontMedBig = null;
	public static Font fontMedGiant = null;

	public static void setup() {
		
		InputStream fontRegIs = LauncherGUI.class.getResourceAsStream("/fonts/GoogleSans-Regular.ttf");
		InputStream fontRegBigIs = LauncherGUI.class.getResourceAsStream("/fonts/GoogleSans-Regular.ttf");
		InputStream fontMedIs = LauncherGUI.class.getResourceAsStream("/fonts/GoogleSans-Medium.ttf");
		InputStream fontMedBigIs = LauncherGUI.class.getResourceAsStream("/fonts/GoogleSans-Medium.ttf");
		InputStream fontMedGiantIs = LauncherGUI.class.getResourceAsStream("/fonts/GoogleSans-Medium.ttf");
		
		fontReg = null;
		fontRegBig = null;
		fontMed = null;
		fontMedBig = null;
		fontMedGiant = null;
		
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
