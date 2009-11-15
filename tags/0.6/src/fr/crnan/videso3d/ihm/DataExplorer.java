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
import java.awt.Dimension;

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
						
		tabs.addTab("Stip", new StipView(wwd, db));
		tabs.addTab("STR", this.buildTab(new StrView(wwd, db)));
		tabs.addTab("Stpv", new StpvView(wwd, db));
		tabs.addTab("Edimap", new EdimapView(wwd, db));
		//tabs.addTab("ODS", new JScrollPane());
		//tabs.addTab("AIP", new JScrollPane());
		
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
		int select = tabs.getSelectedIndex();
		//suppresion du tab, création du tab à l'emplacement précédent et sélection du tab Stip
		tabs.removeTabAt(0);
		tabs.insertTab("Stip", null, new StipView(wwd, db), "Sélecteur de données Stip", 0);
		tabs.setSelectedIndex(select);
	}
	
	/**
	 * Met à jour le tab STR
	 */
	public void updateStrView() {
		int select = tabs.getSelectedIndex();
		//suppresion du tab, création du tab à l'emplacement précédent et sélection du tab Str
		tabs.removeTabAt(1);
		tabs.insertTab("Str", null, this.buildTab(new StrView(wwd, db)), "Sélecteur de données Str", 1);
		tabs.setSelectedIndex(select);
	}
	
	/**
	 * Met à jour le tab STPV
	 */
	public void updateStpvView() {
		int select = tabs.getSelectedIndex();
		//suppresion du tab, création du tab à l'emplacement précédent et sélection du tab Stpv
		tabs.removeTabAt(2);
		tabs.insertTab("Stpv", null, new StpvView(wwd, db), "Sélecteur de données Stpv", 2);
		tabs.setSelectedIndex(select);
	}
	
	/**
	 * Met à jour le tab Edimap
	 */
	public void updateEdimapView() {
		int select = tabs.getSelectedIndex();
		//suppresion du tab, création du tab à l'emplacement précédent et sélection du tab Edimap
		tabs.removeTabAt(3);
		tabs.insertTab("Edimap", null, new EdimapView(wwd, db), "Sélecteur de données Edimap", 3);
		tabs.setSelectedIndex(select);
	}
}
