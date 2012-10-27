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
import java.awt.Insets;
import java.awt.Point;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AnnotationAttributes;
/**
 * Annotation whose attributes are adapted to a Path
 * @author Bruno Spyckerelle
 * @version 0.1.0
 *
 */
public class PathGlobeAnnotation extends VidesoAnnotation {

	public PathGlobeAnnotation(String text, Position position) {
		super(text, position);
		this.setAlwaysOnTop(true);
		
		AnnotationAttributes attrs = new AnnotationAttributes();
		attrs.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
		attrs.setFrameShape(AVKey.SHAPE_RECTANGLE);
		attrs.setDrawOffset(new Point(0, 10));
		attrs.setLeaderGapWidth(5);
		attrs.setTextColor(Color.BLACK);
		attrs.setBackgroundColor(new Color(1f, 1f, 1f, 0.8f));
		attrs.setCornerRadius(5);
		attrs.setBorderColor(new Color(0xababab));
		attrs.setFont(Font.decode("Arial-PLAIN-12"));
		attrs.setTextAlign(AVKey.CENTER);
		attrs.setInsets(new Insets(5, 5, 5, 5));

		this.setAttributes(attrs);

		this.setPickEnabled(true);
	}
	
}
