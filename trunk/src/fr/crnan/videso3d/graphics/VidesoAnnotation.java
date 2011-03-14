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

import fr.crnan.videso3d.Pallet;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.GlobeAnnotation;
/**
 * 
 * @author Annotation
 * @version 0.1.0
 */
public class VidesoAnnotation extends GlobeAnnotation {

	public VidesoAnnotation(String text){
		this(text, Position.ZERO);
	}
	
	public VidesoAnnotation(String text, Position position) {
		super(text, position);
		this.setAlwaysOnTop(true);
		this.getAttributes().setBackgroundColor(Pallet.ANNOTATION_BACKGROUND);
		this.getAttributes().setBorderColor(Color.BLACK);
		this.getAttributes().setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
	}

}
