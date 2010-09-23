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

package fr.crnan.videso3d.layers;

//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedList;

// import java.io.File;
import java.util.ArrayList;
// import java.util.Iterator;

import fr.crnan.videso3d.formats.xml.PolygonDeserializer;
// import fr.crnan.videso3d.formats.xstream.PolygonSerializer;
import fr.crnan.test.data.RadioCoverageInit;

import fr.crnan.videso3d.graphics.RadioCovPolygon;
import fr.crnan.videso3d.radio.RadioDataManager;
// import fr.crnan.videso3d.radio.RadioDirectoryReader;
import fr.crnan.videso3d.VidesoGLCanvas;

import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.render.airspaces.Airspace;

public class RadioCovLayer extends AirspaceLayer{

	//private HashMap<String, RadioCovPolygon> RadioCovHash = new HashMap<String, RadioCovPolygon>();
	//private LinkedList<RadioCovPolygon> ActiveRadioCov = new LinkedList<RadioCovPolygon>();
	
	private AirspaceLayer activeRadioCov = new AirspaceLayer();
	private ArrayList<Airspace> airspaces;
	private LayerList layers;
	private Boolean DEBUG = false;
	private String radioCov;
	private VidesoGLCanvas wwd;
	
	public RadioCovLayer(String radioCov, VidesoGLCanvas wwd) {
		
		this.wwd = wwd;
		this.radioCov = radioCov;
		
		this.setName(radioCov);
		this.setEnableAntialiasing(true);
		this.setEnableBlending(true);
		this.setEnableLighting(true);							
	}
		
	/** Recherche une couverture radio dans la liste, et la rend visible*/
	public void addVisibleRadioCov(String name) {	
		for (Airspace airspace : activeRadioCov.getAirspaces()) {
			if ((airspace  instanceof RadioCovPolygon) && (((RadioCovPolygon) airspace).getName()==name)) {
				airspace.setVisible(true);
			}
		}
	}
	
	/**Recherche une couverture radio dans la liste, et la rend invisible*/
	public void removeVisibleRadioCov(String name) {			
		for (Airspace airspace : activeRadioCov.getAirspaces()) {
			if ((airspace  instanceof RadioCovPolygon) && (((RadioCovPolygon) airspace).getName()==name)) {
				airspace.setVisible(false);
			}
		}
	}
		
	/** Toutes les couvertures radio sont visibles */
	public void displayAllRadioCovLayers() {				
		for (Airspace airspace : activeRadioCov.getAirspaces()) {
			if ((airspace  instanceof RadioCovPolygon)) {
				airspace.setVisible(true);
			}
		}
	}
	
	/**Rend toutes les couvertures radios sont invisibles*/
	public void hideAllRadioCovLayers() {		
		for (Airspace airspace : activeRadioCov.getAirspaces()) {
			if ((airspace  instanceof RadioCovPolygon)) {
				airspace.setVisible(false);
			}
		}
	}
	
	public void insertAllRadioCovLayers(ArrayList <Airspace> airspacesParam){
		try {						
						
			RadioCoverageInit radioCoverageInit= new RadioCoverageInit(radioCov);
			radioCoverageInit.add(); 			
			
			AirspaceLayer airspaceLayer = new AirspaceLayer();			
			airspaces = airspacesParam;			
			//airspaces.addAll(airspacesParam);							
			
			airspaceLayer.addAirspaces(airspaces);
			airspaceLayer.setName(radioCov);
			activeRadioCov = airspaceLayer;
			layers=wwd.getModel().getLayers();				
			layers.add(activeRadioCov);				
		}
		catch(Exception e) {
			e.printStackTrace();
		}																
	}
	

	public void insertAllRadioCovLayers(String path) {
		try {
			
			RadioCoverageInit radioCoverageInit= new RadioCoverageInit(radioCov);
			radioCoverageInit.add(); 			
			
			AirspaceLayer airspaceLayer = new AirspaceLayer();			
			try {
				RadioDataManager radioDataManager = new RadioDataManager();
				airspaces = radioDataManager.loadData();
				//PolygonDeserializer polygonDeserializer = new PolygonDeserializer();
				//airspaces =  polygonDeserializer.Deserialize("e:/radioCoverageData/radioOutput.xml");	 								
				airspaceLayer.addAirspaces(airspaces);
			}
			catch (Exception e) {e.printStackTrace();}
						
			airspaceLayer.setName(radioCov);				
			activeRadioCov = airspaceLayer;
			layers=wwd.getModel().getLayers();				
			layers.add(activeRadioCov);
		}
		catch(Exception e) {
			e.printStackTrace();
		}																
	}

	
	/**Insertion des Layers couvertures Radio au chargement ou lors d'une resélection dans le databaseManager */
	public void insertAllRadioCovLayers() {
		
		AirspaceLayer airspaceLayer = new AirspaceLayer();
		
		ArrayList<Airspace> airspaceTest = null;				
		RadioCoverageInit radioCoverageInit= new RadioCoverageInit(radioCov);
		radioCoverageInit.add(); 								
		/*************************  Serializer  *****************************************/
		//PolygonSerializer polygonSerializer = new PolygonSerializer();
		//polygonSerializer.Serialize(radioCoverageInit.getAirspaces(),"e:/testXML.xml");
		
		/******************* Classe de Tests à supprimer *****************************/				
//		activeRadioCov = radioCoverageInit.getAirspaceLayers();		
				
		try{		
			/************************* Deserializer *************************************/	
			PolygonDeserializer polygonDeserializer = new PolygonDeserializer();
//			airspaceTest = polygonDeserializer.Deserialize("e:/testXML.xml");
			airspaceTest = polygonDeserializer.Deserialize("e:/radioCoverageData/radioOutput.xml");
			airspaceLayer.addAirspaces(airspaceTest);
		}
		
		
		
//			airspaceTest = polygonDeserializer.Deserialize("/media/STOREX/radioOutput.xml");
//			activeRadioCov.addAirspaces(airspaceTest);

			
//			RadioDataManager radioDataManager = new RadioDataManager(new File(radioCovPath));

			// if (radioDataManager.loadData()) {activeRadioCov.addAirspaces(radioDataManager.getAirspaces());}															  		 		 		  	
	
		catch (Exception e) {e.printStackTrace();}
			
		
		// System.out.println("No")
		
		 // RadioDirectoryReader radioDirectoryReader = new RadioDirectoryReader(radioCovPath);
		 // radioDirectoryReader.scanDirectoriesList(new File(radioCovName));												
		
		// activeRadioCov.addAirspaces(airspaceTest);
		// activeRadioCov = airspaceLayer;
		
		
		try {
			// RadioDataManager radioDataManager = new RadioDataManager(path);			
			//radioDataManager.loadData();
			//activeRadioCov.addAirspaces(radioDataManager.getAirspaces());
		}
		catch(Exception e) {
			e.printStackTrace();
		}		
		airspaceLayer.setName(radioCov);
		activeRadioCov=airspaceLayer;
		layers=wwd.getModel().getLayers();
		layers.add(activeRadioCov);						
	}
	
	/** Vidage de les liste des couvertures radios du layer radioCovLayer, et suppression du radioCovLayer */
	public void removeAllRadioCovLayers() {
		if (DEBUG) System.out.println("Liste des layers avant la boucle :+layers");
		if (layers != null) {
			for (Layer layer : layers) {				
						if (layer instanceof AirspaceLayer && layer.getName()==radioCov) {						
							layer.clearList();							
							layers.remove(layer);							
						}								
			}
		}	
	}
	
	public void removeAllAirspaces() {
		super.removeAllAirspaces();		
	}
		
}
