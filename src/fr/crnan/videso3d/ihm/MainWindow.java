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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.layers.MosaiqueLayer;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.geom.LatLon;

/**
 * Fenêtre principale
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class MainWindow extends JFrame {

	/**
	 * Gestionnaire de base de données
	 */
	private DatabaseManager db;
	/**
	 * NASA WorldWind
	 */
	private VidesoGLCanvas wwd;
	/**
	 * Explorateur de données
	 */
	private DataExplorer dataExplorer;
	
	/**
	 * Gestionnaire de bases de données
	 */
	private DatabaseManagerUI databaseUI;
	
	/**
	 * 
	 * @param db
	 */
	public MainWindow(DatabaseManager db){
		
		this.db = db;
		//Style Nimbus
		this.setNimbus();
		
		
		//Instancie WorldWind
		this.createWwd();
		
		//Titre de la fenêtre
		this.setTitle("Videso 3D");
		
		//Fermeture de l'application
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Menu
		this.setJMenuBar(this.createMenuBar());
		
		//Layout
		this.setLayout(new BorderLayout());
		
		//Barre d'actions
		this.add(this.createToolBar(), BorderLayout.PAGE_START);
		
		//Barre de statut
		this.add(this.createStatusBar(), BorderLayout.SOUTH);
		
		//Explorateur de données
		dataExplorer = new DataExplorer(this.db, wwd);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dataExplorer, wwd);
		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);
		this.getContentPane().add(splitPane, BorderLayout.CENTER);
		
		this.pack();
		
	
	}
	
	/**
	 * Crée et configure le canvas WorldWind
	 */
	private void createWwd(){
		this.wwd = new VidesoGLCanvas();
		wwd.setPreferredSize(new java.awt.Dimension(800, 600));

		wwd.setModel(new BasicModel());
		
		//initialisation des objets 3D en background
		new SwingWorker<String, Integer>(){
			@Override
			protected String doInBackground() throws Exception {
				wwd.initialize(db);
				return null;
			}
		}.execute();
		
		
		wwd.getModel().getLayers().add(new MosaiqueLayer(true, LatLon.fromDegrees(43.08444, 6.535), 22, 18, 32, MosaiqueLayer.BOTTOM_UP, MosaiqueLayer.LEFT_RIGHT, null));
		
////		Cautra
//		EarthFlatCautra globe = new EarthFlatCautra();
//		globe.setProjection(globe.PROJECTION_CAUTRA);
//		wwd.getModel().setGlobe(globe);
////         Switch to flat view and update with current position
//        BasicOrbitView orbitView = (BasicOrbitView)wwd.getView();
//        FlatOrbitView flatOrbitView = new FlatOrbitView();
//        flatOrbitView.setCenterPosition(orbitView.getCenterPosition());
//        flatOrbitView.setZoom(orbitView.getZoom( ));
//        flatOrbitView.setHeading(orbitView.getHeading());
//        flatOrbitView.setPitch(orbitView.getPitch());
//        wwd.setView(flatOrbitView);

	}
	
	/**
	 * Barre de menu de l'application
	 * @return {@link JMenuBar} Barre de menu
	 */
	private JMenuBar createMenuBar(){
		JMenu file = new JMenu("Fichier");
		
		JMenuItem dbUI = new JMenuItem("Gestion des bases de données...");
		dbUI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				databaseUI = new DatabaseManagerUI(db);
				databaseUI.setVisible(true);
				databaseUI.addPropertyChangeListener("baseChanged", new PropertyChangeListener() {		
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						String type = (String)evt.getNewValue();
						if(type.equals("STIP")){
							//mise à jour de l'explorateur de données
							dataExplorer.updateStipView();
							//mise à jour de la vue 3D
							wwd.buildStip();
						} else if (type.equals("EXSA")){
							
						}
					}
				});
			}
		});
		
		file.add(dbUI);

		file.add(new JSeparator());
		
		JMenuItem quit = new JMenuItem("Quitter");
		quit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		file.add(quit);
		
		JMenu affichage = new JMenu("Fenêtre");
		
		JMenu help = new JMenu("Aide");
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(file);
		menuBar.add(affichage);
		menuBar.add(help);
		return menuBar;
	}

	
	
	/**
	 * Barre de status de l'application
	 * @return Barre de status
	 */
	private JPanel createStatusBar(){
		JPanel statusBar = new JPanel();
		statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
		
		statusBar.add(new JLabel("Coord Cautra : "));
		statusBar.add(new JLabel("Coord WGS84 :"));
		
		return statusBar;
	}
	
	/**
	 * Barre d'outils principale
	 * @return {@link JToolBar}
	 */
	private JToolBar createToolBar(){
		JToolBar toolbar = new JToolBar("Actions");
		
		toolbar.add(new JToggleButton("Alidad"));
		
		toolbar.add(new JToggleButton("Projection Cautra"));
		
		return toolbar;
	}
	
	/**
	 * Utilise le L&F Nimbus au lieu du L&F Metal</br>
	 * Nécessite Java 6 Update 10
	 */
	private void setNimbus(){				
		for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels() ){
			if ("Nimbus".equals(laf.getName())) {
				try {
					UIManager.setLookAndFeel(laf.getClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}
}
