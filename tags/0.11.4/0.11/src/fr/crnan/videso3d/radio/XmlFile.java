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

package fr.crnan.videso3d.radio;

/***
 * @author mickaël Papail
 * @version 0.2
 * Generates an xml radioCoverage data File coming from radioCoverage data, . Each antenna has 1 or more radioCoverage data, but only one xmlData File.
 * 
 */

//TODO : Ajouter le nom du fichier xml a prendre en compte dans le répertoire.

import java.io.*;

public class XmlFile {

	private File directory = null;
	private File file = null;
	private static FileWriter fileWriter = null;
	private String fileName = null;
	boolean DEBUG = false;
	
	
	public XmlFile(String fileName) {
		this.fileName = fileName;
	}
	
	public  XmlFile(File directory) {						
		// super (directory.getName());
		this.directory = directory;
		this.fileName = "radioCoverage.xml";
		// makeXmlFileAndWrite();
	}				
			
	public void createXmlFile() {
		try {						
			// System.out.println("création fichier ?");			
			file = new File(directory,"radioCoverage.xml");
			if (file.delete()) file.createNewFile();					
			System.out.println("File"+ file);
			
			file.setExecutable(true);
			file.setWritable(true);
			file.setReadable(true);
			
			// fileWriter = new FileWriter(file, true); // with the second parameter to true, text is written each time a the eof.					
			// System.out.println("createXmlFile "+ fileWriter);
		}		
		catch (Exception e) {
			System.out.println("Valeur du file: "+ file);
			e.printStackTrace();
		}
	}
	
	public void createXmlFileWriter() {
		try {							
			fileWriter = new FileWriter(file);
		}				
		catch (Exception e) {			
			e.printStackTrace();
		}
	}
		
	public void openXmlFile() {
		try {			
			// fileWriter = new FileWriter(file, true); // The TRUE parameter is to write each time a the eof.
			fileWriter = new FileWriter(file);
			if (DEBUG) System.out.println("openXmlFile");
		}
		catch (Exception e)  {
			e.printStackTrace();
		}
	}
		
	public void writeXmlFile (String textToWrite) {
		try { 																
			fileWriter.write(textToWrite,0,textToWrite.length());			
		}			
		catch (Exception e){			
			e.printStackTrace();}				
	}	
	
	public boolean xmlFileExists() {
		if (directory.isDirectory()) {
			File[] xmlFile = directory.listFiles(new FilenameFilter() {
				public boolean accept (File file, String fileName) {									
					return fileName.contains("radioCoverage.xml");
				}
			});					
			if  (xmlFile.length !=0) return true;
		}		
		return false;
	}
		
	public void closeXmlFile() {
		try { fileWriter.close(); }
		catch (Exception e) { e.printStackTrace();
			if (DEBUG) System.out.println("Valeur du fileWriter, Exception de la méthode closeXmlFile:"+fileWriter);
		}		
	}

	public void resetXmlFile() {
		file.delete();		
	}
	
	public boolean canRead() {
		return ( file.exists() && file.isFile() && file.canRead() );		
	}

	public boolean canWrite() {
		return ( file.exists() && file.isFile() && file.canWrite());
	}	
	
	public void updateXml() {			
	}		

	public void filesChanged() {		
	}

	/**
	 * Copie source dans destination - On peut copier plusieurs données les unes à la suite des autres dans le même fichier destination.
	 **/
	public  static boolean copy( String source, String destination)
	{
	        System.out.println("Début d'écriture");
			boolean result = false;	        
	        FileInputStream sourceFile=null;	        
	        try {
	                /* open */
	               sourceFile = new FileInputStream(source);
	               OutputStream destinationFile = new FileOutputStream(destination,true);
	                 
	                /* read 0.5MB  segments*/
	                byte buffer[]=new byte[512*1024];
	                int nbRead;
	                
	                while( (nbRead = sourceFile.read(buffer)) != -1 ) {
	                        destinationFile.write(buffer, 0, nbRead);
	                } 
	                
	                /* if copy ok */
	                result = true;
	                destinationFile.close();
	                System.out.println("Fin d'écriture");
	                
	        } catch( java.io.FileNotFoundException f ) {	               
	        } catch( java.io.IOException e ) {	                
	        } finally {	                
	                try {
	                        sourceFile.close();
	                } catch(Exception e) { }
	        } 
	        return( result );
	}
	
	public static void writeIntoFile(String fileName, String stringToWrite) {	
		try {
	BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName, true)); 

			bufferedWriter.write(stringToWrite); 
			bufferedWriter.newLine(); 
			bufferedWriter.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}			
	}
		
}