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
* @author mickael papail
* @version 0.4
* 
* Directory scanner.
* Generates xml data file for each Antenna. Each antenna has a directory containing every Flight level 
* propagation data in text format calculated using radio mobile software
* The propagation limit is 40db microVolt/m
**/


import java.io.*;
import java.io.FilenameFilter;

// import java.util.regex.*; 
// import java.net.URL;

import fr.crnan.videso3d.radio.RadioFileReader;
import fr.crnan.videso3d.radio.FilePatterns;
import javax.swing.SwingWorker;


public class RadioDirectoryReader extends SwingWorker <Integer,String>{
	
	private String directoryPath;	//  root directory path
	//  TODO  Récupérer le nom uniquement du répertoire, et pas tout le chemin
	private String antennaName;
	
	private File directoryToScan;	 	
	private boolean DEBUG = false;
	
	public RadioDirectoryReader(String directoryPath) {
		this.directoryPath = directoryPath;		
	}
	
	public Integer doInBackground() {
		this.setProgress(0);	
		this.scanDirectoriesList(new File(directoryPath));		
		this.setProgress(100);
		return 0;
	}
		
	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}
		
	/**
	 * Recursive directories scan, and radioCoverage xml file generation into each directory (only if doesn't exists, or if sources data have been modified).
	* Scanne complet de tous les reps si currentDir est la racine, ou bien scan d'un répertoire particulier.
	*/	
	public void scanDirectoriesList(File currentDir) {
		
		int nbDirs=0;
//		double progressStart = 0;
//		double progressEnd = 100;
	// double step = (progressEnd - progressStart)/ nbDirs;
		
		if (currentDir.isDirectory()) {
						
			File[] files = getFilesFromList(FilePatterns.AntennaNamePattern.toString(),currentDir);
						
			if (files.length !=0 ) {
				if (DEBUG) for (int i =0;i<files.length;i++) {System.out.println(files[i].getName());}																					
				if (DEBUG )System.out.println("le rep courant est " + currentDir.getAbsolutePath());
				antennaName = currentDir.getName();
				XmlFileWriter xmlFile = new XmlFileWriter(currentDir);						
				if (xmlFileUpToDate(currentDir)) {
				//if (xmlFileExists(currentDir) && nodeChanged(currentDir)) {
					// xmlFile.openXmlFile(currentDir)
					xmlFile.openXmlFile();
				}
				else {
					xmlFile.createXmlFile();								
					xmlFile.createXmlFileWriter();
				}
				
				// if (xmlFile.canWrite())								
				// Open and works with each text File
				
				if (files.length != 0) {					
					for (int i=0;i<files.length;i++) {														
						// Ecriture de l'entete xml en début de fichier  + la racine du document!
						//<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"+"\n\t"						
						if (i==0) xmlFile.writeXmlFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+"\n"+"<Root>"+"\n");
						if (i==0) xmlFile.writeXmlFile("<AntennaName>"+"\n"+antennaName+"</AntennaName>"+"\n");
						RadioFileReader radioFileReader = new RadioFileReader(files[i]);																	
						//TODO		// xmlFile.writeXmlFile(radioFileReader.scan());		
						//TODO ordonner les fichiers FLxx.txt 
						radioFileReader.ArrayScan();
						xmlFile.writeXmlFile(radioFileReader.toString());					
						// fermeture de la racine du document en fin de fichier
						if (i == (files.length-1)) {xmlFile.writeXmlFile("</Root>"+"\n");}
					}
					xmlFile.closeXmlFile();																																 				
			}
			}
			// else, file is the root directory containing sub directories. 			 
			if ((currentDir.listFiles() != null) && (files.length == 0) ) {												
			  	File[] dirs = currentDir.listFiles(new FileFilter() {							 		
			  		public boolean accept (File file) {																					
			 			return file.isDirectory(); 					
					}
				});													  	
			  	nbDirs = dirs.length;
			  	
				// recursive scan of directories
				for (int i=0;i<dirs.length;i++) {
					scanDirectoriesList(dirs[i]);
				}
			}						
			 if (currentDir.listFiles() == null) System.out.println("repertoire vide");						
			 //	 if (!dir.getPath().equals(type)) {
			 //	 Resize size = new Resize(dir.getPath());										
			 //}
		}					
	}
					
	/**	 
	 * @return Files list in the directory 
	 * */
	public File[] listFiles(String directoryPath){
		File[] files = null;
		directoryToScan = new File(directoryPath);
		files = directoryToScan.listFiles();
		return files;
	}
			
	public File[] getFilesFromList(String pattern, File currentDir) {
		final String currentPattern = pattern;
		File[] files = currentDir.listFiles(new FilenameFilter() {
			public boolean accept (File file, String fileName) {									
				// If file is a directory, he probably contains files with the FLxxx pattern (child nodes of the tree)
				return fileName.matches(currentPattern);										
			}
		});
		return files;
	}
	

// commenter la condition if en cas de nullPointerException sur le getFilesFromList pour bloquer la mise à jour des fichiers à chaque exécution
	public Boolean xmlFileExists(File currentDir) {
		// System.out.println("Le répertoire courant pour l'appel de le methode xmlFileExists est : "+currentDir.toString());		
		if (getFilesFromList(FilePatterns.xmlFilePattern.toString(),currentDir).length!=0) { return true;}
		return false;
	}
	
	/**
	 * Tests whether node files have changed in one of the node directories or not 
	 * */
	public boolean xmlFileUpToDate (File currentDir) {
	//public boolean nodeChanged(File currentDir) {		
		File[] AntennaFiles = getFilesFromList(FilePatterns.AntennaNamePattern.toString(),currentDir); 
		if (xmlFileExists(currentDir)) {
			File[] xmlFile = getFilesFromList(FilePatterns.xmlFilePattern.toString(),currentDir); 			
			Boolean test=true;	
			for (int i=0;i<AntennaFiles.length;i++) {				
				if (AntennaFiles[i].lastModified()>xmlFile[0].lastModified()) { // si le lastModified est inférieur alors le fichier est plus ancien
					test=false;	// Dès qu'on trouve un fichier plus récent que le fichier radioCoverage.xml, alors le test est faux
				}
			return test;
			}											
		}		
		return false;
	}
	
	
}