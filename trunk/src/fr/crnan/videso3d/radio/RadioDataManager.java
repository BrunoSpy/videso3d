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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
// import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.FileParser;
import fr.crnan.videso3d.DatabaseManager.Type;
// import fr.crnan.videso3d.formats.xstream.PolygonSerializer;
import fr.crnan.videso3d.formats.xml.PolygonDeserializer;
import fr.crnan.videso3d.formats.xml.SaxonFactory;
import gov.nasa.worldwind.render.airspaces.Airspace;

/**
 * Gestion formatage des données, mise à jour, etc...
 * @author mickael papail
 * @version 0.2
* */
		
public class RadioDataManager extends FileParser {
// TODO test---------------------------	
	/**
	 * Nombre de fichiers gérés
	 */
	private int numberFiles = 0;	//ici numberStep
	private int currentProgress=0;
	private String name="radio";
	private Connection conn;	
//TODO fin test-------------------------	
	
				
	private String airspaceType;
//	private File airspaceTypeFile;			
	private String  directoryPath;	
	private File directory;
	private String outputXmlFilePath;
	private RadioDirectoryReader radioDirectoryReader = new RadioDirectoryReader(airspaceType);
	private File[] rootDirs;	// Répertoires à la racine du rep ppal. 
	private ArrayList<Airspace> airspaces;
	
	public RadioDataManager() {			
	}		
			
	/**
	 * @param string
	 * @param airspaceType : (nom générique pour radioCovname="radioCov"))
	 */	
	public RadioDataManager(String path) {
		
// TODO test-----------------------------				
		super(path);
// TODO fin test-----------------------------
		
		this.directoryPath = path;												
		directory = new File(directoryPath);		
		radioDirectoryReader = new RadioDirectoryReader(directoryPath);				
		outputXmlFilePath = directoryPath+"/radioOutput.xml";		
		
		rootDirs = directory.listFiles(new FileFilter() {						 		
	  		public boolean accept (File file) {																					
	 			return (file.isDirectory()); 
	 			} 							  				
		});				
	}		
	
	public ArrayList<Airspace> getAirspaces() {
		return this.airspaces;
	}		
	
