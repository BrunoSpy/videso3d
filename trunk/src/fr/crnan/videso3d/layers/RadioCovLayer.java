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

import fr.crnan.test.data.RadioCoverageInit;

import fr.crnan.videso3d.graphics.RadioCovPolygon;
import fr.crnan.videso3d.VidesoGLCanvas;

import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.render.airspaces.Airspace;

public class RadioCovLayer extends AirspaceLayer{

	//private HashMap<String, RadioCovPolygon> RadioCovHash = new HashMap<String, RadioCovPolygon>();
	//private LinkedList<RadioCovPolygon> ActiveRadioCov = new LinkedList<RadioCovPolygon>();
	private AirspaceLayer activeRadioCov = new AirspaceLayer();
	private LayerList layers;
	private Boolean DEBUG = true;
	private String radioCovName;
	private VidesoGLCanvas wwd;
	
	public RadioCovLayer(String radioCovName, VidesoGLCanvas wwd) {
		
		this.wwd = wwd;
		this.radioCovName = radioCovName;
		
		this.setName(radioCovName);
		this.setEnableAntialiasing(true);
		this.setEnableBlending(true);
		this.setEnableLighting(true);
		

		insertAllRadioCovLayers();				
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

	/**Insertion des Layers couvertures Radio au chargement ou lors d'une resélection dans le databaseManager */
	public void insertAllRadioCovLayers() {
		RadioCoverageInit radioCoverageInit= new RadioCoverageInit(radioCovName);
		radioCoverageInit.add(); 						
		activeRadioCov=radioCoverageInit.getAirspaceLayers();
		layers=wwd.getModel().getLayers();
		layers.add(activeRadioCov);
	}
	
	/** Vidage de les liste des couvertures radios du layer radioCovLayer, et suppression du radioCovLayer */
	public void removeAllRadioCovLayers() {
		if (DEBUG) System.out.println("Liste des layers avant la boucle :+layers");
		if (layers != null) {
			for (Layer layer : layers) {				
						if (layer instanceof AirspaceLayer && layer.getName()==radioCovName) {						
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
