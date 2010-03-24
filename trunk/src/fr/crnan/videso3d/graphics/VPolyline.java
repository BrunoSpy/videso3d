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

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import java.util.*;

/**
 * Extension de Polyline de façon à changer la couleur en fonction de l'altitude.<br />
 * Permet aussi de dessiner un polygone entre le sol et la polyligne.
 * @author Bruno Spyckerelle
 * @version 0.3
 */
public class VPolyline extends Polyline
{

    private Vec4 referenceCenterPoint;
    private Position referenceCenterPosition = Position.ZERO;
    private ArrayList<ArrayList<Vec4>> currentSpans;
    private long geomGenTimeStamp = -Long.MAX_VALUE;
    private double geomGenVE = 1;
    
    private Boolean plain = false;
    private ArrayList<ArrayList<Vec4>> currentCurtains;
    
    private boolean shadedColors = false;
    private double maxElevation = 2.0e4;
    private double minElevation = 0.0;
    

    /**
     * Draw the polyline like a polygon from the ground ?
     * @return
     */
    public Boolean isPlain(){
    	return this.plain;
    }
    /**
     * Draw the polyline like a polygon from the ground.
     * @param plain
     */
    public void setPlain(Boolean plain){
    	this.plain = plain;
    }

    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (((ArrayList<Position>)this.getPositions()).size() < 2)
            return;

     // vertices potentially computed every frame to follow terrain changes
        if (this.currentSpans == null || (this.isFollowTerrain() && this.geomGenTimeStamp != dc.getFrameTimeStamp())
            || this.geomGenVE != dc.getVerticalExaggeration())
        {
            // Reference center must be computed prior to computing vertices.
            this.computeReferenceCenter(dc);
            this.makeVertices(dc);
            this.geomGenTimeStamp = dc.getFrameTimeStamp();
            this.geomGenVE = dc.getVerticalExaggeration();
        }

        if (this.currentSpans == null || this.currentSpans.size() < 1)
            return;

        if (dc.isPickingMode() && !dc.getPickFrustums().intersectsAny(this.getExtent(dc)))
            return;

        GL gl = dc.getGL();

        int attrBits = GL.GL_HINT_BIT | GL.GL_CURRENT_BIT | GL.GL_LINE_BIT;
        if (!dc.isPickingMode())
        {
            if (this.getColor().getAlpha() != 255)
                attrBits |= GL.GL_COLOR_BUFFER_BIT;
        }

        gl.glPushAttrib(attrBits);
        dc.getView().pushReferenceCenter(dc, this.referenceCenterPoint);

        boolean projectionOffsetPushed = false; // keep track for error recovery
        
