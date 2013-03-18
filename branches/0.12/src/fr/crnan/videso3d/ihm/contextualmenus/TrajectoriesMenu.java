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

import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.ihm.TrajectoryProjectionGUI;
import fr.crnan.videso3d.layers.tracks.TrajectoriesLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.Path;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.0.2
 */
public class TrajectoriesMenu extends JMenu {

	public TrajectoriesMenu(final List<VidesoTrack> tracks, final VidesoGLCanvas wwd){
		
		super("Trajectoires...");
		
		JMenuItem graph = new JMenuItem("Projection");
		graph.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new TrajectoryProjectionGUI(tracks, wwd.getModel().getGlobe()).setVisible(true);
			}
		});
		
		this.add(graph);
		
		this.addSeparator();
		
		final List<Path> visibles = new ArrayList<Path>();
		final List<Path> invisibles = new ArrayList<Path>();
		//find the layer hosting the track
		for(Layer l : wwd.getModel().getLayers()){
			if(l instanceof TrajectoriesLayer){
				for(VidesoTrack t : tracks){
					Object line = ((TrajectoriesLayer) l).getLine(t);
					if(line instanceof Path){
						if(((Path) line).isVisible()){
							visibles.add((Path) line);
						} else {
							invisibles.add((Path) line);
						}
					}
				}
			}
		}
		
		JMenuItem display =new JMenuItem("Afficher");
		display.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for(Path p : invisibles){
					p.setVisible(true);
				}
			}
		});
		if(invisibles.size() > 0)
			this.add(display);
		
		JMenuItem hide =new JMenuItem("Cacher");
		hide.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for(Path p : visibles){
					p.setVisible(false);
				}
			}
		});
		
		if(visibles.size() > 0)
			this.add(hide);
		
		JMenuItem delete = new JMenuItem("Supprimer");
		delete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for(VidesoTrack p : tracks)
					wwd.delete(p);
			}
		});
		
		this.add(delete);
	}
	
}
