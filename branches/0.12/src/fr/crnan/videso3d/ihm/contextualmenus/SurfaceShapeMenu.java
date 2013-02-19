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

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.ihm.ShapeAttributesDialog;
import gov.nasa.worldwind.render.SurfaceShape;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class SurfaceShapeMenu extends JMenu {

	public SurfaceShapeMenu(final SurfaceShape shape, final VidesoGLCanvas wwd){
		
		JMenuItem colorItem = new JMenuItem("Propriétés graphiques...");
		colorItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				new ShapeAttributesDialog(shape.getAttributes(), 
						shape.getHighlightAttributes()).setVisible(true);
				wwd.redraw();
			}
		});
		
		this.add(colorItem);
	}
	
}
