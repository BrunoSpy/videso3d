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

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import fr.crnan.videso3d.geom.LatLonCautra;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.StatusBar;
/**
 * Ajoute les coordonnées Cautra à la barre de status
 * @author Bruno Spyckerelle
 * @version 0.1
 */
@SuppressWarnings("serial")
public class VidesoStatusBar extends StatusBar {

	protected final JLabel xCautraDisplay = new JLabel("");
	protected final JLabel yCautraDisplay = new JLabel("");
	
	protected final JLabel fpsDisplay = new JLabel("");
	
	public VidesoStatusBar(){
		super();
		
		xCautraDisplay.setHorizontalAlignment(SwingConstants.CENTER);
		yCautraDisplay.setHorizontalAlignment(SwingConstants.CENTER);
		fpsDisplay.setHorizontalAlignment(SwingConstants.CENTER);
		
		this.add(xCautraDisplay, 1);
		this.add(yCautraDisplay, 2);
		
		this.add(fpsDisplay, 6);
	}
	
	@Override
	public void moved(PositionEvent event)
    {
        this.handleCursorPositionChange(event);
    }
	
	protected void handleCursorPositionChange(PositionEvent event)
    {
        Position newPos = event.getPosition();
        if (newPos != null)
        {
            String las = makeAngleDescription("Lat", newPos.getLatitude());
            String los = makeAngleDescription("Lon", newPos.getLongitude());
            String els = makeCursorElevationDescription(
                this.getEventSource().getModel().getGlobe().getElevation(newPos.getLatitude(), newPos.getLongitude()));
            double[] cautra = LatLonCautra.fromRadians(newPos.getLatitude().radians, newPos.getLongitude().radians).getCautra();
            String xCautra = makeCautraDescription("X Cautra", cautra[0]);
            String yCautra = makeCautraDescription("Y Cautra", cautra[1]);
            latDisplay.setText(las);
            lonDisplay.setText(los);
            eleDisplay.setText(els);
            xCautraDisplay.setText(xCautra);
            yCautraDisplay.setText(yCautra);
        }
        else
        {
            latDisplay.setText("");
            lonDisplay.setText(Logging.getMessage("term.OffGlobe"));
            eleDisplay.setText("");
            xCautraDisplay.setText("");
            yCautraDisplay.setText("");
        }
        
        fpsDisplay.setText(String.format("FPS %3.0f",this.getEventSource().getSceneController().getFramesPerSecond()));
        
    }
	
	protected String makeCautraDescription(String label, Double cautra)
    {
        return     String.format("%s %7.2f", label, cautra);    
    }
}
