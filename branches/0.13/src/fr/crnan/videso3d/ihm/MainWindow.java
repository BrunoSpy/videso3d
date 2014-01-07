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
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;

import org.jdesktop.swingx.plaf.nimbus.NimbusMultiSliderUI;

import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.action.predefined.CCloseAction;
import bibliothek.gui.dock.common.group.CGroupBehavior;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.common.theme.ThemeMap;

import fr.crnan.videso3d.AirspaceListener;
import fr.crnan.videso3d.ProgressSupport;
import fr.crnan.videso3d.CompatibilityVersionException;
import fr.crnan.videso3d.Configuration;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.FileManager;
import fr.crnan.videso3d.SplashScreen;
import fr.crnan.videso3d.Videso3D;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.stip.PointNotFoundException;
import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.formats.geo.GEOReader;
import fr.crnan.videso3d.formats.lpln.LPLNReader;
import fr.crnan.videso3d.formats.opas.OPASReader;
import fr.crnan.videso3d.formats.plns.PLNSReader;
import fr.crnan.videso3d.formats.fpl.FPLReader;
import fr.crnan.videso3d.ihm.components.AltitudeRangeSlider;
import fr.crnan.videso3d.ihm.components.Omnibox;
import fr.crnan.videso3d.ihm.components.VDefaultEclipseThemConnector;
import fr.crnan.videso3d.ihm.components.VFileChooser;
import fr.crnan.videso3d.layers.tracks.FPLTracksLayer;
import fr.crnan.videso3d.layers.tracks.GEOTracksLayer;
import fr.crnan.videso3d.layers.tracks.LPLNTracksLayer;
import fr.crnan.videso3d.layers.tracks.OPASTracksLayer;
import fr.crnan.videso3d.layers.tracks.PLNSTracksLayer;
import fr.crnan.videso3d.layers.tracks.TrajectoriesLayer;
import fr.crnan.videso3d.project.Project;
import fr.crnan.videso3d.project.ProjectManager;
import fr.crnan.videso3d.trajectography.PLNSTracksModel;
import fr.crnan.videso3d.trajectography.TracksModel;
import fr.crnan.videso3d.trajectography.TrajectoryFileFilter;
import fr.crnan.videso3d.util.VidesoStatusBar;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.util.Logging;

