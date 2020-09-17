package com.lucasallegri.launcher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.apache.commons.io.FileUtils;

import com.lucasallegri.dialog.DialogError;
import com.lucasallegri.launcher.settings.SettingsProperties;
import com.lucasallegri.logging.Logging;
import com.lucasallegri.util.ColorUtil;
import com.lucasallegri.util.Compressor;
import com.lucasallegri.util.FileUtil;
import com.lucasallegri.util.ProcessUtil;

import javax.swing.JButton;

public class JVMPatcher {

	public static JFrame jvmPatcherFrame;
	private static JLabel headerLabel;
	private static JLabel subHeaderLabel;
	private static JButton buttonAccept;
	private static JButton buttonDecline;
	private static JProgressBar jvmPatcherProgressBar;
	private static JLabel jvmPatcherState;
	
	int pY, pX;

	public JVMPatcher(LauncherApp app) {
		initialize();
	}
	
	@SuppressWarnings("static-access")
	public void switchVisibility() {
		this.jvmPatcherFrame.setVisible(this.jvmPatcherFrame.isVisible() ? false : true);
	}

	private void initialize() {
		jvmPatcherFrame = new JFrame();
		jvmPatcherFrame.setVisible(false);
		jvmPatcherFrame.setTitle(LanguageManager.getValue("t.jvm_patcher"));
		jvmPatcherFrame.setBounds(100, 100, 500, 250);
		jvmPatcherFrame.setResizable(false);
		jvmPatcherFrame.setUndecorated(true);
		jvmPatcherFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		jvmPatcherFrame.getContentPane().setLayout(null);
		
		headerLabel = new JLabel(LanguageManager.getValue("m.jvm_patcher_confirm_header"));
		headerLabel.setBounds(10, 40, 480, 37);
		headerLabel.setFont(FontManager.fontRegBig);
		headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
		jvmPatcherFrame.getContentPane().add(headerLabel);
		
		subHeaderLabel = new JLabel(LanguageManager.getValue("m.jvm_patcher_confirm_subheader"));
		subHeaderLabel.setBounds(10, 65, 480, 37);
		subHeaderLabel.setFont(FontManager.fontReg);
		subHeaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
		jvmPatcherFrame.getContentPane().add(subHeaderLabel);
		
		jvmPatcherState = new JLabel("");
		jvmPatcherState.setBounds(11, 180, 480, 15);
		jvmPatcherState.setFont(FontManager.fontReg);
		jvmPatcherFrame.getContentPane().add(jvmPatcherState);
		
		jvmPatcherProgressBar = new JProgressBar();
		jvmPatcherProgressBar.setBounds(10, 204, 480, 5);
		jvmPatcherProgressBar.setVisible(false);
		jvmPatcherFrame.getContentPane().add(jvmPatcherProgressBar);
		
		buttonAccept = new JButton(LanguageManager.getValue("b.jvm_patcher_accept"));
		buttonAccept.setFocusPainted(false);
		buttonAccept.setFocusable(false);
		buttonAccept.setFont(FontManager.fontMed);
		buttonAccept.setBounds(30, 200, 200, 23);
		jvmPatcherFrame.getContentPane().add(buttonAccept);
		buttonAccept.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				buttonAccept.setEnabled(false);
				buttonAccept.setVisible(false);
				buttonDecline.setEnabled(false);
				buttonDecline.setVisible(false);
				headerLabel.setText(LanguageManager.getValue("m.jvm_patcher_header"));
				subHeaderLabel.setText(LanguageManager.getValue("m.jvm_patcher_subheader"));
				jvmPatcherProgressBar.setVisible(true);
				initPatcher();
			}
		});
		
		buttonDecline = new JButton(LanguageManager.getValue("b.jvm_patcher_decline"));
		buttonDecline.setFocusPainted(false);
		buttonDecline.setFocusable(false);
		buttonDecline.setFont(FontManager.fontMed);
		buttonDecline.setBounds(360, 200, 110, 23);
		jvmPatcherFrame.getContentPane().add(buttonDecline);
		buttonDecline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent _action) {
				finish();
			}
		});
		
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
		
		JLabel windowTitle = new JLabel(LanguageManager.getValue("t.jvm_patcher"));
		windowTitle.setFont(FontManager.fontMed);
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
		jvmPatcherState.setText(LanguageManager.getValue("m.jvm_patcher_download", "74"));
		downloadPackagedJVM();
		
		jvmPatcherProgressBar.setValue(2);
		jvmPatcherState.setText(LanguageManager.getValue("m.jvm_patcher_delete"));
		try {
			if(!FileUtil.fileExists(LauncherConstants.USER_DIR + "\\java_vm_unpatched")) {
				FileUtils.moveDirectory(new File(LauncherConstants.USER_DIR + "\\java_vm"), new File(LauncherConstants.USER_DIR + "\\java_vm_unpatched"));
			}
		} catch (IOException e) {
			Logging.logException(e);
		}
		
		jvmPatcherProgressBar.setValue(3);
		jvmPatcherState.setText(LanguageManager.getValue("m.jvm_patcher_extract"));
		Compressor.unzip(LauncherConstants.USER_DIR + "\\jvm_pack.zip", LauncherConstants.USER_DIR, false);
		new File(LauncherConstants.USER_DIR + "\\jvm_pack.zip").delete();
		
		jvmPatcherProgressBar.setValue(4);
		jvmPatcherState.setText(LanguageManager.getValue("m.jvm_patcher_finish"));
		finish();
	}
	
	private static void downloadPackagedJVM() {
		
		String downloadUrl = "https://gitcdn.link/repo/" 
				+ LauncherConstants.GITHUB_AUTHOR + "/"
				+ LauncherConstants.GITHUB_REPO
				+ "/master/jvm/jvm_pack_windows.zip";
		
		Logging.log.info("Downloading Java VM from: " + downloadUrl);
		try {
			FileUtils.copyURLToFile(
				new URL(downloadUrl),
				new File(LauncherConstants.USER_DIR + "\\jvm_pack.zip"), 
				0,
				0
			);
		} catch (IOException e) {
			DialogError.push("The Java VM download couldn't be initiated, will avoid patching on next boot.");
			SettingsProperties.setValue("launcher.jvm_patched", "true");
			Logging.logException(e);
			System.exit(1);
		}
	}
	
	private static void finish() {
		SettingsProperties.setValue("launcher.jvm_patched", "true");
		ProcessUtil.startApplication(new String[] {"java", "-jar", LauncherConstants.USER_DIR + "\\KnightLauncher.jar"});
		jvmPatcherFrame.dispose();
		System.exit(1);
	}
}

