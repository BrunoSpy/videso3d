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

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.graphics.DatabaseVidesoObject;
import fr.crnan.videso3d.graphics.PolygonAnnotation;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;

/**
 * Représentation 3D des mosaiques ODS (volume d'interet, de sécurité, ..)
 * @author Bruno Spyckerelle
 * @version 0.2.3
 */
public class MosaiqueEntity extends LinkedList<Airspace>{
	
	private Double plancher = new Double(0);
	private Double plafond = new Double(660);
	
	private class Volume extends PolygonAnnotation implements DatabaseVidesoObject {
		
		private DatabaseManager.Type base;
		
		private int type;
		
		private String name;
		
		public Volume(int type, double plancher, double plafond){
			super();
			this.type = type;
			this.setAltitudes(plancher, plafond);
			this.setAnnotation("<html><b>Volume " + (type==1?"de sécurité.":"d'intérêt.")+"</b><br />" +
					"<b>Plafond : </b>" + (int) (plafond/30.48) + "<br />" +
					"<b>Plancher : </b>" + (int) (plancher/30.48) );
		}

		@Override
		public Type getDatabaseType() {
			return this.base;
		}

		@Override
		public void setDatabaseType(Type type) {
			this.base = type;
		}

		@Override
		public void setType(int type) {
			this.type = type;
		}

		@Override
		public int getType() {
			return this.type;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String getRestorableClassName() {
			return PolygonAnnotation.class.getName();
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public MosaiqueEntity(Entity entity, String name, HashMap<String, LatLonCautra> pointsRef) {
		plancher = new Double(entity.getValue("value_min"))*30.48;
		plafond = new Double(entity.getValue("value_max"))*30.48;
		this.createAirspaces((LinkedList<Entity>) entity.getEntity("geometry").getValue(), name);
	}

	private void createAirspaces(LinkedList<Entity> value, String name) {
		LatLonCautra p = null;
		
		int i = 0;
		Double step = 4.0; //pas de la mosaïque en NM

		boolean interet = false;
		
		BasicAirspaceAttributes attrsI = new BasicAirspaceAttributes(new Material(Color.BLUE), 0.30);
		BasicAirspaceAttributes attrs = new BasicAirspaceAttributes(new Material(Color.RED), 0.30);
		
		for(Entity e : value){
			if(i == 0){//la première ligne ne sert à rien (centre de la mosaïque)			
				i++;
			} else if (i == 1) {//la deuxième ligne donne le pas de la mosaïque
				step = new Double(((String)e.getValue()).split("\\s+")[1]) / 64;
				i++;
			} else {
				if(e.getKeyword().equals("nautical_mile")) {
					String[] temp = ((String) e.getValue()).split("\\s+");
					if(!temp[4].equals("0")) { 
						interet = temp[4].equals("2");
						p = PointEdimap.fromEntity(e);
					} else {
						p = null;
					}
				} else if (e.getKeyword().equals("distance")){
					if(p != null) {
						Double distance = new Double(((String)e.getValue()).split("\\s+")[1]) / 64;

						
						Volume volume = new Volume(interet?2:1, plancher, plafond);
						
						List<LatLon> locations = new LinkedList<LatLon>();
						
						locations.add(p);
						locations.add(LatLonCautra.fromCautra(p.getCautra()[0] + distance, p.getCautra()[1]));
						locations.add(LatLonCautra.fromCautra(p.getCautra()[0] + distance, p.getCautra()[1] + step));
						locations.add(LatLonCautra.fromCautra(p.getCautra()[0], p.getCautra()[1] + step));
						
						if(interet) {
							volume.setAttributes(attrsI);
						} else {
							volume.setAttributes(attrs);
						}
						volume.setLocations(locations);
						volume.setName(name);
						volume.setType(Cartes.EDIMAP_VOLUME);
						volume.setDatabaseType(Type.Edimap);
						this.add(volume);
						
					}
				}
			}
		}
	}
	
	
}
