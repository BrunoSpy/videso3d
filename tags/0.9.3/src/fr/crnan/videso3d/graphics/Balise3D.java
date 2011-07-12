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

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.2
 */
public class Balise3D extends PointPlacemark implements Balise {

	private int type;
	private Type base;
	
	private VidesoAnnotation annotation;
	
	public Balise3D(CharSequence name, Position position, String annotation, DatabaseManager.Type base, int type){
		super(position);
		this.setType(type);
		this.setDatabaseType(base);
		
		this.setLabelText((String) name);
		
		this.setValue(AVKey.DISPLAY_NAME, annotation);
		this.setAltitudeMode(WorldWind.ABSOLUTE);
		this.setApplyVerticalExaggeration(true);
		this.setLineEnabled(true);
		this.setLinePickWidth(200);
		this.setEnableBatchPicking(false);
		
		PointPlacemarkAttributes ppa = new PointPlacemarkAttributes();
		ppa.setLineWidth(2d);
		ppa.setLineMaterial(Material.WHITE);
		ppa.setUsePointAsDefaultImage(true);
		ppa.setLabelScale(0.7);
		if(annotation == null){
			this.setAnnotation((String) name);
		} else {
			this.setAnnotation(annotation);
		}
		this.setAttributes(ppa);
	}
	
	public Balise3D(String balise, Position position, Type base, int type) {
		this(balise,  position, null, base, type);
	}

	@Override
	public String getName() {
		return this.getLabelText();
	}

	@Override
	public void setAnnotation(String text) {
		if(annotation == null) {
			annotation = new VidesoAnnotation(text);
		} else {
			annotation.setText(text);
		}
	}

	@Override
	public VidesoAnnotation getAnnotation(Position pos) {
		annotation.setPosition(pos);
		return annotation;
	}

	@Override
	public Type getDatabaseType() {
		return this.base;
	}

	@Override
	public void setDatabaseType(Type type) {
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
	public void setName(String name) {
		this.setLabelText(name);
	}

	@Override
	public void highlight(boolean b) {
		// TODO Auto-generated method stub
		
	}

}
