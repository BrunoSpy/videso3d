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

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.UserFacingText;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.Marker;

/**
 * Balise 2D projetée sur le terrain.<br />
 * A ajouter à un BaliseLayer pour être affichée sur le globe.
 * @author Bruno Spyckerelle
 * @version 0.4
 */
public class Balise2D extends UserFacingText {
	
	/**
	 * Cercle centré sur la position de la balise
	 */
	private MarkerAnnotation marker;
	
	/**
	 * Crée une balise 2D
	 * @param name Nom de la balise
	 * @param position {@link LatLon} Position de la balise
	 */
	public Balise2D(CharSequence name, Position position){
		super(name, new Position(Angle.fromDegrees(position.latitude.degrees+0.01), position.longitude, position.elevation));
		this.setFont(new Font("Sans Serif", Font.PLAIN, 9));
		
		BasicMarkerAttributes attrs = new BasicMarkerAttributes();
		attrs.setMarkerPixels(3);
		//attrs.setMaxMarkerSize(3000);
		//attrs.setMinMarkerSize(700);
		this.marker = new MarkerAnnotation(new Position(position, 0), attrs);
		this.marker.setAnnotation("Balise "+name);	
	}
	
	/**
	 * 
	 * @param text Texte de l'annotation
	 */
	public void setAnnotation(String text){
		this.marker.setAnnotation(text);
	}
	
	public UserFacingText getUserFacingText(){
		return this;
	}
	
	public Marker getMarker(){
		return this.marker;
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
