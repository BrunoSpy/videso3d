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


import java.util.ArrayList;

import fr.crnan.videso3d.graphics.RadioCovPolygon;
import fr.crnan.videso3d.VidesoGLCanvas;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.render.airspaces.Airspace;

/**
 * 
 * @author Mickael Papail
 * @version 0.2
 */
public class RadioCovLayer extends FilterableAirspaceLayer{


	private FilterableAirspaceLayer activeRadioCov = new FilterableAirspaceLayer();
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
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	/**Recherche une couverture radio dans la liste, et la rend invisible*/
	public void removeVisibleRadioCov(String name) {			
		for (Airspace airspace : activeRadioCov.getAirspaces()) {
			if ((airspace  instanceof RadioCovPolygon) && (((RadioCovPolygon) airspace).getName()==name)) {
				airspace.setVisible(false);
			}
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	/** Toutes les couvertures radio sont visibles */
	public void displayAllRadioCovLayers() {				
		for (Airspace airspace : activeRadioCov.getAirspaces()) {
			if ((airspace  instanceof RadioCovPolygon)) {
				airspace.setVisible(true);
			}
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	/**Rend toutes les couvertures radios sont invisibles*/
	public void hideAllRadioCovLayers() {		
		for (Airspace airspace : activeRadioCov.getAirspaces()) {
			if ((airspace  instanceof RadioCovPolygon)) {
				airspace.setVisible(false);
			}
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}	
	public void insertAllRadioCovLayers(ArrayList <Airspace> airspacesParam){
		try {									


			RadioCovPolygon poly = new RadioCovPolygon(); // permet dobtenir la Memory cache pour les objets contenus dans l'ArrayList<Airspaces> ( sinon pas de mémoire cache allouée, et pas d'objet 3d visible après désérialisation)

			FilterableAirspaceLayer airspaceLayer = new FilterableAirspaceLayer();						
			airspaces = new ArrayList<Airspace>();				
			airspaces.addAll(airspacesParam);	            	            
			airspaceLayer.addAirspaces(airspaces);
			airspaceLayer.setName(radioCov);
			activeRadioCov = airspaceLayer;
			layers=wwd.getModel().getLayers();				
			layers.add(activeRadioCov);				
		}
		catch(Exception e) {
			e.printStackTrace();			
		}		
		this.firePropertyChange(AVKey.LAYER, null, this);
	}


	/** Vidage des liste des couvertures radios du layer radioCovLayer, et suppression du radioCovLayer */
	public void removeAllRadioCovLayers() {
		if (DEBUG) System.out.println("Liste des layers avant la boucle :+layers");
		if (layers != null) {
			for (Layer layer : layers) {				
				if (layer instanceof FilterableAirspaceLayer && layer.getName()==radioCov) {						
					layer.clearList();							
					layers.remove(layer);							
				}								
			}
		}	
		this.firePropertyChange(AVKey.LAYER, null, this);
	}
	public void removeAllAirspaces() {
		super.removeAllAirspaces();		
	}

}
