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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

/**
 * Manages several kind of files
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class FileManager {

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
	 * Untar file and returns the list of files
	 * @param file
	 * @return list
	 */
	public static List<File> untar(File file){
		List<File> files = new LinkedList<File>();
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
	 * Unzip file and return the list of unzipped files
	 * @param file File to unzip
	 * @return List of unzipped files
	 */
	public static List<File> unzip(File file){
		List<File> files = new LinkedList<File>();
		byte[] data = new byte[2048];
		try {
			BufferedOutputStream dest = null;
			ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
			ZipEntry entree;
			int count;
			String repName = "temp"+file.getName();
			File newRep = new File(repName);
			newRep.mkdirs();
			while((entree = zip.getNextEntry()) != null){
				files.add(new File(repName+"/"+entree.getName()));
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
	
}