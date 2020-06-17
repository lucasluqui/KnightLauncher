package xyz.lucasallegri.launcher;

import xyz.lucasallegri.dialog.DialogError;
import xyz.lucasallegri.launcher.settings.SettingsProperties;
import xyz.lucasallegri.logging.KnightLog;
import xyz.lucasallegri.util.ColorUtil;
import xyz.lucasallegri.util.Compressor;
import xyz.lucasallegri.util.INetUtil;
import xyz.lucasallegri.util.ProcessUtil;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.apache.commons.io.FileUtils;

public class JVMPatcher {

	public static JFrame jvmPatcherFrame;
	private static JProgressBar jvmPatcherProgressBar;
	private static JLabel jvmPatcherState;
	
	int pY, pX;

	public static void compose() {
		EventQueue.invokeLater(new Runnable() {

			public void run() {
				try {
					new JVMPatcher();
				} catch (Exception e) {
					KnightLog.logException(e);
				}
			}
		});
	}

	public JVMPatcher() {
		initialize();
		initPatcher();
	}

	private void initialize() {
		jvmPatcherFrame = new JFrame();
		jvmPatcherFrame.setTitle(Language.getValue("t.jvm_patcher"));
		jvmPatcherFrame.setBounds(100, 100, 500, 250);
		jvmPatcherFrame.setResizable(false);
		jvmPatcherFrame.setUndecorated(true);
		jvmPatcherFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		jvmPatcherFrame.getContentPane().setLayout(null);
		
		JLabel headerLabel = new JLabel(Language.getValue("m.jvm_patcher_header"));
		headerLabel.setBounds(10, 40, 480, 37);
		headerLabel.setFont(Fonts.fontRegBig);
		headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
		jvmPatcherFrame.getContentPane().add(headerLabel);
		
		JLabel subHeaderLabel = new JLabel(Language.getValue("m.jvm_patcher_subheader"));
		subHeaderLabel.setBounds(10, 65, 480, 37);
		subHeaderLabel.setFont(Fonts.fontReg);
		subHeaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
		jvmPatcherFrame.getContentPane().add(subHeaderLabel);
		
		jvmPatcherState = new JLabel("");
		jvmPatcherState.setBounds(11, 180, 480, 15);
		jvmPatcherState.setFont(Fonts.fontReg);
		jvmPatcherFrame.getContentPane().add(jvmPatcherState);
		
		jvmPatcherProgressBar = new JProgressBar();
		jvmPatcherProgressBar.setBounds(10, 204, 480, 5);
		jvmPatcherProgressBar.setVisible(true);
		jvmPatcherFrame.getContentPane().add(jvmPatcherProgressBar);
		
		JPanel titleBar = new JPanel();
		titleBar.setBounds(0, 0, jvmPatcherFrame.getWidth(), 20);
		titleBar.setBackground(ColorUtil.getTitleBarColor());
		jvmPatcherFrame.getContentPane().add(titleBar);
		
		
		/*
		 * Based on Paul Samsotha's reply @ StackOverflow
		 * link: https://stackoverflow.com/questions/24476496/drag-and-resize-undecorated-jframe
		 */
		titleBar.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent me) {
		    	
		        pX = me.getX();
		        pY = me.getY();
		    }
		});
		titleBar.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				
		        pX = me.getX();
		        pY = me.getY();
		    }
		
		    @Override 
		    public void mouseDragged(MouseEvent me) {
		
		    	jvmPatcherFrame.setLocation(jvmPatcherFrame.getLocation().x + me.getX() - pX,
		    	jvmPatcherFrame.getLocation().y + me.getY() - pY);
		    }
		});
		titleBar.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent me) {
		
				jvmPatcherFrame.setLocation(jvmPatcherFrame.getLocation().x + me.getX() - pX,
				jvmPatcherFrame.getLocation().y + me.getY() - pY);
		    }
		
			@Override
			public void mouseMoved(MouseEvent arg0) {
				// Auto-generated method stub
			}
		});
		titleBar.setLayout(null);
		
		JLabel windowTitle = new JLabel(Language.getValue("t.jvm_patcher"));
		windowTitle.setFont(Fonts.fontMed);
		windowTitle.setBounds(10, 0, jvmPatcherFrame.getWidth() - 100, 20);
		titleBar.add(windowTitle);
		
		jvmPatcherFrame.setLocationRelativeTo(null);
		jvmPatcherFrame.setVisible(true);
		
	}
	
	private static void initPatcher() {
		Thread patchThread = new Thread(new Runnable() {
			public void run() {
				patch();
			}
		});
		patchThread.start();
	}
	
	private static void patch() {
		jvmPatcherProgressBar.setMaximum(4);
		jvmPatcherProgressBar.setValue(1);
		jvmPatcherState.setText(Language.getValue("m.jvm_patcher_download", "74"));
		downloadPackagedJVM();
		
		jvmPatcherProgressBar.setValue(2);
		jvmPatcherState.setText(Language.getValue("m.jvm_patcher_delete"));
		try {
			FileUtils.deleteDirectory(new File(LauncherConstants.USER_DIR + "\\java_vm"));
		} catch (IOException e) {
			KnightLog.logException(e);
		}
		
		jvmPatcherProgressBar.setValue(3);
		jvmPatcherState.setText(Language.getValue("m.jvm_patcher_extract"));
		Compressor.unzip(LauncherConstants.USER_DIR + "\\jvm_pack.zip", LauncherConstants.USER_DIR, false);
		new File(LauncherConstants.USER_DIR + "\\jvm_pack.zip").delete();
		
		jvmPatcherProgressBar.setValue(4);
		jvmPatcherState.setText(Language.getValue("m.jvm_patcher_finish"));
		SettingsProperties.setValue("launcher.jvm_patched", "true");
		ProcessUtil.startApplication(new String[] {"java", "-jar", LauncherConstants.USER_DIR + "\\KnightLauncher.jar"});
		
		jvmPatcherFrame.dispose();
		System.exit(1);
	}
	
	private static void downloadPackagedJVM() {
		String downloadUrl = INetUtil.getWebpageContent(LauncherConstants.JVM_DOWNLOAD_URL);
		KnightLog.log.info(downloadUrl);
		try {
			FileUtils.copyURLToFile(
					  new URL(downloadUrl), 
					  new File(LauncherConstants.USER_DIR + "\\jvm_pack.zip"), 
					  0, 
					  0);
		} catch (IOException e) {
			DialogError.push("The Java VM download couldn't be initiated, will avoid patching on next boot.");
			SettingsProperties.setValue("launcher.jvm_patched", "true");
			KnightLog.logException(e);
			System.exit(1);
		}
	}
}

