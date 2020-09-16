package com.lucasallegri.util;

import java.io.IOException;

import com.lucasallegri.logging.Logging;

public class ProcessUtil {
	
	@SuppressWarnings("unused")
	public static void startApplication(String commandLine) {
		try {
			final Process p = Runtime.getRuntime().exec(commandLine);
		} catch (IOException e) {
			Logging.logException(e);
		}
	}
	
	@SuppressWarnings("unused")
	public static void startApplication(String[] args) {
		try {
			final Process p = Runtime.getRuntime().exec(args);
		} catch (IOException e) {
			Logging.logException(e);
		}
	}

}
