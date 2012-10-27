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

package fr.crnan.videso3d.util;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.UnitsFormat;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.util.measure.MeasureToolController;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class VMeasureTool extends MeasureTool {

	
	public VMeasureTool(WorldWindow wwd) {
		super(wwd);
		this.setController(new MeasureToolController());
		this.setMeasureShapeType(MeasureTool.SHAPE_LINE);
		this.setFollowTerrain(true);
		this.setShowAnnotation(true);
		this.setUnitsFormat(new UnitsFormat(UnitsFormat.NAUTICAL_MILES, UnitsFormat.SQUARE_KILOMETERS, true));
	
	}

	@Override
    protected String formatLineMeasurements(Position pos)
    {
        // TODO: Compute the heading of individual path segments
        StringBuilder sb = new StringBuilder();

        sb.append(this.unitsFormat.lengthNL(this.getLabel(LENGTH_LABEL), this.getLength()));

        Double accumLength = this.computeAccumulatedLength(pos);
        if (accumLength != null && accumLength >= 1 && !lengthsEssentiallyEqual(this.getLength(), accumLength))
            sb.append(this.unitsFormat.lengthNL(this.getLabel(ACCUMULATED_LABEL), accumLength));

        if (this.getOrientation() != null)
            sb.append(this.unitsFormat.angleNL(this.getLabel(HEADING_LABEL), computeNormalizedHeading(this.getOrientation())));

        sb.append(this.unitsFormat.angleNL(this.getLabel(LATITUDE_LABEL), pos.getLatitude()));
        sb.append(this.unitsFormat.angleNL(this.getLabel(LONGITUDE_LABEL), pos.getLongitude()));

        return sb.toString();
    }
	
}
