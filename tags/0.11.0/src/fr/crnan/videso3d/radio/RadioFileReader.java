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
* Scanner de fichiers. Transforme chacun des fichiers contenant des données de niveau de vol en fichier xml de base. 
* Tous les fichiers d'un même répertoire sont concaténés dans un xml de sortie.
* 
* 0.1 : Scan partiel de fichiers
* 0.2 : Scan complet avec génération xml de sortie (Traitement trop long)
* 0.3 : Améiloration du temps de scan  : /100
* 0.4 : Implémentation Algo de recherche des points en  limite de  portée (extremumFinder) + remise en forme des données de sortie
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
// import java.util.regex.Pattern;
// import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
// import java.util.ArrayList;
// import java.util.Iterator;

// import fr.crnan.videso3d.Triplet;
//import fr.crnan.videso3d.radio.*;

import java.io.*;
import java.util.*;

public  class RadioFileReader {

	private boolean DEBUG = false;
	private String fileName;
	private File file;	
	private String antennaName;
	private ExtremumFinder extremumFinder = new ExtremumFinder();
    	  
	
	public RadioFileReader(File file) {
		this.file = file;
		this.fileName = file.getName();		
	}
	
	public RadioFileReader(String fileName) {
		this.fileName = fileName;	
	}			

	
	/* Scanne le fichier, et renvoie le contenu traité */ 
	public void ArrayScan() {
	       try { 
	    	 Scanner scanner = new Scanner(new BufferedReader(new FileReader(file)));	    	 	    	 
	    	 
	    	 /****************************************************************************/
	    	 // scanner.useDelimiter(FilePatterns.radioCoveragePattern);			
	    	 // Scanner scanner = new Scanner(file);
	    	 // scanner.useDelimiter(System.getProperty("line.separator"));	         	    	 
	         //System.out.println(scanner.hasNext(Pattern.compile(FilePatterns.radioCoveragePattern)));
	    	 //while (scanner.hasNext(Pattern.compile(FilePatterns.radioCoveragePattern))){
	    	 /****************************************************************************/
	    	 
	    	 Pattern p = Pattern.compile(FilePatterns.radioCoveragePattern);	  	    	 
	    	 while(scanner.hasNextLine()) {	    		
	    		 Matcher m = p.matcher(scanner.nextLine());	    	 	    	 	
	    		 while (m.find()) {	    	 			    	 			    	 
	    	 		String lat =m.group(2)+m.group(3);
	    	 		String lon =m.group(5)+m.group(6);
	    	 		String level = m.group(8)+m.group(9);
	    	 	
	    	 		lat = lat.replace(",","." );
	    	 		lon = lon.replace(",", ".");
	    	 		level = level.replace(",", ".");	    	 	
	    	 		
	    	 		extremumFinder.compute(Double.parseDouble(lat),Double.parseDouble(lon),Double.parseDouble(level));	    	 		    	 		    	 			    	 			    	 			    		 
	    		 }	    	 	
	    	 }
	    	 extremumFinder.reduceToCouple();    	 
	    	 scanner.close();	    
	    	 	    	 	    	 
	    	 Scanner altScanner = new Scanner(new BufferedReader(new FileReader(file)));	    	 	    
	    	 Pattern altPattern = Pattern.compile(FilePatterns.radioCoverageAltitudePattern);	  	    	 
	    	 while(altScanner.hasNextLine()) {	    		
	    		 Matcher m = altPattern.matcher(altScanner.nextLine());	    	 	    	 	
	    		 while (m.find()) {	    	 			    	 			    	 
	    	 		 double alt =Double.parseDouble(m.group(1)); // group(0) renvoie toute la chaine de caracteres.	    	 			    	 		    	 			    	 		
	    	 		 extremumFinder.setAlt(alt);
	    		 }	    	 	
	    	 }	    	 	    	 	    		    	 
	    	 /****************************************************************************/	    	 	    	 	    	 	    	 	    	 
//	    	 while (scanner.hasNext()) {	    		 
	    		 	// 
//	    		 		 if (scanner.hasNext(FilePatterns.testPattern)) { 
	    		 	// if (scanner.findInLine(FilePatterns.radioCoveragePattern)!="") {
	    		 //if (scanner.findInLine(FilePatterns.testPattern)!="") {
//	    		 		String line = scanner.nextLine();  	    		 		 
//	    		 		MatchResult result = scanner.match();
//	    		 		System.out.println(result.toString());
//	    		 		System.out.println("--"+result.group(0)+"--");
	    		 		//content+=createLine(result);
//	    		 	}
//	    		 	else {
//	    		 		System.out.println("Donnée incompatible");
//	    		 		scanner.nextLine();
//	    		 	}	    		 	    	 	    		 	    		 	    			    		
	    	 	// if (DEBUG) System.out.println("hasNextLine");	        
//	        	System.out.println(cpt);
	        	 //content+=scanner.next(Pattern.compile(FilePatterns.radioCoveragePattern));
	        	// content+=scanner.next();
//	        	cpt++;
	        	//scanner.next(FilePatterns.radioCoveragePattern);	        	
	        	 //content += parseLine(scanner.nextLine());	        	
	        	// scanner.findInLine(FilePatterns.radioCoveragePattern);	         
//	         }
	    	 altScanner.close();	    	    	         	         
	       }	    		    	
	    	 catch (FileNotFoundException e) {
	    		 e.printStackTrace();
	       }	
	    	 catch(NoSuchElementException e2) {
	    		 e2.printStackTrace();
	    	 }
	}
			
	 /*
	  * Parsing de chaque ligne du fichier
	  */
	 private String parseLine(String line) {
	     try {	    	 
	    	 byte[] utf8Bytes = line.getBytes("UTF8");
	    	 String utf8Line = new String(utf8Bytes,"UTF8");   	 
	    	 if (DEBUG) System.out.println("Exécution méthode parseLine"); 	    	 	    	 
	    	Scanner lineScanner = new Scanner(utf8Line);	      	    	    
	    	//lineScanner.findInLine(FilePatterns.radioCoveragePattern);
	    	//MatchResult result = lineScanner.match();	    	 	   	    	 	    	 
	    	 lineScanner.findInLine(FilePatterns.radioCoveragePattern);
	    	 //return (createLine(result));	    	    	 
	    	 return "";
	     }	    
	     /*
	     catch (UnsupportedEncodingException e) {
	         System.out.println("Exception encodage"); 
	    	 e.printStackTrace();
	      }
	      */
	     catch (Exception e) {	    	 	     
	    	 // Le scanner trouve une ligne non conforme.
	    	 // System.out.println("ligne non conforme");
	    	 System.out.println("Exception méthode parseLine classse RadioReader");
	    	 e.printStackTrace();
	     }	 	     	     
	     return "";
	 }
	 	
	 public String toString() {
		 //Prise en compte de l'ajout de l'entete xml dans le fichier.
		 // Si le fichier xml n'existe pas encore, on lui met un entete , sinon pas la peine 
		 return extremumFinder.toString();		 
	 }
	 

}