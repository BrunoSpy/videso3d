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

package fr.crnan.videso3d.util.measure;

import java.awt.Point;
import java.util.Iterator;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.measure.MeasureTool;
/**
 * Ajout des distances en NM ainsi que de l'orientation dans l'outil de mesure<br />
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class VidesoMeasureTool extends MeasureTool {

	public VidesoMeasureTool(WorldWindow arg0) {
		super(arg0);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.worldwind.util.measure.MeasureTool#updateAnnotation(gov.nasa.worldwind.geom.Position)
	 */
	@Override
	public void updateAnnotation(Position pos) {
		if (pos != null) {
			String text = "";
			if (this.measureShape.equals(SHAPE_CIRCLE) && this.shapeRectangle != null)
				text += "Radius " + formatLength(this.shapeRectangle.width / 2);
			else if (this.measureShape.equals(SHAPE_SQUARE) && this.shapeRectangle != null)
				text += "Size " + formatLength(this.shapeRectangle.width);
			else if (this.measureShape.equals(SHAPE_QUAD) && this.shapeRectangle != null)
				text += "Width " + formatLength(this.shapeRectangle.width) + "\n"
				+ "Height " + formatLength(this.shapeRectangle.height);
			else if (this.measureShape.equals(SHAPE_ELLIPSE) && this.shapeRectangle != null)
				text += "Major " + formatLength(this.shapeRectangle.width) + "\n"
				+ "Minor " + formatLength(this.shapeRectangle.height);
			else if (this.measureShape.equals(SHAPE_LINE) || this.measureShape.equals(SHAPE_PATH))
			{
				text += "Length " + formatLength(getLength()) + "\n";
				//Added by Bruno Spyckerelle
				text += "Heading " + formatHeading(getHeading()) + "\n";
				text += String.format("Lat %7.4f\u00B0 Lon %7.4f\u00B0",
						pos.getLatitude().degrees, pos.getLongitude().degrees);
			}
			else if (this.measureShape.equals(SHAPE_POLYGON))
			{
				text += "Perimeter " + formatLength(getLength()) + "\n";
				text += "Area " + formatArea(getArea()) + "\n";
				text += String.format("Lat %7.4f\u00B0 Lon %7.4f\u00B0",
						pos.getLatitude().degrees, pos.getLongitude().degrees);
			}
			this.annotation.setText(text);
			// set help message screen position - follow position
			Vec4 surfacePoint = this.wwd.getSceneController().getTerrain().getSurfacePoint(
					pos.getLatitude(), pos.getLongitude());
			if (surfacePoint == null)
			{
				Globe globe = this.wwd.getModel().getGlobe();
				surfacePoint = globe.computePointFromPosition(pos.getLatitude(), pos.getLongitude(),
						globe.getElevation(pos.getLatitude(), pos.getLongitude()));
			}
			Vec4 screenPoint = this.wwd.getView().project(surfacePoint);
			if (screenPoint != null)
				this.annotation.setScreenPoint(new Point((int)screenPoint.x,  (int)screenPoint.y));
			this.annotation.getAttributes().setVisible(true);
		}
		else
		{
			this.annotation.getAttributes().setVisible(false);
		}
	}


	/**
	 * @return {@link Angle} L'azimuth entre le premier et le dernier point de la polyligne
	 */
	private Angle getHeading(){
		LatLon start = LatLon.ZERO;
		LatLon end = LatLon.ZERO;
		if (this.line != null) {
			Iterator<Position> iterator = this.line.getPositions().iterator();
			start = iterator.next();
			while(iterator.hasNext()){
				end = iterator.next();
			}
		}
		return LatLon.rhumbAzimuth(start, end);
	}
	
	protected String formatHeading(Angle value){
		return String.format("%, 7.2f °", value.degrees);
	}
	
	/* (non-Javadoc)
	 * @see gov.nasa.worldwind.util.measure.MeasureTool#formatLength(double)
	 */
	@Override
	protected String formatLength(double value)
    {
        String s;
        if (value <= 0)
            s = "na";
        else //if(value < 1e3)
            s = String.format("%,7.2f NM", value / 1852);
//        else
//            s = String.format("%,7.3f km", value / 1e3);
        return s;
    }

}
