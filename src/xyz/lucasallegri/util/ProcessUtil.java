package xyz.lucasallegri.util;

import java.io.IOException;

import xyz.lucasallegri.logging.KnightLog;

public class ProcessUtil {
	
	public static void startApplication(String commandLine) {
		try {
			final Process p = Runtime.getRuntime().exec(commandLine);
		} catch (IOException e) {
			KnightLog.logException(e);
		}
	}
	
	public static void startApplication(String[] args) {
		try {
			final Process p = Runtime.getRuntime().exec(args);
		} catch (IOException e) {
			KnightLog.logException(e);
		}
	}

}
