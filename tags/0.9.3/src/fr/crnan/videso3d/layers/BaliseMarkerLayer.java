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

import java.util.Iterator;
import java.util.LinkedList;

import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.render.markers.Marker;
/**
 * MarkerLayer avec une possibilité d'ajouter des Marker à l'ensemble existant
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class BaliseMarkerLayer extends MarkerLayer {

	public BaliseMarkerLayer(){
		super();
		this.setKeepSeparated(false);
		this.setMinActiveAltitude(0);
		//inutile d'afficher le point avant 1000km d'altitude
		this.setMaxActiveAltitude(10e5);
	}
	
	/**
	 * Ajoute un {@link Marker} à l'ensemble existant
	 * @param marker {@link Marker} à ajouter
	 */
	public void addMarker(Marker marker){
		LinkedList<Marker> markersList = new LinkedList<Marker>();
		markersList.add(marker);
		//ajoute les markers précédents si besoin
		if(this.getMarkers() != null){
			Iterator<Marker> iterator = this.getMarkers().iterator();
			while(iterator.hasNext()){
				markersList.add(iterator.next());
			}
		}
		this.setMarkers(markersList);
	}
	
	/**
	 * Enlève un marker au layer. Si il n'existe pas, ne fait rien.
	 * @param marker Marker à enlever
	 */
	public void removeMarker(Marker marker){
		LinkedList<Marker> markersList = new LinkedList<Marker>();
		if(this.getMarkers() != null) {
			for(Marker m : this.getMarkers()){
				if(!m.equals(marker)){
					markersList.add(m);
				}
			}
		}
		this.setMarkers(markersList);
	}
	
}
