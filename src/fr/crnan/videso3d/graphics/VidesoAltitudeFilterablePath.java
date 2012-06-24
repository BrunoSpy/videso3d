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

/**
 * {@link AltitudeFilterablePath} with support for annotations
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class VidesoAltitudeFilterablePath extends AltitudeFilterablePath
		implements VidesoObject {

	private String name;
	private String annotation;
	
	@Override
	public void setAnnotation(String text) {
		this.annotation = text;
	}

	
	@Override
	public VidesoAnnotation getAnnotation(Position pos) {
		if(annotation != null){
			return new PathGlobeAnnotation(annotation, pos);
		} else {
			return null;
		}
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
