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

package fr.crnan.videso3d.kml;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;


import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.FileParser;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.graphics.Route;
import gov.nasa.worldwind.avlist.AVKey;

import gov.nasa.worldwind.examples.util.layertree.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.util.Logging;

/**
 * Lecteur des donnees KML
 * @author Mickael papail
 * @version 0.1
 */
public class KML extends FileParser{

	private final Integer numberFiles = 1;

	/**
	 * Le nom de la base de données.
	 */
	private String name;

	/**
	 * Le chemin du fichier kml utilisé
	 */
	private String filePath;

	/**
	 * Connection Ã  la base de données
	 */
	private Connection conn;

	/**
	 * Le <code>document</code> construit à  partir du fichier xml.
	 */
	private Document document=null;
	private KMLRoot kmlRoot;

	private Object kmlSource;
	
	public KMLRoot getKmlRoot() {
		return this.kmlRoot;
	}
	
	
	public String getFilePath() {
		return this.filePath;
	}
	
	
	public KML(String path) {
		super(path);
		//System.out.println("KML / KML Le filePath est :" + path);
		this.filePath=path;
		  try
          {
              //System.out.println("Le chemin d'accès est : "+ filePath);
			   kmlRoot = KMLRoot.create(path);
              if (kmlRoot == null)
              {
                  String message = Logging.getMessage("generic.UnrecognizedSourceTypeOrUnavailableSource",
                      this.path.toString());
                  throw new IllegalArgumentException(message);
              }

              kmlRoot.parse();              
             // kmlRoot.setField(AVKey.DISPLAY_NAME, formName(path, kmlRoot));
              //kmlRoot.setField(AVKey.DISPLAY_NAME, "machin");
              //System.out.println("constructeur KML"+kmlRoot.getField(AVKey.DISPLAY_NAME));
              kmlRoot.getField(AVKey.DISPLAY_NAME);
              //kmlRoot.setField(AVKey.DISPLAY_NAME, formName(this.kmlSource, kmlRoot));
          }
		  catch (Exception e)
          {
              e.printStackTrace();
          }				
	}

	
	public KML() {
		try {	
			Statement st = DatabaseManager.getCurrent(DatabaseManager.Type.Databases);
			ResultSet rs;
			rs = st.executeQuery("select * from clefs where name='path' and type='"+DatabaseManager.getCurrentName(DatabaseManager.Type.KML)+"'");
			if(rs.next()){
				//System.out.println("La requete renvoie des résultats");
				this.filePath = rs.getString(4);
				//System.out.println("Le filePath est :"+this.filePath);
	           /*
				if (kmlRoot == null)
	              {
	                  String message = Logging.getMessage("generic.UnrecognizedSourceTypeOrUnavailableSource",
	                      this.path.toString());
	                  throw new IllegalArgumentException(message);
	              }
	              */
	              try
	              {
	                 // System.out.println("Le chemin d'accès est : "+ filePath);
	    			   kmlRoot = KMLRoot.create(this.filePath);
	                  if (kmlRoot == null)
	                  {
	                      String message = Logging.getMessage("generic.UnrecognizedSourceTypeOrUnavailableSource",
	                          this.path.toString());
	                      throw new IllegalArgumentException(message);
	                  }

	                  kmlRoot.parse();              
	                  //kmlRoot.setField(AVKey.DISPLAY_NAME, path(path, kmlRoot));
	                  kmlRoot.setField(AVKey.DISPLAY_NAME, path);
	              }
	    		  catch (Exception e)
	              {
	                  e.printStackTrace();
	              }							
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} 	 
		catch (Exception e) { // catchet le XMLStreamException
			e.printStackTrace();
		}
	}

	@Override
	public Integer doInBackground() {
		try {
			//récupération du nom de la base a créer
			this.name ="KMLTest";
			//crÃ©ation de la connection à la base de données
			this.conn = DatabaseManager.selectDB(Type.KML, this.name);
			this.conn.setAutoCommit(false); //fixes performance issue
			if(!DatabaseManager.databaseExists(this.name)){
				//création de la structure de la base de données
				DatabaseManager.createKML(this.name,path);
				//parsing des fichiers et stockage en base
				this.getFromFiles();
				this.setProgress(1);
				this.conn.commit();
				this.setProgress(this.numberFiles());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			this.cancel(true);
		}
		return this.numberFiles();
	}
	
	public Element getDocumentRoot(){
		return document.getRootElement().getChild("Situation");
	}


	private void getName() {		
		this.name = "";
	}

	@Override
	protected void getFromFiles() throws SQLException {
	}


	@Override
	public void done() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int numberFiles() {
		// TODO Auto-generated method stub
		return 0;
	}
		
}
