package xyz.lucasallegri.util;

public class SystemUtil {
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	
	public static boolean isWindows() {
		return OS.contains("win");
	}
	
	public static boolean isMac() {
		return OS.contains("mac");
	}
	
	public static boolean isUnix() {
		return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
	}
    
	public static boolean is64Bit() {
		boolean is64Bit;
		if (isWindows()) {
			is64Bit = (System.getenv("ProgramFiles(x86)") != null);
		} else {
			is64Bit = (System.getProperty("os.arch").indexOf("64") != -1);
		}
		return is64Bit;
	}
	
	public static boolean isARM() {
		boolean isARM = false;
		if(System.getProperty("os.arch").contains("arm")) {
			isARM = true;
		}
		return isARM;
	}
	
	public static boolean hasValidJavaHome() {
		return System.getProperty("java.home").contains("1.6") || 
				System.getProperty("java.home").contains("1.7") ||
				System.getProperty("java.home").contains("1.8");
	}

}
