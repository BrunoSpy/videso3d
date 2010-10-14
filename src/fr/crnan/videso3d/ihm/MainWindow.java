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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import fr.crnan.videso3d.AirspaceListener;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.SplashScreen;
import fr.crnan.videso3d.Videso3D;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.formats.geo.GEOFileFilter;
import fr.crnan.videso3d.formats.lpln.LPLNFileFilter;
import fr.crnan.videso3d.formats.opas.OPASFileFilter;
import fr.crnan.videso3d.geom.LatLonUtils;
import fr.crnan.videso3d.globes.FlatGlobeCautra;
import fr.crnan.videso3d.ihm.components.DropDownToggleButton;
import fr.crnan.videso3d.ihm.components.VFileChooser;
import fr.crnan.videso3d.util.VidesoStatusBar;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.Logging;

/**
 * Fenêtre principale
 * @author Bruno Spyckerelle
 * @version 0.3.3
 */
@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	/**
	 * NASA WorldWind
	 */
	private VidesoGLCanvas wwd;
	/**
	 * Explorateur de données
	 */
	private DataExplorer dataExplorer;

	/**
	 * Panel contextuel
	 */
	private ContextPanel context;
	/**
	 * Gestionnaire de bases de données
	 */
	private DatabaseManagerUI databaseUI;

	private final SplashScreen splashScreen;
	
	private ProgressMonitor progressMonitor;
	
	/**
	 * Nombre d'étapes d'initialisation pour la barre de progression
	 */
	private int step = 0;
	
	static {
		// Ensure that menus and tooltips interact successfully with the WWJ window.
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}

	public MainWindow(){
		//Création du splashscreen
		splashScreen = new SplashScreen();
		splashScreen.setVisible(true);
		splashScreen.setStatus("Création de la vue 3D", step);
		step++;		
		{
			//Instancie WorldWind
			this.createWwd();
		}
		
		//la suite du lancement de l'application (launch())
		//est lancée par createWwd lorsque l'initialisation est terminée


		//fermeture des connections aux bases de données avant de quitter afin de ne pas perdre les dernières transactions
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				DatabaseManager.closeAll();
			}
		});

	}

	/**
	 * Crée et configure le canvas WorldWind
	 */
	private void createWwd(){
		this.wwd = new VidesoGLCanvas();
		wwd.setPreferredSize(new java.awt.Dimension(0, 0));
		
		wwd.setModel(new BasicModel());

		wwd.addPropertyChangeListener("step", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				splashScreen.setStatus((String) evt.getNewValue(), (step*100)/(wwd.getNumberInitSteps()+3) );
				Logging.logger().info(evt.getNewValue()+" "+(step*100)/(wwd.getNumberInitSteps()+3));
				step++;
			}
		});
		
		//initialisation des objets 3D en background
		new SwingWorker<String, Integer>(){
			@Override
			protected String doInBackground() {
				try {
					wwd.initialize();
					dataExplorer = new DataExplorer(wwd);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			@Override
			protected void done(){
				//une fois terminé, on lance l'application
				try {
					launchVideso3D();
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
		}.execute();
	}


	private void launchVideso3D(){
		
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/videso3d.png")));

		//Titre de la fenêtre
		this.setTitle("Videso 3D ("+Videso3D.VERSION+")");

		//Fermeture de l'application
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Menu
		//this.setJMenuBar(this.createMenuBar());

		//Layout
		this.setLayout(new BorderLayout());

		//Barre d'actions
		this.add(this.createToolBar(), BorderLayout.PAGE_START);

		//Barre de statut
		this.add(this.createStatusBar(), BorderLayout.SOUTH);



		//Explorateur de données
//		dataExplorer = new DataExplorer(wwd);
		//		JDesktopPane desktop = new JDesktopPane();
		//		JInternalFrame wwdFrame = new JInternalFrame("WorldWind", true, false, true, true);
		//		wwdFrame.setSize(500, 300);
		//		wwdFrame.add(wwd);
		//		wwdFrame.setVisible(true);
		//		desktop.add(wwdFrame);

		context = new ContextPanel(wwd);
		context.setStipController(dataExplorer.getStipController());
		if(dataExplorer.getAIPController()!=null)
			context.setAIP(dataExplorer.getAIPController().getAIP());
		
		JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, dataExplorer, wwd);
		mainPane.setOneTouchExpandable(true);
		mainPane.setBorder(null);
		mainPane.setPreferredSize(new Dimension(600, 0));
		
		
		wwd.addSelectListener(context);
		final AirspaceListener airspaceListener = new AirspaceListener(wwd, context, dataExplorer.getStipController());
		wwd.addSelectListener(airspaceListener);
		context.setMinimumSize(new Dimension(0,0)); //taille mini à 0 pour permettre la fermeture du panneau avec setDividerLocation
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, mainPane, context);
	//	splitPane.setOneTouchExpandable(true); //en attendant de trouver mieux ...
		splitPane.setResizeWeight(1.0);
		
		this.getContentPane().add(splitPane, BorderLayout.CENTER);

		
		//mises à jour en cas de changement de base
		progressMonitor = new ProgressMonitor(this, "Mise à jour", "", 0, 6);
		wwd.addPropertyChangeListener("step", new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				progressMonitor.setNote(evt.getNewValue().toString());
			}
		});

		DatabaseManager.addPropertyChangeListener(DatabaseManager.BASE_CHANGED, new PropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				//précréation des éléments 3D dans un SwingWorker avec ProgressMonitor
				new SwingWorker<Integer, Integer>(){

					@Override
					protected Integer doInBackground() throws Exception {
						progressMonitor.setProgress(0);
						try { 
							progressMonitor.setNote(evt.getNewValue().toString());
						} catch (Exception e){
							e.printStackTrace();
						}
						dataExplorer.updateView((DatabaseManager.Type)evt.getNewValue());
						return null;
					}

					@Override
					protected void done() {
						if(DatabaseManager.Type.STIP.equals(evt.getNewValue())){
							context.setStipController(dataExplorer.getStipController());
							airspaceListener.setStipController(dataExplorer.getStipController());
						} else if(DatabaseManager.Type.AIP.equals(evt.getNewValue())){
							context.setAIP(dataExplorer.getAIPController().getAIP());
						}
						
						progressMonitor.close();
					}
				}.execute();
			}
		});

		//suppression du splashscreen et affichage de la fenêtre
		splashScreen.dispose();
		this.pack();
		//FullScreen
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setVisible(true);
		
		//ferme le panneau d'informations, doit être fait après l'affichage de la fenêtre
		splitPane.setDividerLocation(1.0);
	}