        try
        {
            if (!dc.isPickingMode())
            {
                if (this.getColor().getAlpha() != 255)
                {
                    gl.glEnable(GL.GL_BLEND);
                    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
                }
                if(!isShadedColors()) 
                	dc.getGL().glColor4ub((byte) this.getColor().getRed(),
                			(byte) this.getColor().getGreen(),
                			(byte) this.getColor().getBlue(), 
                			(byte) this.getColor().getAlpha());
            }


            if (this.getStippleFactor() > 0)
            {
                gl.glEnable(GL.GL_LINE_STIPPLE);
                gl.glLineStipple(this.getStippleFactor(), this.getStipplePattern());
            }
            else
            {
                gl.glDisable(GL.GL_LINE_STIPPLE);
            }

            int hintAttr = GL.GL_LINE_SMOOTH_HINT;
            if (this.isFilled() || this.isPlain())
                hintAttr = GL.GL_POLYGON_SMOOTH_HINT;
            gl.glHint(hintAttr, this.getAntiAliasHint());

            int primType = GL.GL_LINE_STRIP;
            if (this.isFilled() || this.isPlain())
                primType = GL.GL_POLYGON;

            if (dc.isPickingMode())
                gl.glLineWidth((float) this.getLineWidth() + 8);
            else
                gl.glLineWidth((float) this.getLineWidth());


            if (this.isFollowTerrain()){
            	dc.pushProjectionOffest(0.99);
            	projectionOffsetPushed = true;
            }

            for (int i=0;i< this.currentSpans.size();i++)
            {
            	ArrayList<Vec4> span = this.currentSpans.get(i);
            	ArrayList<Vec4> ground = this.currentCurtains.get(i);
            	
                if (span == null)
                    continue;

                // Since segements can very often be very short -- two vertices -- use explicit rendering. The
                // overhead of batched rendering, e.g., gl.glDrawArrays, is too high because it requires copying
                // the vertices into a DoubleBuffer, and DoubleBuffer creation and access performs relatively poorly.
                gl.glBegin(primType);
                Vec4 g;
                if(isPlain()) {
                	g = ground.get(0);
                	gl.glVertex3d(g.x, g.y, g.z);
                }
                for (Vec4 p : span)
                {
                	if(isShadedColors()){
                	Double k = ((ArrayList<Position>)this.getPositions()).get(i).elevation;
                	dc.getGL().glColor4ub((byte) (255*((k-this.getMinElevation())/(this.getMaxElevation()-this.getMinElevation()))),
                			(byte) (255*((this.getMaxElevation()-k)/(this.getMaxElevation()-this.getMinElevation()))),
                			(byte) 0,
                			(byte) this.getColor().getAlpha());
                	}
                    gl.glVertex3d(p.x, p.y, p.z);
                }
                if(isPlain()) {
                	g = ground.get(ground.size()-1);
                	gl.glVertex3d(g.x, g.y, g.z);
                }
                gl.glEnd();
            }

            if (this.isHighlighted())
            {
                if (!dc.isPickingMode())
                {
                    if (this.getHighlightColor().getAlpha() != 255)
                    {
                        gl.glEnable(GL.GL_BLEND);
                        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
                    }
                    dc.getGL().glColor4ub((byte) this.getHighlightColor().getRed(), (byte) this.getHighlightColor().getGreen(),
                        (byte) this.getHighlightColor().getBlue(), (byte) this.getHighlightColor().getAlpha());

                    gl.glLineWidth((float) this.getLineWidth() + 2);
                    for (ArrayList<Vec4> span : this.currentSpans)
                    {
                        if (span == null)
                            continue;

                        gl.glBegin(primType);
                        for (Vec4 p : span)
                        {
                            gl.glVertex3d(p.x, p.y, p.z);
                        }
                        gl.glEnd();
                    }
                }
            }

           
        }
        finally
        {
        	if (projectionOffsetPushed)
                dc.popProjectionOffest();
        	
            gl.glPopAttrib();
            dc.getView().popReferenceCenter(dc);
        }
    }
    
    @Override
    protected void makeVertices(DrawContext dc)
    {
        if (this.currentSpans == null)
            this.currentSpans = new ArrayList<ArrayList<Vec4>>();
        else
            this.currentSpans.clear();

        if(this.currentCurtains == null){
        	this.currentCurtains = new ArrayList<ArrayList<Vec4>>();
        } else {
        	this.currentCurtains.clear();
        }
        
        if (((ArrayList<Position>)this.getPositions()).size() < 1)
            return;

        Position posA = ((ArrayList<Position>)this.getPositions()).get(0);
        Position posGrdA = new Position(posA.getLatitude(), posA.getLongitude(), 0);
        Vec4 ptA = this.computePoint(dc, posA, true);
        Vec4 grdA =  this.computePoint(dc, posGrdA, true);
        for (int i = 1; i <= ((ArrayList<Position>)this.getPositions()).size(); i++)
        {
            Position posB;
            Position posGrdB;
            if (i < ((ArrayList<Position>)this.getPositions()).size()){
                posB = ((ArrayList<Position>)this.getPositions()).get(i);
                posGrdB = new Position(posB.getLatitude(), posB.getLongitude(), 0);
            }
            else if (this.isClosed()) {
                posB = ((ArrayList<Position>)this.getPositions()).get(0);
                posGrdB = new Position(posB.getLatitude(), posB.getLongitude(), 0);
            }
            else
                break;

            Vec4 ptB = this.computePoint(dc, posB, true);
            Vec4 grdB = this.computePoint(dc, posGrdB, true);
            
            if (this.isFollowTerrain() && !this.isSegmentVisible(dc, posA, posB, ptA, ptB))
            {
                posA = posB;
                ptA = ptB;
                continue;
            }

            ArrayList<Vec4> span;
            span = this.makeSegment(dc, posA, posB, ptA, ptB);
            ArrayList<Vec4> ground;
            ground = this.makeSegment(dc, posGrdA, posGrdB, grdA, grdB);
            if (span != null)
                this.addSpan(span);

            this.currentCurtains.add(ground);
            
            grdA = grdB;
            posGrdA = posGrdB;
            posA = posB;
            ptA = ptB;
        }
    }

    private void addSpan(ArrayList<Vec4> span)
    {
        if (span != null && span.size() > 0)
            this.currentSpans.add(span);
    }

    private boolean isSegmentVisible(DrawContext dc, Position posA, Position posB, Vec4 ptA, Vec4 ptB)
    {
        Frustum f = dc.getView().getFrustumInModelCoordinates();

        if (f.contains(ptA))
            return true;

        if (f.contains(ptB))
            return true;

        if (ptA.equals(ptB))
            return false;

        Position posC = Position.interpolate(0.5, posA, posB);
        Vec4 ptC = this.computePoint(dc, posC, true);
        if (f.contains(ptC))
            return true;

        // TODO: Find a more efficient bounding geometry for this frustum intersection test.
        double r = Line.distanceToSegment(ptA, ptB, ptC);
        Cylinder cyl = new Cylinder(ptA, ptB, r == 0 ? 1 : r);
        return cyl.intersects(dc.getView().getFrustumInModelCoordinates());
    }

    private Vec4 computePoint(DrawContext dc, Position pos, boolean applyOffset)
    {
        if (this.isFollowTerrain())
        {
            double height = !applyOffset ? 0 : this.getOffset();
            return this.computeTerrainPoint(dc, pos.getLatitude(), pos.getLongitude(), height);
        }
        else
        {
            double height = (pos.getElevation() + (applyOffset ? this.getOffset() : 0));
            return dc.getGlobe().computePointFromPosition(pos.getLatitude(), pos.getLongitude(),
            		height * dc.getVerticalExaggeration());
        }
    }

    private double computeSegmentLength(DrawContext dc, Position posA, Position posB)
    {
        LatLon llA = new LatLon(posA.getLatitude(), posA.getLongitude());
        LatLon llB = new LatLon(posB.getLatitude(), posB.getLongitude());

        Angle ang = LatLon.greatCircleDistance(llA, llB);

        if (this.isFollowTerrain())
        {
            return ang.radians * (dc.getGlobe().getRadius() + this.getOffset() * dc.getVerticalExaggeration());
        }
        else
        {
            double height = this.getOffset() + 0.5 * (posA.getElevation() + posB.getElevation());
            return ang.radians * (dc.getGlobe().getRadius() + height * dc.getVerticalExaggeration());
        }
    }

    private ArrayList<Vec4> makeSegment(DrawContext dc, Position posA, Position posB, Vec4 ptA, Vec4 ptB)
    {
        ArrayList<Vec4> span = null;

        double arcLength = this.computeSegmentLength(dc, posA, posB);
        if (arcLength <= 0) // points differing only in altitude
        {
            span = this.addPointToSpan(ptA, span);
            if (!ptA.equals(ptB))
                span = this.addPointToSpan(ptB, span);
            return span;
        }
        // Variables for great circle and rhumb computation.
        Angle segmentAzimuth = null;
        Angle segmentDistance = null;

        for (double s = 0, p = 0; s < 1;)
        {
            if (this.isFollowTerrain())
                p += this.getTerrainConformance() * dc.getView().computePixelSizeAtDistance(
                    ptA.distanceTo3(dc.getView().getEyePoint()));
            else
                p += arcLength / this.getNumSubsegments();

            s = p / arcLength;

            Position pos;
            if (s >= 1)
            {
                pos = posB;
            }
            else if (this.getPathType() == LINEAR)
            {
                pos = Position.interpolate(s, posA, posB);
            }
            else if (this.getPathType() == RHUMB_LINE) // or LOXODROME
            {
                if (segmentAzimuth == null)
                {
                    segmentAzimuth = LatLon.rhumbAzimuth(posA, posB);
                    segmentDistance = LatLon.rhumbDistance(posA, posB);
                }
                Angle distance = Angle.fromRadians(s * segmentDistance.radians);
                LatLon latLon = LatLon.rhumbEndPosition(posA, segmentAzimuth, distance);
                pos = new Position(latLon, (1 - s) * posA.getElevation() + s * posB.getElevation());
            }
            else // GREAT_CIRCLE
            {
                if (segmentAzimuth == null)
                {
                    segmentAzimuth = LatLon.greatCircleAzimuth(posA, posB);
                    segmentDistance = LatLon.greatCircleDistance(posA, posB);
                }
                Angle distance = Angle.fromRadians(s * segmentDistance.radians);
                LatLon latLon = LatLon.greatCircleEndPosition(posA, segmentAzimuth, distance);
                pos = new Position(latLon, (1 - s) * posA.getElevation() + s * posB.getElevation());
            }

            ptB = this.computePoint(dc, pos, true);
            span = this.clipAndAdd(dc, ptA, ptB, span);

            ptA = ptB;
        }

        return span;
    }

    private ArrayList<Vec4> clipAndAdd(DrawContext dc, Vec4 ptA, Vec4 ptB, ArrayList<Vec4> span)
    {
        // Line clipping appears to be useful only for long lines with few segments. It's costly otherwise.
        // TODO: Investigate trade-off of line clipping.
//        if (Line.clipToFrustum(ptA, ptB, dc.getView().getFrustumInModelCoordinates()) == null)
//        {
//            if (span != null)
//            {
//                this.addSpan(span);
//                span = null;
//            }
//            return span;
//        }

        if (span == null)
            span = this.addPointToSpan(ptA, span);

        return this.addPointToSpan(ptB, span);
    }

    private ArrayList<Vec4> addPointToSpan(Vec4 p, ArrayList<Vec4> span)
    {
        if (span == null)
            span = new ArrayList<Vec4>();

        span.add(p.subtract3(this.referenceCenterPoint));

        return span;
    }

    private void computeReferenceCenter(DrawContext dc)
    {
        if (((ArrayList<Position>)this.getPositions()).size() < 1)
            return;

        if (((ArrayList<Position>)this.getPositions()).size() < 3)
            this.referenceCenterPosition = ((ArrayList<Position>)this.getPositions()).get(0);
        else
            this.referenceCenterPosition = ((ArrayList<Position>)this.getPositions()).get(((ArrayList<Position>)this.getPositions()).size() / 2);

        this.referenceCenterPoint = this.computeTerrainPoint(dc,
            this.referenceCenterPosition.getLatitude(), this.referenceCenterPosition.getLongitude(), this.getOffset());
    }
    
    public Position getReferencePosition()
    {
        return this.referenceCenterPosition;
    }
    
    private Vec4 computeTerrainPoint(DrawContext dc, Angle lat, Angle lon, double offset)
    {
        Vec4 p = dc.getSurfaceGeometry().getSurfacePoint(lat, lon, offset);

        if (p == null)
        {
            p = dc.getGlobe().computePointFromPosition(lat, lon,
                offset + dc.getGlobe().getElevation(lat, lon) * dc.getVerticalExaggeration());
        }

        return p;
    }
    

    /**
     * Set the max elevation used for shaded colors
     * @param maxElevation
     */
	public void setMaxElevation(double maxElevation) {
		this.maxElevation = maxElevation;
	}

	public double getMaxElevation() {
		return maxElevation;
	}

	/**
     * Set the min elevation used for shaded colors
     * @param maxElevation
     */
	public void setMinElevation(double minElevation) {
		this.minElevation = minElevation;
	}

	public double getMinElevation() {
		return minElevation;
	}

	/**
	 * If true, polyline color will shade from red to green depending on the elevation.
	 * @param shadedColors
	 */
	public void setShadedColors(boolean shadedColors) {
		this.shadedColors = shadedColors;
	}

	public boolean isShadedColors() {
		return shadedColors;
	}
}
