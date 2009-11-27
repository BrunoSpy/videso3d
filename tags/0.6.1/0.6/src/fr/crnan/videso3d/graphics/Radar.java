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

import fr.crnan.videso3d.geom.LatLonCautra;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfaceCircle;
/**
 * Représentation graphique de la portée d'un radar
 * @author Bruno Spyckerelle
 *
 */
public class Radar extends SurfaceCircle implements ObjectAnnotation {
	
	private GlobeAnnotation annotation;
	
	/**
	 * Construit un radar
	 * @param name Nom du radar
	 * @param pos Position du radar
	 * @param portee Portee du radar
	 */
	public Radar(String name, LatLon pos, Integer portee){
		this.setCenter(pos);
		this.setRadius(portee*LatLonCautra.NM);
		this.setAnnotation("Radar "+name+"\nPortée : "+portee+" NM.");
		BasicShapeAttributes attrs = new BasicShapeAttributes();
		attrs.setInteriorMaterial(Material.BLUE);
		attrs.setInteriorOpacity(0.5);
		this.setAttributes(attrs);
	}
	
	
	/* (non-Javadoc)
	 * @see fr.crnan.videso3d.graphics.ObjectAnnotation#setAnnotation(java.lang.String)
	 */
	public void setAnnotation(String text){
		if(annotation == null) {
			annotation = new GlobeAnnotation(text, Position.ZERO);
			annotation.setAlwaysOnTop(true);
		} else {
			annotation.setText(text);
		}
	}
	
	/* (non-Javadoc)
	 * @see fr.crnan.videso3d.graphics.ObjectAnnotation#getAnnotation(gov.nasa.worldwind.geom.Position)
	 */
	public GlobeAnnotation getAnnotation(Position pos){
		annotation.setPosition(pos);
		return annotation;
	}
}
