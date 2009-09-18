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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;

import fr.crnan.videso3d.DatabaseManager;

/**
 * Interface de gestion des base de données
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class DatabaseManagerUI extends JFrame {

	private DatabaseManager db;
	
	private MainWindow mainWindow;
	
	public DatabaseManagerUI(DatabaseManager db, MainWindow mainWindow) {
		this.db = db;
		
		this.mainWindow = mainWindow;
		
		this.build();
		
	}
	
	private void build(){
		JTable table = new JTable();
		this.add(table, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel();
		this.add(buttons, BorderLayout.SOUTH);
	}
	
}
