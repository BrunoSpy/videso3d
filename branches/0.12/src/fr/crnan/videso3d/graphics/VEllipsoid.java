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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Ellipsoid;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class VEllipsoid extends Ellipsoid implements VidesoObject {

	private VidesoAnnotation annotation;
	
	@Override
	public void setAnnotation(String text) {
		if(annotation == null)
			annotation = new VidesoAnnotation(text);
		else
			annotation.setText(text);
	}

	@Override
	public VidesoAnnotation getAnnotation(Position pos) {
		if(annotation == null)
			setAnnotation(getName());
		annotation.setPosition(pos);
		return annotation;
	}

	@Override
	public String getName() {
		return (String) getValue(AVKey.DISPLAY_NAME);
	}

	@Override
	public void setName(String name) {
		setValue(AVKey.DISPLAY_NAME, name);
	}

	@Override
	public Object getNormalAttributes() {
		return super.getAttributes();
	}

}
