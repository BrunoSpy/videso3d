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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.graphics.Balise;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.airspaces.Airspace;

/**
 * Menu for right click on selection<br />
 * Copy is done only for Stip objects
 * @author Bruno Spyckerelle
 * @version 0.0.2
 *
 */
public class MultipleSelectionMenu extends JPopupMenu{

	public MultipleSelectionMenu(final List<?> objects, final VidesoGLCanvas wwd) {
		super("Menu");
		
		JMenu allMenu = new JMenu("Tous les objets...");
		
		JMenuItem copy = new JMenuItem("Copier dans le presse-papier");
		copy.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				wwd.copySelectedObjectsToClipboard();
			}
		});
		
		allMenu.add(copy);
		
		JMenuItem delete = new JMenuItem("Supprimer tout ("+objects.size()+" objets)");
		delete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Iterator<?> it = objects.iterator();
				while(it.hasNext()){
					Object o = it.next();
					it.remove();
					wwd.delete(o);
				}
			}
			
			
		});
		
		allMenu.add(delete);
			
		this.add(allMenu);
		
		final List<Airspace> airspaces = new ArrayList<Airspace>();
		final List<Balise> balises = new ArrayList<Balise>();
		final List<Path> trajectoires = new ArrayList<Path>();
		for(Object o : objects){
			if(o instanceof Airspace) {
				airspaces.add((Airspace) o);
			} else if(o instanceof Balise){
				balises.add((Balise) o);
			} else if(o instanceof Path){
				trajectoires.add((Path) o);
			}
				
		}
		if(!airspaces.isEmpty()){
			this.add(new AirspaceMenu(airspaces, null, wwd));
		}
		
		if(!balises.isEmpty()){
			JMenuItem balisesItem = new JMenuItem("Supprimer balises ("+balises.size()+")");
			balisesItem.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					for(Balise a : balises)
						wwd.delete(a);
				}
			});
			this.add(balisesItem);
		}
		
		if(!trajectoires.isEmpty()){
			this.add(new TrajectoriesMenu(trajectoires, wwd));
		}
	}

}
