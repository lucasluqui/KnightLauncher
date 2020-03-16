package xyz.lucasallegri.util;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import xyz.lucasallegri.logging.KnightLog;

public class FileUtil {
	
	public static void createDir(String path) {
		new File(path).mkdirs();
	}
	
	public static void createFile(String path) {
		File file = new File(path);
		try {
			file.createNewFile();
		} catch (IOException e) {
			KnightLog.logException(e);
		}
	}
	
	public static void recreateFile(String path) {
		File file = new File(path);
		file.delete();
		try {
			file.createNewFile();
		} catch (IOException e) {
			KnightLog.logException(e);
		}
	}
	
	public static void rename(File old, File dest) {
		old.renameTo(dest);
	}
    
	public static List<String> fileNamesInDirectory(String dir) {
		
		File folder = new File(dir);
		File[] fileList = folder.listFiles();
		List<String> fileNames = new ArrayList<String>();
		
		for(int i = 0; i < fileList.length; i++) {
			if(fileList[i].isDirectory() == false) { fileNames.add(fileList[i].getName()); }
		}
		
		return fileNames;
	}
	
	public static List<String> fileNamesInDirectory(String dir, String ext) {
		
		File folder = new File(dir);
		File[] fileList = folder.listFiles();
		List<String> fileNames = new ArrayList<String>();
		
		for(int i = 0; i < fileList.length; i++) {
			if(fileList[i].isDirectory() == false && fileList[i].toString().contains(ext)) { 
				fileNames.add(fileList[i].getName()); 
			}
		}
		
		return fileNames;
	}
	
	public static boolean fileExists(String path) {
		File file = new File(path);
		return file.exists();
	}
	
	public static String readFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, StandardCharsets.UTF_8);
	}
	
	public static void writeFile(String path, String content) {
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream(path), "utf-8"))) {
			writer.write(content);
		} catch (IOException e) {
			KnightLog.logException(e);
		}
	}

}
