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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import fr.crnan.videso3d.stip.Stip;
import fr.crnan.videso3d.stip.StipController;
import fr.crnan.videso3d.stpv.Stpv;
import fr.crnan.videso3d.stpv.StpvController;

/**
 * Résultats de données Stip/Stpv sur une balise/terrain
 * @author Bruno Spyckerelle
 * @version 0.3
 */
public class BaliseResultPanel extends ResultPanel {

	private String titleTab = "Balise";
	
	public BaliseResultPanel(String balise){
		
		titleTab += " "+balise;
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Boolean stip = false;
		Boolean stpv = false;
		try {
			stip = DatabaseManager.getCurrentStip() != null;
			stpv = DatabaseManager.getCurrentStpv() != null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Component panel = null;
		
		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
		
		JPanel bottom = null;
		
		Vector<Component> panels = new Vector<Component>();
		
		if(stip && (panel = this.createConsignesTable(balise)) != null) {
			panels.add(panel);
		}
		if(stip && (panel = this.createBalintTable(balise)) != null){
			panels.add(panel);
		}
		if(stpv && (panel = this.createLieu26Table(balise)) != null) {
			panels.add(panel);
		}
		if(stpv && (panel = this.createLieu27Table(balise)) != null) {
			panels.add(panel);
		}
		if(stpv && (panel = this.createLieu6Table(balise)) != null){
			panels.add(panel);
		}
		if(stpv && (panel = this.createLieu8Table(balise)) != null){
			panels.add(panel);
		}
		if(stpv && (panel = this.createLieu91Table(balise)) != null){
			panels.add(panel);
		}
		
		if(panels.size() <= 2){ //cas particulier, en dessous de 2 éléments, on mets les résultats sur une seule ligne
			for(Component c : panels) {
				top.add(c);
			}
		} else {
			bottom = new JPanel();
			bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
			int middle = panels.size() /2;
			for(int i = 0; i < middle; i++){
				top.add(panels.get(i));
			}
			for(int i = middle; i<panels.size(); i++){
				bottom.add(panels.get(i));
			}
		}
		
		this.add(top);
		if(bottom != null) this.add(bottom);
	}



	private Component createLieu91Table(String balise) {
		final JXTable table = new JXTable(){
			//Redéfinition de processKeyEvent pour faire le copier/coller au même format que dans la BDS
			@Override
			protected void processKeyEvent(KeyEvent e){
				String tfl = new String();
				if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){
					int[] rows = this.getSelectedRows();
					for(int i=0; i< rows.length; i++){
						int id = (Integer)this.getModel().getValueAt(this.convertRowIndexToModel(this.getSelectedRows()[i]), this.getModel().getColumnCount()-1);
						tfl += Stpv.getString(StpvController.TFL, id)+"\n";
					}
					tfl+="\n";
					Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection ss = new StringSelection(tfl);
					clipBoard.setContents(ss , ss);
				}
			}
		};
		table.setHorizontalScrollEnabled(true);
		table.setEditable(false);
		table.setColumnControlVisible(true);
		String[] columns = {"Terrain", "Type", "Donnant", "Recevant", "Balise 1", "Balise 2", "Piste", "Avion", "TFL", "Terrain 1", "Conf 1", "Terrain 2", "Conf 2", "id"};
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setColumnIdentifiers(columns);

		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select oaci, indicateur, secteur_donnant, secteur_recevant, bal1, bal2, piste, avion, tfl, terrain1, conf1, terrain2, conf2, id from lieu91 where oaci "+forgeSql(balise)+" or bal1 "+forgeSql(balise)+ " or bal2 "+forgeSql(balise));
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
		if(model.getRowCount() > 0){
			table.setModel(model);
			table.removeColumn(table.getColumn(table.getColumnCount()-1));
			table.packAll();

			JPanel lieux91 = new JPanel();
			lieux91.setLayout(new BorderLayout());
			lieux91.add(new TitledPanel("TFL (lieu 91)"), BorderLayout.PAGE_START);
			lieux91.add(new JScrollPane(table), BorderLayout.CENTER);
			return lieux91;
		} else {
			return null;
		}
	}



