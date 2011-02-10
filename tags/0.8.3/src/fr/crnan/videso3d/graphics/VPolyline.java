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
 * <b>Attention : </b>Préférer l'usage de Path, sauf si besoin d'un dégradé de couleur.
 * @author Bruno Spyckerelle
 * @version 0.4
 */
public class VPolyline extends Polyline
{
    
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
