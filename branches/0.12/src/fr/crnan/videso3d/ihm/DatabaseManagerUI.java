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
import java.awt.Dialog;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import eu.medsea.mimeutil.MimeUtil;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.FileManager;
import fr.crnan.videso3d.FileParser;
import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.aip.AIP;
import fr.crnan.videso3d.databases.edimap.Cartes;
import fr.crnan.videso3d.databases.exsa.Exsa;
import fr.crnan.videso3d.databases.pays.Pays;
import fr.crnan.videso3d.databases.radio.RadioDataManager;
import fr.crnan.videso3d.databases.stip.Stip;
import fr.crnan.videso3d.databases.stpv.Stpv;
import fr.crnan.videso3d.ihm.components.VFileChooser;
import gov.nasa.worldwind.util.Logging;

/**
 * Interface de gestion des base de données.<br />
 * @author Bruno Spyckerelle
 * @version 0.4.1
 */
@SuppressWarnings("serial")
public class DatabaseManagerUI extends JDialog {

	private JXTable table;
	private JButton select;
	private JButton add;
	private JButton delete;
				
	private DoubleProgressMonitor progressMonitor2;
	
	private HashMap<FileParser, File[]> databases;
	
	public DatabaseManagerUI() {

		this.setTitle("Gestion des bases de données");
		
		this.setPreferredSize(new Dimension(500, 300));
		
		this.setLayout(new BorderLayout());
		
		this.build();	
				
		this.pack();
		
		this.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		
	}
	
