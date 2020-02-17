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

}
