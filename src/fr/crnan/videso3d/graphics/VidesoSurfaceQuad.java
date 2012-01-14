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
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.SurfaceQuad;
import gov.nasa.worldwind.util.RestorableSupport;
import gov.nasa.worldwind.util.RestorableSupport.StateObject;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.2
 */
public class VidesoSurfaceQuad extends SurfaceQuad implements VidesoObject, PriorityRenderable{

	private VidesoAnnotation annotation;
	private String name;
	private int priority = 0;

	public VidesoSurfaceQuad(BasicShapeAttributes basicShapeAttributes) {
		super(basicShapeAttributes);
	}

	public VidesoSurfaceQuad(){
		super(new BasicShapeAttributes());
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
		return this.priority  ;
	}

	@Override
	public void setPriority(int priority) {
		this.priority = priority;
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