//	/**
//	 * Barre de menu de l'application
//	 * @return {@link JMenuBar} Barre de menu
//	 */
//	private JMenuBar createMenuBar(){
//		JMenu file = new JMenu("Fichier");
//
//		JMenuItem dbUI = new JMenuItem("Gestion des bases de données...");
//		dbUI.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				databaseUI = new DatabaseManagerUI(db);
//				databaseUI.setVisible(true);
//				databaseUI.addPropertyChangeListener("baseChanged", new PropertyChangeListener() {		
//					@Override
//					public void propertyChange(PropertyChangeEvent evt) {
//						final String type = (String)evt.getNewValue();
//						//mises à jour en background
//						new SwingWorker<String, Integer>(){
//							@Override
//							protected String doInBackground() throws Exception {
//								try {
//									if(type.equals("STIP")){
//										//mise à jour de la vue 3D
//										//TODO mettre à jour seulement si la base de données a changé
//										wwd.buildStip();
//										//mise à jour de l'explorateur de données
//										dataExplorer.updateStipView();
//									} else if (type.equals("EXSA")){
//										//mise à jour de l'explorateur de données
//										dataExplorer.updateStrView();
//										//mise à jour de la vue 3D
//										wwd.removeMosaiques();
//										//suppression des radars
//										wwd.removeRadars();
//									} else if(type.equals("STPV")){
//										dataExplorer.updateStpvView();
//										wwd.removeMosaiques();
//									} else if(type.equals("Edimap")){
//										dataExplorer.updateEdimapView();
//										wwd.removeAllEdimapLayers();
//									}
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
//								return null;
//							}
//						}.execute();
//					}
//				});
//			}
//		});
//
//		file.add(dbUI);
//
//		file.add(new JSeparator());
//
//		JMenuItem quit = new JMenuItem("Quitter");
//		quit.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				db.closeAll();
//				System.exit(0);
//			}
//		});
//		file.add(quit);
//
//		JMenu affichage = new JMenu("Fenêtre");
//
//		JMenu help = new JMenu("Aide");
//
//		JMenuBar menuBar = new JMenuBar();
//		menuBar.add(file);
//		menuBar.add(affichage);
//		menuBar.add(help);
//		return menuBar;
//	}



	/**
	 * Barre de status de l'application
	 * @return Barre de status
	 */
	private JPanel createStatusBar(){
		VidesoStatusBar statusBar = new VidesoStatusBar();
		statusBar.setEventSource(wwd);
		return statusBar;
	}

	/**
	 * Barre d'outils principale
	 * @return {@link JToolBar}
	 */
	private JToolBar createToolBar(){
		JToolBar toolbar = new JToolBar("Actions");

		
		//Reset de l'affichage
		final JButton reset = new JButton(new ImageIcon(getClass().getResource("/resources/reset_22.png")));
		reset.setToolTipText("Remettre à zéro la carte.");

		reset.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dataExplorer.resetView();
				wwd.resetView();
			}
		});
		
		toolbar.add(reset);
		
		//Configuration
		final JButton config = new JButton(new ImageIcon(getClass().getResource("/resources/configure.png")));
		config.setToolTipText("Configurer les paramètres généraux de l'application");
		config.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				new ConfigurationUI().setVisible(true);
			}
		});
		toolbar.add(config);
		
		//Analyse
		final JButton analyze = new JButton(new ImageIcon(getClass().getResource("/resources/analyze_22.png")));
		analyze.setToolTipText("Analyser les données Stip/Stpv");
		analyze.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				AnalyzeUI.showAnalyzeUI();
				AnalyzeUI.setWWD(wwd);
			}
		});
		toolbar.add(analyze);
		toolbar.addSeparator();
		
		
		//Ajouter trajectoires
		final JButton trajectoires = new JButton(new ImageIcon(getClass().getResource("/resources/plus_traj_22.png")));
		trajectoires.setToolTipText("Importer des trajectoires");
		trajectoires.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final VFileChooser fileChooser = new VFileChooser();
				fileChooser.setFileSelectionMode(VFileChooser.FILES_ONLY);
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.addChoosableFileFilter(new OPASFileFilter());
				fileChooser.addChoosableFileFilter(new LPLNFileFilter());
				fileChooser.addChoosableFileFilter(new GEOFileFilter());
				if(fileChooser.showOpenDialog(trajectoires) == VFileChooser.APPROVE_OPTION){

					new SwingWorker<String, Integer>(){
						@Override
						protected String doInBackground() throws Exception {
							try {
								dataExplorer.addTrajectoriesViews(fileChooser.getSelectedFiles());
							} catch(Exception e1){
								e1.printStackTrace();
							}
							return null;
						}
					}.execute();

				}
			}
		});
		toolbar.add(trajectoires);

		//Ajouter données
		JButton datas = new JButton(new ImageIcon(getClass().getResource("/resources/database_22.png")));
		datas.setToolTipText("Ajouter/supprimer des données");
		datas.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				databaseUI = new DatabaseManagerUI();
				databaseUI.setVisible(true);
			}
		});
		toolbar.add(datas);
		toolbar.addSeparator();

		//fond de la France
		final JToggleButton fond = new JToggleButton("Fond");
		fond.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				wwd.toggleFrontieres(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		toolbar.add(fond);

		//Alidade
		final JToggleButton alidad = new JToggleButton("Alidade");
		alidad.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				wwd.switchMeasureTool(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		toolbar.add(alidad);

		//Projections 
		DropDownToggleButton toggle2D = new DropDownToggleButton();
		toggle2D.setText("2D/3D");

		final ButtonGroup projections = new ButtonGroup();

		JRadioButtonMenuItem cautra = new JRadioButtonMenuItem("Cautra (exp.)");
		cautra.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					wwd.setProjection(FlatGlobeCautra.PROJECTION_CAUTRA);
				}
			}
		});
		projections.add(cautra);

		JRadioButtonMenuItem mercator = new JRadioButtonMenuItem("Mercator");
		mercator.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					wwd.setProjection(FlatGlobeCautra.PROJECTION_MERCATOR);
				}
			}
		});
		mercator.setSelected(true);
		projections.add(mercator);

		JRadioButtonMenuItem latlon = new JRadioButtonMenuItem("Lat-Lon");
		latlon.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					wwd.setProjection(FlatGlobeCautra.PROJECTION_LAT_LON);
				}
			}
		});
		projections.add(latlon);

		JRadioButtonMenuItem sin = new JRadioButtonMenuItem("Sin.");
		sin.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					wwd.setProjection(FlatGlobeCautra.PROJECTION_SINUSOIDAL);
				}
			}
		});
		projections.add(sin);

		JRadioButtonMenuItem modSin = new JRadioButtonMenuItem("Mod. Sin.");
		modSin.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					wwd.setProjection(FlatGlobeCautra.PROJECTION_MODIFIED_SINUSOIDAL);
				}
			}
		});
		projections.add(modSin);

		toggle2D.getPopupMenu().add(cautra);
		toggle2D.getPopupMenu().add(mercator);
		toggle2D.getPopupMenu().add(latlon);
		toggle2D.getPopupMenu().add(sin);
		toggle2D.getPopupMenu().add(modSin);

		toggle2D.addToToolBar(toolbar);

		toggle2D.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				wwd.enableFlatGlobe(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		toolbar.addSeparator();

		JLabel label = new JLabel("Exagération verticale : ");
		toolbar.add(label);

		int MIN_VE = 1;
		int MAX_VE = 8;
		int curVe = (int) this.wwd.getSceneController().getVerticalExaggeration();
		curVe = curVe < MIN_VE ? MIN_VE : (curVe > MAX_VE ? MAX_VE : curVe);
		final JSlider slider = new JSlider(MIN_VE, MAX_VE, curVe);
		slider.setMajorTickSpacing(1);
		slider.setPaintTicks(false);
		slider.setSnapToTicks(true);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(1, new JLabel("1x"));
		labelTable.put(2, new JLabel("2x"));
		labelTable.put(4, new JLabel("4x"));
		labelTable.put(8, new JLabel("8x"));
		slider.setLabelTable(labelTable);
		slider.setPaintLabels(true);
		toolbar.addPropertyChangeListener("orientation", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				slider.setOrientation((Integer)evt.getNewValue());
			}
		});
		slider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				double ve = ((JSlider) e.getSource()).getValue();
				wwd.getSceneController().setVerticalExaggeration(ve);
			}
		});
		toolbar.add(slider);		

		//recherche avec autocomplétion
		toolbar.addSeparator();
		toolbar.add(new JLabel(new ImageIcon(getClass().getResource("/resources/zoom-original.png"))));


		LinkedList<String> results = new LinkedList<String>();
		results.add("");//utile pour supprimer l'élément de la vue
		try {
			Statement st = DatabaseManager.getCurrentStip();
			if(st != null){
				ResultSet rs = st.executeQuery("select name from balises UNION select name from routes UNION select nom from secteurs");
				while(rs.next()){
					results.add(rs.getString(1));
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		final JComboBox search = new JComboBox(results.toArray());
		search.setEditable(true);
		search.setToolTipText("<html>Recherche universelle.<br />" +
				"<ul><li>Si une base Stip est importée, permet de rechercher dans les éléments Stip (balises, secteurs, routes).</li>" +
				"<li>Permet de centrer la vue sur des coordonnées. Syntaxe acceptée :" +
				"<ul><li>45N 123W</li><li>+45.1234, -123.12</li><li>45.1234N 123.12W</li>" +
				"<li>45° 30' 00\"N, 50° 30'W</li><li>45°30' -50°30'</li><li>45 30 N 50 30 W</li></ul></ul></html>");
		AutoCompleteDecorator.decorate(search);
		
		search.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals("comboBoxEdited")){
					
					String input = (String)((JComboBox)e.getSource()).getSelectedItem();
					//try to convert into latlon first
					LatLon coord = LatLonUtils.computeLatLonFromString(input);
					if(coord != null) {
						wwd.getView().goTo(new Position(coord, 0), 1e6);
					} else {
						if(!input.isEmpty())
							context.showInfo(input);
						//wwd.highlight(input);
						if(dataExplorer.getStipController() != null)
							dataExplorer.getStipController().highlight(input);
					}
				}
			}
		});

		//Mise à jour du contenu en cas de changement de base de données Stip
		DatabaseManager.addPropertyChangeListener(DatabaseManager.BASE_CHANGED, new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(DatabaseManager.Type.STIP.equals(evt.getNewValue())){
					search.removeAllItems();
					search.addItem("");
					try {
						Statement st = DatabaseManager.getCurrentStip();
						if(st != null){
							search.setEnabled(true);
							ResultSet rs = st.executeQuery("select name from balises UNION select name from routes UNION select nom from secteurs");
							while(rs.next()){
								search.addItem(rs.getString(1));
							}
						} else {
							search.setEnabled(false);
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
					} catch (Exception e){
						e.printStackTrace();
					}
				}
			}
		});
		
		toolbar.add(search);

		return toolbar;
	}

}
