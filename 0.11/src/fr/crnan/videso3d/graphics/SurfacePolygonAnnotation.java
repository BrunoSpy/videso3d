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
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.util.RestorableSupport;
import gov.nasa.worldwind.util.RestorableSupport.StateObject;
/**
 * SurfacePolygon avec Annotation intégrée
 * @author Bruno Spyckerelle
 * @version 0.1.4
 */
public class SurfacePolygonAnnotation extends SurfacePolygon implements VidesoObject, PriorityRenderable {

	private VidesoAnnotation annotation;
	
	private String name;

	private int priority = 1;
	
	public SurfacePolygonAnnotation(){
		super();
	}
	
	/**
	 * 
	 * @param locations les coordonnées du polygone
	 */
	public SurfacePolygonAnnotation(List<? extends LatLon> locations){
		super(locations);
	}

	public SurfacePolygonAnnotation(BasicShapeAttributes basicShapeAttributes) {
		super(basicShapeAttributes);
	}


	@Override
	public void setAnnotation(String text){
		if(annotation == null) {
			annotation = new VidesoAnnotation(text);		
		} else {
			annotation.setText(text);
		}
	}
	@Override
	public VidesoAnnotation getAnnotation(Position pos){
		if(this.annotation == null){
			if(this.getName() == null)
				return null;
			this.annotation = new VidesoAnnotation(this.getName());
		}
		annotation.setPosition(pos);
		return annotation;
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

	@Override
	public int getPriority() {
		return this.priority;
	}

	@Override
	public void setPriority(int priority) {
		this.priority  = priority;
	}
	
	@Override
	protected void doGetRestorableState(RestorableSupport rs,
			StateObject context) {
		super.doGetRestorableState(rs, context);
		
		if(this.getName() != null) rs.addStateValueAsString(context, "name", this.getName());
		if(this.annotation != null) rs.addStateValueAsString(context, "annotation", this.annotation.getText(), true);
		rs.addStateValueAsInteger(context, "priority", this.getPriority());
	}

	@Override
	protected void doRestoreState(RestorableSupport rs, StateObject context) {
		super.doRestoreState(rs, context);
		
		String s = rs.getStateValueAsString(context, "name");
		if(s != null)
			this.setName(s);
		
		s = rs.getStateValueAsString(context, "annotation");
		if(s != null)
			this.setAnnotation(s);
		
		Integer i = rs.getStateValueAsInteger(context, "priority");
		if(i != null)
			this.setPriority(i);
	}
	
	
}
