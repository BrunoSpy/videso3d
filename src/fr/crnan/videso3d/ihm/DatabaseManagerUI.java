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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.FileParser;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.edimap.Cartes;
import fr.crnan.videso3d.exsa.Exsa;
import fr.crnan.videso3d.pays.Pays;
import fr.crnan.videso3d.stip.Stip;
import fr.crnan.videso3d.stpv.Stpv;

/**
 * Interface de gestion des base de données
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
@SuppressWarnings("serial")
public class DatabaseManagerUI extends JFrame {

	private JXTable table;
	private JButton select;
	private JButton add;
	private JButton delete;
		
	private static ProgressMonitor progressMonitor;
	
	private final DatabaseManager db;
	
	public DatabaseManagerUI(final DatabaseManager db) {
		this.db = db;
		
		this.setTitle("Gestion des bases de données");
		
		this.setPreferredSize(new Dimension(500, 300));
		
		this.setLayout(new BorderLayout());
		
		this.build();	
		
		this.pack();
		
	}
	
	private void build(){
		
		table = new JXTable(new DBTableModel());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setEditable(false);
		table.setSortable(false);//TODO à mettre à true une fois la solution de la cohérence table/model trouvée
		table.setAutoResizeMode(JXTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setColumnControlVisible(true);
		this.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		
		select = new JButton("Sélectionner");
		select.addActionListener(new TableListener());
		
		buttons.add(select);
		buttons.add(Box.createHorizontalGlue());
		add = new JButton("Ajouter une base...");
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if(fileChooser.showOpenDialog(add) == JFileChooser.APPROVE_OPTION){
					addDatabase(fileChooser.getSelectedFile());
				}
			}
		});
		buttons.add(add);
		
		delete = new JButton("Supprimer");
		delete.addActionListener(new TableListener());
		buttons.add(delete);
		this.getContentPane().add(buttons, BorderLayout.SOUTH);
	}
	
	private void addDatabase(File file){
		if(file.isFile()){
			int index = file.getName().lastIndexOf(".");
			String suffix = index == -1 ? "" : file.getName().substring(index);
			if(suffix.equalsIgnoreCase(".lst") || suffix.equalsIgnoreCase(".txt")){
				//import données EXSA
				Exsa exsa = new Exsa(file.getAbsolutePath(), this.db);
				this.getDatas(exsa, "Import des données EXSA", "EXSA");
				return;
			} else {
				file = file.getParentFile();
			}
		}
		
		List<File> files = Arrays.asList(file.listFiles());
		if(files.contains(new File(file.getAbsolutePath()+"/LIEUX"))){//une méthode comme une autre pour vérifier que le dossier est une dossier de données STIP
			Stip stip = new Stip(file.getAbsolutePath(), this.db);
			this.getDatas(stip, "Import des données STIP", "STIP");
		} else if(files.contains(new File(file.getAbsolutePath()+"/LIEU"))) {//une méthode comme une autre pour vérifier que le dossier est une dossier de données STPV
			Stpv stpv = new Stpv(file.getAbsolutePath(),this.db);
			this.getDatas(stpv, "Import des données STPV", "STPV");
		} else if(files.contains(new File(file.getAbsolutePath()+"/carac_jeu"))
				|| files.contains(new File(file.getAbsolutePath()+"/carac_jeu.nct"))
				|| files.contains(new File(file.getAbsolutePath()+"/carac_jeu.NCT"))) {
			//TODO trouver une meilleure gestion des extensions
			String caracJeuPath = "";
			if(files.contains(new File(file.getAbsolutePath()+"/carac_jeu"))) caracJeuPath = "carac_jeu";
			if(files.contains(new File(file.getAbsolutePath()+"/carac_jeu.nct"))) caracJeuPath = "carac_jeu.nct";
			if(files.contains(new File(file.getAbsolutePath()+"/carac_jeu.NCT"))) caracJeuPath = "carac_jeu.NCT";
			Cartes cartes = new Cartes(file.getAbsolutePath(),caracJeuPath,this.db);
			this.getDatas(cartes, "Import des données EDIMAP", "EDIMAP");
		} else if(files.contains(new File(file.getAbsolutePath()+"/PAYS"))) {
			Pays pays = new Pays(file.getAbsolutePath(), this.db);
			this.getDatas(pays, "Import des contours des pays", "PAYS");
		} else if(files.contains(new File(file.getAbsolutePath()+"/CartesDynamiques.csv"))){
//			Ods ods = new Ods(file.absolutePath(), this.db);
//			this.getDatas(ods, "Import des données BDSATCATM");
		} else {
			System.out.println("Pas de fichier de base de données trouvé");
    		}
    	
	}
	
	/**
     * Parses files and displays a progress window
     * @param fileParser File parser to be launched
     * @param title Title of the progress window
     * @param type Type of the database
     */
    private void getDatas(final FileParser fileParser, String title, final String type){
    	
    	progressMonitor = new ProgressMonitor(this, title, "", 1, fileParser.numberFiles());
    	progressMonitor.setMillisToDecideToPopup(0);
    	progressMonitor.setMillisToPopup(0);
    	
    	fileParser.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals("done")){
					if((Boolean) evt.getNewValue()) firePropertyChange("baseChanged", "", type);
					((DBTableModel)table.getModel()).update();
				} else if(evt.getPropertyName().equals("progress")){
					if(progressMonitor.isCanceled()) fileParser.cancel(true);
					progressMonitor.setProgress((Integer)evt.getNewValue());	
				} else if(evt.getPropertyName().equals("file")){
					progressMonitor.setNote("Import du fichier "+(String)evt.getNewValue());
				}
			}
		});
    	
    	fileParser.execute();
    }
	
	/*-------------- TableModel ----------------*/
	private  class DBTableModel extends AbstractTableModel {
		
		private String[] titles = {"Nom", "Type", "Date d'import", "Sélectionné"};
		
		@SuppressWarnings("unchecked")
		private Class[] types = new Class[] {String.class, String.class, String.class, Boolean.class};
		
		private Vector<Integer> id = new Vector<Integer>();
		
		private Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		
		public DBTableModel(){
			try {
				Statement st = db.getCurrent(Type.Databases);
				ResultSet rs = st.executeQuery("select * from databases");
				while(rs.next()){
					Vector<Object> line = new Vector<Object>();
					id.add(rs.getInt("id"));
					line.add(rs.getString("name"));
					line.add(rs.getString("type"));
					line.add(rs.getString("date"));
					line.add(rs.getBoolean("selected"));
					data.add(line);
				}		
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public int getColumnCount() {
			return titles.length;
		}

		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data.get(rowIndex).get(columnIndex);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Class getColumnClass(int columnIndex){
			return types[columnIndex];
		}
		
		@SuppressWarnings("unused")
		public void addDB(Vector<Object> row){
			data.add(row);
			fireTableDataChanged();
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int column) {
			return titles[column];
		}

		public void select(int rowIndex){
			String type = (String) this.getValueAt(rowIndex, 1);
			for(Vector<Object> row : data){
				if(row.get(1).equals(type)){
					row.set(3, false);
				}
			}
			data.get(rowIndex).set(3, true);
			fireTableDataChanged();
		}
		
		public void delete(int rowIndex) {
			data.remove(rowIndex);
			id.remove(rowIndex);
			fireTableDataChanged();
		}
		/**
		 * Synchronise le modèle avec la base de données
		 */
		public void update(){
			data.removeAllElements();
			id.removeAllElements();
			try {
				Statement st = db.getCurrent(Type.Databases);
				ResultSet rs = st.executeQuery("select * from databases");
				while(rs.next()){
					Vector<Object> line = new Vector<Object>();
					id.add(rs.getInt("id"));
					line.add(rs.getString("name"));
					line.add(rs.getString("type"));
					line.add(rs.getString("date"));
					line.add(rs.getBoolean("selected"));
					data.add(line);
				}		
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			fireTableDataChanged();
		}
		
		public Integer getId(int rowIndex){
			return id.get(rowIndex);
		}
	}
	
	/*-------------- Listener ------------------*/
	
	private class TableListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if(source == select){
				int index = table.getSelectionModel().getAnchorSelectionIndex();
				try {
					String type = (String)table.getModel().getValueAt(index, 1);
					db.selectDatabase((Integer)((DBTableModel)table.getModel()).getId(index), type);
					//Mise à jour de la vue
					((DBTableModel)table.getModel()).select(index);
					firePropertyChange("baseChanged", "", type);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			} else if (source == delete) {
				int index = table.getSelectedRow();
				try {
					String type = (String)table.getModel().getValueAt(index, 1);
					db.deleteDatabase((Integer)((DBTableModel)table.getModel()).getId(index));
					firePropertyChange("baseChanged", "", type);
					((DBTableModel)table.getModel()).delete(index);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		};
	}
}
