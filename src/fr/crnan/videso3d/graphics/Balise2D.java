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

package fr.crnan.videso3d.graphics;

import fr.crnan.videso3d.layers.BaliseMarkerLayer;
import fr.crnan.videso3d.layers.TextLayer;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

import gov.nasa.worldwind.render.UserFacingText;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.Marker;

/**
 * Balise 2D projetée sur le terrain
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class Balise2D {

	/**
	 * Nom de la balise
	 */
	private UserFacingText name;
	
	/**
	 * Cercle centré sur la position de la balise
	 */
	private Marker marker;
	/**
	 * Crée une balise 2D
	 * @param name Nom de la balise
	 * @param position {@link LatLon} Position de la balise
	 */
	public Balise2D(String name, LatLon position){
		this.name = new UserFacingText(name, new Position(position, 100.0));
		
		BasicMarkerAttributes attrs = new BasicMarkerAttributes();
		attrs.setMarkerPixels(3);
		
		this.marker = new BasicMarker(new Position(position, 0), attrs);
		
	}
	/**
	 * Ajoute la balise aux calques
	 * @param layer {@link BaliseMarkerLayer} Calque pour le dessin
	 * @param textLayer {@link TextLayer} Calque pour le nom de la balise
	 */
	public void addToLayer(BaliseMarkerLayer layer, TextLayer textLayer){
		layer.addMarker(marker);
		textLayer.addGeographicText(name);
		
	}
}
