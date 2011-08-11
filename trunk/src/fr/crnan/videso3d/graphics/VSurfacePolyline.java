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

import java.util.LinkedList;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolyline;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.2.0
 */
public class VSurfacePolyline extends SurfacePolyline implements VidesoObject {

	private String name;
	
	public VSurfacePolyline(){
		super();
	}
	
	public VSurfacePolyline(LinkedList<LatLon> line) {
		super(line);
	}

	public VSurfacePolyline(BasicShapeAttributes basicShapeAttributes) {
		super(basicShapeAttributes);
	}

	@Override
	public void setAnnotation(String text) {}

	@Override
	public VidesoAnnotation getAnnotation(Position pos) {
		return null;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Object getNormalAttributes() {
		return this.getAttributes();
	}

}
