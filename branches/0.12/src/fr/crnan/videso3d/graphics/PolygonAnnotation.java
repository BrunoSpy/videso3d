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
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.util.RestorableSupport;
/**
 * Polygon avec Annotation intégrée
 * @author Bruno Spyckerelle
 * @version 0.4.0
 */
public class PolygonAnnotation extends VPolygon implements VidesoObject{

	private VidesoAnnotation annotation;

	private String name;
	
	private boolean highlighted = false;

	private AirspaceAttributes normalAttrs;

	private AirspaceAttributes highlightAttrs;
	
	public PolygonAnnotation(){
		super();
	}
	
	public PolygonAnnotation(List<? extends LatLon> locations) {
		super(locations);
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
		if(annotation == null){
			annotation = new VidesoAnnotation(this.getName());
		}
		annotation.setPosition(pos);
		return annotation;
	}
	
	@Override
	public String toString(){
		return this.getName();
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
	public boolean isHighlighted() {
		return this.highlighted;
	}

	@Override
	public void setHighlighted(boolean highlighted) {
		if(this.highlighted != highlighted){
			this.setAttributes(highlighted ? this.getHighlightAttributes() : this.getNormalAttributes());
			this.highlighted = highlighted;
		}
	}
	
    public AirspaceAttributes getNormalAttributes() {
    	if(this.normalAttrs == null)
    		this.normalAttrs = this.getAttributes();
        return this.normalAttrs;
    }

    public void setNormalAttributes(AirspaceAttributes normalAttrs) {
        this.normalAttrs = normalAttrs;
        if(!highlighted) this.setAttributes(this.normalAttrs);
    }
    
    /**
     * At the first call, if null, set it to White
     */
    public AirspaceAttributes getHighlightAttributes() {
    	if(highlightAttrs == null){
    		highlightAttrs = new BasicAirspaceAttributes(this.getNormalAttributes());
    		highlightAttrs.setMaterial(Material.YELLOW);
    	}
        return this.highlightAttrs;
    }

    /**
     * Specifies highlight attributes.
     *
     * @param highlightAttrs highlight attributes. May be null, in which case default attributes are used.
     */
    public void setHighlightAttributes(AirspaceAttributes highlightAttrs) {
        this.highlightAttrs = highlightAttrs;
        if(highlighted) this.setAttributes(this.highlightAttrs);
    }
    
    @Override
    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doGetRestorableState(rs, context);
      
        rs.addStateValueAsString(context, "annotation", this.getAnnotation(Position.ZERO).getText());
        rs.addStateValueAsString(context, "name", this.getName());
    }

    @Override
    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doRestoreState(rs, context);
        
        String annotation = rs.getStateValueAsString(context, "annotation");
        if(annotation != null)
        	this.setAnnotation(annotation);
        
        String name = rs.getStateValueAsString(context, "name");
        if(name != null)
        	this.setName(name);
        
    }
}
