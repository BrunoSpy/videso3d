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


import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoGLCanvas;

/**
 * Panel de configuration des objets affichés sur le globe
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class DataExplorer extends JTabbedPane {
	
	private DatabaseManager db; 
	
	private VidesoGLCanvas wwd;
	/**
	 * Constructeur
	 * @param db {@link DatabaseManager} Association avec la gestionnaire de db
	 * @param wwd {@link VidesoGLCanvas} Association avec la vue 3D
	 */
	public DataExplorer(DatabaseManager db, VidesoGLCanvas wwd){
		
		this.db = db; 
		this.wwd = wwd;
		
		//les tabs au dessus
		this.setTabPlacement(JTabbedPane.TOP);
		//tabs scrollables si conteneur trop petit
		this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		this.setPreferredSize(new Dimension(300, 0));
		
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Sélecteur de données", TitledBorder.CENTER, TitledBorder.TOP));
				
		this.addTab("Stip", new StipView(wwd, db));
		this.addTab("STR", new StrView(db, wwd));
		this.addTab("Stpv", new JScrollPane());
		this.addTab("Edimap", new JScrollPane());
		this.addTab("ODS", new JScrollPane());
		this.addTab("AIP", new JScrollPane());
		
		this.setVisible(true);
	}

	/**
	 * Met à jour le tab de données Stip
	 */
	public void updateStipView() {
		//suppresion du tab, création du tab à l'emplacement précédent et sélection du tab Stip
		this.removeTabAt(0);
		this.insertTab("Stip", null, new StipView(wwd, db), "Sélecteur de données Stip", 0);
		this.setSelectedIndex(0);
	}
	
	
}
