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
import java.util.ArrayList;
import java.util.Iterator;

import com.jogamp.common.nio.Buffers;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Path;
/**
 * Filters segments of the Path that are that fully inside maxAltitude and minAltitude<br/>
 * If the path is splitted in mulptiple visible segments, only the first one will be displayed
 * @author Bruno Spyckerelle
 * @version 0.1.3
 *
 */
public class AltitudeFilterablePath extends Path {

	private double maxAltitude = 800.0*30.48;
	private double minAltitude = 0.0;
	
	private ArrayList<Position> usefullPositions;
	
	public AltitudeFilterablePath(){
		super();
		this.setEnableBatchPicking(false);
	}
	
	public void setMaximumViewableAltitude(double altitude) {
		this.maxAltitude = altitude;
	}

	public void setMinimumViewableAltitude(double altitude) {
		this.minAltitude = altitude;
	}

	/* (non-Javadoc)
	 * @see gov.nasa.worldwind.render.Path#makeTessellatedPositions(gov.nasa.worldwind.render.DrawContext, gov.nasa.worldwind.render.Path.PathData)
	 */
	@Override
	protected void makeTessellatedPositions(DrawContext dc, PathData pathData) {

		//compute usefull positions
		usefullPositions = this.computeUsefullPositions(this.positions);
		
		if (usefullPositions.size() < 2)
            return;

        if (pathData.getTessellatedPositions() == null|| pathData.getTessellatedPositions().size() < this.usefullPositions.size())
        {
            int size = (this.numSubsegments * (usefullPositions.size() - 1) + 1) * (this.isExtrude() ? 2 : 1);
            pathData.setTessellatedPositions(new ArrayList<Position>(size));
            pathData.setTessellatedColors((this.positionColors != null) ? new ArrayList<Color>(size) : null);
        }
        else
        {
            pathData.getTessellatedPositions().clear();
           
            if (pathData.getTessellatedColors() != null)
                pathData.getTessellatedColors().clear();
            
        }

        if (pathData.getPolePositions() == null || pathData.getPolePositions().capacity() < usefullPositions.size() * 2)
            pathData.setPolePositions(Buffers.newDirectIntBuffer(usefullPositions.size() * 2));
        else
            pathData.getPolePositions().clear();     
        
        if (pathData.getPositionPoints() == null || pathData.getPositionPoints().capacity() < this.usefullPositions.size())
            pathData.setPositionPoints(Buffers.newDirectIntBuffer(this.usefullPositions.size()));
        else
            pathData.getPositionPoints().clear();

        this.makePositions(dc, pathData);

        ((ArrayList<Position>) pathData.getTessellatedPositions()).trimToSize();
        pathData.getPolePositions().flip();
        pathData.getPositionPoints().flip();

        if (pathData.getTessellatedColors() != null)
            ((ArrayList<Color>) pathData.getTessellatedColors()).trimToSize();
	}

    protected void makePositions(DrawContext dc, PathData pathData)
    {
        Iterator<? extends Position> iter = this.usefullPositions.iterator();
        Position posA = iter.next();
        int ordinalA = 0;
        Color colorA = this.getColor(posA, ordinalA);

        this.addTessellatedPosition(posA, colorA, ordinalA, pathData); // add the first position of the path

        // Tessellate each segment of the path.
        Vec4 ptA = this.computePoint(dc.getTerrain(), posA);

        while (iter.hasNext())
        {
            Position posB = iter.next();
            int ordinalB = ordinalA + 1;
            Color colorB = this.getColor(posB, ordinalB);
            Vec4 ptB = this.computePoint(dc.getTerrain(), posB);

            if (iter.hasNext()) // if this is not the final position
            {
                // If the segment is very small or not visible, don't tessellate, just add the segment's end position.
                if (this.isSmall(dc, ptA, ptB, 8) || !this.isSegmentVisible(dc, posA, posB, ptA, ptB))
                    this.addTessellatedPosition(posB, colorB, ordinalB, pathData);
                else
                    this.makeSegment(dc, posA, posB, ptA, ptB, colorA, colorB, ordinalA, ordinalB, pathData);
            }
            else
            {
                // Add the final point.
                this.addTessellatedPosition(posB, colorB, ordinalB, pathData);
            }

            posA = posB;
            ptA = ptB;
            ordinalA = ordinalB;
            colorA = colorB;
        }
    }
	
	private ArrayList<Position> computeUsefullPositions(
			Iterable<? extends Position> positions) {
		ArrayList<Position> newPositions = new ArrayList<Position>();
		for(Position p : this.positions){
			if(isInside(p)){
				newPositions.add(p);
			} else {
				if(!newPositions.isEmpty()) 
					//only take into account the first part of the path that is inside min and max altitudes
					break;
			}
		}
		return newPositions;
	}

	private boolean isInside(Position pos){
		return pos.getAltitude() <= maxAltitude && pos.getAltitude() >= minAltitude;
	}

	/* (non-Javadoc)
	 * @see gov.nasa.worldwind.render.AbstractShape#setHighlighted(boolean)
	 */
	@Override
	public void setHighlighted(boolean highlighted) {
		boolean old = isHighlighted();
		super.setHighlighted(highlighted);
		firePropertyChange("HIGHLIGHT", old, highlighted);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.worldwind.render.AbstractShape#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		boolean old = isVisible();
		super.setVisible(visible);
		firePropertyChange("VISIBLE", old, visible);
	}

	

}
