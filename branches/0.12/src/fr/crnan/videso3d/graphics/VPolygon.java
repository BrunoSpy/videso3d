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

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Highlightable;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.DetailLevel;
import gov.nasa.worldwind.render.airspaces.Polygon;
import gov.nasa.worldwind.render.airspaces.ScreenSizeDetailLevel;
/**
 * Adds the ability to find if a point is inside the Polygon
 * @author Bruno Spyckerelle
 * @version 0.1.4
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
		List<DetailLevel> levels = new ArrayList<DetailLevel>();
        double[] ramp = ScreenSizeDetailLevel.computeDefaultScreenSizeRamp(1);

        DetailLevel level;
        level = new ScreenSizeDetailLevel(ramp[0], "Detail-Level-0");
        level.setValue(SUBDIVISIONS, 0);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        this.setDetailLevels(levels);
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

	/**
	 * 
	 * @param line Intersecting line
	 * @param segment True if only considering <code>line</code> as a segment 
	 * @return All intersection points
	 * @throws Exception 
	 */
	public Set<Point2D> getIntersections(Line2D.Double line, boolean segment){
		Set<Point2D> intersections = new HashSet<Point2D>();
		
		LatLon last = null;
		for(LatLon p : this.getLocations()){
			if(last != null){
				Line2D.Double poly = new Line2D.Double(last.latitude.degrees, last.longitude.degrees,
														p.latitude.degrees, p.longitude.degrees);
				Point2D inter = segment ? getIntersectionInside(line, poly) : getIntersection(line, poly);
				if(inter !=null) intersections.add(inter);
			}
			last = p;
		}
	
        return intersections;
	}
	
	/**
	 * 
	 * @param line1
	 * @param line2
	 * @return Intersection point, null if lines are parallels
	 */
	public static Point2D getIntersection(final Line2D.Double line1, final Line2D.Double line2) {

        final double x1,y1, x2,y2, x3,y3, x4,y4;
        x1 = line1.x1; y1 = line1.y1; x2 = line1.x2; y2 = line1.y2;
        x3 = line2.x1; y3 = line2.y1; x4 = line2.x2; y4 = line2.y2;

        double xq = (x1 - x2)*(y3 - y4) - (y1 - y2)*(x3 - x4);
        double yq = (x1 - x2)*(y3 - y4) - (y1 - y2)*(x3 - x4);
        if(xq != 0 && yq != 0) {
        	final double x = (
        			(x2 - x1)*(x3*y4 - x4*y3) - (x4 - x3)*(x1*y2 - x2*y1)
        			) /
        			(xq);
        	final double y = (
        			(y3 - y4)*(x1*y2 - x2*y1) - (y1 - y2)*(x3*y4 - x4*y3)
        			) /
        			(yq);
        	return new Point2D.Double(x, y);
        } else {
        	return null;
        }
    }
	
	/**
	 * 
	 * @param line1
	 * @param line2
	 * @return Intersection point between line1 and line2, only if this points is inside line1.
	 */
	public static Point2D getIntersectionInside(final Line2D.Double line1, final Line2D.Double line2) {
		Point2D p = getIntersection(line1, line2);
		if(p != null){
			if(line1.x1 < line1.x2){
				if(p.getX() >= line1.x1 && p.getX() <= line1.x2){
					return p;
				} else {
					return null;
				}
			} else if(line1.x1 > line1.x2){
				if(p.getX() <= line1.x1 && p.getX() >= line1.x2){
					return p;
				} else {
					return null;
				}
			} else { //x1==x2
				if(line1.y1 < line1.y2) {
					if(p.getY() >= line1.y1 && p.getY() <= line1.y2){
						return p;
					} else {
						return null;
					}
				} else {
					if(p.getY() <= line1.y1 && p.getY() >= line1.y2){
						return p;
					} else {
						return null;
					}
				}
			}
		} else {
			return null;
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
