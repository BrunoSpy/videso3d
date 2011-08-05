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

import java.util.ArrayList;
import java.util.Iterator;

import com.sun.opengl.util.BufferUtil;


import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Path;
/**
 * Filters segments of the Path that are that fully inside maxAltitude and minAltitude<br/>
 * If the path is splitted in mulptiple visible segments, only the first one will be displayed
 * @author Bruno Spyckerelle
 * @version 0.1.0
 *
 */
public class AltitudeFilterablePath extends Path {

	private double maxAltitude = 800.0*30.48;
	private double minAltitude = 0.0;
	
	
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
		ArrayList<Position> usefullPositions = this.computeUsefullPositions(this.positions);
		
		if (usefullPositions.size() < 2)
            return;

        if (pathData.getTessellatedPositions() == null)
        {
            int size = (this.numSubsegments * (usefullPositions.size() - 1) + 1) * (this.isExtrude() ? 2 : 1);
            pathData.setTessellatedPositions(new ArrayList<Position>(size));
        }
        else
        {
            pathData.getTessellatedPositions().clear();
        }

        if (pathData.getPolePositions() == null || pathData.getPolePositions().capacity() < usefullPositions.size() * 2)
            pathData.setPolePositions(BufferUtil.newIntBuffer(usefullPositions.size() * 2));
        else
            pathData.getPolePositions().clear();     
        
        Iterator<? extends Position> iter = usefullPositions.iterator();
        Position posA = iter.next();
        this.addTessellatedPosition(posA, true, pathData); // add the first position of the path

        // Tessellate each segment of the path.
        Vec4 ptA = this.computePoint(dc.getTerrain(), posA);

        for (int i = 1; i <= usefullPositions.size(); i++)
        {
            Position posB;
            if (i < usefullPositions.size())
                posB = iter.next();
            else
                break;

            Vec4 ptB = this.computePoint(dc.getTerrain(), posB);

            // If the segment is very small or not visible, don't tessellate it, just add the segment's end position.
            if (this.isSmall(dc, ptA, ptB, 8) || !this.isSegmentVisible(dc, posA, posB, ptA, ptB))
                this.addTessellatedPosition(posB, true, pathData);
            else
                this.makeSegment(dc, posA, posB, ptA, ptB, pathData);

            posA = posB;
            ptA = ptB;
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


}
