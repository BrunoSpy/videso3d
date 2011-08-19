/*
 * This file is part of ViDESO.
 * ViDESO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ViDESO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ViDESO.  If not, see <http://www.gnu.org/licenses/>.
*/
package fr.crnan.videso3d;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

/**
 * Manages several kind of files.<br />
 * Manages compressed files.
 * @author Bruno Spyckerelle
 * @version 0.5.2
 */
public class FileManager {

	/**
	 * Counts the number of files in a directory
	 * @param file
	 * @return
	 */
	public static int getFilesCount(File file){
		int count = 0;
		if(file.isDirectory()){
			for(File f : file.listFiles()){
				count += getFilesCount(f);
			}
			return count;
		} else {
			return 1;
		}
	}
	
	/** 
	 * Read a text file into an array of String.
	 */
	public static String[] textFiletoArray(String file) throws IOException {
		BufferedReader rdr = new BufferedReader(new FileReader(file));
		Vector<String> s = new Vector<String>();
		for (;;) {
			String line = rdr.readLine();
			if (line == null) break;
			s.addElement(line);
		}
		String[] a = new String[s.size()];
		s.copyInto(a);
		return a;
	}
	
	/** 
	 * Read a text file into an array of String.
	 */
	public static String[] textFiletoArray(File file) throws IOException {
		BufferedReader rdr = new BufferedReader(new FileReader(file));
		Vector<String> s = new Vector<String>();
		for (;;) {
			String line = rdr.readLine();
			if (line == null) break;
			s.addElement(line);
		}
		String[] a = new String[s.size()];
		s.copyInto(a);
		return a;
	}
	
	/**
	 * Remove temp files created by untar, unzip or gunzip
	 */
	public static void removeTempFiles(){
		for(File f : new File("").getAbsoluteFile().listFiles()){
			if(f.isDirectory() && f.getName().startsWith("temp")){
				for(File d : f.listFiles()){
					d.delete();
				}
				f.delete();
			}
		}
	}
	
	/**
	 * Deletes a file even if it is a non-empty directory
	 * @param file
	 * @return True si suppression réussie
	 */
	public static boolean deleteFile(File file){
		if(file.isFile()){
			return file.delete();
		} else if (file.exists()){
			for(File f : file.listFiles()){
				FileManager.deleteFile(f);
			}
			return file.delete();
		} else {
			return false;
		}
	}
	
