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
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import eu.medsea.mimeutil.MimeUtil;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.FileManager;
import fr.crnan.videso3d.FileParser;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.edimap.Cartes;
import fr.crnan.videso3d.exsa.Exsa;
import fr.crnan.videso3d.pays.Pays;
import fr.crnan.videso3d.stip.Stip;
import fr.crnan.videso3d.stpv.Stpv;
import fr.crnan.videso3d.radio.Radio;

/**
 * Interface de gestion des base de données.<br />
 * @author Bruno Spyckerelle
 * @version 0.3
 */
@SuppressWarnings("serial")
public class DatabaseManagerUI extends JDialog {

	private JXTable table;
	private JButton select;
	private JButton add;
	private JButton delete;
		
	private static ProgressMonitor progressMonitor;
		
	public DatabaseManagerUI() {

		this.setTitle("Gestion des bases de données");
		
		this.setPreferredSize(new Dimension(500, 300));
		
		this.setLayout(new BorderLayout());
		
		this.build();	
				
		this.pack();
		
		this.setModal(true);
		
	}
	
	private void build(){
		
		table = new JXTable(new DBTableModel());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	//	table.setEditable(false);
		table.setAutoResizeMode(JXTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setColumnControlVisible(true);
		table.getColumnExt("id").setVisible(false);
		table.getColumnExt("Commentaire").setEditable(true);
		this.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		
		select = new JButton("Sélectionner");
		select.setEnabled(false);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(table.getSelectedRow() != -1 && DatabaseManager.isSelected(((DBTableModel)table.getModel()).getId(table.convertRowIndexToModel(table.getSelectedRow())))){
					select.setEnabled(false);
				} else { 
					select.setEnabled(true);
				}
			}
		});
		
		select.addActionListener(new TableListener());

		buttons.add(select);
		buttons.add(Box.createHorizontalGlue());
		add = new JButton("Ajouter une base...");
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					if(fileChooser.showOpenDialog(add) == JFileChooser.APPROVE_OPTION){
						MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
						File file = fileChooser.getSelectedFile();
						if(MimeUtil.getMimeTypes(file).contains("application/zip")){
							//add datas
							addDatabase(FileManager.unzip(file).get(0).getAbsoluteFile());
							//la suppression des fichiers est faite lorsque le parser a terminé
						} else if(MimeUtil.getMimeTypes(file).contains("application/x-gzip")){
							File tarFile = FileManager.gunzip(file);
							if(MimeUtil.getMimeTypes(tarFile).contains("application/x-tar")){
								addDatabase(FileManager.untar(tarFile).get(0).getAbsoluteFile());
							} else {
								//if ungzipped file is not a tar file, try to add datas
								addDatabase(tarFile);
							}
						} else {
							addDatabase(file);
						}
					}
				} catch(Exception e1){
					e1.printStackTrace();
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
				Exsa exsa = new Exsa(file.getAbsolutePath());
				this.getDatas(exsa, "Import des données EXSA", "EXSA");
				return;
			} else {
				file = file.getParentFile();
			}
		}
		
		List<File> files = Arrays.asList(file.listFiles());
		if(files.contains(new File(file.getAbsolutePath()+"/LIEUX"))){//une méthode comme une autre pour vérifier que le dossier est une dossier de données STIP
			Stip stip = new Stip(file.getAbsolutePath());
			this.getDatas(stip, "Import des données STIP", "STIP");
		} else if(files.contains(new File(file.getAbsolutePath()+"/LIEU"))) {//une méthode comme une autre pour vérifier que le dossier est une dossier de données STPV
			Stpv stpv = new Stpv(file.getAbsolutePath());
			this.getDatas(stpv, "Import des données STPV", "STPV");
		} else if(files.contains(new File(file.getAbsolutePath()+"/carac_jeu"))
				|| files.contains(new File(file.getAbsolutePath()+"/carac_jeu.nct"))
				|| files.contains(new File(file.getAbsolutePath()+"/carac_jeu.NCT"))) {
			//TODO trouver une meilleure gestion des extensions
			String caracJeuPath = "";
			if(files.contains(new File(file.getAbsolutePath()+"/carac_jeu"))) caracJeuPath = "carac_jeu";
			if(files.contains(new File(file.getAbsolutePath()+"/carac_jeu.nct"))) caracJeuPath = "carac_jeu.nct";
			if(files.contains(new File(file.getAbsolutePath()+"/carac_jeu.NCT"))) caracJeuPath = "carac_jeu.NCT";
			Cartes cartes = new Cartes(file.getAbsolutePath(),caracJeuPath);
			this.getDatas(cartes, "Import des données EDIMAP", "Edimap");
		} else if(files.contains(new File(file.getAbsolutePath()+"/PAYS"))) {
			Pays pays = new Pays(file.getAbsolutePath());
			this.getDatas(pays, "Import des contours des pays", "PAYS");
		} else if(files.contains(new File(file.getAbsolutePath()+"/CartesDynamiques.csv"))){
//			Ods ods = new Ods(file.absolutePath(), this.db);
//			this.getDatas(ods, "Import des données BDSATCATM");
		} 
		else if ( files.contains(new File(file.getAbsolutePath()+"/output.xml"))){
			//Radio radio = new Radio(file.getAbsolutePath());
			
				Radio radio = new Radio();					
				this.getDatas(radio,"Import des données Radio","RadioCov");
		}		
		else {
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
					if((Boolean) evt.getNewValue()) {
						DatabaseManager.importFinished(type);
					}
					((DBTableModel)table.getModel()).update();
					//suppression des fichiers temporaires si besoin
					FileManager.removeTempFiles();
				} else if(evt.getPropertyName().equals("progress")){
					if(progressMonitor.isCanceled()) {
						fileParser.cancel(true);
						//suppression des fichiers temporaires si besoin
						FileManager.removeTempFiles();
					}
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
		
		private String[] titles = {"id", "Nom", "Type", "Date d'import", "Commentaire", "Sélectionné"};
		
		@SuppressWarnings("unchecked")
		private Class[] types = new Class[] {Integer.class, String.class, String.class, String.class, String.class, Boolean.class};
				
		private Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		
		public DBTableModel(){
			try {
				Statement st = DatabaseManager.getCurrent(Type.Databases);
				ResultSet rs = st.executeQuery("select * from databases");
				while(rs.next()){
					Vector<Object> line = new Vector<Object>();
					line.add(rs.getInt("id"));
					line.add(rs.getString("name"));
					line.add(rs.getString("type"));
					line.add(rs.getString("date"));
					line.add(rs.getString("commentaire"));
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
			String type = (String) this.getValueAt(rowIndex, 2);
			for(Vector<Object> row : data){
				if(row.get(2).equals(type)){
					row.set(5, false);
				}
			}
			data.get(rowIndex).set(5, true);
			fireTableDataChanged();
		}
		
		public void delete(int rowIndex) {
			data.remove(rowIndex);
			fireTableDataChanged();
		}
		/**
		 * Synchronise le modèle avec la base de données
		 */
		public void update(){
			data.removeAllElements();
			try {
				Statement st = DatabaseManager.getCurrent(Type.Databases);
				ResultSet rs = st.executeQuery("select * from databases");
				while(rs.next()){
					Vector<Object> line = new Vector<Object>();
					line.add(rs.getInt("id"));
					line.add(rs.getString("name"));
					line.add(rs.getString("type"));
					line.add(rs.getString("date"));
					line.add(rs.getString("commentaire"));
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
			return (Integer) data.get(rowIndex).get(0);
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if(columnIndex == 4) {
				return true;
			} else {
				return false;
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
		 */
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if(columnIndex == 4){
				DatabaseManager.setComment((Integer)this.getValueAt(rowIndex, 0), aValue.toString());
				data.get(rowIndex).set(4, aValue);
				fireTableDataChanged();
			}
		}
		
		
		
	}
	
	/*-------------- Listener ------------------*/
	
	private class TableListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if(source == select){
				int index = table.convertRowIndexToModel(table.getSelectionModel().getAnchorSelectionIndex());
				try {
					String type = (String)table.getModel().getValueAt(index, 2);
					DatabaseManager.selectDatabase((Integer)((DBTableModel)table.getModel()).getId(index), type);
					//Mise à jour de la vue
					((DBTableModel)table.getModel()).select(index);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			} else if (source == delete) {				
				try {
					int index = table.convertRowIndexToModel(table.getSelectionModel().getAnchorSelectionIndex());
					Integer id = (Integer)((DBTableModel)table.getModel()).getId(index);
					DatabaseManager.deleteDatabase(id);
					((DBTableModel)table.getModel()).delete(index);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		};
	}
}