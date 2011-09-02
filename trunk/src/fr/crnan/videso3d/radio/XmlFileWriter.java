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

/**
 * @author mickaël Papail
 * @version 0.1
 * Generates an xml radioCoverage data File coming from radioCoverage data, . Each antenna has 1 radioCoverage data file or more, but only one xmlData File.
 */


// TODO : Ajouter le nom du fichier xml a prendre en compte dans le répertoire.

import java.io.*;

public class XmlFileWriter {

	private File directory = null;
	private File file = null;
	private String fileName = "radioCoverage.xml";
	private FileWriter fileWriter = null;
	
	boolean DEBUG = false;
	/*
	private File[] AntennaFiles = directory.listFiles(new FilenameFilter() {
		public boolean accept (File file, String fileName) {									
			return fileName.contains(FilePatterns.AntennaNamePattern.toString());
		}
	});
	*/
	
	public XmlFileWriter(File directory) {						
		// super (directory.getName());
		this.directory = directory;		
		// makeXmlFileAndWrite();
	}				
			
	public void createXmlFile() {
		try {						
			file = new File(directory,fileName);
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
		return ( file.exists() && file.isFile() && file.canRead());
	}	
	
	public void updateXml() {			
	}		

	public void filesChanged() {		
	}

}
