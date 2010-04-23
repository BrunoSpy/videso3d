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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.formats.geo.GEOReader;
import fr.crnan.videso3d.formats.lpln.LPLNReader;
import fr.crnan.videso3d.formats.opas.OPASReader;
import fr.crnan.videso3d.ihm.components.ButtonTabComponent;
import fr.crnan.videso3d.ihm.components.TitledPanel;

/**
 * Panel de configuration des objets affichés sur le globe
 * @author Bruno Spyckerelle
 * @version 0.4.1
 */
@SuppressWarnings("serial")
public class DataExplorer extends JPanel {

	private VidesoGLCanvas wwd;

	private JTabbedPane tabs = new JTabbedPane();

	private Component stip;
	private Component exsa;
	private JScrollPane exsaPane;
	private Component edimap;
	private Component stpv;
	private Component radioCov;

	private ProgressMonitor progressMonitor;
	
	/**
	 * Constructeur
	 * @param db {@link DatabaseManager} Association avec la gestionnaire de db
	 * @param wwd {@link VidesoGLCanvas} Association avec la vue 3D
	 */
	public DataExplorer(final VidesoGLCanvas wwd){

		this.wwd = wwd;

		progressMonitor = new ProgressMonitor(this, "Mise à jour", "", 0, 6);
		wwd.addPropertyChangeListener("step", new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				progressMonitor.setNote(evt.getNewValue().toString());
			}
		});
		
		setLayout(new BorderLayout());

		//Title
		JPanel titleAreaPanel = new TitledPanel("Sélecteur de données");
		add(titleAreaPanel, BorderLayout.NORTH);

		//Tabs
		tabs.setTabPlacement(JTabbedPane.TOP);

		tabs.setPreferredSize(new Dimension(300, 0));		
		
		this.updateStipView();
		this.updateStrView();
		this.updateStpvView();
		this.updateEdimapView();
		this.updateRadioCovView();

		add(tabs, BorderLayout.CENTER);
		
		//listener des changements de base de données
		DatabaseManager.addPropertyChangeListener(DatabaseManager.BASE_CHANGED, new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				switch ((Type)evt.getNewValue()) {
				case STIP:
					//précréation des éléments 3D dans un SwingWorker avec ProgressMonitor
					new SwingWorker<Integer, Integer>(){

						@Override
						protected Integer doInBackground() throws Exception {
							progressMonitor.setProgress(0);
							wwd.buildStip();
							return null;
						}

						@Override
						protected void done() {
							updateStipView();
							progressMonitor.close();
						}
					}.execute();
					break;
				case STPV:
					updateStpvView();
					break;
				case Edimap:
					updateEdimapView();
					break;
				case EXSA:
					updateStrView();
					break;
				case RadioCov:				
					updateRadioCovView();
					break;
				default : break;
				}				
			}
		});
	}	

	private JScrollPane buildTab(Component panel){
		JScrollPane pane = new JScrollPane(panel);
		pane.setBorder(null);
		return pane;
	}
	
	/**
	 * Met à jour le tab de données Stip
	 * @param progressMonitor Si vrai, affiche un progressMonitor
	 */
	public void updateStipView() {	
		if(stip == null){
			try {
				if(DatabaseManager.getCurrentStip() != null){
					stip = new StipView(wwd);
					tabs.add("Stip", stip);
					ButtonTabComponent buttonTab = new ButtonTabComponent(tabs);
					buttonTab.getButton().addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								DatabaseManager.unselectDatabase(Type.STIP);
							} catch (SQLException e1) {
								e1.printStackTrace();
							}
						}
					});
					tabs.setTabComponentAt(tabs.indexOfComponent(stip), buttonTab);
					tabs.setSelectedIndex(tabs.getTabCount()-1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				if(DatabaseManager.getCurrentStip() != null){
					int i = tabs.indexOfComponent(stip);
					stip = new StipView(wwd);
					tabs.setComponentAt(i, stip);
					tabs.setSelectedIndex(i);
				} else {
					System.out.println("test0");
					int i = tabs.indexOfComponent(stip);
					System.out.println("i "+i);
					if(i>=0){
						System.out.println("test");
						tabs.removeTabAt(i);
					}
					stip = null;
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
				if(DatabaseManager.getCurrentExsa() != null){
					exsa = new StrView(wwd);
					exsaPane = this.buildTab(exsa);
					ButtonTabComponent buttonTab = new ButtonTabComponent(tabs);
					buttonTab.getButton().addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								DatabaseManager.unselectDatabase(Type.EXSA);
							} catch (SQLException e1) {
								e1.printStackTrace();
							}
						}
					});
					tabs.addTab("STR", exsaPane);
					tabs.setTabComponentAt(tabs.indexOfComponent(exsaPane), buttonTab);
					tabs.setSelectedIndex(tabs.getTabCount()-1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				if(DatabaseManager.getCurrentExsa() != null){
					int i = tabs.indexOfComponent(exsaPane);
					wwd.removeMosaiques();
					wwd.removeRadars();
					exsa = new StrView(wwd);
					exsaPane = this.buildTab(exsa);
					tabs.setComponentAt(i, exsaPane);
					tabs.setSelectedIndex(i);
				} else {
					int i = tabs.indexOfComponent(exsaPane);
					if(i>=0) tabs.removeTabAt(i);
					exsa = null;
					exsaPane = null;
					wwd.removeMosaiques();
					wwd.removeRadars();
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
				if(DatabaseManager.getCurrentStpv() != null){
					stpv = new StpvView(wwd);
					ButtonTabComponent buttonTab = new ButtonTabComponent(tabs);
					buttonTab.getButton().addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								DatabaseManager.unselectDatabase(Type.STPV);
							} catch (SQLException e1) {
								e1.printStackTrace();
							}
						}
					});
					tabs.addTab("Stpv", stpv);
					tabs.setTabComponentAt(tabs.indexOfComponent(stpv), buttonTab);
					tabs.setSelectedIndex(tabs.getTabCount()-1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				if(DatabaseManager.getCurrentStpv() != null){
					int i = tabs.indexOfComponent(stpv);
					stpv = new StpvView(wwd);
					tabs.setComponentAt(i, stpv);
					tabs.setSelectedIndex(i);
				} else {
					int i = tabs.indexOfComponent(stpv);
					if(i>=0) tabs.removeTabAt(i);
					stpv = null;
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
				if(DatabaseManager.getCurrentEdimap() != null){
					edimap = new EdimapView(wwd);
					ButtonTabComponent buttonTab = new ButtonTabComponent(tabs);
					buttonTab.getButton().addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								DatabaseManager.unselectDatabase(Type.Edimap);
							} catch (SQLException e1) {
								e1.printStackTrace();
							}
						}
					});
					tabs.addTab("Edimap", edimap);
					tabs.setTabComponentAt(tabs.indexOfComponent(edimap), buttonTab);
					tabs.setSelectedIndex(tabs.getTabCount()-1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				if(DatabaseManager.getCurrentEdimap() != null){
					int i = tabs.indexOfComponent(edimap);
					wwd.removeAllEdimapLayers();
					edimap = new EdimapView(wwd);
					tabs.setComponentAt(i, edimap);
					tabs.setSelectedIndex(i);
				} else {
					wwd.removeAllEdimapLayers();
					int i = tabs.indexOfComponent(edimap);
					if(i>=0) tabs.removeTabAt(i);
					edimap = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Ajoute un tab de sélection des trajectoires.<br />
	 * Un tab est créé pour chaque type de fichier si plusieurs fichiers sont sélectionnés.
	 * @param file
	 */
	public void addTrajectoriesViews(File[] files) {
		Vector<File> opasFile = new Vector<File>();
		Vector<File> geoFile = new Vector<File>();
		Vector<File> lplnFile = new Vector<File>();
		
		for(File f : files){
			if(OPASReader.isOpasFile(f)) {
				opasFile.add(f);
			} else if(LPLNReader.isLPLNFile(f)) {
				lplnFile.add(f);
			} else if(GEOReader.isGeoFile(f)) {
				geoFile.add(f);
			}
		}
		
		if(opasFile.size()>0){
			this.addTrajectoriesView(new OPASReader(opasFile));
		}
		if(geoFile.size()>0){
			this.addTrajectoriesView(new GEOReader(geoFile));
		}
		if(lplnFile.size()>0){
			this.addTrajectoriesView(new LPLNReader(lplnFile));
		}
	}
	
	private void addTrajectoriesView(TrackFilesReader reader){
		final Component content = new TrajectoriesView(wwd, reader);
		tabs.addTab(reader.getName(), content);	
		ButtonTabComponent buttonTab = new ButtonTabComponent(tabs);
		buttonTab.getButton().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				((TrajectoriesView)content).delete();
			}
		});
		tabs.setTabComponentAt(tabs.indexOfComponent(content), buttonTab);
		tabs.setSelectedIndex(tabs.getTabCount()-1);
	}
	
	/**
	 *  Ajoute un tab de sélection des couvertures radio
	 */
	
	public void updateRadioCovView() {	
		if(radioCov == null){
			try {
				if(DatabaseManager.getCurrentRadioCov() != null){					
					wwd.insertAllRadioCovLayers();
					radioCov = new RadioCovView(wwd);
					ButtonTabComponent buttonTab = new ButtonTabComponent(tabs);
					buttonTab.getButton().addActionListener(new ActionListener() {					
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								DatabaseManager.unselectDatabase(Type.RadioCov);
							} 
							catch (SQLException e1) {
								e1.printStackTrace();
							}
						}
					});
					tabs.addTab("Antennes", radioCov);
					tabs.setTabComponentAt(tabs.indexOfComponent(radioCov), buttonTab);
					tabs.setSelectedIndex(tabs.getTabCount()-1);
				}
			
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		} 
		else {
			try {
				if(DatabaseManager.getCurrentRadioCov() != null){
					int i = tabs.indexOfComponent(radioCov);
					wwd.removeAllRadioCovLayers();
					radioCov = new RadioCovView(wwd);
					tabs.setComponentAt(i, radioCov);
					tabs.setSelectedIndex(i);
				} 
				else {
					/* On supprime une ligne radioCov dans la liste du databaseManagerUI */									
					wwd.removeAllRadioCovLayers();					
					tabs.remove(radioCov);
					radioCov = null;											
				}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}	
		


	/**
	 * Remet à zéro les différentes sélections
	 * TODO : prendre en charge le sélecteur Stpv et Radio
	 */
	public void resetView() {
		if(stip != null){
			((StipView)stip).reset();
		}
		if(exsa != null){
			((StrView)exsa).reset();
		}
		if(edimap != null){
			((EdimapView)edimap).reset();
		}
		if (radioCov != null) {
			((RadioCovView)radioCov).reset();
		}
	}


}

