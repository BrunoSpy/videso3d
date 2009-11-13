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
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import java.awt.*;
import java.util.*;

/**
 * Extension de Polyline de façon à prendre en compte l'exagération verticale et à changer la couleur en fonction de l'altitude.
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class VPolyline extends Polyline
{

    private ArrayList<Position> positions;
    private Vec4 referenceCenterPoint;
    private Position referenceCenterPosition = Position.ZERO;
    private Extent extent;
    private double extentVerticalExaggeration = 1;
    private double verticalExaggeration = 1.0;
    private boolean followVerticalExaggeration = false;
    private ArrayList<ArrayList<Vec4>> currentSpans;
    private Globe globe;

    private boolean shadedColors = false;
    private double maxElevation = 2.0e4;
    private double minElevation = 0.0;
    

    private void reset()
    {
        if (this.currentSpans != null)
            this.currentSpans.clear();
        this.currentSpans = null;
    }

    public boolean isFollowVerticalExaggeration(){
    	return followVerticalExaggeration;
    }
    
    
    /**
     * Indicates whether the path should follow the vertical exaggeration.
     * @param followVerticalExaggeration <code>true</code> to follow the vertical exaggeration, otherwise <code>false</code>.
     */
    public void setFollowVerticalExaggeration(boolean followVerticalExaggeration)
    {
        this.reset();
        this.followVerticalExaggeration = followVerticalExaggeration;
    }

    /**
     * Returns the length of the line as drawn. If the path follows the terrain, the length returned is the distance one
     * would travel if on the surface. If the path does not follow the terrain, the length returned is the distance
     * along the full length of the path at the path's elevations and current path type.
     *
     * @return the path's length in meters.
     */
    public double getLength()
    {
        return this.globe != null ? this.getMeasurer().getLength(this.globe) : 0;
    }


    /**
     * Specifies the path's positions.
     *
     * @param inPositions the path positions.
     */
    public void setPositions(Iterable<? extends Position> inPositions)
    {
        this.reset();
        this.positions = new ArrayList<Position>();
        this.extent = null;
        if (inPositions != null)
        {
            for (Position position : inPositions)
            {
                this.positions.add(position);
            }
            this.getMeasurer().setPositions(this.positions);
        }

        if ((this.isFilled() && this.positions.size() < 3))
        {
            String msg = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Sets the paths positions as latitude and longitude values at a constant altitude.
     *
     * @param inPositions the latitudes and longitudes of the positions.
     * @param elevation   the elevation to assign each position.
     */
    public void setPositions(Iterable<? extends LatLon> inPositions, double elevation)
    {
        this.reset();
        this.positions = new ArrayList<Position>();
        this.extent = null;
        if (inPositions != null)
        {
            for (LatLon position : inPositions)
            {
                this.positions.add(new Position(position, elevation));
            }
            this.getMeasurer().setPositions(this.positions);
        }

        if (this.isFilled() && this.positions.size() < 3)
        {
            String msg = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    public Iterable<Position> getPositions()
    {
        return this.positions;
    }

    protected Extent getExtent(DrawContext dc)
    {
        double ve = dc.getVerticalExaggeration();
        if (this.extent == null || this.isFollowTerrain() && ve != this.extentVerticalExaggeration)
        {
            Sector sector = Sector.boundingSector(this.getPositions());
            double[] minAndMaxElevations;
            if (this.isFollowTerrain())
            {
                minAndMaxElevations = dc.getGlobe().getMinAndMaxElevations(sector);
            }
            else
            {
                minAndMaxElevations = computeElevationExtremes(this.getPositions());
            }
            minAndMaxElevations[0] += this.getOffset();
            minAndMaxElevations[1] += this.getOffset();
            this.extent = dc.getGlobe().computeBoundingCylinder(ve, sector, minAndMaxElevations[0],
                minAndMaxElevations[1]);
            this.extentVerticalExaggeration = ve;
        }
        
        return this.extent;
    }

    protected static double[] computeElevationExtremes(Iterable<? extends Position> positions)
    {
        double[] extremes = new double[] {Double.MAX_VALUE, -Double.MAX_VALUE};
        for (Position pos : positions)
        {
            if (extremes[0] > pos.getElevation())
                extremes[0] = pos.getElevation(); // min
            if (extremes[1] < pos.getElevation())
                extremes[1] = pos.getElevation(); // max
        }
        
        return extremes;
    }

    private long geomGenFrameTime = -Long.MAX_VALUE;

    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.globe = dc.getGlobe();

        if (this.positions.size() < 2)
            return;

        // vertices potentially computed every frame to follow terrain changes
        if (this.currentSpans == null || (this.isFollowTerrain() && this.geomGenFrameTime != dc.getFrameTimeStamp())|| (this.followVerticalExaggeration && dc.getVerticalExaggeration() != verticalExaggeration))
        {
        	this.verticalExaggeration = dc.getVerticalExaggeration();
            // Reference center must be computed prior to computing vertices.
            this.computeReferenceCenter(dc);
            this.makeVertices(dc);
            this.geomGenFrameTime = dc.getFrameTimeStamp();
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
            if (this.isFilled())
                hintAttr = GL.GL_POLYGON_SMOOTH_HINT;
            gl.glHint(hintAttr, this.getAntiAliasHint());

            int primType = GL.GL_LINE_STRIP;
            if (this.isFilled())
                primType = GL.GL_POLYGON;

            if (dc.isPickingMode())
                gl.glLineWidth((float) this.getLineWidth() + 8);
            else
                gl.glLineWidth((float) this.getLineWidth());


            if (this.isFollowTerrain())
                this.pushOffest(dc);

            for (int i=0;i< this.currentSpans.size();i++)
            {
            	ArrayList<Vec4> span = this.currentSpans.get(i);
                if (span == null)
                    continue;

                // Since segements can very often be very short -- two vertices -- use explicit rendering. The
                // overhead of batched rendering, e.g., gl.glDrawArrays, is too high because it requires copying
                // the vertices into a DoubleBuffer, and DoubleBuffer creation and access performs relatively poorly.
                gl.glBegin(primType);
                for (Vec4 p : span)
                {
                	if(isShadedColors()){
                	Double k = this.positions.get(i).elevation;
                	dc.getGL().glColor4ub((byte) (255*((k-this.getMinElevation())/(this.getMaxElevation()-this.getMinElevation()))),
                			(byte) (255*((this.getMaxElevation()-k)/(this.getMaxElevation()-this.getMinElevation()))),
                			(byte) 0,
                			(byte) this.getColor().getAlpha());
                	}
                    gl.glVertex3d(p.x, p.y, p.z);
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

            if (this.isFollowTerrain())
                this.popOffest(dc);
        }
        finally
        {
            gl.glPopAttrib();
            dc.getView().popReferenceCenter(dc);
        }
    }

    private void pushOffest(DrawContext dc)
    {
        // Modify the projection transform to shift the depth values slightly toward the camera in order to
        // ensure the lines are selected during depth buffering.
        GL gl = dc.getGL();

        float[] pm = new float[16];
        gl.glGetFloatv(GL.GL_PROJECTION_MATRIX, pm, 0);
        pm[10] *= 0.99; // TODO: See Lengyel 2 ed. Section 9.1.2 to compute optimal/minimal offset

        gl.glPushAttrib(GL.GL_TRANSFORM_BIT);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadMatrixf(pm, 0);
    }

    private void popOffest(DrawContext dc)
    {
        GL gl = dc.getGL();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glPopAttrib();
    }

    protected void makeVertices(DrawContext dc)
    {
        if (this.currentSpans == null)
            this.currentSpans = new ArrayList<ArrayList<Vec4>>();
        else
            this.currentSpans.clear();

        if (this.positions.size() < 1)
            return;

        Position posA = this.positions.get(0);
        Vec4 ptA = this.computePoint(dc, posA, true);
        for (int i = 1; i <= this.positions.size(); i++)
        {
            Position posB;
            if (i < this.positions.size())
                posB = this.positions.get(i);
            else if (this.isClosed())
                posB = this.positions.get(0);
            else
                break;

            Vec4 ptB = this.computePoint(dc, posB, true);

            if (this.isFollowTerrain() && !this.isSegmentVisible(dc, posA, posB, ptA, ptB))
            {
                posA = posB;
                ptA = ptB;
                continue;
            }

            ArrayList<Vec4> span;
            span = this.makeSegment(dc, posA, posB, ptA, ptB);

            if (span != null)
                this.addSpan(span);

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
            double height = (pos.getElevation() + (applyOffset ? this.getOffset() : 0))*verticalExaggeration;
            return dc.getGlobe().computePointFromPosition(pos.getLatitude(), pos.getLongitude(), height);
        }
    }

    private double computeSegmentLength(DrawContext dc, Position posA, Position posB)
    {
        LatLon llA = new LatLon(posA.getLatitude(), posA.getLongitude());
        LatLon llB = new LatLon(posB.getLatitude(), posB.getLongitude());

        Angle ang = LatLon.greatCircleDistance(llA, llB);

        if (this.isFollowTerrain())
        {
            return ang.radians * (dc.getGlobe().getRadius() + this.getOffset());
        }
        else
        {
            double height = this.getOffset() + 0.5 * (posA.getElevation() + posB.getElevation());
            return ang.radians * (dc.getGlobe().getRadius() + height);
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

    @SuppressWarnings({"UnusedDeclaration"})
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
        if (this.positions.size() < 1)
            return;

        if (this.positions.size() < 3)
            this.referenceCenterPosition = this.positions.get(0);
        else
            this.referenceCenterPosition = this.positions.get(this.positions.size() / 2);

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

    public void move(Position delta)
    {
        if (delta == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.moveTo(this.getReferencePosition().add(delta));
    }

    public void moveTo(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.reset();

        if (this.positions.size() < 1)
            return;

        Vec4 origRef = this.referenceCenterPoint;
        Vec4 newRef = this.globe.computePointFromPosition(position);
        Angle distance =
            LatLon.greatCircleDistance(this.referenceCenterPosition, position);
        Vec4 axis = origRef.cross3(newRef).normalize3();
        Quaternion q = Quaternion.fromAxisAngle(distance, axis);

        for (int i = 0; i < this.positions.size(); i++)
        {
            Position pos = this.positions.get(i);
            Vec4 p = this.globe.computePointFromPosition(pos);
            p = p.transformBy3(q);
            pos = this.globe.computePositionFromPoint(p);
            this.positions.set(i, pos);
        }
    }

    /**
     * Returns an XML state document String describing the public attributes of this Polyline.
     *
     * @return XML state document string describing this Polyline.
     */
    public String getRestorableState()
    {
        RestorableSupport restorableSupport = RestorableSupport.newRestorableSupport();
        // Creating a new RestorableSupport failed. RestorableSupport logged the problem, so just return null.
        if (restorableSupport == null)
            return null;

        if (this.getColor() != null)
        {
            String encodedColor = RestorableSupport.encodeColor(this.getColor());
            if (encodedColor != null)
                restorableSupport.addStateValueAsString("color", encodedColor);
        }

        if (this.getHighlightColor() != null)
        {
            String encodedColor = RestorableSupport.encodeColor(this.getHighlightColor());
            if (encodedColor != null)
                restorableSupport.addStateValueAsString("highlightColor", encodedColor);
        }

        if (this.positions != null)
        {
            // Create the base "positions" state object.
            RestorableSupport.StateObject positionsStateObj = restorableSupport.addStateObject("positions");
            if (positionsStateObj != null)
            {
                for (Position p : this.positions)
                {
                    // Save each position only if all parts (latitude, longitude, and elevation) can be
                    // saved. We will not save a partial iconPosition (for example, just the elevation).
                    if (p != null && p.getLatitude() != null && p.getLongitude() != null)
                    {
                        // Create a nested "position" element underneath the base "positions".
                        RestorableSupport.StateObject pStateObj =
                            restorableSupport.addStateObject(positionsStateObj, "position");
                        if (pStateObj != null)
                        {
                            restorableSupport.addStateValueAsDouble(pStateObj, "latitudeDegrees",
                                p.getLatitude().degrees);
                            restorableSupport.addStateValueAsDouble(pStateObj, "longitudeDegrees",
                                p.getLongitude().degrees);
                            restorableSupport.addStateValueAsDouble(pStateObj, "elevation",
                                p.getElevation());
                        }
                    }
                }
            }
        }

        restorableSupport.addStateValueAsInteger("antiAliasHint", this.getAntiAliasHint());
        restorableSupport.addStateValueAsBoolean("filled", this.isFilled());
        restorableSupport.addStateValueAsBoolean("closed", this.isClosed());
        restorableSupport.addStateValueAsBoolean("highlighted", this.isHighlighted());
        restorableSupport.addStateValueAsInteger("pathType", this.getPathType());
        restorableSupport.addStateValueAsBoolean("followTerrain", this.isFollowTerrain());
        restorableSupport.addStateValueAsDouble("offset", this.getOffset());
        restorableSupport.addStateValueAsDouble("terrainConformance", this.getTerrainConformance());
        restorableSupport.addStateValueAsDouble("lineWidth", this.getLineWidth());
        restorableSupport.addStateValueAsInteger("stipplePattern", this.getStipplePattern());
        restorableSupport.addStateValueAsInteger("stippleFactor", this.getStippleFactor());
        restorableSupport.addStateValueAsInteger("numSubsegments", this.getNumSubsegments());

        return restorableSupport.getStateAsXml();
    }

    /**
     * Restores publicly settable attribute values found in the specified XML state document String. The
     * document specified by <code>stateInXml</code> must be a well formed XML document String, or this will throw an
     * IllegalArgumentException. Unknown structures in <code>stateInXml</code> are benign, because they will
     * simply be ignored.
     *
     * @param stateInXml an XML document String describing a Polyline.
     * @throws IllegalArgumentException If <code>stateInXml</code> is null, or if <code>stateInXml</code> is not
     *                                  a well formed XML document String.
     */
    public void restoreState(String stateInXml)
    {
        if (stateInXml == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport restorableSupport;
        try
        {
            restorableSupport = RestorableSupport.parse(stateInXml);
        }
        catch (Exception e)
        {
            // Parsing the document specified by stateInXml failed.
            String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", stateInXml);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message, e);
        }

        String colorState = restorableSupport.getStateValueAsString("color");
        if (colorState != null)
        {
            Color color = RestorableSupport.decodeColor(colorState);
            if (color != null)
                setColor(color);
        }

        colorState = restorableSupport.getStateValueAsString("highlightColor");
        if (colorState != null)
        {
            Color color = RestorableSupport.decodeColor(colorState);
            if (color != null)
                setHighlightColor(color);
        }

        // Get the base "positions" state object.
        RestorableSupport.StateObject positionsStateObj = restorableSupport.getStateObject("positions");
        if (positionsStateObj != null)
        {
            ArrayList<Position> newPositions = new ArrayList<Position>();
            // Get the nested "position" states beneath the base "positions".
            RestorableSupport.StateObject[] positionStateArray =
                restorableSupport.getAllStateObjects(positionsStateObj, "position");
            if (positionStateArray != null && positionStateArray.length != 0)
            {
                for (RestorableSupport.StateObject pStateObj : positionStateArray)
                {
                    if (pStateObj != null)
                    {
                        // Restore each position only if all parts are available.
                        // We will not restore a partial position (for example, just the elevation).
                        Double latitudeState = restorableSupport.getStateValueAsDouble(pStateObj, "latitudeDegrees");
                        Double longitudeState = restorableSupport.getStateValueAsDouble(pStateObj, "longitudeDegrees");
                        Double elevationState = restorableSupport.getStateValueAsDouble(pStateObj, "elevation");
                        if (latitudeState != null && longitudeState != null && elevationState != null)
                            newPositions.add(Position.fromDegrees(latitudeState, longitudeState, elevationState));
                    }
                }
            }

            // Even if there are no actual positions specified, we set positions as an empty list.
            // An empty set of positions is still a valid state.
            setPositions(newPositions);
        }

        Integer antiAliasHintState = restorableSupport.getStateValueAsInteger("antiAliasHint");
        if (antiAliasHintState != null)
            setAntiAliasHint(antiAliasHintState);

        Boolean isFilledState = restorableSupport.getStateValueAsBoolean("filled");
        if (isFilledState != null)
            setFilled(isFilledState);

        Boolean isClosedState = restorableSupport.getStateValueAsBoolean("closed");
        if (isClosedState != null)
            setClosed(isClosedState);

        Boolean isHighlightedState = restorableSupport.getStateValueAsBoolean("highlighted");
        if (isHighlightedState != null)
            setHighlighted(isHighlightedState);

        Integer pathTypeState = restorableSupport.getStateValueAsInteger("pathType");
        if (pathTypeState != null)
            setPathType(pathTypeState);

        Boolean isFollowTerrainState = restorableSupport.getStateValueAsBoolean("followTerrain");
        if (isFollowTerrainState != null)
            setFollowTerrain(isFollowTerrainState);

        Double offsetState = restorableSupport.getStateValueAsDouble("offset");
        if (offsetState != null)
            setOffset(offsetState);

        Double terrainConformanceState = restorableSupport.getStateValueAsDouble("terrainConformance");
        if (terrainConformanceState != null)
            setTerrainConformance(terrainConformanceState);

        Double lineWidthState = restorableSupport.getStateValueAsDouble("lineWidth");
        if (lineWidthState != null)
            setLineWidth(lineWidthState);

        Integer stipplePatternState = restorableSupport.getStateValueAsInteger("stipplePattern");
        if (stipplePatternState != null)
            setStipplePattern(stipplePatternState.shortValue());

        Integer stippleFactorState = restorableSupport.getStateValueAsInteger("stippleFactor");
        if (stippleFactorState != null)
            setStippleFactor(stippleFactorState);

        Integer numSubsegmentsState = restorableSupport.getStateValueAsInteger("numSubsegments");
        if (numSubsegmentsState != null)
            setNumSubsegments(numSubsegmentsState);
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
