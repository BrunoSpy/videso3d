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

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.databases.edimap.EdimapController;
import fr.crnan.videso3d.databases.exsa.STRController;
import fr.crnan.videso3d.databases.skyview.SkyViewController;
import fr.crnan.videso3d.databases.stpv.StpvController;
import fr.crnan.videso3d.graphics.DatabaseVidesoObject;
import fr.crnan.videso3d.graphics.Route;
import fr.crnan.videso3d.ihm.AnalyzeUI;
import fr.crnan.videso3d.ihm.ContextPanel;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.markers.Marker;
/**
 * Specific contectual menus for {@link DatabaseVidesoObject}
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class DatabaseVidesoObjectMenu extends JMenu{

	private DatabaseVidesoObject object;
	private ContextPanel context;
	private VidesoGLCanvas wwd;
	
	
	public DatabaseVidesoObjectMenu(DatabaseVidesoObject o, ContextPanel context, VidesoGLCanvas wwd){
		this.object = o;
		this.context = context;
		this.wwd = wwd;
		
		this.createMenu();
	}
	
	private void createMenu(){
		
		JMenuItem info = new JMenuItem("Informations...");
		info.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				context.showInfo(object.getDatabaseType(), object.getType(), object.getName());
			}
		});
		this.add(info);

		if((object instanceof Marker || object instanceof PointPlacemark) && object.getDatabaseType().equals(DatasManager.Type.STIP)){
			JMenu analyseItem = new JMenu("Analyse");
			JMenuItem analyseIti = new JMenuItem("Itinéraires");
			JMenuItem analyseTrajet = new JMenuItem("Trajets");
			JMenuItem analyseRoute = new JMenuItem("Routes");
			JMenuItem analyseBalise = new JMenuItem("Balise");
			analyseBalise.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					AnalyzeUI.showResults("balise", object.getName());
				}
			});
			analyseItem.add(analyseBalise);
			analyseIti.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					AnalyzeUI.showResults("iti", object.getName());
				}
			});
			analyseItem.add(analyseIti);
			analyseTrajet.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					AnalyzeUI.showResults("trajet", object.getName());
				}
			});
			analyseItem.add(analyseTrajet);
			analyseRoute.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					AnalyzeUI.showResults("route", object.getName());
				}
			});
			analyseItem.add(analyseRoute);
			this.add(analyseItem);
		}
		
		//Afficher les coordonnées : non implémenté dans tous les controleurs
		VidesoController c = DatasManager.getController(object.getDatabaseType());
		if(!(c instanceof STRController || 
				c instanceof EdimapController ||
				c instanceof StpvController ||
				(c instanceof SkyViewController && object instanceof Route))){
			final int type = object.getType();
			final String name = object.getName();
			final boolean locationsVisible = c.areLocationsVisible(type, name);
			JMenuItem locationsItem = new JMenuItem((locationsVisible ? "Cacher" : "Afficher") +" les coordonnées");
			this.add(locationsItem);
			locationsItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					VidesoController c = DatasManager.getController(object.getDatabaseType());
					c.setLocationsVisible(type, name, !locationsVisible);
				}
			});
		}
	}
	
}
