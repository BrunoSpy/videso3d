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

import java.util.List;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.airspaces.Polygon;;
/**
 * Adds the ability to find if a point is inside the Polygon
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class VPolygon extends Polygon {

	public VPolygon(List<? extends LatLon> locations) {
		super(locations);
	}

	public VPolygon() {
		super();
	}

	/**
	 * 
	 * @param pos
	 * @return True if the point is inside
	 */
	public boolean contains(Position pos){
		if(pos.elevation >= this.getAltitudes()[0] && pos.elevation <= this.getAltitudes()[1]) {
			java.awt.Polygon surface = new java.awt.Polygon();
			for(LatLon l : this.getLocations()){
				surface.addPoint((int)(l.longitude.degrees*100), (int)(l.latitude.degrees*100));
			}
			return surface.contains((int)(pos.longitude.degrees*100), (int)(pos.latitude.degrees*100));
		} else {
			return false;
		}
	}
	
}
