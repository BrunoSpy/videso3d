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
import gov.nasa.worldwind.globes.Earth;

import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

/**
 * Classe qui prend la description textuelle des points du contour d'une zone et fournit une liste de LatLon.
 * @author VIDAL Adrien
 *
 */
public class Geometrie{


	private List<LatLon> locations;

	/**
	 * 
	 * @param elt Un élément "partie" dans le fichier xml du SIA.
	 */
	public Geometrie(Element elt){
		String geometrie = elt.getChild("Geometrie").getValue();
		locations = parseLocations(geometrie);
	}
	
	public Geometrie(String geometrie){
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
		int step = 1;
		if(locs.length>500){
			step = locs.length/500;
			if(!locs[1].isEmpty()){
				LatLon latLon = parseLatLon(locs[1]);
				locations.add(latLon);
			}	
			boolean stepReduced = false; 
			for (int i=2;i<locs.length;i+=step){
				if(!locs[i].isEmpty()){
					LatLon latLon = parseLatLon(locs[i]);
					if(getDistance(latLon, locations.get(locations.size()-1))<5000 || stepReduced || step<2){
						locations.add(latLon);
						stepReduced=false;
					}else{
						i-=Math.min(i-3, 2*step-1);
						stepReduced = true;
					}
				}
			}
		}else{
			for (int i=0;i<locs.length;i++){
				if(!locs[i].isEmpty()){
					LatLon latLon = parseLatLon(locs[i]);
					locations.add(latLon);
				}
			}
		}
		return locations;
	}
	
	
	private LatLon parseLatLon(String latLonText){
		String[] location = latLonText.split(",");
		LatLon latLon = null;
		if(location.length == 2)
			latLon = LatLon.fromDegrees(Double.parseDouble(location[0]), Double.parseDouble(location[1]));
		return latLon;
	}
	
	
	/**
	 * 
	 * @return La liste des points du contour de la zone.
	 */
	public List<LatLon> getLocations(){
		return locations;
	}

	/**
	 * 
	 * @param l1
	 * @param l2
	 * @return Renvoie la distance entre l1 et l2.
	 * 
	 */
    public static double getDistance(LatLon l1, LatLon l2){
        return LatLon.greatCircleDistance(l1, l2).radians*Earth.WGS84_EQUATORIAL_RADIUS;
    }
	
	
	
	
	
	
	
	
	
}
