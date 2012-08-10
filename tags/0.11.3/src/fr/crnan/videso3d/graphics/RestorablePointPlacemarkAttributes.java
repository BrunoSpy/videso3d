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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.util.RestorableSupport;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class RestorablePointPlacemarkAttributes extends
		PointPlacemarkAttributes {

	 public RestorablePointPlacemarkAttributes(
			RestorablePointPlacemarkAttributes ppa) {
		 super(ppa);
	}

	public RestorablePointPlacemarkAttributes() {
		super();
	}

	public void getRestorableState(RestorableSupport rs, RestorableSupport.StateObject so) {
	        
	        if(this.getImageAddress() != null) rs.addStateValueAsString(so, "imageAdress", this.getImageAddress());
	        if(this.getScale() != null) rs.addStateValueAsDouble(so, "scale", this.getScale());
	        if(this.getHeading() != null) rs.addStateValueAsDouble(so, "heading", this.getHeading());
	        if(this.getHeadingReference() != null) rs.addStateValueAsString(so, "headingReference", this.getHeadingReference());
	        if(this.getPitch() != null) rs.addStateValueAsDouble(so, "pitch", this.getPitch());
	        if(this.getImageOffset() != null) this.getImageOffset().getRestorableState(rs, rs.addStateObject(so, "imageOffset"));
	        
	        if(this.getImageColor() != null) rs.addStateValueAsColor(so, "imageColor", this.getImageColor());
	        if(this.getLineWidth() != null) rs.addStateValueAsDouble(so, "lineWidth", this.getLineWidth());
	        if(this.getLineMaterial() != null) this.getLineMaterial().getRestorableState(rs, rs.addStateObject(so, "lineMaterial"));
	        rs.addStateValueAsInteger(so, "antiAliasHint", getAntiAliasHint());
	        if(this.getLabelFont() != null) {
	        	rs.addStateValueAsString(so, "labelFont", this.getLabelFont().getFontName());
	        	rs.addStateValueAsInteger(so, "labelFontStyle", this.getLabelFont().getStyle());
	        	rs.addStateValueAsInteger(so, "labelFontSize", this.getLabelFont().getSize());
	        }
	        if(this.getLabelOffset() != null) this.getLabelOffset().getRestorableState(rs, rs.addStateObject(so, "labelOffset"));
	        if(this.getLabelMaterial() != null) this.getLabelMaterial().getRestorableState(rs, rs.addStateObject(so, "labelMaterial"));   
	        if(this.getLabelScale() != null) rs.addStateValueAsDouble(so, "labelScale", this.getLabelScale());
	        rs.addStateValueAsBoolean(so, "usePointAsDefault", isUsePointAsDefaultImage());
	        rs.addStateValueAsBoolean(so, "unresolved", isUnresolved());
	        
	    }

	    public void restoreState(RestorableSupport rs, RestorableSupport.StateObject so) {
	       
	    	String s = rs.getStateValueAsString(so, "imageAdress");
	    	if(s != null)
	    		this.setImageAddress(s);
	    	
	    	Double d = rs.getStateValueAsDouble(so, "scale");
	    	if(d!= null)
	    		this.setScale(d);
	    	
	    	d = rs.getStateValueAsDouble(so, "heading");
	    	if(d != null)
	    		this.setHeading(d);
	    	
	    	s = rs.getStateValueAsString(so, "headingReference");
	    	if(s != null)
	    		this.setHeadingReference(s);
	    	
	    	d = rs.getStateValueAsDouble(so, "pitch");
	    	if(d != null)
	    		this.setPitch(d);
	    	
	    	RestorableSupport.StateObject io = rs.getStateObject(so, "imageOffset");
	    	if(io != null){
	    		if(this.getImageOffset() == null){
	    			this.setImageOffset(new Offset(0.0, 0.0, AVKey.PIXELS, AVKey.PIXELS));
	    		}
	    		this.getImageOffset().restoreState(rs, io);
	    	}
	    	
	    	Color c = rs.getStateValueAsColor(so, "imageColor");
	    	if(c != null)
	    		this.setImageColor(c);
	    	
	    	d =  rs.getStateValueAsDouble(so, "lineWidth");
	    	if(d != null)
	    		this.setLineWidth(d);
	    	
	        RestorableSupport.StateObject mo = rs.getStateObject(so, "lineMaterial");
	        if (mo != null)
	            this.setLineMaterial(Material.WHITE.restoreState(rs, mo));

	        Integer i = rs.getStateValueAsInteger(so, "antiAliasHint");
	        if(i != null)
	        	this.setAntiAliasHint(i);
	        
	        s = rs.getStateValueAsString(so, "labelFont");
	        i = rs.getStateValueAsInteger(so, "labelFontStyle");
	        Integer i1 = rs.getStateValueAsInteger(so, "labelFontSize");
	        if(s != null){
	        	Font f = new Font(s, i, i1);
	        	this.setLabelFont(f);
	        }
	        
	        RestorableSupport.StateObject lo = rs.getStateObject(so, "labelOffset");
	    	if(lo != null) {
	    		if(this.getLabelOffset() == null)
	    			this.setLabelOffset(new Offset(0.0, 0.0, AVKey.PIXELS, AVKey.PIXELS));
	    		this.getLabelOffset().restoreState(rs, lo);
	    	}
	        
	        RestorableSupport.StateObject hmo = rs.getStateObject(so, "labelMaterial");
	        if (hmo != null)
	            this.setLabelMaterial(Material.WHITE.restoreState(rs, hmo));
	        
	        d = rs.getStateValueAsDouble(so, "labelScale");
	        if(d != null)
	        	this.setLabelScale(d);
	        
	        Boolean b = rs.getStateValueAsBoolean(so, "usePointAsDefault");
	        if(b != null)
	        	this.setUsePointAsDefaultImage(b);
	        	
	        b = rs.getStateValueAsBoolean(so, "unresolved");
	        if(b != null)
	        	this.setUnresolved(b);
	    }
	
}