/**
 * Fenêtre principale
 * @author Bruno Spyckerelle
 * @version 0.3.14
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


	private JToolBar drawToolbar;

	private JPanel toolbars;
	
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
		//suppression des dossiers temporaires
		//et enregistrement de la session
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				DatabaseManager.closeAll();
				FileManager.removeTempFiles();
				
				//suppression de la dernière session
				if(new File(Configuration.SESSION_FILENAME).exists()){
					new File(Configuration.SESSION_FILENAME).delete();
				}
				ProjectManager project = new ProjectManager();
				project.prepareSaving(wwd);
				Set<String> types = new HashSet<String>();
				for(DatasManager.Type type : project.getTypes()){
					types.add(type.toString());
				}
				try {
					project.saveProject(new File(Configuration.SESSION_FILENAME), types, null, null, false, true);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
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
		for(DatasManager.Type t : DatabaseManager.getSelectedDatabases()) {
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
					for(DatasManager.Type t : DatabaseManager.getSelectedDatabases()) {
						DatasManager.createDatas(t, wwd);
					}
					omniBox = new Omnibox(wwd, context);
					for(DatasManager.Type t : DatabaseManager.getSelectedDatabases()){
						omniBox.addDatabase(t, DatabaseManager.getAllVisibleObjects(t, omniBox), false);
					}
					//chargement de la session précédente si elle existe
					if(new File(Configuration.SESSION_FILENAME).exists()){
						//don't add it to the userobjectView as all objects
						//are linked to a database
						new ProjectManager().loadProject(new File(Configuration.SESSION_FILENAME), wwd, MainWindow.this, false);
					}
					wwd.firePropertyChange("step", "", "Création de l'interface");
				} catch (Exception e) {
					Logging.logger().severe(e.getMessage());
				}
				return null;
			}
			@Override
			protected void done(){
				omniBox.update();
				//une fois terminé, on lance l'application
				try {
					launchVideso3D();
				} catch (Exception e) {
					Logging.logger().severe(e.getMessage());
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
		control.getThemes().select(ThemeMap.KEY_ECLIPSE_THEME);
		control.putProperty(EclipseTheme.THEME_CONNECTOR, new VDefaultEclipseThemConnector());
		
		control.setGroupBehavior(CGroupBehavior.TOPMOST);
		control.getContentArea().setBorder(null);
		this.add(control.getContentArea(), BorderLayout.CENTER);

		DefaultSingleCDockable dockableDatas = new DefaultSingleCDockable("dataExplorer");
		DefaultSingleCDockable dockableWWD = new DefaultSingleCDockable("wwd");
		DefaultSingleCDockable dockableContext = new DefaultSingleCDockable("context");
		
		AltitudeRangeSlider rangeSlider = new AltitudeRangeSlider(wwd);
		rangeSlider.setUI(new NimbusMultiSliderUI(rangeSlider));
		JPanel wwdContainer = new JPanel(new BorderLayout());
		wwdContainer.setMinimumSize(new Dimension(600, 600));
		wwdContainer.add(rangeSlider, BorderLayout.WEST);
		wwdContainer.add(wwd, BorderLayout.CENTER);
		dockableWWD.setTitleShown(false);
		dockableWWD.add(wwdContainer);

		//fill dockableDatas with a fake panel to allow a correct layout
		dockableDatas.add(new JPanel());
		
		dockableContext.add(context);
		dockableContext.setTitleText("Informations");
		dockableContext.setDefaultLocation(ExtendedMode.MINIMIZED, CLocation.base().minimalEast());
		dockableContext.setCloseable(false);
		context.setDockable(dockableContext);
		
		//layout of the elements		
		CGrid grid = new CGrid(control);
		grid.add(0, 0, 1, 1, dockableDatas);
		grid.add(1, 0, 3, 1, dockableWWD);
		grid.add(3, 0, 1.2, 1, dockableContext);
		control.getContentArea().deploy(grid);
		//minimizing context
		dockableContext.setExtendedMode(ExtendedMode.MINIMIZED);

		//save location of future panel
		locationDatas = dockableDatas.getBaseLocation();

		for(DatasManager.Type type : DatabaseManager.getSelectedDatabases()){
			this.updateDockables(type);
		}

		control.removeDockable(dockableDatas);

		airspaceListener = new AirspaceListener(wwd, context);
		wwd.addSelectListener(airspaceListener);

		//initialisation contextpanel
		for(DatasManager.Type t : DatabaseManager.getSelectedDatabases()){	
			context.addTaskPane(DatasManager.getContext(t), t);
			AnalyzeUI.getContextPanel().addTaskPane(DatasManager.getContext(t), t);	
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
				DatasManager.Type type = (DatasManager.Type) evt.getNewValue();
				control.removeSingleDockable(type.toString());
				DatasManager.deleteDatas(type);
				omniBox.removeDatabase(type);
				context.removeTaskPane(type);
				AnalyzeUI.getContextPanel().removeTaskPane(type);
				AnalyzeUI.updateSearchBoxes();
			}
			
		});
		
		//Mise à jour quand une base est selectionnée ou créée
		DatabaseManager.addPropertyChangeListener(DatabaseManager.BASE_SELECTED, new PropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
													
				//précréation des éléments 3D dans un SwingWorker avec ProgressMonitor
				new SwingWorker<Integer, Integer>(){
					
					@Override
					protected Integer doInBackground() throws Exception {
						progressMonitor.setProgress(0);
						try { 
							progressMonitor.setNote(evt.getNewValue().toString());

							DatasManager.Type type = (DatasManager.Type) evt.getNewValue();
							DatasManager.createDatas(type, wwd);
							omniBox.addDatabase(type, DatabaseManager.getAllVisibleObjects(type, omniBox), true);
						} catch (Exception e){
							Logging.logger().severe(e.getMessage());
						}
						return null;
					}

					@Override
					protected void done() {
						try{
						DatasManager.Type type = (DatasManager.Type) evt.getNewValue();
							if(DatasManager.getView(type) != null){
								updateDockables(type);
								context.addTaskPane(DatasManager.getContext(type), type);
								AnalyzeUI.getContextPanel().addTaskPane(DatasManager.getContext(type), type);
								AnalyzeUI.updateSearchBoxes();
							}
						progressMonitor.close();
						} catch(Exception e){
							Logging.logger().severe(e.getMessage());
						}
					}
				}.execute();
			}
		});
		
		//listen to the datasmanager to detect creation of a user object view
		DatasManager.addPropertyChangeListener("new datas", new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				DatasManager.Type type = (DatasManager.Type) evt.getNewValue();
				if(type.equals(DatasManager.Type.UserObject)){
					MainWindow.this.setUserObjectViewVisible();
				}
			}
		});
		
		//Barre d'actions
		toolbars = new JPanel();
		toolbars.setLayout(new BoxLayout(toolbars, BoxLayout.Y_AXIS));
		toolbars.add(new MainToolbar(this, wwd, omniBox));
		this.add(toolbars, BorderLayout.PAGE_START);
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
	private void updateDockables(final DatasManager.Type type){
		this.control.removeSingleDockable(type.toString());
		DefaultSingleCDockable dockable = new DefaultSingleCDockable(type.toString(), type.toString(), (Component) DatasManager.getView(type),
				new CCloseAction(control){

			@Override
			public void close(CDockable dockable) {
				super.close(dockable);
				try {
					DatabaseManager.unselectDatabase(type);
				} catch (SQLException e) {
					Logging.logger().severe(e.getMessage());
				}
			}

		});
		
		this.addDockable(dockable);
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
	 * Get the UserObjectView and create it if needed
	 */
	public void setUserObjectViewVisible(){
		if(this.control.getSingleDockable(DatasManager.Type.UserObject.toString()) == null){
			if(DatasManager.getView(DatasManager.Type.UserObject) == null){
				try {
					DatasManager.createDatas(DatasManager.Type.UserObject, wwd);
				} catch (Exception e) {
					Logging.logger().severe(e.getMessage());
				}
			}
			DefaultSingleCDockable dockable = new DefaultSingleCDockable(DatasManager.Type.UserObject.toString(), DatasManager.Type.UserObject.toString(), 
					(Component) DatasManager.getView(DatasManager.Type.UserObject));
			dockable.setCloseable(false);
			this.addDockable(dockable);
		}
	}
	
	/* ******************************************************* */
	/* ***************** Gestion des trajectoires ************ */
	/* ******************************************************* */
	
	public void addTrajectoriesViews(File[] filesT){
		this.addTrajectoriesViews(filesT, null, true, true);
	}
	
	/**
	 * Ajoute un tab de sélection des trajectoires.<br />
	 * Un tab est créé pour chaque type de fichier si plusieurs fichiers sont sélectionnés.
	 * @param file
	 * @param filters Filters to be applied before importing
	 * @throws Exception 
	 */
	//nombre de fichiers importés
	int current = -1;
	public void addTrajectoriesViews(File[] filesT, final List<TrajectoryFileFilter> filters, final boolean disjunctive, final boolean importRapide){
		final ProgressMonitorCanceller progressMonitorT = new ProgressMonitorCanceller(this, "Import des trajectoires", "", 0, 100, false, true, false);
		progressMonitorT.setMillisToDecideToPopup(0);
		progressMonitorT.setMillisToPopup(0);
		progressMonitorT.setNote("Extraction des fichiers compressés...");
		
		List<File> files = FileManager.extractFilesIfNeeded(Arrays.asList(filesT));
		
		progressMonitorT.setNote("Détection des types de fichiers...");
		
		final Vector<File> opasFile = new Vector<File>();
		final Vector<File> geoFile = new Vector<File>();
		final Vector<File> lplnFile = new Vector<File>();
		final Vector<File> fplFile = new Vector<File>();
		final Vector<File> plnsFile = new Vector<File>();
		final Vector<File> sqlitePlnsFile = new Vector<File>();
		for(File f : files){
			if(OPASReader.isOpasFile(f)) {
				opasFile.add(f);
			} else if(LPLNReader.isLPLNFile(f)) {
				lplnFile.add(f);
			} else if(GEOReader.isGeoFile(f)) {
				geoFile.add(f);
			} else if (FPLReader.isFPLFile(f)){
				fplFile.add(f);
			} else if (PLNSReader.isPLNSFile(f)){
				plnsFile.add(f);
			} else if (PLNSReader.isSQLitePLNSFile(f)){
				sqlitePlnsFile.add(f);
			}
		}
		
		progressMonitorT.setNote("Import des trajectoires...");
		
		progressMonitorT.setMaximum(100*((opasFile.size() > 0 ? 1 : 0 )
										+(lplnFile.size() > 0 ? 1 : 0 )
										+(geoFile.size() > 0 ? 1 : 0 )
										+(fplFile.size() > 0 ? 1 : 0 )
										+(plnsFile.size() > 0 ? 1 : 0 )
										+(sqlitePlnsFile.size() > 0 ? 1 : 0 )));
		
		final PropertyChangeListener readerListener = new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {

				if(evt.getPropertyName().equals(ProgressSupport.TASK_STARTS)){
					current++;
				} else if(evt.getPropertyName().equals(ProgressSupport.TASK_PROGRESS)){
					progressMonitorT.setProgress(current*100+(Integer) evt.getNewValue());
				} else if(evt.getPropertyName().equals(ProgressSupport.TASK_INFO)){
					progressMonitorT.setNote((String) evt.getNewValue());
				}
			}
		};

		new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {

				if(opasFile.size()>0){
					try{
						OPASTracksLayer layer = new OPASTracksLayer(new TracksModel());
						layer.setPrecision(Double.parseDouble(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_PRECISION, "0.01")));
						MainWindow.this.wwd.toggleLayer(layer, true);
						//lecture et création des tracks à la volée
						OPASReader reader = new OPASReader(opasFile, layer.getModel());
						reader.addPropertyChangeListener(readerListener);
						progressMonitorT.setCancelable(reader);
						reader.doRead();
						if(reader.getModel().getAllTracks().size() > 0 && !reader.isCanceled()){
							//changement du style en fonction de la conf
							if(reader.getModel().getAllTracks().size()< Integer.parseInt(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_SEUIL, "20"))){
								layer.setStyle(TrajectoriesLayer.STYLE_CURTAIN);
							}
							layer.setName(reader.getName());
							MainWindow.this.addTrajectoriesView(reader, layer);
						} else {
							//aucune trajectoire trouvée dans les fichiers
							opasFile.clear();
							MainWindow.this.wwd.removeLayer(layer);
							layer.getModel().dispose();
							layer.dispose();
						}
					} catch (PointNotFoundException e) {
						Logging.logger().severe(e.getMessage());
					}
				}
				if(geoFile.size()>0){
					try{
						GEOTracksLayer layer = new GEOTracksLayer(new TracksModel());
						layer.setPrecision(Double.parseDouble(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_PRECISION, "0.01")));
						MainWindow.this.wwd.toggleLayer(layer, true);
						//lecture et création des tracks à la volée
						GEOReader reader = new GEOReader(geoFile, layer.getModel(), filters, disjunctive, importRapide);
						reader.addPropertyChangeListener(readerListener);
						progressMonitorT.setCancelable(reader);
						reader.doRead();
						if(reader.getModel().getAllTracks().size() > 0 && !reader.isCanceled()){
							//changement du style en fonction de la conf
							if(reader.getModel().getAllTracks().size()< Integer.parseInt(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_SEUIL, "20"))){
								layer.setStyle(TrajectoriesLayer.STYLE_CURTAIN);
							}
							layer.setName(reader.getName());
							MainWindow.this.addTrajectoriesView(reader, layer);
						} else {
							//aucune trajectoire trouvée dans les fichiers
							//ou annulation
							geoFile.clear();
							MainWindow.this.wwd.removeLayer(layer);
							layer.getModel().dispose();
							layer.dispose();
							
						}
					} catch (PointNotFoundException e) {
						Logging.logger().severe(e.getMessage());
					}
				}
				if(lplnFile.size()>0){
					try {
						LPLNTracksLayer layer = new LPLNTracksLayer(new TracksModel());
						LPLNReader reader = new LPLNReader(lplnFile, layer.getModel());
						reader.addPropertyChangeListener(readerListener);
						progressMonitorT.setCancelable(reader);
						reader.doRead();
						MainWindow.this.wwd.toggleLayer(layer, true);
						if(reader.getModel().getAllTracks().size() > 0 && !reader.isCanceled()){
							MainWindow.this.addTrajectoriesView(reader, layer);
						} else {
							lplnFile.clear();
							MainWindow.this.wwd.removeLayer(layer);
							layer.getModel().dispose();
							layer.dispose();
						}
					} catch (PointNotFoundException e) {
						Logging.logger().warning("Point non trouvé : "+e.getName());
						JOptionPane.showMessageDialog(null, "<html><b>Problème :</b><br />Impossible de trouver certains points du fichier ("+e.getName()+").<br /><br />" +
								"<b>Solution :</b><br />Vérifiez qu'une base de données STIP existe et qu'elle est cohérente avec le fichier de trajectoires.</html>",
								"Erreur", JOptionPane.ERROR_MESSAGE);
					}
				}
				if(fplFile.size()>0){
					try {
						FPLTracksLayer layer = new FPLTracksLayer(new TracksModel());
						FPLReader fplR = new FPLReader(fplFile, layer.getModel());
						fplR.addPropertyChangeListener(readerListener);
						progressMonitorT.setCancelable(fplR);
						fplR.doRead();
						MainWindow.this.wwd.toggleLayer(layer, true);
						String msgErreur = fplR.getErrorMessage();
						if(!msgErreur.isEmpty())
							JOptionPane.showMessageDialog(null, msgErreur, "Erreur lors de la lecture du plan de vol", JOptionPane.ERROR_MESSAGE);
						if(fplR.getModel().getAllTracks().size()>0)
							MainWindow.this.addTrajectoriesView(fplR, layer);
						else  {
							fplFile.clear();
							MainWindow.this.wwd.removeLayer(layer);
							layer.getModel().dispose();
							layer.dispose();
						}
					} catch (PointNotFoundException e) {
						e.printStackTrace();
					}
				}
				if(plnsFile.size()>0){
					//choix de la base de données
					VFileChooser fileChooser = new VFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileChooser.setMultiSelectionEnabled(false);
					fileChooser.setDialogTitle("Sélectionner le fichier de base de données");
					fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
					File database;
					if(fileChooser.showOpenDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION){
						database = fileChooser.getSelectedFile();
						try{
							PLNSTracksLayer layer = new PLNSTracksLayer(new PLNSTracksModel());
							MainWindow.this.wwd.toggleLayer(layer, true);
							PLNSReader reader = new PLNSReader(plnsFile.toArray(new File[]{}), database, (PLNSTracksModel)layer.getModel(), readerListener);
							MainWindow.this.addTrajectoriesView(reader, layer);
						} catch(PointNotFoundException e){
							Logging.logger().warning("Point non trouvé : "+e.getName());
							JOptionPane.showMessageDialog(null, "<html><b>Problème :</b><br />Impossible de trouver certains points du fichier ("+e.getName()+").<br /><br />" +
									"<b>Solution :</b><br />Vérifiez qu'une base de données STIP existe et qu'elle est cohérente avec le fichier de trajectoires.</html>",
									"Erreur", JOptionPane.ERROR_MESSAGE);
						}
					} else {
						//pas de base de données choisie, on met plnsFile à 0 pour déclencher un message d'erreur
						plnsFile.clear();
					}
				}

				if(sqlitePlnsFile.size() > 0){
					try {
						for(File f : sqlitePlnsFile){

							PLNSTracksModel model = new PLNSTracksModel();
							PLNSTracksLayer layer = new PLNSTracksLayer(model);
							MainWindow.this.wwd.toggleLayer(layer, true);

							final ProgressMonitor progress = new ProgressMonitor(MainWindow.this, "Import des trajectoires", "", 0, 100);
							model.getProgressSupport().addPropertyChangeListener(new PropertyChangeListener() {

								@Override
								public void propertyChange(PropertyChangeEvent evt) {
									if(evt.getPropertyName().equals(ProgressSupport.TASK_STARTS)){
										progress.setMaximum(((Integer) evt.getNewValue()));
									} else if(evt.getPropertyName().equals(ProgressSupport.TASK_PROGRESS)){
										progress.setProgress((Integer) evt.getNewValue());
									} else if(evt.getPropertyName().equals(ProgressSupport.TASK_INFO)){
										progress.setNote((String)evt.getNewValue());
									}
								}
							});
							model.setDatabase(f);
							MainWindow.this.addTrajectoriesView(new PLNSReader(f, (PLNSTracksModel) layer.getModel(), readerListener), layer);
						}
					} catch (PointNotFoundException e) {
						Logging.logger().warning("Point non trouvé : "+e.getName());
						JOptionPane.showMessageDialog(null, "<html><b>Problème :</b><br />Impossible de trouver certains points du fichier ("+e.getName()+").<br /><br />" +
								"<b>Solution :</b><br />Vérifiez qu'une base de données STIP existe et qu'elle est cohérente avec le fichier de trajectoires.</html>",
								"Erreur", JOptionPane.ERROR_MESSAGE);
					}
				}
				return null;
			}

			@Override
			protected void done() {
				current = -1;
				if((opasFile.size() == 0 && geoFile.size() == 0 && lplnFile.size() == 0 
						&& fplFile.size()==0 && plnsFile.size() == 0 && sqlitePlnsFile.size() == 0)
					&& !progressMonitorT.isCanceled()){
					//pas de trajecto trouvée et pas d'annulation => problème
					Logging.logger().warning("Aucun fichier trajectoire trouvé.");
					JOptionPane.showMessageDialog(null, "<html><b>Problème :</b><br />Aucun fichier trajectoire trouvé.<br /><br />" +
							"<b>Solution :</b><br />Vérifiez que les fichiers sélectionnés sont bien dans un format pris en compte.</html>", "Erreur", JOptionPane.ERROR_MESSAGE);
				}
				progressMonitorT.close();
			}


		}.execute();

	
	}
	
	public void addTrajectoriesView(final TrackFilesReader reader, final TrajectoriesLayer layer){
		
		final TrajectoriesView content = new TrajectoriesView(wwd, reader, layer, context);
		//register view
		DatasManager.addTrajectory(layer.getModel(), content);
		this.wwd.toggleLayer(layer, true);
		int i = 0;
		if(control.getSingleDockable(reader.getName()) != null){
			do{
				i++;
			} while (control.getSingleDockable(reader.getName()+"-"+i) != null);
		}
		
		DefaultSingleCDockable dockable = new DefaultSingleCDockable(i==0?reader.getName():reader.getName()+"-"+i,
				reader.getName(),
				content,
				new CCloseAction(control){

				@Override
				public void close(CDockable dockable) {
					super.close(dockable);
					//unregister view
					DatasManager.removeTrajectory(layer.getModel());
					wwd.removeLayer(layer);
					control.removeDockable((SingleCDockable) dockable);
					//force close instead of just changing the visibility
					layer.getModel().dispose();
					layer.dispose();
			}

		});
		
		this.addDockable(dockable);
	}

	/**
	 * Add the {@link DefaultSingleCDockable} to the left position of the {@link MainWindow}
	 * @param dockable
	 */
	private void addDockable(DefaultSingleCDockable dockable){
		if(this.control.getCDockableCount() <= 2 ){
			locationDatas = CLocation.base().normalWest(0.2);
		} 
		dockable.setDefaultLocation(ExtendedMode.MINIMIZED, CLocation.base().minimalWest());
		
		dockable.setLocation(locationDatas);
		
		control.addDockable(dockable);
		dockable.setVisible(true);
	}
	
	
	/**
	 * 
	 * @param file Project to load
	 */
	public void loadProject(final File file){
		final ProjectManager project = new ProjectManager();
		final ProgressMonitor monitor = new ProgressMonitor(null, "Import d'un fichier projet",
				"Import ...", 0, 100, true, true);
		monitor.setMillisToPopup(0);
		monitor.setMillisToDecideToPopup(0);
		project.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals(ProgressSupport.TASK_STARTS)){
					monitor.setMaximum((Integer) evt.getNewValue());
				} else if(evt.getPropertyName().equals(ProgressSupport.TASK_INFO)){
					if(monitor.isCanceled()){

					} else {
						monitor.setNote((String) evt.getNewValue());
					}
				} else if(evt.getPropertyName().equals(ProgressSupport.TASK_PROGRESS)){
					monitor.setProgress((Integer) evt.getNewValue());
				}
			}
		});
		new SwingWorker<Void, Void>(){

			@Override
			protected Void doInBackground() throws Exception {
				Project p = null;
				try {
					p = project.loadProject(file, wwd, MainWindow.this, false);
				} catch (CompatibilityVersionException e) {
					if(JOptionPane.showConfirmDialog(null, "<html>Le fichier que souhaitez importer n'est pas compatible avec la version de Videso que vous utilisez.<br/>" +
							"Souhaitez vous tout de même l'importer ?<br/><br/>" +
							"<b>Avertissement : </b>L'import d'un fichier non compatible peut faire planter l'application.<br/><br/>" +
							"<i>Information : </i> Version du fichier : "+e.getMessage()+"</html>",
							"Version du fichier incompatible.", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE)
							== JOptionPane.YES_OPTION) {
						p = project.loadProject(file, wwd, MainWindow.this, true);
					}
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				} catch (Exception e){
					e.printStackTrace();
				}
				if(p != null){
					try{
						DatasManager.getUserObjectsController(wwd).addProject(p);
					} catch(Exception e){
						e.printStackTrace();
					}
				}
				return null;
			}

		}.execute();
	}

	public void setDrawToolbar(boolean selected) {
		if(drawToolbar == null){
			this.drawToolbar = new DrawToolbar(wwd);
			this.drawToolbar.setFloatable(true);
		}
		if(selected){
			this.toolbars.add(drawToolbar, BorderLayout.PAGE_START);
			this.validate();
		} else {
			this.toolbars.remove(drawToolbar);
			this.validate();
		}
	}
}
