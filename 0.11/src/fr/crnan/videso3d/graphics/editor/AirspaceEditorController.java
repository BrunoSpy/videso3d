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

import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;

import fr.crnan.videso3d.ihm.components.MovePositionDialog;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.airspaces.editor.AirspaceControlPoint;

/**
 * Allows fine editing of control points
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class AirspaceEditorController extends gov.nasa.worldwind.render.airspaces.editor.AirspaceEditorController {

	public AirspaceEditorController(WorldWindow wwd)  {
		super(wwd);
	}

	public AirspaceEditorController() {
		super();
	}
	
	@Override
    public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
		if(e.getButton() == MouseEvent.BUTTON3){
			AirspaceControlPoint controlPoint = this.getTopOwnedControlPointAtCurrentPosition();
			if(controlPoint != null){
				Position oldPos = this.getWorldWindow().getModel().getGlobe().computePositionFromPoint(controlPoint.getPoint());
				MovePositionDialog dialog = new MovePositionDialog(this.getWorldWindow().getModel().getGlobe().computePositionFromPoint(controlPoint.getPoint()));
				if(dialog.showDialog(e) == JOptionPane.OK_OPTION){
					Position newPosition = dialog.getPosition();
					if(newPosition.getAltitude() != oldPos.getAltitude()){
						((PolygonEditor)this.getEditor()).doResizeAtControlPoint(this.getWorldWindow(), controlPoint, newPosition, oldPos);
					}
					((PolygonEditor)this.getEditor()).doMoveControlPoint(this.getWorldWindow(), controlPoint, newPosition, oldPos);
				} 
			}
        }
    }
}
