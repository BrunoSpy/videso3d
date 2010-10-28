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
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import fr.crnan.videso3d.ihm.components.DataView;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import fr.crnan.videso3d.radio.RadioDataManager;
import fr.crnan.videso3d.stip.StipController;
import fr.crnan.videso3d.stpv.StpvController;
import gov.nasa.worldwind.util.Logging;

/**
 * Panel de configuration des objets affichés sur le globe
 * @author Bruno Spyckerelle
 * @version 0.5.0
 */
@SuppressWarnings("serial")
public class DataExplorer extends JPanel {

	private VidesoGLCanvas wwd;

	private JTabbedPane tabs = new JTabbedPane();

	private HashMap<Type, DataView> panels = new HashMap<DatabaseManager.Type, DataView>(); 
	
	private Component radioCov;
	
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
		
		for(Type base : DatabaseManager.getSelectedDatabases()){
			this.updateView(base);
		}

		add(tabs, BorderLayout.CENTER);		
		
	}	

	public void updateView(final Type type){
		if(!panels.containsKey(type)){
			try {
				if(DatabaseManager.getCurrent(type) != null){
					panels.put(type, this.createView(type));
					tabs.add(type.toString(), (Component) panels.get(type));
					ButtonTabComponent buttonTab = new ButtonTabComponent(tabs);
					buttonTab.getButton().addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								DatabaseManager.unselectDatabase(type);
							} catch (SQLException e1) {
								e1.printStackTrace();
							}
						}
					});
					tabs.setTabComponentAt(tabs.indexOfComponent((Component) panels.get(type)), buttonTab);
					tabs.setSelectedIndex(tabs.getTabCount()-1);
				}
			} catch (SQLException e){
				e.printStackTrace();
			}
		} else {
			try {
				if(DatabaseManager.getCurrent(type)!= null) {
					int i = tabs.indexOfComponent((Component) panels.get(type));
					panels.get(type).reset();
					panels.remove(type);
					panels.put(type, this.createView(type));
					tabs.setComponentAt(i, (Component) panels.get(type));
					tabs.setSelectedIndex(i);
				} else {
					((DataView)panels.get(type)).getController().reset();
					int i = tabs.indexOfComponent((Component) panels.get(type));
					if(i>=0){
						tabs.removeTabAt(i);
					}
					panels.remove(type);
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
		
	private DataView createView(Type type){
		switch (type) {
		case STIP:
			return new StipView(new StipController(wwd));
		case STPV:
			return new StpvView(new StpvController(wwd));
		case EXSA:
			return new StrView(new STRController(wwd));
		case Edimap:
			return new EdimapView(new EdimapController(wwd));
		case AIP:
			return new AIPView(new AIPController(wwd));
//		case RadioCov:
//			return new RadioCovView(wwd);
		default:
			return null;
		}
	}

	public DataView getView(Type type){
		if(panels.containsKey(type)){
			return (DataView) panels.get(type);
		} else {
			return null;
		}
	}
	
	/**
	 * Remet à zéro les différentes sélections
	 * TODO : prendre en charge le sélecteur Stpv et Radio
	 */
	public void resetView() {
		for(DataView view : panels.values()){
			view.reset();
		}
	}


}

