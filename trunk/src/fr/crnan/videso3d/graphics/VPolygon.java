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
import gov.nasa.worldwind.render.Highlightable;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.Polygon;;
/**
 * Adds the ability to find if a point is inside the Polygon
 * @author Bruno Spyckerelle
 * @version 0.1.2
 */
public class VPolygon extends Polygon implements Highlightable{

	private java.awt.Polygon surface;
	private boolean highlighted = false;
	private AirspaceAttributes normalAttrs;
	private AirspaceAttributes highlightAttrs;
	
	public VPolygon(List<? extends LatLon> locations) {
		super(locations);
	}

	public VPolygon() {
		super();
	}

	/**
	 * 
	 * @param pos
	 * @return True if the point is inside
	 */
	public boolean contains(Position pos){
		if(pos.elevation >= this.getAltitudes()[0] && pos.elevation <= this.getAltitudes()[1]) {
			if(this.surface == null) {
				this.surface = new java.awt.Polygon();
				for(LatLon l : this.getLocations()){
					surface.addPoint((int)(l.longitude.degrees*100), (int)(l.latitude.degrees*100));
				}
			}
			return surface.contains((int)(pos.longitude.degrees*100), (int)(pos.latitude.degrees*100));
		} else {
			return false;
		}
	}

	@Override
	public void setLocations(Iterable<? extends LatLon> locations) {
		super.setLocations(locations);
		this.surface = null;
	}

	@Override
	protected void addLocations(Iterable<? extends LatLon> newLocations) {
		super.addLocations(newLocations);
		this.surface = null;
	}

	@Override
	protected void doMoveTo(Position oldRef, Position newRef) {
		super.doMoveTo(oldRef, newRef);
		this.surface = null;
	}

	@Override
	public void move(Position position) {
		super.move(position);
		this.surface = null;
	}

	@Override
	public void moveTo(Position position) {
		super.moveTo(position);
		this.surface = null;
	}
	
	@Override
	public boolean isHighlighted() {
		return this.highlighted ;
	}

	@Override
	public void setHighlighted(boolean highlighted) {
		if(this.highlighted != highlighted){
			this.setAttributes(highlighted ? this.getHighlightAttributes() : this.getNormalAttributes());
			this.highlighted = highlighted;
		}
	}
	
    public AirspaceAttributes getNormalAttributes() {
        return this.normalAttrs == null ? this.getAttributes() : this.normalAttrs;
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
    		highlightAttrs = new BasicAirspaceAttributes(this.getAttributes());
    		highlightAttrs.setMaterial(Material.WHITE);
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
	
}
