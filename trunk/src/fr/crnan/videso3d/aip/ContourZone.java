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

package fr.crnan.videso3d.aip;

import gov.nasa.worldwind.geom.LatLon;

import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

/**
 * Classe qui prend la description textuelle des points du contour d'une zone et fournit une liste de LatLon.
 * @author VIDAL Adrien
 *
 */
public class ContourZone{


	private List<LatLon> locations;

	/**
	 * 
	 * @param partie Un élément "partie" dans le fichier xml du SIA.
	 */
	public ContourZone(Element partie){
		String geometrie = partie.getChild("Geometrie").getValue();
		locations = parseLocations(geometrie);
	}

	/**
	 * Transforme la description textuelle du contour de la zone en liste de LatLon.   
	 * @param geometrie Le noeud géométrie dans un élément "partie", qui décrit le contour de la zone.
	 * @return
	 */
	private List<LatLon> parseLocations(String geometrie) {
		List<LatLon> locations = new LinkedList<LatLon>();
		String[] locs = geometrie.split("\\s+");
		for (int i=0;i<locs.length;i++){
			if(!locs[i].isEmpty()){
				String[] latlon = locs[i].split(",");
				double lat=Double.parseDouble(latlon[0]);
				double lon = Double.parseDouble(latlon[1]);
				locations.add(LatLon.fromDegrees(lat, lon));
			}
		}
		return locations;
	}
	
	/**
	 * 
	 * @return La liste des points du contour de la zone.
	 */
	public Iterable<LatLon> getLocations(){
		return locations;
	}


	
	
	
	
	
	
	
	
	
}
