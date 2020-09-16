package com.lucasallegri.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

import com.lucasallegri.logging.Logging;

public class FileUtil {
	
	public static void createDir(String path) {
		new File(path).mkdirs();
	}
	
	public static void createFile(String path) {
		File file = new File(path);
		try {
			file.createNewFile();
		} catch (IOException e) {
			Logging.logException(e);
		}
	}
	
	public static void recreateFile(String path) {
		File file = new File(path);
		file.delete();
		try {
			file.createNewFile();
		} catch (IOException e) {
			Logging.logException(e);
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
			if(fileList[i].isDirectory() == false && fileList[i].toString().endsWith(ext)) { 
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
			Logging.logException(e);
		}
	}
	
    /*
     * Method to convert InputStream to String
     */
    public static String convertInputStreamToString(InputStream is) {
        
        BufferedReader br = null;
        StringBuilder sbContent = new StringBuilder();
        
        try {
            /*
             * Create BufferedReader from InputStreamReader 
             */
            br = new BufferedReader(new InputStreamReader(is));
            
            /*
             * read line by line and append content to
             * StringBuilder
             */
            String strLine = null;
            boolean isFirstLine = true;
            
            while((strLine = br.readLine()) != null) {
                if(isFirstLine) {
                	sbContent.append(strLine);
                } else {
                	sbContent.append("\n").append(strLine);
                }
                
                /*
                 * Flag to make sure we don't append new line
                 * before the first line. 
                 */
                isFirstLine = false;
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {            
            try {
                if(br  != null) br.close();
            } catch(Exception e) { 
            	e.printStackTrace();
            }
        }
        
        //convert StringBuilder to String and return
        return sbContent.toString();
    }
    
    public static void extractFileWithinJar(String pathInside, String pathOutside) throws IOException {
		URL filePath = FileUtil.class.getResource(pathInside);
		File destPath = new File(pathOutside);
		FileUtils.copyURLToFile(filePath, destPath);
    }

}
