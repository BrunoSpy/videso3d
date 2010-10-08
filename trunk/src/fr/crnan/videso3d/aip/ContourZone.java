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
		int step = 1;
		if(locs.length>500){
			step = locs.length/500;
			if(!locs[1].isEmpty()){
				String[] loc = locs[1].split(",");
				LatLon latLon=LatLon.fromDegrees(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
				locations.add(latLon);
			}	
			int nbreducs=0;
			boolean stepReduced = false; 
			for (int i=2;i<locs.length;i+=step){
				if(!locs[i].isEmpty()){
					String[] loc = locs[i].split(",");
					LatLon latLon=LatLon.fromDegrees(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
					if(getDistance(latLon, locations.get(locations.size()-1))<5000 || stepReduced || step<2){
						locations.add(latLon);
						stepReduced=false;
					}else{
						i-=Math.min(i-3, 2*step-1);
						stepReduced = true;nbreducs++;
					}
				}
			}
		}else{
			for (int i=0;i<locs.length;i++){
				if(!locs[i].isEmpty()){
					String[] loc = locs[i].split(",");
					LatLon latLon=LatLon.fromDegrees(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
						locations.add(latLon);
				}
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

	/**
	 * 
	 * @param l1
	 * @param l2
	 * @return Renvoie la distance entre l1 et l2.
	 * 
	 * @since Visualisation_de_flotte 1.0
	 */
    public static double getDistance(LatLon l1, LatLon l2){
        return LatLon.greatCircleDistance(l1, l2).radians*Earth.WGS84_EQUATORIAL_RADIUS;
    }
	
	
	
	
	
	
	
	
	
}
