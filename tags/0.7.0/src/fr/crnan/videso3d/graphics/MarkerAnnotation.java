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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.MarkerAttributes;

/**
 * {@link BasicMarker} avec {@link GlobeAnnotation} intégré
 * @author  Bruno Spyckerelle
 * @version 0.1
 */
public class MarkerAnnotation extends BasicMarker implements ObjectAnnotation {

	private GlobeAnnotation annotation;
	
	public MarkerAnnotation(Position position, MarkerAttributes attrs) {
		super(position, attrs);
	}

	public MarkerAnnotation(String annotation, Position position,
			BasicMarkerAttributes attrs) {
		super(position, attrs);
		this.setAnnotation(annotation);
	}

	@Override
	public void setAnnotation(String text){
		if(annotation == null) {
			annotation = new GlobeAnnotation(text, Position.ZERO);
			annotation.setAlwaysOnTop(true);
		} else {
			annotation.setText(text);
		}
	}
	
	@Override
	public GlobeAnnotation getAnnotation(Position pos){
		annotation.setPosition(pos);
		return annotation;
	}
	
}
