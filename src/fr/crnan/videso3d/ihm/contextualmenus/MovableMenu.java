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

package fr.crnan.videso3d.ihm.contextualmenus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.ihm.components.MovePositionDialog;
import gov.nasa.worldwind.Movable;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0 
 */
public class MovableMenu extends JMenu {

	public MovableMenu(final Movable object, final VidesoGLCanvas wwd, final MouseEvent mouseEvent) {
		JMenuItem changePos = new JMenuItem("Modifier les coordonnées");
		changePos.addActionListener(new ActionListener() {
			
				
			@Override
			public void actionPerformed(ActionEvent e) {
				MovePositionDialog dialog = new MovePositionDialog(object.getReferencePosition());
				if(dialog.showDialog(mouseEvent)== JOptionPane.OK_OPTION){
					object.moveTo(dialog.getPosition());
					wwd.redraw();
				}
			}
		});
		this.add(changePos);
	}
	
}
