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
import java.awt.Color;
import java.awt.Component;
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
import java.util.Vector;
import java.util.zip.ZipException;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jdesktop.swingx.plaf.nimbus.NimbusMultiSliderUI;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.event.CDockableStateListener;
import bibliothek.gui.dock.common.group.CGroupBehavior;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.common.theme.ThemeMap;

import fr.crnan.videso3d.AirspaceListener;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.Configuration;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.ProjectManager;
import fr.crnan.videso3d.SplashScreen;
import fr.crnan.videso3d.Videso3D;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.formats.geo.GEOFileFilter;
import fr.crnan.videso3d.formats.geo.GEOReader;
import fr.crnan.videso3d.formats.lpln.LPLNFileFilter;
import fr.crnan.videso3d.formats.lpln.LPLNReader;
import fr.crnan.videso3d.formats.opas.OPASFileFilter;
import fr.crnan.videso3d.formats.opas.OPASReader;
import fr.crnan.videso3d.formats.fpl.FPLFileFilter;
import fr.crnan.videso3d.formats.fpl.FPLReader;
import fr.crnan.videso3d.globes.FlatGlobeCautra;
import fr.crnan.videso3d.graphics.VPolygon;
import fr.crnan.videso3d.graphics.editor.PolygonEditorsManager;
import fr.crnan.videso3d.ihm.components.AltitudeRangeSlider;
import fr.crnan.videso3d.ihm.components.ClosableSingleDockable;
import fr.crnan.videso3d.ihm.components.DataView;
import fr.crnan.videso3d.ihm.components.DropDownButton;
import fr.crnan.videso3d.ihm.components.DropDownToggleButton;
import fr.crnan.videso3d.ihm.components.Omnibox;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import fr.crnan.videso3d.ihm.components.VFileChooser;
import fr.crnan.videso3d.layers.GEOTracksLayer;
import fr.crnan.videso3d.layers.OPASTracksLayer;
import fr.crnan.videso3d.layers.TrajectoriesLayer;
import fr.crnan.videso3d.stip.PointNotFoundException;
import fr.crnan.videso3d.util.VidesoStatusBar;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.examples.util.ScreenShotAction;
import gov.nasa.worldwind.examples.util.ShapeUtils;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.util.Logging;

/**
 * Fenêtre principale
 * @author Bruno Spyckerelle
 * @version 0.3.9
 */
