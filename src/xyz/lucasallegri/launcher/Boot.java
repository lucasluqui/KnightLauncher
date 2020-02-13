package xyz.lucasallegri.launcher;

import java.util.List;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import twitter4j.Status;
import twitter4j.TwitterException;

import javax.swing.UIManager.LookAndFeelInfo;

import xyz.lucasallegri.util.TwitterUtil;

public class Boot {
	
	public static void onBootStart() {
		
		setupLookAndFeel();
		
//		List<Status> ses = null;
//		String parsedTweets = "<html>";
//		
//		try {
//			ses = TwitterUtil.getStatuses("SpiralKnights", 5);
//		} catch (TwitterException e) {
//			e.printStackTrace();
//		}
//		
//		for(int i = 0; i < ses.size(); i++) {
//			Status status = ses.get(i);
//			parsedTweets = parsedTweets + status.getText() + "<br>";			
//		}
//		
//		parsedTweets = parsedTweets + "</html>";
//		
//		LauncherGUI.tweetsContainer.setText(parsedTweets);
		
	}
	
	public static void onBootEnd() {
		
		populatePlatforms();
		
	}
	
	private static void setupLookAndFeel() {
		
		for( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
			if( "Windows".equals(info.getName()) ) {
				try {
					UIManager.setLookAndFeel(info.getClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private static void populatePlatforms() {
		//LauncherGUI.platformChoice.add("Steam");
		//LauncherGUI.platformChoice.add("Standalone");
	}

}