	/**
	 **/						
	public  ArrayList<Airspace> loadData() {
		//boolean test = false;
		boolean xmlUpToDate = true;
		try {			
			// Fichiers xml de sortie à jour ??
			if (rootDirs.length != 0) {
				for (int i=0;i<rootDirs.length;i++) {					
					if (!(radioDirectoryReader.xmlFileUpToDate(rootDirs[i]))) {
						xmlUpToDate = false;
					}	
				}				
				if (!xmlUpToDate)  { 					
					radioDirectoryReader.scanDirectoriesList(directory); // scan complet des nouveaux fichiers						
					this.computeXSL();
				}				
			}
			if (new File(outputXmlFilePath).exists()) {			
			System.out.println("Debut de désérialisation");
			PolygonDeserializer polygonDeserializer = new PolygonDeserializer();			
			this.airspaces= polygonDeserializer.Deserialize(outputXmlFilePath);

//			this.airspaces= polygonDeserializer.Deserialize("e:/radioCoverageData/radioOutput.xml");
//			test = true;
			System.out.println("Fin de désérialisation");
			return this.airspaces;
			}
		} 													
		catch (Exception e) {
			e.printStackTrace();
		}			
		return null;
	}
					
		
	/** 
	 * 1) Parsing de l'arborecence,
	 * 2) génération du xml dans tous les reps (Vérifier si radioCoverage.xml existe (cf pattern)), 
	 * 3) concaténation de toutes les données et écriture du fichier de sortie dans le root directory.
	**/		
	public void computeXSL() {
		if (directory.isDirectory()) {				
			/*
			File[] dirList=null;
			File[] dirs = directory.listFiles();			
			if (dirs != null) {
				for (int i=0;i<dirs.length;i++) {					
				}
			}
			*/
			File[] dirs = directory.listFiles(new FileFilter() {	// si directoryPath contient aussi des directories, on est bien dans la root Path.						 		
		  		public boolean accept (File file) {																					
		 			return ((file.isDirectory() )) ; 
		 			}				 							  	
			});						
			
			numberFiles = dirs.length;
			this.setProgress(0);
			
			String XSL = directoryPath+File.separator+"radioCoverageXSL.xsl";
			String XSL2 = directoryPath+File.separator+"radioCoverageXSLEnd.xsl";
			String XSL3 = directoryPath+File.separator+"Fileconcat.xsl";
			String XML_outTemp = directory.getAbsolutePath()+File.separator+"tempradioOutput.xml";
			String XML_out = directory.getAbsolutePath()+File.separator+"radioOutput.xml";
			String XML_Temp_In = directory.getAbsolutePath()+File.separator+"radioIntput.xml";
			
			XmlFile.writeIntoFile(XML_out, "<list>"+"\n");
			
			for (int i=0;i<dirs.length;i++) {				
				
				String XML_in = dirs[i].getAbsolutePath()+File.separator+"radioCoverage.xml";								
				String XML_temp_out = dirs[i].getAbsolutePath()+File.separator+"tempRadioOutput.xml";
				
				try {							
					SaxonFactory.SaxonJob(XSL, XML_in, XML_temp_out);					
					this.setProgress(currentProgress++);
					// concaténation des chaines des fichiers XML temporaires de chaque répertoire dans  XML_outTemp
					XmlFile.copy (XML_temp_out,XML_out);
					
				
//					SaxonFactory.SaxonJobWithParam(XSL3,XML_Temp_In,XML_out, XML_temp_out);
				}			
				catch (Exception e) {
					e.printStackTrace();
				}
			}			
			XmlFile.writeIntoFile(XML_out, "</list>"+"\n");
			
			try {									
//				 SaxonFactory.SaxonJob(XSL2, XML_outTemp, XML_out);												
				// suppression fichier temporaire
/*				
				File file = new File(XML_outTemp);
				if (file.exists()) {file.delete();}
*/
			}			
			catch (Exception e) {
				e.printStackTrace();
			}					
		}
	}																				
		
	/*Les fichiers parents sont-ils à jour ?**/
	public boolean scanForUpdate() {
		boolean test = false;				
		return test;
	}

//TODO : tests

	@Override
	public Integer doInBackground() {
		
		try {
			// System.out.println("(Radio.java / Appel méthode doInBackground())");			
			//création de la connection à la base de données
			this.conn = DatabaseManager.selectDB(Type.RadioCov, this.name);
			this.conn.setAutoCommit(false); //fixes performance issue
		
			if(!DatabaseManager.databaseExists(this.name)){
				
				System.out.println("(Radio.java) / La base de données n'existe pas" +"");
				
				// création de la structure de la base de données
				 DatabaseManager.createRadioCov(this.name,this.path);
				 DatabaseManager.insertRadioCov(this.name, this.path);
				 // parsing des fichiers et stockage en base
				// /this.getFromFiles();				
				// this.setProgress(12);
				
				try {this.conn.commit();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				//this.setProgress(this.numberFiles());
			}
			else {		
				// ajout d'une ligne dans la table radio
				// Ajout d'une ligne dans la table databases.
				System.out.println("La base de données existe");
				DatabaseManager.insertRadioCov(this.name, this.path);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		return 0; 
	}

	@Override
	public void done(){
		if(this.isCancelled()){//si le parsing a été annulé, on fait le ménage
			try {
				DatabaseManager.deleteDatabase(this.name, Type.RadioCov);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		firePropertyChange("done", false, true);
	}
	
	@Override
	public int numberFiles() {
		return this.numberFiles;
	}
	
	@Override
	public void getFromFiles() {				
	}
}