@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	/**
	 * NASA WorldWind
	 */
	private VidesoGLCanvas wwd;

	/**
	 * Panel contextuel
	 */
	private ContextPanel context;
	/**
	 * OmniBox
	 */
	private Omnibox omniBox;

	/**
	 * Dock Control
	 */
	private CControl control;
	private CLocation locationDatas;
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

		//compter le nombre d'étapes d'init
		Integer temp = 0;
		temp += wwd.getNumberInitSteps();
		for(Type t : DatabaseManager.getSelectedDatabases()) {
			temp += DatasManager.getNumberInitSteps(t);
		}
		temp++;
		final int numberInitSteps = temp;

		wwd.addPropertyChangeListener("step", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				splashScreen.setStatus((String) evt.getNewValue(), (step*100)/(numberInitSteps) );
				Logging.logger().info(evt.getNewValue()+" "+(step*100)/(numberInitSteps));
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
					wwd.firePropertyChange("step", "", "Création de l'interface");
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
				
		control = new CControl(this);
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		control.setGroupBehavior(CGroupBehavior.TOPMOST);
		this.add(control.getContentArea(), BorderLayout.CENTER);
		
		DefaultSingleCDockable dockableDatas = new DefaultSingleCDockable("dataExplorer");
		DefaultSingleCDockable dockableWWD = new DefaultSingleCDockable("wwd");
		DefaultSingleCDockable dockableContext = new DefaultSingleCDockable("context");
		
		CGrid grid = new CGrid(control);
		grid.add(0, 0, 1, 1, dockableDatas);
		grid.add(1, 0, 3, 1, dockableWWD);
		grid.add(3, 0, 1.2, 1, dockableContext);
		control.getContentArea().deploy(grid);
		
		AltitudeRangeSlider rangeSlider = new AltitudeRangeSlider(wwd);
		rangeSlider.setUI(new NimbusMultiSliderUI(rangeSlider));
		JPanel wwdContainer = new JPanel(new BorderLayout());
		wwdContainer.setMinimumSize(new Dimension(600, 600));
		wwdContainer.add(rangeSlider, BorderLayout.WEST);
		wwdContainer.add(wwd, BorderLayout.CENTER);
		dockableWWD.setTitleShown(false);
		dockableWWD.add(wwdContainer);
		
		dockableDatas.add(new JPanel());
		
		dockableContext.add(context);
		dockableContext.setTitleText("Informations");
		dockableContext.setDefaultLocation(ExtendedMode.MINIMIZED, CLocation.base().minimalEast());
		dockableContext.setExtendedMode(ExtendedMode.MINIMIZED);
		dockableContext.setCloseable(false);
		context.setDockable(dockableContext);
		
		locationDatas = /*CLocation.base().normalWest(0.2);*/dockableDatas.getBaseLocation().aside();
		
		for(Type type : DatabaseManager.getSelectedDatabases()){
			this.updateDockables(type, false);
		}
		
		control.removeDockable(dockableDatas);
		
		
		airspaceListener = new AirspaceListener(wwd, context);
		wwd.addSelectListener(airspaceListener);


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
		progressMonitor = new ProgressMonitor(this, "Mise à jour", "", 0, 7, true, false);
		wwd.addPropertyChangeListener("step", new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				progressMonitor.setNote(evt.getNewValue().toString());
			}
		});

		
		//Mise à jour quand une base est déselectionnée ou supprimée
		DatabaseManager.addPropertyChangeListener(DatabaseManager.BASE_UNSELECTED, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Type type = (Type) evt.getNewValue();
				control.removeSingleDockable(type.toString());
				DatasManager.deleteDatas(type);
				omniBox.removeDatabase(type);
				context.removeTaskPane(type);
				AnalyzeUI.getContextPanel().removeTaskPane(type);
			}
			
		});
		
		//Mise à jour quand une base est selectionnée ou créée
		DatabaseManager.addPropertyChangeListener(DatabaseManager.BASE_SELECTED, new PropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
												
				//précréation des éléments 3D dans un SwingWorker avec ProgressMonitor
				new SwingWorker<Integer, Integer>(){

					private boolean empty = false;
					
					@Override
					protected Integer doInBackground() throws Exception {
						progressMonitor.setProgress(0);
						try { 
							progressMonitor.setNote(evt.getNewValue().toString());
						} catch (Exception e){
							e.printStackTrace();
						}
						DatabaseManager.Type type = (Type) evt.getNewValue();
						empty = DatasManager.numberViews() == 0;
						DatasManager.createDatas(type, wwd);
						return null;
					}

					@Override
					protected void done() {
						Type type = (Type) evt.getNewValue();
						try {
							if(DatasManager.getView(type) != null){
								updateDockables(type, empty);
								context.addTaskPane(DatasManager.getContext(type), type);
								AnalyzeUI.getContextPanel().addTaskPane(DatasManager.getContext(type), type);
								omniBox.addDatabase(type, DatasManager.getView(type).getController(), DatabaseManager.getAllVisibleObjects(type));
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
		
		firePropertyChange("done", null, true);
	}

	/**
	 * 
	 * @param type Type de la base de données
	 * @param empty Vrai si il n'y a plus de tabs
	 */
	private void updateDockables(Type type, boolean empty){
		this.control.removeSingleDockable(type.toString());
		ClosableSingleDockable dockable = new ClosableSingleDockable(type.toString());
		dockable.addCloseAction(control);
				
		dockable.setType(type);
		dockable.setTitleText(type.toString());
		if(empty){
			locationDatas = CLocation.base().normalWest(0.2);
		} 
		dockable.setDefaultLocation(ExtendedMode.MINIMIZED, CLocation.base().minimalWest());
		dockable.setLocation(locationDatas);
		dockable.add((Component) DatasManager.getView(type));
		control.addDockable(dockable);
		dockable.setVisible(true);
		

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
				for(DataView view : DatasManager.getViews()){
					view.reset();
				}
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
		final JButton analyze = new JButton(new ImageIcon(getClass().getResource("/resources/datas_analyze_22.png")));
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
		
		final JButton loadProject = new JButton(new ImageIcon(getClass().getResource("/resources/load_project_22.png")));
		loadProject.setToolTipText("Charger un projet");
		loadProject.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				VFileChooser fileChooser = new VFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if(fileChooser.showOpenDialog(loadProject) == JFileChooser.APPROVE_OPTION){
					try {
						ProjectManager.loadProject(fileChooser.getSelectedFile());
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		toolbar.add(loadProject);
		
		final JButton saveProject = new JButton(new ImageIcon(getClass().getResource("/resources/save_project_22.png")));
		saveProject.setToolTipText("Enregistrer le projet");
		saveProject.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				VFileChooser fileChooser = new VFileChooser();
				if(fileChooser.showSaveDialog(saveProject) == JFileChooser.APPROVE_OPTION){
					final File file = fileChooser.getSelectedFile();
					if(!(file.exists()) || 
							(file.exists() &&
									JOptionPane.showConfirmDialog(null, "Le fichier existe déjà.\n\nSouhaitez-vous réellement l'écraser ?",
											"Confirmer la suppression du fichier précédent",
											JOptionPane.OK_CANCEL_OPTION,
											JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)) {
						try {
							ProjectManager.saveProject(file);
						} catch(ZipException e) {
							JOptionPane.showMessageDialog(null, "Aucun fichier projet sauvé, vérifiez qu'il y a bien des objets à sauver.",
									"Impossible de créer un fichier projet", JOptionPane.ERROR_MESSAGE);
							Logging.logger().warning("Impossible de créer un fichier projet");
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		
		toolbar.add(saveProject);
		toolbar.addSeparator();
		//ajout d'images
		final DropDownButton images = new DropDownButton(new ImageIcon(getClass().getResource("/resources/add_geotiff_22.png")));
		images.setToolTipText("Ajouter une image");
		
		final JMenuItem dalle = new JMenuItem("Ajout permanent");
		dalle.setToolTipText("Importer des images géoréférencées (GeoTiff, ...) de manière permanente");
		dalle.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				VFileChooser file = new VFileChooser();
				file.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if(file.showOpenDialog(null) == VFileChooser.APPROVE_OPTION){
					wwd.getImagesController().importImage(file.getSelectedFile());
				}
			}
		});
		
		images.getPopupMenu().add(dalle);
		
		final JMenuItem image = new JMenuItem("Ajout temporaire");
		image.setToolTipText("Importer une image de manière temporaire");
		
		image.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				VFileChooser file = new VFileChooser();
				file.setFileSelectionMode(JFileChooser.FILES_ONLY);
				file.setMultiSelectionEnabled(true);
				file.addChoosableFileFilter(
						new FileNameExtensionFilter("Images", ImageIO.getReaderFormatNames()));
				if(file.showOpenDialog(null) == VFileChooser.APPROVE_OPTION){
					wwd.getImagesController().addEditableImages(file.getSelectedFiles());
				}
			}
		});
		
		images.getPopupMenu().add(image);
		images.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				image.doClick();
			}
		});
		images.addToToolBar(toolbar);
		
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
								addTrajectoriesViews(fileChooser.getSelectedFiles());
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
				new FPLImportUI(getThis()).setVisible(true);
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

		final DropDownButton addAirspace = new DropDownButton(new ImageIcon(getClass().getResource("/resources/draw-polygon_22_1.png")));
		
		
		final JMenuItem addPolygon = new JMenuItem("Nouveau");
		addPolygon.setToolTipText("Nouveau polygone");
		addPolygon.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
				 VPolygon polygon = new VPolygon();
		         polygon.setAltitudes(0.0, 0.0);
		         polygon.setTerrainConforming(true, false);
		         BasicAirspaceAttributes attrs = new BasicAirspaceAttributes();
		         attrs.setDrawOutline(true);
		         attrs.setMaterial(new Material(Color.CYAN));
		         attrs.setOutlineMaterial(new Material(Pallet.makeBrighter(Color.CYAN)));
		         attrs.setOpacity(0.2);
		         attrs.setOutlineOpacity(0.9);
		         attrs.setOutlineWidth(1.5);
		         polygon.setAttributes(attrs);
				
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
				
		        PolygonEditorsManager.editAirspace(polygon, true);
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
					BasicAirspaceAttributes attrs = new BasicAirspaceAttributes();
					attrs.setDrawOutline(true);
					attrs.setMaterial(new Material(Color.CYAN));
					attrs.setOutlineMaterial(new Material(Pallet.makeBrighter(Color.CYAN)));
					attrs.setOpacity(0.2);
					attrs.setOutlineOpacity(0.9);
					attrs.setOutlineWidth(1.5);
					VPolygon p = new VPolygon();
					p.setAttributes(attrs);
					try {
						BufferedReader input = new BufferedReader(new FileReader(file));
						String s = input.readLine();
						p.restoreState(s);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					PolygonEditorsManager.editAirspace(p, true);
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
		final DropDownToggleButton fond = new DropDownToggleButton();
		fond.setText("Fond");
		final ButtonGroup territoire = new ButtonGroup();

		final JRadioButtonMenuItem france = new JRadioButtonMenuItem("France");
		france.setSelected(true);
		france.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				wwd.setFrontieresEurope(false);
				fond.setSelected(e.getStateChange() == ItemEvent.SELECTED);
				wwd.toggleFrontieres(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		territoire.add(france);

		final JRadioButtonMenuItem europe = new JRadioButtonMenuItem("Europe");
		europe.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				wwd.setFrontieresEurope(true);
				fond.setSelected(e.getStateChange() == ItemEvent.SELECTED);
				wwd.toggleFrontieres(e.getStateChange() == ItemEvent.SELECTED);

			}
		});
		territoire.add(europe);

		
		fond.getPopupMenu().add(france);
		fond.getPopupMenu().add(europe);
	
		fond.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				wwd.setFrontieresEurope(europe.isSelected());
				wwd.toggleFrontieres(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		fond.addToToolBar(toolbar);

		
		
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
		
		JLabel label = new JLabel("Échelle verticale : ");
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
		labelTable.put(1, new JLabel("x1"));
		labelTable.put(2, new JLabel("x2"));
		labelTable.put(4, new JLabel("x4"));
		labelTable.put(8, new JLabel("x8"));
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
				wwd.getSceneController().setVerticalExaggeration(ve == 1.0 ? 1.01 : ve);
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
	/**
	 * Ajoute un tab de sélection des trajectoires.<br />
	 * Un tab est créé pour chaque type de fichier si plusieurs fichiers sont sélectionnés.
	 * @param file
	 * @throws Exception 
	 */
	public void addTrajectoriesViews(File[] files){
		Vector<File> opasFile = new Vector<File>();
		Vector<File> geoFile = new Vector<File>();
		Vector<File> lplnFile = new Vector<File>();
		Vector<File> fplFile = new Vector<File>();
		for(File f : files){
			if(OPASReader.isOpasFile(f)) {
				opasFile.add(f);
			} else if(LPLNReader.isLPLNFile(f)) {
				lplnFile.add(f);
			} else if(GEOReader.isGeoFile(f)) {
				geoFile.add(f);
			} else if (FPLReader.isFPLFile(f)){
				fplFile.add(f);
			}
		}
		
		if(opasFile.size()>0){
			try{
				OPASTracksLayer layer = new OPASTracksLayer();
				layer.setPrecision(Double.parseDouble(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_PRECISION, "0.01")));
				this.wwd.toggleLayer(layer, true);
				//lecture et création des tracks à la volée
				OPASReader reader = new OPASReader(opasFile, layer);
				//changement du style en fonction de la conf
				if(reader.getTracks().size()< Integer.parseInt(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_SEUIL, "20"))){
					layer.setStyle(TrajectoriesLayer.STYLE_CURTAIN);
				}
				layer.setName(reader.getName());
				this.addTrajectoriesView(reader);
			} catch (PointNotFoundException e) {
				e.printStackTrace();
			}
		}
		if(geoFile.size()>0){
			try{
				GEOTracksLayer layer = new GEOTracksLayer();
				layer.setPrecision(Double.parseDouble(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_PRECISION, "0.01")));
				this.wwd.toggleLayer(layer, true);
				//lecture et création des tracks à la volée
				GEOReader reader = new GEOReader(geoFile, layer);
				//changement du style en fonction de la conf
				if(reader.getTracks().size()< Integer.parseInt(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_SEUIL, "20"))){
					layer.setStyle(TrajectoriesLayer.STYLE_CURTAIN);
				}
				layer.setName(reader.getName());
				this.addTrajectoriesView(reader);
			} catch (PointNotFoundException e) {
				e.printStackTrace();
			}
		}
		if(lplnFile.size()>0){
			try {
				this.addTrajectoriesView(new LPLNReader(lplnFile));
			} catch (PointNotFoundException e) {
				Logging.logger().warning("Point non trouvé : "+e.getName());
				JOptionPane.showMessageDialog(null, "<html><b>Problème :</b><br />Impossible de trouver certains points du fichier.<br /><br />" +
						"<b>Solution :</b><br />Vérifiez qu'une base de données STIP existe et qu'elle est cohérente avec le fichier de trajectoires.</html>",
						"Erreur", JOptionPane.ERROR_MESSAGE);
			}
		}
		if(fplFile.size()>0){
			try {
				FPLReader fplR = new FPLReader(fplFile);
				String msgErreur = fplR.getErrorMessage();
				if(!msgErreur.isEmpty())
					JOptionPane.showMessageDialog(null, msgErreur, "Erreur lors de la lecture du plan de vol", JOptionPane.ERROR_MESSAGE);
				if(fplR.getTracks().size()>0)
					this.addTrajectoriesView(fplR);
			} catch (PointNotFoundException e) {
				e.printStackTrace();
			}
		}
		if(opasFile.size() == 0 && geoFile.size() == 0 && lplnFile.size() == 0 && fplFile.size()==0){
			Logging.logger().warning("Aucun fichier trajectoire trouvé.");
			JOptionPane.showMessageDialog(null, "<html><b>Problème :</b><br />Aucun fichier trajectoire trouvé.<br /><br />" +
					"<b>Solution :</b><br />Vérifiez que les fichiers sélectionnés sont bien dans un format pris en compte.</html>", "Erreur", JOptionPane.ERROR_MESSAGE);
		}
	
	}
	
	public void addTrajectoriesView(final TrackFilesReader reader){
		final Component content = new TrajectoriesView(wwd, reader, context);
		DefaultSingleCDockable dockable = new DefaultSingleCDockable(reader.getName());
		dockable.setTitleText(reader.getName());

		dockable.setLocation(locationDatas);
		dockable.setCloseable(true);
		dockable.add(content);
		control.addDockable(dockable);
		dockable.setVisible(true);
		
		dockable.addCDockableStateListener(new CDockableStateListener() {
			
			@Override
			public void visibilityChanged(CDockable dockable) {
				wwd.removeLayer(reader.getLayer());
			}
			
			@Override
			public void extendedModeChanged(CDockable dockable, ExtendedMode mode) {}
		});
	}
	
	private MainWindow getThis(){
		return this;
	}
}