	private Component createLieu8Table(String balise) {
		final JXTable table = new JXTable(){
			//Redéfinition de processKeyEvent pour faire le copier/coller au même format que dans la BDS STPV
			@Override
			protected void processKeyEvent(KeyEvent e){
				String lieu8 = new String();
				if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){
					int[] rows = this.getSelectedRows();
					for(int i=0; i< rows.length; i++){
						int id = (Integer)this.getModel().getValueAt(this.convertRowIndexToModel(this.getSelectedRows()[i]), this.getModel().getColumnCount()-1);
						lieu8 += Stpv.getString(StpvController.CITYPAIR, id)+"\n";
					}
					lieu8+="\n";
					Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection ss = new StringSelection(lieu8);
					clipBoard.setContents(ss , ss);
				}
			}
		};
		table.setHorizontalScrollEnabled(true);
		table.setEditable(false);
		table.setColumnControlVisible(true);
		String[] columns = {"Départ", "Arrivée", "Niveau", "id"};
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setColumnIdentifiers(columns);

		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select depart, arrivee, fl, id from lieu8 where depart "+forgeSql(balise)+" or arrivee "+forgeSql(balise));
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
		if(model.getRowCount() > 0){
		
			table.setModel(model);
			table.packAll();
			table.removeColumn(table.getColumn(table.getColumnCount()-1));

			JPanel lieux8 = new JPanel();
			lieux8.setLayout(new BorderLayout());
			lieux8.add(new TitledPanel("City pairs (lieu 8)"), BorderLayout.PAGE_START);
			lieux8.add(new JScrollPane(table), BorderLayout.CENTER);
			return lieux8;
		} else {
			return null;
		}
	}



	private Component createLieu6Table(String balise) {
		final JXTable table = new JXTable(){
			//Redéfinition de processKeyEvent pour faire le copier/coller au même format que dans la BDS STPV
			@Override
			protected void processKeyEvent(KeyEvent e){
				String lieu6 = new String();
				HashSet<String> listeTerrains = new HashSet<String>();
				if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){
					int[] rows = this.getSelectedRows();
					for(int i=0; i< rows.length; i++){
						String terrain = (String)this.getModel().getValueAt(this.convertRowIndexToModel(this.getSelectedRows()[i]), 0);
						if(listeTerrains.add(terrain)){
							int id = (Integer)this.getModel().getValueAt(this.convertRowIndexToModel(this.getSelectedRows()[i]), this.getModel().getColumnCount()-1);
							lieu6 += Stpv.getString(StpvController.XFL, id)+"\n";
						}
					}
					lieu6+="\n";
					Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection ss = new StringSelection(lieu6);
					clipBoard.setContents(ss , ss);
				}
			}
		};
		table.setHorizontalScrollEnabled(true);
		table.setEditable(false);
		table.setColumnControlVisible(true);
		String[] columns = {"Terrain", "Balise", "Niveau", "id"};
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setColumnIdentifiers(columns);

		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select oaci, bal1, xfl1, id from lieu6 where oaci "+forgeSql(balise)+" or bal1 "+forgeSql(balise));
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
		if(model.getRowCount() > 0){
			table.setModel(model);
			table.removeColumn(table.getColumn(table.getColumnCount()-1));
			table.packAll();

			JPanel lieux6 = new JPanel();
			lieux6.setLayout(new BorderLayout());
			lieux6.add(new TitledPanel("XFL (lieu 6)"), BorderLayout.PAGE_START);
			lieux6.add(new JScrollPane(table), BorderLayout.CENTER);
			return lieux6;
		} else {
			return null;
		}
	}

	private Component createLieu27Table(String balise) {
		final JXTable table = new JXTable(){
			//Redéfinition de processKeyEvent pour faire le copier/coller au même format que dans la BDS STPV
			@Override
			protected void processKeyEvent(KeyEvent e){
				String lieu27 = new String();
				if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){
					int[] rows = this.getSelectedRows();
					for(int i=0; i< rows.length; i++){
						int id = (Integer)this.getModel().getValueAt(this.convertRowIndexToModel(this.getSelectedRows()[i]), this.getModel().getColumnCount()-1);
						lieu27 += Stpv.getString(StpvController.NIV_TAB_SORTIE, id)+"\n";
					}
					lieu27+="\n";
					Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection ss = new StringSelection(lieu27);
					clipBoard.setContents(ss , ss);
				}
			}
		};
		table.setHorizontalScrollEnabled(true);
		table.setEditable(false);
		table.setColumnControlVisible(true);
		String[] columns = {"Terrain", "Balise", "Niveau", "Rév. RFL", "id"};
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setColumnIdentifiers(columns);

		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select oaci, balise, niveau, rerfl, id from lieu27 where oaci "+forgeSql(balise)+" or balise "+forgeSql(balise));
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

		if(model.getRowCount() > 0){
			table.setModel(model);
			table.removeColumn(table.getColumn(table.getColumnCount()-1));
			table.packAll();

			JPanel lieux27 = new JPanel();
			lieux27.setLayout(new BorderLayout());
			lieux27.add(new TitledPanel("FL tabulés en sortie (lieu 27)"), BorderLayout.PAGE_START);
			lieux27.add(new JScrollPane(table), BorderLayout.CENTER);
			return lieux27;
		} else {
			return null;
		}
	}

	private Component createLieu26Table(String balise) {
		final JXTable table = new JXTable(){
			//Redéfinition de processKeyEvent pour faire le copier/coller au même format que dans la BDS STPV
			@Override
			protected void processKeyEvent(KeyEvent e){
				String lieu26 = new String();
				if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){
					int[] rows = this.getSelectedRows();
					for(int i=0; i< rows.length; i++){
						int id = (Integer)this.getModel().getValueAt(this.convertRowIndexToModel(this.getSelectedRows()[i]), this.getModel().getColumnCount()-1);
						lieu26 += Stpv.getString(StpvController.NIV_TAB_ENTREE, id)+"\n";
					}
					lieu26+="\n";
					Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection ss = new StringSelection(lieu26);
					clipBoard.setContents(ss , ss);
				}
			}
		};
		table.setHorizontalScrollEnabled(true);
		table.setEditable(false);
		table.setColumnControlVisible(true);
		String[] columns = {"Terrain", "Balise", "Niveau", "Act Auto", "Rév. RFL", "id"};
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setColumnIdentifiers(columns);

		try {
			Statement st = DatabaseManager.getCurrentStpv();
			ResultSet rs = st.executeQuery("select oaci, balise, niveau, actau, rerfl, id from lieu26 where oaci "+forgeSql(balise)+" or balise "+forgeSql(balise));
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
		if(model.getRowCount() > 0){
			table.setModel(model);
			table.removeColumn(table.getColumn(table.getColumnCount()-1));
			table.packAll();

			JPanel lieux26 = new JPanel();
			lieux26.setLayout(new BorderLayout());
			lieux26.add(new TitledPanel("FL tabulés en entrée (lieu 26)"), BorderLayout.PAGE_START);
			lieux26.add(new JScrollPane(table), BorderLayout.CENTER);
			return lieux26;
		} else { 
			return null;
		}
	}

	private Component createConsignesTable(String balise) {
		final JXTable table = new JXTable(){
			//Redéfinition de processKeyEvent pour faire le copier/coller au même format que dans les fichiers SATIN
			@Override
			protected void processKeyEvent(KeyEvent e){
				String consignes = new String();
				if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){
					int[] rows = this.getSelectedRows();
					for(int i=0; i< rows.length; i++){
						int id = (Integer)this.getModel().getValueAt(this.convertRowIndexToModel(this.getSelectedRows()[i]), this.getModel().getColumnCount()-1);
						consignes += Stip.getString(StipController.CONSIGNE, id)+"\n";
					}
					consignes+="\n";
					Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection ss = new StringSelection(consignes);
					clipBoard.setContents(ss , ss);
				}
			}
		};
		table.setHorizontalScrollEnabled(true);
		table.setEditable(false);
		table.setColumnControlVisible(true);
		String[] columns = {"Type", "Terrain", "Balise", "Niveau", "Ecart", "Eveil", "Act", "Mod", "Base", "id"};
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setColumnIdentifiers(columns);
		try {
			Statement st = DatabaseManager.getCurrentStip();
			String query = "select type, oaci, balise, niveau, ecart, eve, act, mod, base, id from consignes where oaci "+forgeSql(balise);
			ResultSet rs = st.executeQuery("select oaci from consignes where oaci "+forgeSql(balise));
			if(rs.next() && !balise.endsWith("*")){//ajout des consignes en 999 si la recherche concerne un terrain
				query += " or oaci ='"+balise.substring(0, 3)+"9' ";
				query += " or oaci ='"+balise.substring(0, 2)+"99' ";
				query += " or oaci ='"+balise.substring(0, 1)+"999' ";
			} 
			query += " or balise "+forgeSql(balise);
			rs = st.executeQuery(query);
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
		if(model.getRowCount() > 0){
			table.setModel(model);
			table.removeColumn(table.getColumn(table.getColumnCount()-1));
			table.packAll();

			JPanel consignes = new JPanel();
			consignes.setLayout(new BorderLayout());
			consignes.add(new TitledPanel("Consignes"), BorderLayout.PAGE_START);
			consignes.add(new JScrollPane(table), BorderLayout.CENTER);
			return consignes;
		} else { 
			return null;
		}
	}

	private Component createBalintTable(String balise){
		final JXTable table = new JXTable(){
			//Redéfinition de processKeyEvent pour faire le copier/coller au même format que dans les fichiers SATIN
			@Override
			protected void processKeyEvent(KeyEvent e){
				String balint = new String();
				if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){
					int[] rows = this.getSelectedRows();
					for(int i=0; i< rows.length; i++){
						int id = (Integer)this.getModel().getValueAt(this.convertRowIndexToModel(this.getSelectedRows()[i]), this.getModel().getColumnCount()-1);
						balint += Stip.getString(StipController.BALINT, id)+"\n";
					}
					balint+="\n";
					Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection ss = new StringSelection(balint);
					clipBoard.setContents(ss , ss);
				}
			}
		};
		table.setHorizontalScrollEnabled(true);
		table.setEditable(false);
		table.setColumnControlVisible(true);
		String[] columns = {"UIR", "FIR", "Balise 1", "Balise", "Travers", "Balise 2", "id"};
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setColumnIdentifiers(columns);

		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select uir, fir, bal1, balise, appartient, bal2, id from balint where bal1 "+forgeSql(balise)+" or bal2 "+forgeSql(balise)+" or balise "+forgeSql(balise));
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while(rs.next()){
				Object[] objects = new Object[columnCount];
				for(int i=0; i<columnCount;i++){
					if(i==4 || i==1 || i==0){//on affiche un peu mieux les booléens
						int b = (Integer) rs.getObject(i+1);
						if(b==0) {
							objects [i] = "Non";
						} else {
							objects [i] = "Oui";
						}
					} else {
						objects[i] = rs.getObject(i+1);
					}
				}
				model.addRow(objects);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(model.getRowCount() > 0){
			table.setModel(model);
			table.removeColumn(table.getColumn(table.getColumnCount()-1));
			table.packAll();

			JPanel balint = new JPanel();
			balint.setLayout(new BorderLayout());
			balint.add(new TitledPanel("Couples interdits"), BorderLayout.PAGE_START);
			balint.add(new JScrollPane(table), BorderLayout.CENTER);
			return balint;
		} else { 
			return null;
		}
	}
	
	@Override
	public void setContext(ContextPanel context) {
		//la vue contextuelle est sans objet pour l'instant
	}



	@Override
	public String getTitleTab() {
		return this.titleTab;
	}

}