	/**
	 * Copy a file to a directory inside the datas repertory
	 * @param file to copy
	 * @return File : the new file
	 */
	public static File copyFile(File file, String dirName){
		
		File destDir = new File(dirName);
		if(!destDir.exists() || !destDir.isDirectory()){
			destDir.mkdirs();	
		}

		File src = file;
		File dest = new File(dirName,src.getName());
		try {
			if(!dest.exists()){
				dest.createNewFile();
			}

			FileChannel source = new FileInputStream(src).getChannel();
			FileChannel destination = new FileOutputStream(dest).getChannel();

			destination.transferFrom(source, 0, source.size());
			
			source.close();
			destination.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return dest;
	}
	
	/**
	 * Copy a file to the datas repertory
	 * @param file to copy
	 * @return File : the new file
	 */
	public static File copyFile(File file){
		File src = file;
		File dest = new File(src.getName());
		try {
			
			if(!dest.exists()){
				dest.createNewFile();
			}

			FileChannel source = new FileInputStream(src).getChannel();
			FileChannel destination = new FileOutputStream(dest).getChannel();

			destination.transferFrom(source, 0, source.size());
			
			source.close();
			destination.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return dest;
	}
	
	/**
	 * Copy a file to the datas repertory
	 * @param path to copy
	 * @return File : the new file
	 */
	public static File copyFile(String path){
		File src = new File(path);
		File dest = new File(src.getName());
		try {
			
			if(!dest.exists()){
				dest.createNewFile();
			}

			FileChannel source = new FileInputStream(src).getChannel();
			FileChannel destination = new FileOutputStream(dest).getChannel();

			destination.transferFrom(source, 0, source.size());
			
			source.close();
			destination.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return dest;
	}
	
	
	/**
	 * Copy a file to the current repertory
	 * @param file to copy
	 * @param newName name of the copied file
	 * @return File : the new file
	 */
	public static File copyFileAs(String path, String newName){
		File src = new File(path);
		File dest = new File(newName);
		try {
			
			if(!dest.exists()){
				dest.createNewFile();
			}

			FileChannel source = new FileInputStream(src).getChannel();
			FileChannel destination = new FileOutputStream(dest).getChannel();

			destination.transferFrom(source, 0, source.size());
			
			source.close();
			destination.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return dest;
	}
	
	
	/**
	 * Recherche le fichier correspondant au chemin en essaynt les différentes casses possibles.
	 * Essaye aussi de trouver le fichier en ajoutant l'extension ".txt"
	 * @param path
	 * @return path
	 */
	public static String getFile(String path){
		File f = new File(path);
		String file = f.getName();
		String rep = f.getParent();
		if((new File(path)).exists()){
			return path;
		} else if((new File(rep+"/"+file.toLowerCase()).exists())){
			return rep+"/"+file.toLowerCase();
		} else if((new File(rep+"/"+file.toUpperCase()).exists())){
			return rep+"/"+file.toUpperCase();
		} else if((new File(rep+"/"+file.toLowerCase()+".txt").exists())){
			return rep+"/"+file.toLowerCase()+".txt";
		} else if((new File(rep+"/"+file.toUpperCase()+".txt").exists())){
			return rep+"/"+file.toUpperCase()+".txt";
		} 
		else {
			return null;
		}
	}
	
	/**
	 * Untar file and returns the list of files
	 * @param file
	 * @return list
	 */
	public static List<File> untar(File file){
		List<File> files = new ArrayList<File>();
		byte[] data = new byte[2048];
		try {
			BufferedOutputStream dest = null;
			TarInputStream tar = new TarInputStream(new BufferedInputStream(new FileInputStream(file)));		
			TarEntry entree;
			int count;
			String repName = "temp"+file.getName();
			File newRep = new File(repName);
			newRep.mkdirs();
			while((entree = tar.getNextEntry()) != null){
				files.add(new File(repName+"/"+entree.getName()));
				dest = new BufferedOutputStream(new FileOutputStream(repName+"/"+entree.getName()), 2048);
				while((count = tar.read(data, 0, 2048)) != -1){
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
			}
			tar.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return files;
	}
	
	/**
	 * GUnzip file and return the unzipped file
	 * @param file File to gunzip
	 * @return unzipped file
	 */
	public static File gunzip(File file){
		String name = null;
		byte[] data = new byte[2048];
		try {
			GZIPInputStream zip = new GZIPInputStream(new FileInputStream(file));
			
			String repName = "temp"+file.getName();
			File newRep = new File(repName);
			newRep.mkdirs();
			
			name = repName + "/" + file.getName().substring(0, file.getName().length() - 3);
			
			OutputStream dest = new FileOutputStream(name);
			
			int count;
			while((count = zip.read(data)) > 0){
				dest.write(data, 0, count);
			}
			
			dest.close();
			zip.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new File(name);
	}
	
	/**
	 * Unzip file to a temporary repertory and return the list of unzipped files<br />
	 * WARNING : if the temporary repertory already exists, it will be deleted before unzipping the new file.
	 * @param file File to unzip
	 * @return List of unzipped files
	 */
	public static List<File> unzip(File file){
		List<File> files = new ArrayList<File>();
		byte[] data = new byte[2048];
		try {
			BufferedOutputStream dest = null;
			ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
			ZipEntry entree;
			int count;
			String repName = "temp"+file.getName();
			File newRep = new File(repName);
			if(newRep.exists()){
				FileManager.deleteFile(newRep);
			}
			newRep.mkdirs();
			while((entree = zip.getNextEntry()) != null){
				File newFile = new File(repName+"/"+entree.getName());
				newFile.getParentFile().mkdirs();
				files.add(newFile);
				dest = new BufferedOutputStream(new FileOutputStream(repName+"/"+entree.getName()), 2048);
				while((count = zip.read(data, 0, 2048)) != -1){
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
			}
			zip.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return files;
	}
	
	/**
	 * 
	 * @param zipFile Zip file
	 * @param files Files to zip
	 * @throws IOException 
	 */
	public static void createZipFile(File zipFile, File[] files) throws IOException{

		// Create the ZIP file

		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

		// Compress the files
		addToZip(files, out);
		
		// Complete the ZIP file
		out.close();

	}
	
	private static void addToZip(File[] files, ZipOutputStream out) throws IOException{
		// Create a buffer for reading the files
		byte[] buf = new byte[1024];

		for(int i=0; i<files.length; i++){
			if(files[i].isDirectory()){
				addToZip(files[i].listFiles(), out);
			} else {
				FileInputStream in = new FileInputStream(files[i]);

				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(files[i].getPath()));

				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				// Complete the entry
				out.closeEntry();
				in.close();
			}
		}
	}
	
	/**
	 * 
	 * @param zipFile
	 * @param file File to be zipped, can be a directory
	 * @throws IOException 
	 */
	public static void createZipFile(File zipFile, File file) throws IOException{
		File[] files = {file};
		createZipFile(zipFile, files);
	}
	

	
}
