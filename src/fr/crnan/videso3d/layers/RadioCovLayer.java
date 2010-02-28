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
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.render.airspaces.Airspace;

public class RadioCovLayer extends AirspaceLayer{

	//private HashMap<String, RadioCovPolygon> RadioCovHash = new HashMap<String, RadioCovPolygon>();
	//private LinkedList<RadioCovPolygon> ActiveRadioCov = new LinkedList<RadioCovPolygon>();
	private AirspaceLayer activeRadioCov = new AirspaceLayer();
	private LayerList layers;
	
	public RadioCovLayer(String radioCovName, VidesoGLCanvas wwd) {
		this.setName(radioCovName);
		this.setEnableAntialiasing(true);
		this.setEnableBlending(true);
		this.setEnableLighting(true);
		// A changer...
		RadioCoverageInit radioCoverageInit= new RadioCoverageInit(radioCovName);
		radioCoverageInit.add(); 						
		activeRadioCov=radioCoverageInit.getAirspaceLayers();
		layers=wwd.getModel().getLayers();
		layers.add(activeRadioCov);
	}
	
	
	/*TODO*/
	public void addVisibleRadioCov(String name) {	
		for (Airspace airspace : activeRadioCov.getAirspaces()) {
			if ((airspace  instanceof RadioCovPolygon) && (((RadioCovPolygon) airspace).getName()==name)) {
				airspace.setVisible(true);
			}
		}
	}
	
	/*TODO*/
	public void removeVisibleRadioCov(String name) {			
		for (Airspace airspace : activeRadioCov.getAirspaces()) {
			if ((airspace  instanceof RadioCovPolygon) && (((RadioCovPolygon) airspace).getName()==name)) {
				airspace.setVisible(false);
			}
		}
	}
		
	/*TODO*/
	public void displayAllRadioCovLayers() {		
		//Iterator<RadioCovPolygon> iterator = RadioCovHash.values().iterator();
	}
	
	/*TODO*/
	public void hideAllRdioCovLayers() {		
	}
		
	public void removeAllAirspaces() {
		super.removeAllAirspaces();		
	}
		
}
