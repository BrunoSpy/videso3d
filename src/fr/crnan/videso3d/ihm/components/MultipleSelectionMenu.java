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
package fr.crnan.videso3d.ihm.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import fr.crnan.videso3d.VidesoGLCanvas;

/**
 * Menu for right click on selection<br />
 * Copy is done only for Stip objects
 * @author Bruno Spyckerelle
 * @version 0.0.1
 *
 */
public class MultipleSelectionMenu extends JPopupMenu{

	public MultipleSelectionMenu(final List<?> objects, final VidesoGLCanvas wwd) {
		super("Menu");
		
		JMenuItem copy = new JMenuItem("Copier dans le presse-papier");
		copy.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				wwd.copySelectedObjectsToClipboard();
			}
		});

		this.add(copy);
		
		JMenuItem delete = new JMenuItem("Supprimer");
		delete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for(Object o : objects){
					wwd.delete(o);
				}
			}
		});
	}

}
