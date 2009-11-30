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

package fr.crnan.videso3d.ihm;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.sql.SQLException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoGLCanvas;

/**
 * Panel de configuration des objets affichés sur le globe
 * @author Bruno Spyckerelle
 * @version 0.4
 */
@SuppressWarnings("serial")
public class DataExplorer extends JPanel {

	private DatabaseManager db; 

	private VidesoGLCanvas wwd;

	private JTabbedPane tabs = new JTabbedPane();

	private Component stip;
	private Component exsa;
	private Component edimap;
	private Component stpv;

	/**
	 * Constructeur
	 * @param db {@link DatabaseManager} Association avec la gestionnaire de db
	 * @param wwd {@link VidesoGLCanvas} Association avec la vue 3D
	 */
	public DataExplorer(DatabaseManager db, VidesoGLCanvas wwd){

		this.db = db; 
		this.wwd = wwd;

		setLayout(new BorderLayout());

		//Title
		JPanel titleAreaPanel = new TitledPanel("Sélecteur de données");
		add(titleAreaPanel, BorderLayout.NORTH);

		//Tabs
		tabs.setTabPlacement(JTabbedPane.TOP);
		//tabs scrollables si conteneur trop petit
		//	tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		tabs.setPreferredSize(new Dimension(300, 0));

		this.updateStipView();
		this.updateEdimapView();
		this.updateStrView();
		this.updateStpvView();

		add(tabs, BorderLayout.CENTER);
	}	

	private JScrollPane buildTab(JPanel panel){
		JScrollPane pane = new JScrollPane(panel);
		pane.setBorder(null);
		return pane;
	}

	/**
	 * Met à jour le tab de données Stip
	 */
	public void updateStipView() {		
		if(stip == null){
			try {
				if(this.db.getCurrentStip() != null){
					stip = new StipView(wwd, db);
					tabs.addTab("Stip", stip);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			tabs.remove(stip);
			try {
				if(this.db.getCurrentStip() != null){
					stip = new StipView(wwd, db);
					tabs.insertTab("Stip", null, stip, "Données stip", 0);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Met à jour le tab STR
	 */
	public void updateStrView() {
		if(exsa == null){
			try {
				if(this.db.getCurrentExsa() != null){
					exsa = this.buildTab(new StrView(wwd, db));
					tabs.addTab("STR", exsa);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			tabs.remove(stip);
			try {
				if(this.db.getCurrentExsa() != null){
					exsa = this.buildTab(new StrView(wwd, db));
					tabs.insertTab("STR", null, exsa, "Données STR", 1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Met à jour le tab STPV
	 */
	public void updateStpvView() {
		if(stpv == null){
			try {
				if(this.db.getCurrentStpv() != null){
					stpv = new StpvView(wwd, db);
					tabs.addTab("Stpv", stpv);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			tabs.remove(stpv);
			try {
				if(this.db.getCurrentStpv() != null){
					stpv = new StpvView(wwd, db);
					tabs.insertTab("Stpv", null, stpv, "Données stpv", 2);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Met à jour le tab Edimap
	 */
	public void updateEdimapView() {
		if(edimap == null){
			try {
				if(this.db.getCurrentEdimap() != null){
					edimap = new EdimapView(wwd, db);
					tabs.addTab("Edimap", edimap);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			tabs.remove(edimap);
			try {
				if(this.db.getCurrentEdimap() != null){
					edimap = new EdimapView(wwd, db);
					tabs.insertTab("Edimap", null, edimap, "Données Edimap", 3);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Ajoute un tab de sélection des trajectoires
	 * @param file
	 */
	public void addTrajectoriesView(File file) {
		tabs.addTab(file.getName(), new TrajectoriesView(wwd, file));
		tabs.setSelectedIndex(tabs.getTabCount()-1);
	}
}
