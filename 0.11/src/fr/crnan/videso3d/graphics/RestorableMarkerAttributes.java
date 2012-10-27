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

import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.util.RestorableSupport;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class RestorableMarkerAttributes extends BasicMarkerAttributes {

	public RestorableMarkerAttributes(){
		super();
	}
	
	public RestorableMarkerAttributes(BasicMarkerAttributes attrs) {
		super(attrs);
	}
	
    public void getRestorableState(RestorableSupport rs, RestorableSupport.StateObject so) {
        this.getMaterial().getRestorableState(rs, rs.addStateObject(so, "material"));

        this.getHeadingMaterial().getRestorableState(rs, rs.addStateObject(so, "headingMaterial"));
        
        rs.addStateValueAsDouble(so, "headingScale", this.getHeadingScale());
        
        rs.addStateValueAsDouble(so, "opacity", this.getOpacity());
        
        rs.addStateValueAsString(so, "shapeType", this.getShapeType());

        rs.addStateValueAsDouble(so, "markerPixels", this.getMarkerPixels());
        rs.addStateValueAsDouble(so, "minMarkerSize", this.getMinMarkerSize());
        rs.addStateValueAsDouble(so, "maxMarkerSize", this.getMaxMarkerSize());
        
    }

    public void restoreState(RestorableSupport rs, RestorableSupport.StateObject so) {
       
        RestorableSupport.StateObject mo = rs.getStateObject(so, "material");
        if (mo != null)
            this.setMaterial(this.getMaterial().restoreState(rs, mo));

        RestorableSupport.StateObject hmo = rs.getStateObject(so, "headingMaterial");
        if (hmo != null)
            this.setHeadingMaterial(this.getHeadingMaterial().restoreState(rs, hmo));
        
        Double h = rs.getStateValueAsDouble(so, "headingScale");
        if(h != null)
        	this.setHeadingScale(h);
        
        String s = rs.getStateValueAsString(so, "shapeType");
        if(s != null)
        	this.setShapeType(s);
        
        Double d = rs.getStateValueAsDouble(so, "opacity");
        if (d != null)
            this.setOpacity(d);
        
        Double p = rs.getStateValueAsDouble(so, "markerPixels");
        if (p != null){
        	this.setMarkerPixels(3);
        }
        
        Double min = rs.getStateValueAsDouble(so, "minMarkerSize");
        if (min != null)
            this.setMinMarkerSize(min);
        
        Double max = rs.getStateValueAsDouble(so, "maxMarkerSize");
        if (max != null)
            this.setMaxMarkerSize(max);
        
    }

}
