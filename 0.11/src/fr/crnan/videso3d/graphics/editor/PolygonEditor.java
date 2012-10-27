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

package fr.crnan.videso3d.graphics.editor;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.airspaces.editor.AirspaceControlPoint;
import gov.nasa.worldwind.render.airspaces.editor.AirspaceEditEvent;
import gov.nasa.worldwind.render.airspaces.editor.AirspaceEditorUtil;

/**
 * Adds the ability to move a control point to a new {@link Position}
 * @author Bruno Spyckerelle
 * @version 0.1
 * @see gov.nasa.worldwind.render.airspaces.editor.PolygonEditor
 */
public class PolygonEditor extends
gov.nasa.worldwind.render.airspaces.editor.PolygonEditor {

	protected void doMoveControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint,
			Position newPosition, Position oldPosition)  {

		LatLon change = newPosition.subtract(oldPosition);

		int index = controlPoint.getLocationIndex();
		List<LatLon> newLocationList = new ArrayList<LatLon>(this.getPolygon().getLocations());
		LatLon newLatLon = newLocationList.get(index).add(change);
		newLocationList.set(index, newLatLon);
		this.getPolygon().setLocations(newLocationList);

		this.fireControlPointChanged(new AirspaceEditEvent(wwd, controlPoint.getAirspace(), this, controlPoint));
	}

	protected void doResizeAtControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint,
			Position newPosition, Position oldPosition)   {

		double elevationChange = newPosition.getElevation() - oldPosition.getElevation();

		int index;
		if (this.getPolygon().isAirspaceCollapsed())
		{
			index = (elevationChange < 0) ? LOWER_ALTITUDE : UPPER_ALTITUDE;
		}
		else
		{
			index = controlPoint.getAltitudeIndex();
		}

		double[] altitudes = controlPoint.getAirspace().getAltitudes();
		boolean[] terrainConformance = controlPoint.getAirspace().isTerrainConforming();

		if (this.isKeepControlPointsAboveTerrain())
		{
			if (terrainConformance[index])
			{
				if (altitudes[index] + elevationChange < 0.0)
					elevationChange = -altitudes[index];
			}
			else
			{
				double height = AirspaceEditorUtil.computeLowestHeightAboveSurface(
						wwd, this.getCurrentControlPoints(), index);
				if (elevationChange <= -height)
					elevationChange = -height;
			}
		}

		double d = AirspaceEditorUtil.computeMinimumDistanceBetweenAltitudes(this.getPolygon().getLocations().size(),
				this.getCurrentControlPoints());
		if (index == LOWER_ALTITUDE)
		{
			if (elevationChange > d)
				elevationChange = d;
		}
		else if (index == UPPER_ALTITUDE)
		{
			if (elevationChange < -d)
				elevationChange = -d;
		}

		altitudes[index] += elevationChange;
		controlPoint.getAirspace().setAltitudes(altitudes[LOWER_ALTITUDE], altitudes[UPPER_ALTITUDE]);

		AirspaceEditEvent editEvent = new AirspaceEditEvent(wwd, controlPoint.getAirspace(), this, controlPoint);
		this.fireControlPointChanged(editEvent);
		this.fireAirspaceResized(editEvent);
	}

}
