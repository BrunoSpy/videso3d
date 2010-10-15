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
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.aip.AIPController;
import fr.crnan.videso3d.edimap.EdimapController;
import fr.crnan.videso3d.exsa.STRController;
import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.formats.geo.GEOReader;
import fr.crnan.videso3d.formats.lpln.LPLNReader;
import fr.crnan.videso3d.formats.opas.OPASReader;
import fr.crnan.videso3d.ihm.components.ButtonTabComponent;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import fr.crnan.videso3d.radio.RadioDataManager;
import fr.crnan.videso3d.skyview.SkyViewController;
import fr.crnan.videso3d.stip.StipController;
import fr.crnan.videso3d.stpv.StpvController;
import gov.nasa.worldwind.util.Logging;

/**
 * Panel de configuration des objets affichés sur le globe
 * @author Bruno Spyckerelle
 * @version 0.4.2
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
	private Component skyview;
	private Component aip;
	
	/**
	 * Constructeur
	 * @param wwd {@link VidesoGLCanvas} Association avec la vue 3D
	 */
	public DataExplorer(final VidesoGLCanvas wwd){

		this.wwd = wwd;
		
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
		this.updateSkyView();
		this.updateAIPView();

		add(tabs, BorderLayout.CENTER);		
		
	}	

	/**
	 * Force une mise à jour de la vue
	 */
	public void updateView(Type type){
		switch (type) {
		case STIP:
			updateStipView();
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
		case SkyView:
			updateSkyView();
			break;
		case AIP:
			updateAIPView();
			break;
		default : break;
		}	
	}
	
	//TODO en attendant de faire mieux
	public StipController getStipController(){
		if (stip != null){
			return ((StipView)stip).getController();
		} 
		return null;
	}
	
	public AIPController getAIPController(){
		if(aip != null){
			return (AIPController) ((AIPView )aip).getController();
		}
		return null;
	}
	
	
	private JScrollPane buildTab(Component panel){
		JScrollPane pane = new JScrollPane(panel);
		pane.setBorder(null);
		return pane;
	}
	
	/**
	 * Met à jour le tab SkyView
	 */
	public void updateSkyView() {	
		if(skyview == null){
			try {
				if(DatabaseManager.getCurrentSkyView() != null){
					skyview = new SkyView(new SkyViewController(wwd));
					tabs.add("SkyView", skyview);
					ButtonTabComponent buttonTab = new ButtonTabComponent(tabs);
					buttonTab.getButton().addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								DatabaseManager.unselectDatabase(Type.SkyView);
							} catch (SQLException e1) {
								e1.printStackTrace();
							}
						}
					});
					tabs.setTabComponentAt(tabs.indexOfComponent(skyview), buttonTab);
					tabs.setSelectedIndex(tabs.getTabCount()-1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				if(DatabaseManager.getCurrentSkyView() != null){
					int i = tabs.indexOfComponent(skyview);
					skyview = new SkyView(new SkyViewController(wwd));
					tabs.setComponentAt(i, skyview);
					tabs.setSelectedIndex(i);
				} else {
					int i = tabs.indexOfComponent(skyview);
					if(i>=0){
						tabs.removeTabAt(i);
					}
					((SkyView)skyview).getController().reset();
					skyview = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Met à jour le tab de données Stip
	 * @param progressMonitor Si vrai, affiche un progressMonitor
	 */
	public void updateStipView() {	
		if(stip == null){
			try {
				if(DatabaseManager.getCurrentStip() != null){
					stip = new StipView(new StipController(wwd));
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
					stip = new StipView(new StipController(wwd));
					tabs.setComponentAt(i, stip);
					tabs.setSelectedIndex(i);
				} else {
					((StipView)stip).getController().reset();
					int i = tabs.indexOfComponent(stip);
					if(i>=0){
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
					exsa = new StrView(new STRController(wwd));
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
					((StrView)exsa).getController().removeAllLayers();
					exsa = new StrView(new STRController(wwd));
					exsaPane = this.buildTab(exsa);
					tabs.setComponentAt(i, exsaPane);
					tabs.setSelectedIndex(i);
				} else {
					int i = tabs.indexOfComponent(exsaPane);
					if(i>=0) tabs.removeTabAt(i);
					((StrView)exsa).getController().removeAllLayers();
					exsa = null;
					exsaPane = null;
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
					stpv = new StpvView(new StpvController(wwd));
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
					((StpvView)stpv).getController().removeAllLayers();
					stpv = new StpvView(new StpvController(wwd));
					tabs.setComponentAt(i, stpv);
					tabs.setSelectedIndex(i);
				} else {
					int i = tabs.indexOfComponent(stpv);
					if(i>=0) tabs.removeTabAt(i);
					((StpvView)stpv).getController().removeAllLayers();
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
					edimap = new EdimapView(new EdimapController(wwd));
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
					((EdimapView)edimap).getController().removeAllLayers();
					edimap = new EdimapView(new EdimapController(wwd));
					tabs.setComponentAt(i, edimap);
					tabs.setSelectedIndex(i);
				} else {
					((EdimapView)edimap).getController().removeAllLayers();
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
	 * Met à jour l'onglet AIP 
	 */
	public void updateAIPView(){
		if(aip == null){
			try {
				if(DatabaseManager.getCurrentAIP() != null){
					aip = new AIPView(new AIPController(wwd));
					ButtonTabComponent buttonTab = new ButtonTabComponent(tabs);
					buttonTab.getButton().addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								DatabaseManager.unselectDatabase(Type.AIP);
							} catch (SQLException e1) {
								e1.printStackTrace();
							}
						}
					});
					tabs.addTab("AIP", aip);
					tabs.setTabComponentAt(tabs.indexOfComponent(aip), buttonTab);
					tabs.setSelectedIndex(tabs.getTabCount()-1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				if(DatabaseManager.getCurrentAIP() != null){
					int i = tabs.indexOfComponent(aip);
					aip = new AIPView(new AIPController(wwd));
					tabs.setComponentAt(i, aip);
					tabs.setSelectedIndex(i);
				} else {
					((AIPView)aip).reset();
					int i = tabs.indexOfComponent(aip);
					if(i>=0){
						tabs.removeTabAt(i);
					}
					aip = null;
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
		if(opasFile.size() == 0 && geoFile.size() == 0 && lplnFile.size() == 0){
			Logging.logger().warning("Aucun fichier trajectoire trouvé.");
			JOptionPane.showMessageDialog(null, "<html><b>Problème :</b><br />Aucun fichier trajectoire trouvé.<br /><br />" +
					"<b>Solution :</b><br />Vérifiez que les fichiers sélectionnés sont bien dans un format pris en compte.</html>", "Erreur", JOptionPane.ERROR_MESSAGE);
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
				if(DatabaseManager.getCurrentRadioCov() != null) {
						ArrayList<String> radioCovPathTab = new ArrayList<String>();
						radioCovPathTab = DatabaseManager.getCurrentRadioCovPath();
						for (int i=0;i<radioCovPathTab.size();i++) {

						this.wwd.firePropertyChange("step", "", "Création des données radio");
						RadioDataManager radioDataManager = new RadioDataManager(radioCovPathTab.get(i));				
						wwd.insertAllRadioCovLayers(radioDataManager.loadData());			
						
						}	
												
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
//				else {System.out.println("(DataExplorer)  getCurrentRadio est nul");}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
			catch (Exception e) {
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
					/* On supprime une ligne radioCov dans la liste du databaseManagerUI  */									
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
		if(stpv != null){
			((StpvView)stpv).reset();
		}
		if (radioCov != null) {
			((RadioCovView)radioCov).reset();
		}
		if(aip != null){
			((AIPView)aip).reset();
		}
		if(skyview != null) {
			((SkyView)skyview).reset();
		}
	}


}

