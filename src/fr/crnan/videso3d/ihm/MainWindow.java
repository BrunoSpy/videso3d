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
import java.awt.Desktop;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Hashtable;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
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
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import fr.crnan.videso3d.AirspaceListener;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.SplashScreen;
import fr.crnan.videso3d.Videso3D;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.formats.geo.GEOFileFilter;
import fr.crnan.videso3d.formats.lpln.LPLNFileFilter;
import fr.crnan.videso3d.formats.opas.OPASFileFilter;
import fr.crnan.videso3d.formats.fpl.FPLFileFilter;
import fr.crnan.videso3d.globes.FlatGlobeCautra;
import fr.crnan.videso3d.ihm.components.DropDownButton;
import fr.crnan.videso3d.ihm.components.DropDownToggleButton;
import fr.crnan.videso3d.ihm.components.Omnibox;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import fr.crnan.videso3d.ihm.components.VFileChooser;
import fr.crnan.videso3d.util.VidesoStatusBar;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.examples.util.ScreenShotAction;
import gov.nasa.worldwind.examples.util.ShapeUtils;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.airspaces.Polygon;
import gov.nasa.worldwind.util.Logging;

/**
 * Fenêtre principale
 * @author Bruno Spyckerelle
 * @version 0.3.7
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
	 * OmniBox
	 */
	private Omnibox omniBox;
	/**
	 * SplitPane qui contient la vue et le contextPanel.
	 */
	private JSplitPane splitPane;
	
	/**
	 * Gestionnaire de bases de données
	 */
	private DatabaseManagerUI databaseUI;

	private final SplashScreen splashScreen;
	
	private ProgressMonitor progressMonitor;
	
	private AirspaceListener airspaceListener;
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
					for(Type t : DatabaseManager.getSelectedDatabases()) {
						DatasManager.createDatas(t, wwd);
					}
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

		//Layout
		this.setLayout(new BorderLayout());

		//Panneau contextuel
		context = new ContextPanel();
				
		JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, dataExplorer, wwd);
		mainPane.setOneTouchExpandable(true);
		mainPane.setBorder(null);
		mainPane.setPreferredSize(new Dimension(600, 0));
		
		
		wwd.addSelectListener(context);
		airspaceListener = new AirspaceListener(wwd, context);
		wwd.addSelectListener(airspaceListener);
		context.setMinimumSize(new Dimension(0,0)); //taille mini à 0 pour permettre la fermeture du panneau avec setDividerLocation
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, mainPane, context);
	//	splitPane.setOneTouchExpandable(true); //en attendant de trouver mieux ...
		splitPane.setResizeWeight(1.0);
		
		this.getContentPane().add(splitPane, BorderLayout.CENTER);

		//initialisation omnibox et contextpanel
		omniBox = new Omnibox(wwd, context);
		for(Type t : DatabaseManager.getSelectedDatabases()){
			try {
				omniBox.addDatabase(t, DatasManager.getController(t), DatabaseManager.getAllVisibleObjects(t));
				context.addTaskPane(DatasManager.getContext(t), t);
				AnalyzeUI.getContextPanel().addTaskPane(DatasManager.getContext(t), t);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		
		//mises à jour en cas de changement de base
		progressMonitor = new ProgressMonitor(this, "Mise à jour", "", 0, 7);
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
						DatabaseManager.Type type = (Type) evt.getNewValue();
						DatasManager.createDatas(type, wwd);
						dataExplorer.updateView(type);
						return null;
					}

					@Override
					protected void done() {
						Type type = (Type) evt.getNewValue();
						try {
							if(dataExplorer.getView(type) == null){
								omniBox.removeDatabase(type);
								context.removeTaskPane(type);
								AnalyzeUI.getContextPanel().removeTaskPane(type);
							} else {
								context.addTaskPane(DatasManager.getContext(type), type);
								AnalyzeUI.getContextPanel().addTaskPane(DatasManager.getContext(type), type);
								omniBox.addDatabase(type, dataExplorer.getView(type).getController(), DatabaseManager.getAllVisibleObjects(type));
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
						progressMonitor.close();
					}
				}.execute();
			}
		});
		
		//Barre d'actions
		this.add(this.createToolBar(), BorderLayout.PAGE_START);

		//Barre de statut
		this.add(this.createStatusBar(), BorderLayout.SOUTH);
		
		//suppression du splashscreen et affichage de la fenêtre
		splashScreen.dispose();
		this.pack();
		//FullScreen
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setVisible(true);
		
		//ferme le panneau d'informations, doit être fait après l'affichage de la fenêtre
		splitPane.setDividerLocation(1.0);
		
		firePropertyChange("done", null, true);
	}

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
				splitPane.setDividerLocation(splitPane.getMaximumDividerLocation());
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
			}
		});
		toolbar.add(analyze);
		
		//Screenshot
		JButton snapshot = new JButton(new ImageIcon(getClass().getResource("/resources/snapshot.png")));
		snapshot.setToolTipText("Enregistrer la vue 3D.");
		snapshot.addActionListener(new ScreenShotAction(wwd));
		toolbar.add(snapshot);
		
		toolbar.addSeparator();
		
		//ajout de dalles
		JButton dalle = new JButton(new ImageIcon(getClass().getResource("/resources/add_geotiff_22.png")));
		dalle.setToolTipText("Importer des images géoréférencées.");
		dalle.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				VFileChooser file = new VFileChooser();
				file.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if(file.showOpenDialog(null) == VFileChooser.APPROVE_OPTION){
					wwd.importImage(file.getSelectedFile());
				}
			}
		});
		toolbar.add(dalle);
		
		//Ajouter trajectoires
		final DropDownButton trajectoires = new DropDownButton(new ImageIcon(getClass().getResource("/resources/plus_traj_22.png")));
		
		final JMenuItem file = new JMenuItem("Fichier");
		file.setToolTipText("Importer des trajectoires dans un fichier");
		file.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final VFileChooser fileChooser = new VFileChooser();
				fileChooser.setFileSelectionMode(VFileChooser.FILES_ONLY);
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.addChoosableFileFilter(new OPASFileFilter());
				fileChooser.addChoosableFileFilter(new LPLNFileFilter());
				fileChooser.addChoosableFileFilter(new GEOFileFilter());
				fileChooser.addChoosableFileFilter(new FPLFileFilter());
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
		
		
		JMenuItem text = new JMenuItem("Texte");
		text.setToolTipText("Importer des trajectoires par copier/coller");
		text.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new FPLImportUI(dataExplorer).setVisible(true);
			}
		});
		trajectoires.getPopupMenu().add(file);
		trajectoires.getPopupMenu().add(text);
		
		trajectoires.setToolTipText("Importer des trajectoires");
		trajectoires.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				file.doClick();
			}
		});
		trajectoires.addToToolBar(toolbar);
		//toolbar.add(trajectoires);

		final DropDownButton addAirspace = new DropDownButton(new ImageIcon(getClass().getResource("/resources/draw-polygon_22_1.png")));
		
		
		final JMenuItem addPolygon = new JMenuItem("Nouveau");
		addPolygon.setToolTipText("Nouveau polygone");
		addPolygon.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
				 Polygon polygon = new Polygon();
		         polygon.setAltitudes(0.0, 0.0);
		         polygon.setTerrainConforming(true, false);
				
				Position position = ShapeUtils.getNewShapePosition(wwd);
		        Angle heading = ShapeUtils.getNewShapeHeading(wwd, true);
		        double sizeInMeters = ShapeUtils.getViewportScaleFactor(wwd);

		        java.util.List<LatLon> locations = ShapeUtils.createSquareInViewport(wwd, position, heading, sizeInMeters);

		        double maxElevation = -Double.MAX_VALUE;
		        Globe globe = wwd.getModel().getGlobe();

		        for (LatLon ll : locations)
		        {
		            double e = globe.getElevation(ll.getLatitude(), ll.getLongitude());
		            if (e > maxElevation)
		                maxElevation = e;
		        }

		        polygon.setAltitudes(0.0, maxElevation + sizeInMeters);
		        polygon.setTerrainConforming(true, false);
		        polygon.setLocations(locations);
				
		        wwd.editAirspace(polygon, true);
			}
		});
        
		JMenuItem addFromFile = new JMenuItem("Charger un fichier");
		addFromFile.setToolTipText("Nouveau polygone depuis un fichier");
		addFromFile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				VFileChooser fileChooser = new VFileChooser();
				fileChooser.setMultiSelectionEnabled(false);
				if(fileChooser.showOpenDialog(null) == VFileChooser.APPROVE_OPTION){
					File file = fileChooser.getSelectedFile();
					//TODO prendre en charge d'autres formes
					Polygon p = new Polygon();
					try {
						BufferedReader input = new BufferedReader(new FileReader(file));
						String s = input.readLine();
						p.restoreState(s);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					wwd.editAirspace(p, true);
				}
			}
		});
		
		addAirspace.getPopupMenu().add(addPolygon);
		addAirspace.getPopupMenu().add(addFromFile);
		addAirspace.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				addPolygon.doClick();
			}
		});
		//toolbar.add(addPolygon);
		addAirspace.addToToolBar(toolbar);
		
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

		//échelle verticale
		
		final JToggleButton verticalScaleBar = new JToggleButton(new ImageIcon(getClass().getResource("/resources/scale_22_2.png")));
		verticalScaleBar.setToolTipText("Afficher une échelle verticale");
		verticalScaleBar.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				wwd.activateVerticalScaleBar(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		toolbar.add(verticalScaleBar);
	
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
		toolbar.addSeparator();
		
		
		//recherche avec autocomplétion
		omniBox.addToToolbar(toolbar);

		toolbar.addSeparator();
	
		JButton aide = new JButton(new ImageIcon(getClass().getResource("/resources/bullet_about_22.png")));
		aide.setToolTipText("A propos...");
		aide.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JDialog help = new JDialog();
				help.setTitle("A propos ...");
				help.setModal(true);
				help.add(new TitledPanel("ViDeso 3D "+Videso3D.VERSION), BorderLayout.NORTH);
				JEditorPane text = new JEditorPane("text/html", "<p align=center><b>Auteurs</b><br />" +
						"Bruno Spyckerelle<br />" +
						"Adrien Vidal<br />" +
						"Mickael Papail<br />" +
						"<br />" +
						"<b>Liens</b><br />" +
				"<a href=\"http://code.google.com/p/videso3d/wiki/Home?tm=6\">Aide en ligne</a><br />" +
				"<a href=\"http://code.google.com/p/videso3d/issues/list\">Signaler un bug</a><br /></p>");
				text.setEditable(false);
				text.setOpaque(false);
				text.addHyperlinkListener(new HyperlinkListener() {

					@Override
					public void hyperlinkUpdate(HyperlinkEvent evt) {
						if(evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED && Desktop.isDesktopSupported()){
							final Desktop dt = Desktop.getDesktop();
							if ( dt.isSupported( Desktop.Action.BROWSE ) ){	
								try {
									dt.browse( evt.getURL().toURI() );
								} catch (IOException e) {
									e.printStackTrace();
								} catch (URISyntaxException e) {
									e.printStackTrace();
								}
							}
						}
					}
				});
				//text.setHorizontalAlignment(JLabel.CENTER);
				help.add(text);
				help.setPreferredSize(new Dimension(400, 240));
				help.pack();
				Toolkit tk = help.getToolkit();
				int x = (tk.getScreenSize().width - help.getWidth())/2;
				int y = (tk.getScreenSize().height - help.getHeight())/2;
				help.setLocation(x, y);
				help.setVisible(true);
			}
		});
		
		toolbar.add(aide);
		return toolbar;
	}

	public DataExplorer getDataExplorer(){
		return this.dataExplorer;
	}
}
