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

import xyz.lucasallegri.logging.KnightLog;

public class FileUtil {
	
	private static final int EXTRACT_BUFFER_SIZE = 8196;
	private static final int HASH_BUFFER_SIZE = 4096;
	
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

    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        createDir(destDirectory);
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                createDir(filePath);
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
    
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[EXTRACT_BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
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
	
	public static String getZipHash(String source) {
	    InputStream file = null;
	    String hash = null;
		try {
			file = new FileInputStream(source);
		} catch (FileNotFoundException ex) {
			KnightLog.logException(ex);
		}
	    ZipInputStream stream = new ZipInputStream(file);
	    try {
	        ZipEntry entry;
	        while((entry = stream.getNextEntry()) != null) {
	            MessageDigest md = MessageDigest.getInstance("MD5");
	            DigestInputStream dis = new DigestInputStream(stream, md);
	            byte[] buffer = new byte[HASH_BUFFER_SIZE];
	            int read = dis.read(buffer);
	            while (read > -1) {
	                read = dis.read(buffer);
	            }
	            hash = new String(dis.getMessageDigest().digest());
	        }
	    } catch (NoSuchAlgorithmException | IOException e) {
			KnightLog.logException(e);
		} finally { 
			try {
				stream.close();
			} catch (IOException e) {
				KnightLog.logException(e);
			}
		}
	    return hash;
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
