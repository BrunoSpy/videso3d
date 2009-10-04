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

import java.awt.Color;
import java.awt.Font;

import fr.crnan.videso3d.layers.BaliseMarkerLayer;
import fr.crnan.videso3d.layers.TextLayer;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.UserFacingText;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.Marker;

/**
 * Balise 2D projetée sur le terrain
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class Balise2D extends UserFacingText {
	
	/**
	 * Cercle centré sur la position de la balise
	 */
	private Marker marker;
	
	
	/**
	 * Crée une balise 2D
	 * @param name Nom de la balise
	 * @param position {@link LatLon} Position de la balise
	 */
	public Balise2D(CharSequence name, Position position){
		super(name, position);
		this.setFont(new Font("Sans Serif", Font.PLAIN, 9));
		
		BasicMarkerAttributes attrs = new BasicMarkerAttributes();
		attrs.setMarkerPixels(2);
		
		this.marker = new BasicMarker(new Position(position, 0), attrs);
		
	}
	/**
	 * Ajoute la balise aux calques
	 * @param layer {@link BaliseMarkerLayer} Calque pour le dessin
	 * @param textLayer {@link TextLayer} Calque pour le nom de la balise
	 */
	public void addToLayer(BaliseMarkerLayer layer, TextLayer textLayer){
		layer.addMarker(marker);
		textLayer.addGeographicText(this);
		
	}
	/**
	 * Met en valeur la balise
	 * @param bool
	 */
	public void highlight(Boolean bool){
		if(bool) {
			this.marker.getAttributes().setMaterial(Material.YELLOW);
			this.setColor(Color.YELLOW);
		} else {
			this.marker.getAttributes().setMaterial(Material.WHITE);
			this.setColor(Color.WHITE);
		}
	}
	
}
