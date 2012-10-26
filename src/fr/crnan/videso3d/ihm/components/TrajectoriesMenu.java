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

import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.ihm.TrajectoryProjectionGUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
