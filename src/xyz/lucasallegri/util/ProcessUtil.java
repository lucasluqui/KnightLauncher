package xyz.lucasallegri.util;

import java.io.IOException;

public class ProcessUtil {
	
	public static void startApplication(String commandLine) {
		try {
			final Process p = Runtime.getRuntime().exec(commandLine);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void startApplication(String[] args) {
		try {
			final Process p = Runtime.getRuntime().exec(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
