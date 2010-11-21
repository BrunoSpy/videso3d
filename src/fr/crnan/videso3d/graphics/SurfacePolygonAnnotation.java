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
import java.util.List;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.geom.LatLonCautra;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.SurfacePolygon;
/**
 * SurfacePolygon avec Annotation intégrée
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class SurfacePolygonAnnotation extends SurfacePolygon implements VidesoObject {

	private GlobeAnnotation annotation;
	
	private DatabaseManager.Type base;
	
	private int type;
	
	private String name;
	
	public SurfacePolygonAnnotation(List<LatLonCautra> locations) {
		super(locations);
	}

	@Override
	public void setAnnotation(String text){
		if(annotation == null) {
			annotation = new GlobeAnnotation(text, Position.ZERO);
			annotation.setAlwaysOnTop(true);
			annotation.getAttributes().setBackgroundColor(Pallet.ANNOTATION_BACKGROUND);
			annotation.getAttributes().setBorderColor(Color.BLACK);
			annotation.getAttributes().setAdjustWidthToText(Annotation.SIZE_FIT_TEXT);			
		} else {
			annotation.setText(text);
		}
	}
	@Override
	public GlobeAnnotation getAnnotation(Position pos){
		annotation.setPosition(pos);
		return annotation;
	}
	
	@Override
	public DatabaseManager.Type getDatabaseType() {
		return this.base;
	}

	@Override
	public void setDatabaseType(DatabaseManager.Type type) {
		this.base = type;
	}
	
	@Override
	public void setType(int type) {
		this.type = type;
	}

	@Override
	public int getType() {
		return this.type;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
