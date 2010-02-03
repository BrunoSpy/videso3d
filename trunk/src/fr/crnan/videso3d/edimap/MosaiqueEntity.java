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
package fr.crnan.videso3d.edimap;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.geom.LatLonCautra;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.Polygon;

/**
 * Représentation 3D des mosaiques ODS (volume d'interet, de sécurité, ..)
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class MosaiqueEntity extends LinkedList<Airspace>{
	
	private Double plancher = new Double(0);
	private Double plafond = new Double(660);
	
	public MosaiqueEntity(Entity entity, HashMap<String, LatLonCautra> pointsRef) {
		plancher = new Double(entity.getValue("value_min"))*30.48;
		plafond = new Double(entity.getValue("value_max"))*30.48;
		this.createAirspaces((LinkedList<Entity>) entity.getEntity("geometry").getValue());
	}

	private void createAirspaces(LinkedList<Entity> value) {
		LatLonCautra p = null;
		BasicAirspaceAttributes attrs = new BasicAirspaceAttributes(new Material(Color.BLUE), 0.30);
		for(Entity e : value){
			if(e.getKeyword().equals("nautical_mile")) {
				String[] temp = ((String) e.getValue()).split("\\s+");
				if(!temp[4].equals("0")) { 
					p = PointEdimap.fromEntity(e);
				} else {
					p = null;
				}
			} else if (e.getKeyword().equals("distance")){
				if(p != null) {
					Double distance = new Double(((String)e.getValue()).split("\\s+")[1]) / 64;
					List<LatLon> locations = new LinkedList<LatLon>();
					locations.add(p);
					locations.add(LatLonCautra.fromCautra(p.getCautra()[0] + distance, p.getCautra()[1]));
					locations.add(LatLonCautra.fromCautra(p.getCautra()[0] + distance, p.getCautra()[1] + 2));
					locations.add(LatLonCautra.fromCautra(p.getCautra()[0], p.getCautra()[1] + 2));
					Polygon polygon = new Polygon();
					polygon.setAltitudes(plancher, plafond);
					polygon.setLocations(locations);
					polygon.setAttributes(attrs);
					this.add(polygon);
				}
			}
		}
	}

}
