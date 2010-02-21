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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

import fr.crnan.videso3d.DatabaseManager;

/**
 * Résultats de données Stip/Stpv sur une balise/terrain
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class BaliseResultPanel extends ResultPanel {

	public BaliseResultPanel(String balise){
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
		top.add(this.createConsignesTable(balise));
		top.add(this.createLieu26Table(balise));
		top.add(this.createLieu27Table(balise));
		
		this.add(top);
		
		JPanel bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
		bottom.add(this.createLieu6Table(balise));
		bottom.add(this.createLieu8Table(balise));
		bottom.add(this.createLieu91Table(balise));
		
		this.add(bottom);
	}



	private Component createLieu91Table(String balise) {

		JXTable table = new JXTable();
		table.setHorizontalScrollEnabled(true);
		table.setEditable(false);
		table.setColumnControlVisible(true);
		String[] columns = {"Terrain", "Type", "Donnant", "Recevant", "Balise 1", "Balise 2", "Piste", "Avion", "TFL", "Terrain 1", "Conf 1", "Terrain 2", "Conf 2"};
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setColumnIdentifiers(columns);

		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select oaci, indicateur, secteur_donnant, secteur_recevant, bal1, bal2, piste, avion, tfl, terrain1, conf1, terrain2, conf2 from lieu91 where oaci "+forgeSql(balise)+" or bal1 "+forgeSql(balise)+ " or bal2 "+forgeSql(balise));
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while(rs.next()){
				Object[] objects = new Object[columnCount];
				for(int i=0; i<columnCount;i++){
					objects[i] = rs.getObject(i+1);
				}
				model.addRow(objects);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		table.setModel(model);
		table.packAll();

		JPanel lieux91 = new JPanel();
		lieux91.setLayout(new BorderLayout());
		lieux91.add(new TitledPanel("TFL (lieu 91)"), BorderLayout.PAGE_START);
		lieux91.add(new JScrollPane(table), BorderLayout.CENTER);
		return lieux91;
	}



	private Component createLieu8Table(String balise) {
		JXTable table = new JXTable();
		table.setHorizontalScrollEnabled(true);
		table.setEditable(false);
		table.setColumnControlVisible(true);
		String[] columns = {"Départ", "Arrivée", "Niveau"};
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setColumnIdentifiers(columns);

		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select depart, arrivee, fl from lieu8 where depart "+forgeSql(balise)+" or arrivee "+forgeSql(balise));
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while(rs.next()){
				Object[] objects = new Object[columnCount];
				for(int i=0; i<columnCount;i++){
					objects[i] = rs.getObject(i+1);
				}
				model.addRow(objects);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		table.setModel(model);
		table.packAll();

		JPanel lieux8 = new JPanel();
		lieux8.setLayout(new BorderLayout());
		lieux8.add(new TitledPanel("City pairs (lieu 8)"), BorderLayout.PAGE_START);
		lieux8.add(new JScrollPane(table), BorderLayout.CENTER);
		return lieux8;
	}



	private Component createLieu6Table(String balise) {
		JXTable table = new JXTable();
		table.setHorizontalScrollEnabled(true);
		table.setEditable(false);
		table.setColumnControlVisible(true);
		String[] columns = {"Terrain", "Balise", "Niveau"};
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setColumnIdentifiers(columns);

		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select oaci, bal1, xfl1 from lieu6 where oaci "+forgeSql(balise)+" or bal1 "+forgeSql(balise));
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while(rs.next()){
				Object[] objects = new Object[columnCount];
				for(int i=0; i<columnCount;i++){
					objects[i] = rs.getObject(i+1);
				}
				model.addRow(objects);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		table.setModel(model);
		table.packAll();

		JPanel lieux6 = new JPanel();
		lieux6.setLayout(new BorderLayout());
		lieux6.add(new TitledPanel("XFL (lieu 6)"), BorderLayout.PAGE_START);
		lieux6.add(new JScrollPane(table), BorderLayout.CENTER);
		return lieux6;
	}

	private Component createLieu27Table(String balise) {
		JXTable table = new JXTable();
		table.setHorizontalScrollEnabled(true);
		table.setEditable(false);
		table.setColumnControlVisible(true);
		String[] columns = {"Terrain", "Balise", "Niveau"};
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setColumnIdentifiers(columns);

		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select oaci, balise, niveau from lieu27 where oaci "+forgeSql(balise)+" or balise "+forgeSql(balise));
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while(rs.next()){
				Object[] objects = new Object[columnCount];
				for(int i=0; i<columnCount;i++){
					objects[i] = rs.getObject(i+1);
				}
				model.addRow(objects);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		table.setModel(model);
		table.packAll();

		JPanel lieux27 = new JPanel();
		lieux27.setLayout(new BorderLayout());
		lieux27.add(new TitledPanel("FL tabulés en sortie (lieu 27)"), BorderLayout.PAGE_START);
		lieux27.add(new JScrollPane(table), BorderLayout.CENTER);
		return lieux27;
	}

	private Component createLieu26Table(String balise) {
		JXTable table = new JXTable();
		table.setHorizontalScrollEnabled(true);
		table.setEditable(false);
		table.setColumnControlVisible(true);
		String[] columns = {"Terrain", "Balise", "Niveau"};
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setColumnIdentifiers(columns);

		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select oaci, balise, niveau from lieu26 where oaci "+forgeSql(balise)+" or balise "+forgeSql(balise));
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while(rs.next()){
				Object[] objects = new Object[columnCount];
				for(int i=0; i<columnCount;i++){
					objects[i] = rs.getObject(i+1);
				}
				model.addRow(objects);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		table.setModel(model);
		table.packAll();

		JPanel lieux26 = new JPanel();
		lieux26.setLayout(new BorderLayout());
		lieux26.add(new TitledPanel("FL tabulés en entrée (lieu 26)"), BorderLayout.PAGE_START);
		lieux26.add(new JScrollPane(table), BorderLayout.CENTER);
		return lieux26;
	}

	private Component createConsignesTable(String balise) {
		JXTable table = new JXTable();
		table.setHorizontalScrollEnabled(true);
		table.setEditable(false);
		table.setColumnControlVisible(true);
		String[] columns = {"Type", "Terrain", "Balise", "Niveau", "Ecart", "Eveil", "Act", "Mod", "Base"};
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setColumnIdentifiers(columns);

		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select type, oaci, balise, niveau, ecart, eve, act, mod, base from consignes where oaci "+forgeSql(balise)+" or balise "+forgeSql(balise));
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while(rs.next()){
				Object[] objects = new Object[columnCount];
				for(int i=0; i<columnCount;i++){
					objects[i] = rs.getObject(i+1);
				}
				model.addRow(objects);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		table.setModel(model);
		table.packAll();

		JPanel consignes = new JPanel();
		consignes.setLayout(new BorderLayout());
		consignes.add(new TitledPanel("Consignes"), BorderLayout.PAGE_START);
		consignes.add(new JScrollPane(table), BorderLayout.CENTER);
		return consignes;
	}

	@Override
	public void setContext(ContextPanel context) {
		//la vue contextuelle est sans objet pour l'instant
	}

}