	private void build(){
		
		progressMonitor2 = new DoubleProgressMonitor(this, "Import des fichiers sélectionnés", "", 0, 100);
		progressMonitor2.setAlwaysOnTop(true);
		
		table = new JXTable(new DBTableModel());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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

				final VFileChooser fileChooser = new VFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fileChooser.setMultiSelectionEnabled(true);
				if(fileChooser.showOpenDialog(add) == JFileChooser.APPROVE_OPTION) {
					databases = new HashMap<FileParser, File[]>();

					new SwingWorker<Integer, Integer>() {

						boolean databaseFound = false;

						@Override
						protected Integer doInBackground() throws Exception {

							progressMonitor2.setCancelled(false);
							progressMonitor2.getMainProgressBar().setIndeterminate(true);
							progressMonitor2.getSecondaryProgressBar().setIndeterminate(true);
							progressMonitor2.setVisible(true);
							progressMonitor2.setMainNote("Recherche des bases de données...");

							databaseFound = processFiles(fileChooser.getSelectedFiles());

							return null;
						}

						@Override
						protected void done() {
							if(!progressMonitor2.isCanceled()){
								if(!databaseFound){
									progressMonitor2.setVisible(false);
									JOptionPane.showMessageDialog(null, "<html><b>Problème :</b><br />Aucune base de donnée trouvée.<br /><br />" +
											"<b>Solution :</b><br />Vérifiez que le fichier sélectionné est bien pris en charge.</html>", "Erreur", JOptionPane.INFORMATION_MESSAGE);
									Logging.logger().warning("Pas de fichier de base de données trouvé");
								} else {
									importDatabases();
								}
							}
						}

					}.execute();
				}
			}
		});
		buttons.add(add);
		
		delete = new JButton("Supprimer");
		delete.addActionListener(new TableListener());
		buttons.add(delete);
		this.getContentPane().add(buttons, BorderLayout.SOUTH);
	}
	
	/**
	 * Determine how many databases to add and call the addDatabase() method for each one
	 * @param files Files selected
	 * @return True if at least a database has been imported
	 */
	private boolean processFiles(File[] filesSelected){
			
		boolean baseImported = false;
		TreeSet<File> files = new TreeSet<File>();
		for(File f : filesSelected){
			files.add(f);
		}
		MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
		
		while(files.size() > 0){
			File file = files.first();
			if(file.isDirectory()){
				boolean temp = processFiles(file.listFiles());
				baseImported = baseImported || temp;
			} else {
				if(MimeUtil.getMimeTypes(file).contains("application/zip")){
					boolean temp = processFiles(FileManager.unzip(file).toArray(new File[]{}));
					baseImported = baseImported || temp;
				} else if(MimeUtil.getMimeTypes(file).contains("application/x-gzip")){
					boolean temp = processFiles(new File[]{FileManager.gunzip(file)});
					baseImported = baseImported || temp;
				} else if(MimeUtil.getMimeTypes(file).contains("application/x-tar")){
					boolean temp = processFiles(FileManager.untar(file).toArray(new File[]{}));
					baseImported = baseImported || temp;
				} else { //file not zipped
					if(AIP.isAIPFile(file)){
						baseImported = true;
						addDatabase(DatasManager.Type.AIP, file);
					} else if(Exsa.isExsaFile(file)){
						baseImported = true;
						addDatabase(DatasManager.Type.EXSA, file);
					} else if(SkyView.isSkyViewFile(file)){
						baseImported = true;
						addDatabase(DatasManager.Type.SkyView, file);
					} else if(Stpv.containsSTPVFiles(files)){
						baseImported = true;
						//remove RESULTAT file in order to avoid a new detection of STPV files
						Iterator<File> iterator = files.iterator();
						while(iterator.hasNext()){
							File currentFile = iterator.next();
							if(currentFile.getName().equalsIgnoreCase("lieu") || currentFile.getName().equalsIgnoreCase("lieu.txt")){
								iterator.remove();
							}
						}
						addDatabase(DatasManager.Type.STPV, file.getParentFile());
					} else if(Cartes.containsCartes(files)){
						baseImported = true;
						//assuming there's only one set of maps in a directory
						//remove carac_jeu, .nct files and palette
						Iterator<File> iterator = files.iterator();
						while(iterator.hasNext()){
							File currentFile = iterator.next();
							if(currentFile.getName().equalsIgnoreCase("carac_jeu") 
									|| currentFile.getName().toLowerCase().endsWith("nct") 
									|| currentFile.getName().equalsIgnoreCase("palette")){
								iterator.remove();
							}
						}
						addDatabase(DatasManager.Type.Edimap, file.getParentFile());
					} else if(RadioDataManager.containsRadioDatas(files)){
						baseImported = true;
						//remove radioCoverageXSL.xsl file in order to avoid a new detection of radio files
						Iterator<File> iterator = files.iterator();
						while(iterator.hasNext()){
							File currentFile = iterator.next();
							if(currentFile.getName().equalsIgnoreCase("radioCoverageXSL.xsl")){
								iterator.remove();
							}
						}
						addDatabase(DatasManager.Type.RadioCov, file.getParentFile());
					} else if(Stip.containsStipFiles(files)){
						baseImported = true;
						//remove LIEUX file in order to avoid a new detection of stip files
						Iterator<File> iterator = files.iterator();
						while(iterator.hasNext()){
							File currentFile = iterator.next();
							if(currentFile.getName().equalsIgnoreCase("LIEUX")){
								iterator.remove();
							}
						}
						addDatabase(DatasManager.Type.STIP, file.getParentFile());
					} else if(Pays.containsPaysFiles(files)){
						baseImported = true;
						//remove PAYS file in order to avoid a new detection of pays files
						Iterator<File> iterator = files.iterator();
						while(iterator.hasNext()){
							File currentFile = iterator.next();
							if(currentFile.getName().equalsIgnoreCase("PAYS")){
								iterator.remove();
							}
						}
						addDatabase(DatasManager.Type.PAYS, file.getParentFile());
					}
				}
			}
			files.remove(file);
		}
		return baseImported;
	}
	
	private void addDatabase(DatasManager.Type type, File file){
		switch (type) {
		case AIP:
			AIP aip = new AIP(file.getAbsolutePath());
			databases.put(aip, new File[]{file});
			break;
		case EXSA:
			Exsa exsa = new Exsa(file.getAbsolutePath());
			databases.put(exsa, new File[]{file});
			break;
		case Edimap:
			String caracJeuPath = "";
			List<File> files = Arrays.asList(file.listFiles());
			if(files.contains(new File(file.getAbsolutePath()+"/carac_jeu"))) caracJeuPath = "carac_jeu";
			if(files.contains(new File(file.getAbsolutePath()+"/carac_jeu.nct"))) caracJeuPath = "carac_jeu.nct";
			if(files.contains(new File(file.getAbsolutePath()+"/carac_jeu.NCT"))) caracJeuPath = "carac_jeu.NCT";
			Cartes cartes = new Cartes(file.getAbsolutePath(),caracJeuPath);
			databases.put(cartes, file.listFiles());
			break;
		case SkyView:
			DatabaseManager.createSkyView(file.getName(), file.getAbsolutePath());
			DatabaseManager.importFinished(DatasManager.Type.SkyView);
			((DBTableModel)table.getModel()).update();
			break;
		case STPV:
			Stpv stpv = new Stpv(file.getAbsolutePath());
			databases.put(stpv, file.listFiles());
			break;
		case RadioCov:
			RadioDataManager radioDataManager = new RadioDataManager(new File(System.getProperty("user.dir")).toURI().relativize(new File(file.getAbsolutePath()).toURI()).getPath());
			databases.put(radioDataManager, new File[]{});
		    break;
		case STIP:
			Stip stip = new Stip(file.getAbsolutePath());
			databases.put(stip, file.listFiles());
			break;
		case PAYS:
			Pays pays = new Pays(file.getAbsolutePath());
			databases.put(pays, file.listFiles());
		default:
			break;
		}
	}

	private Entry<FileParser, File[]> current = null;
	private Iterator<Entry<FileParser, File[]>> iterator = null;
	private HashSet<DatasManager.Type> types = null;
	private int done = 0;
	
	private void importDatabases(){
		int max = 0;
		for(FileParser fileParser : databases.keySet()){
			max += fileParser.numberFiles();
		}
		
		done = 0;
		
		progressMonitor2.getMainProgressBar().setMaximum(max);
		progressMonitor2.getMainProgressBar().setIndeterminate(false);
		progressMonitor2.getSecondaryProgressBar().setIndeterminate(false);
				
		iterator = databases.entrySet().iterator();
		
		types = new HashSet<DatasManager.Type>();
		
		PropertyChangeListener fileParserListener = new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals("done")){
					if(progressMonitor2.isCanceled()){
						for(Entry<FileParser, File[]> entry : databases.entrySet()){
							try {
								DatabaseManager.deleteDatabase(entry.getKey().getName(), entry.getKey().getType());
								for(File f : entry.getValue()){
									FileManager.deleteFile(f);
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
						//plus de base de données à importer : on fait le ménage
						FileManager.removeTempFiles();
						//et on cache le progressmonitor
						progressMonitor2.setVisible(false);
						//on met à jour la fenetre
						((DBTableModel)table.getModel()).update();
					} else {
						if((Boolean) evt.getNewValue()) { //l'import s'est bien terminé
							types.add(current.getKey().getType());
						}
						//copie des fichiers
						for(File f : current.getValue()){
							//si le parser indique une liste des fichiers pertinents, ne copier que cette liste
							if(current.getKey().getRelevantFileNames() == null || 
									(current.getKey().getRelevantFileNames() != null 
									&& FileManager.containsFile(current.getKey().getRelevantFileNames(), f))){
								FileManager.copyFile(f, current.getKey().getName()+"_files");
							} 
						}
						//suppression du listener
						current.getKey().removePropertyChangeListener(this);
						//import de la base suivante
						if(iterator.hasNext()){
							done += current.getKey().numberFiles();
							importDatabase(iterator.next(), this);
						} else {
							//plus de base de données à importer : on fait le ménage
							FileManager.removeTempFiles();
							//et on cache le progressmonitor
							progressMonitor2.setVisible(false);
							//on met à jour la fenetre
							((DBTableModel)table.getModel()).update();
							//on met à jour les tabs
							for(DatasManager.Type t : types){
								DatabaseManager.importFinished(t);
							}
						}
					}
				} else if(evt.getPropertyName().equals("progress")){
					if(progressMonitor2.isCanceled()) {
						//user information
						progressMonitor2.setMainNote("Annulation en cours ...");
						progressMonitor2.getMainProgressBar().setIndeterminate(true);
						progressMonitor2.getSecondaryProgressBar().setIndeterminate(true);
						//annulation du parsing
						//on n'utilise pas SwingWorker.cancel(true) à cause des effets de bord avec sqlite
						//la fin de l'annulation est faite lorsque "done" est reçu
					}
					progressMonitor2.getSecondaryProgressBar().setValue((Integer)evt.getNewValue());
					progressMonitor2.getMainProgressBar().setValue(done+(Integer)evt.getNewValue());
				} else if(evt.getPropertyName().equals("file")){
					progressMonitor2.setSecondNote("import du fichier "+(String)evt.getNewValue());
				}
			}
		};
		
		
		if(iterator.hasNext()){			
			importDatabase(iterator.next(), fileParserListener);
		}
	}
	
	private void importDatabase(Entry<FileParser, File[]> entry, PropertyChangeListener listener){
		current = entry;
		current.getKey().addPropertyChangeListener(listener);
		progressMonitor2.setMainNote("Base "+current.getKey().getClass().getSimpleName());
		progressMonitor2.getSecondaryProgressBar().setMaximum(current.getKey().numberFiles());
		progressMonitor2.getSecondaryProgressBar().setValue(0);
		current.getKey().execute();
	}
	
	
	/*-------------- TableModel ----------------*/
	private  class DBTableModel extends AbstractTableModel {
		
		private String[] titles = {"id", "Nom", "Type", "Date d'import", "Commentaire", "Sélectionné"};
		
		@SuppressWarnings("rawtypes")
		private Class[] types = new Class[] {Integer.class, String.class, String.class, String.class, String.class, Boolean.class};
				
		private Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		
		public DBTableModel(){
			try {
				Statement st = DatabaseManager.getCurrent(DatasManager.Type.Databases);
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
		
		@Override
		public Class<?> getColumnClass(int columnIndex){
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
				Statement st = DatabaseManager.getCurrent(DatasManager.Type.Databases);
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
			switch (columnIndex) {
			case 4:
				return true;
			case 5:
				return true;
			default:
				return false;
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
		 */
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 4: //commentaire dans la table
				DatabaseManager.setComment((Integer)this.getValueAt(rowIndex, 0), aValue.toString());
				data.get(rowIndex).set(4, aValue);
				fireTableDataChanged();
				break;
			case 5: //table sélectionnée
				try {
					if(!(Boolean)data.get(rowIndex).get(5)){
						DatabaseManager.selectDatabase((Integer)data.get(rowIndex).get(0), (String)data.get(rowIndex).get(2));
						DatabaseManager.fireBaseSelected(DatabaseManager.stringToType((String)data.get(rowIndex).get(2)));
					} else {
						DatabaseManager.unselectDatabase(DatabaseManager.stringToType((String)data.get(rowIndex).get(2)));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				//Puis on change la valeur des cellules conformément aux nouvelles valeurs de la base
				this.update();
				break;
			}
		}
		
		
		
	}
	
	/*-------------- Listener ------------------*/
	
	private class TableListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			int index = table.convertRowIndexToModel(table.getSelectionModel().getAnchorSelectionIndex());
			if(source == select){
				try {
					String type = (String)table.getModel().getValueAt(index, 2);
					DatabaseManager.selectDatabase((Integer)((DBTableModel)table.getModel()).getId(index), type);
					DatabaseManager.fireBaseSelected(DatabaseManager.stringToType(type));
					//Mise à jour de la vue
					((DBTableModel)table.getModel()).select(index);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			} else if (source == delete) {				
				try {
					if(JOptionPane.showConfirmDialog(table, "Supprimer une base de données supprimera définitivement toutes les données associées.\n" +
							"Confirmez vous la suppresion de la base de données "+table.getModel().getValueAt(index, 1)+" ?", "Suppression d'une base de données", JOptionPane.YES_NO_OPTION) 
							== JOptionPane.YES_OPTION){
						FileManager.deleteFile(new File(table.getModel().getValueAt(index, 1)+"_files"));
						Integer id = (Integer)((DBTableModel)table.getModel()).getId(index);
						DatabaseManager.deleteDatabase(id);
						((DBTableModel)table.getModel()).delete(index);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		};
	}
}
