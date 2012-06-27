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
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
//import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;

import org.jdesktop.swingx.plaf.nimbus.NimbusMultiSliderUI;

import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.event.CDockableStateListener;
import bibliothek.gui.dock.common.group.CGroupBehavior;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.common.theme.CEclipseTheme;
import bibliothek.gui.dock.common.theme.ThemeMap;
import bibliothek.gui.dock.util.Priority;

import fr.crnan.videso3d.AirspaceListener;
import fr.crnan.videso3d.ProgressSupport;
import fr.crnan.videso3d.CompatibilityVersionException;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.Configuration;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.FileManager;
import fr.crnan.videso3d.ProjectManager;
import fr.crnan.videso3d.SplashScreen;
import fr.crnan.videso3d.Videso3D;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.formats.geo.GEOReader;
import fr.crnan.videso3d.formats.lpln.LPLNReader;
import fr.crnan.videso3d.formats.opas.OPASReader;
import fr.crnan.videso3d.formats.plns.PLNSReader;
import fr.crnan.videso3d.formats.fpl.FPLReader;
import fr.crnan.videso3d.ihm.components.AltitudeRangeSlider;
import fr.crnan.videso3d.ihm.components.DatabaseSingleDockable;
import fr.crnan.videso3d.ihm.components.Omnibox;
import fr.crnan.videso3d.ihm.components.VDefaultEclipseThemConnector;
import fr.crnan.videso3d.ihm.components.VFileChooser;
import fr.crnan.videso3d.layers.tracks.FPLTracksLayer;
import fr.crnan.videso3d.layers.tracks.GEOTracksLayer;
import fr.crnan.videso3d.layers.tracks.LPLNTracksLayer;
import fr.crnan.videso3d.layers.tracks.OPASTracksLayer;
import fr.crnan.videso3d.layers.tracks.PLNSTracksLayer;
import fr.crnan.videso3d.layers.tracks.TrajectoriesLayer;
import fr.crnan.videso3d.stip.PointNotFoundException;
import fr.crnan.videso3d.trajectography.PLNSTracksModel;
import fr.crnan.videso3d.trajectography.TracksModel;
import fr.crnan.videso3d.trajectography.TrajectoryFileFilter;
import fr.crnan.videso3d.util.VidesoStatusBar;

import glass.eclipse.theme.CGlassEclipseTabPainter;
import glass.eclipse.theme.CGlassStationPaint;
import glass.eclipse.theme.CMiniPreviewMovingImageFactory;
import glass.eclipse.theme.EclipseThemeExtension;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.util.Logging;

/**
 * Fenêtre principale
 * @author Bruno Spyckerelle
 * @version 0.3.13
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

//	private JToolBar drawToolbar;
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
		//et suppression des dossiers temporaires
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				DatabaseManager.closeAll();
				FileManager.removeTempFiles();
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
		for(DatabaseManager.Type t : DatabaseManager.getSelectedDatabases()) {
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
					for(DatabaseManager.Type t : DatabaseManager.getSelectedDatabases()) {
						DatasManager.createDatas(t, wwd);
					}
					omniBox = new Omnibox(wwd, context);
					for(DatabaseManager.Type t : DatabaseManager.getSelectedDatabases()){
						omniBox.addDatabase(t, DatabaseManager.getAllVisibleObjects(t, omniBox), false);
					}
					wwd.firePropertyChange("step", "", "Création de l'interface");
				} catch (Exception e) {
					e.printStackTrace();
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
		control.putProperty(EclipseTheme.THEME_CONNECTOR, new VDefaultEclipseThemConnector());
		//glass l&f
		control.putProperty(EclipseThemeExtension.GLASS_FACTORY, null);
		control.putProperty(EclipseTheme.TAB_PAINTER, CGlassEclipseTabPainter.FACTORY);
		((CEclipseTheme)control.intern().getController().getTheme()).intern().setMovingImageFactory(new CMiniPreviewMovingImageFactory(128), Priority.CLIENT);
	     ((CEclipseTheme)control.intern().getController().getTheme()).intern().setPaint(new CGlassStationPaint(), Priority.CLIENT);
		
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
		locationDatas = /*CLocation.base().normalWest(0.2);*/dockableDatas.getBaseLocation().aside();

		for(DatabaseManager.Type type : DatabaseManager.getSelectedDatabases()){
			this.updateDockables(type, false);
		}

		control.removeDockable(dockableDatas);

		airspaceListener = new AirspaceListener(wwd, context);
		wwd.addSelectListener(airspaceListener);

		//initialisation contextpanel
		for(DatabaseManager.Type t : DatabaseManager.getSelectedDatabases()){	
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
				DatabaseManager.Type type = (DatabaseManager.Type) evt.getNewValue();
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

					private boolean empty = false;
					
					@Override
					protected Integer doInBackground() throws Exception {
						progressMonitor.setProgress(0);
						try { 
							progressMonitor.setNote(evt.getNewValue().toString());
						} catch (Exception e){
							e.printStackTrace();
						}
						DatabaseManager.Type type = (DatabaseManager.Type) evt.getNewValue();
						empty = DatasManager.numberViews() == 0;
						DatasManager.createDatas(type, wwd);
						omniBox.addDatabase(type, DatabaseManager.getAllVisibleObjects(type, omniBox), true);
						return null;
					}

					@Override
					protected void done() {
						DatabaseManager.Type type = (DatabaseManager.Type) evt.getNewValue();
							if(DatasManager.getView(type) != null){
								updateDockables(type, empty);
								context.addTaskPane(DatasManager.getContext(type), type);
								AnalyzeUI.getContextPanel().addTaskPane(DatasManager.getContext(type), type);
								AnalyzeUI.updateSearchBoxes();
							}
						progressMonitor.close();
					}
				}.execute();
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
	private void updateDockables(DatabaseManager.Type type, boolean empty){
		this.control.removeSingleDockable(type.toString());
		DatabaseSingleDockable dockable = new DatabaseSingleDockable(type.toString());
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
	 * Adds a KMLView to the data explorer
	 * @param view
	 */
	public void addKMLView(final KMLView view){
		if(!(view instanceof Component))
			return;
		
		int i = 0;
		if(control.getSingleDockable(view.getTitle()) != null){
			do{
				i++;
			} while (control.getSingleDockable(view.getTitle()+"-"+i) != null);
		}
		DefaultSingleCDockable dockable = new DefaultSingleCDockable(i==0?view.getTitle():view.getTitle()+"-"+i);
		dockable.setTitleText(view.getTitle());

		dockable.setLocation(locationDatas);
		dockable.setCloseable(true);
		dockable.add((Component) view);
		control.addDockable(dockable);
		dockable.setVisible(true);
		
		dockable.addCDockableStateListener(new CDockableStateListener() {
			
			@Override
			public void visibilityChanged(CDockable dockable) {
					wwd.removeLayer(view.getLayer());
			}
				
			@Override
			public void extendedModeChanged(CDockable dockable, ExtendedMode mode) {}
		});
	}
	
	/* ******************************************************* */
	/* ***************** Gestion des trajectoires ************ */
	/* ******************************************************* */
	
	public void addTrajectoriesViews(File[] filesT){
		this.addTrajectoriesViews(filesT, null, true);
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
	public void addTrajectoriesViews(File[] filesT, List<TrajectoryFileFilter> filters, boolean disjunctive){
		final ProgressMonitor progressMonitorT = new ProgressMonitor(this, "Import des trajectoires", "", 0, 100, false, true, false);
		progressMonitorT.setMillisToDecideToPopup(0);
		progressMonitorT.setMillisToPopup(0);
		progressMonitorT.setNote("Extraction des fichiers compressés...");
		
		List<File> files = FileManager.extractFilesIfNeeded(Arrays.asList(filesT));
		
		progressMonitorT.setNote("Détection des types de fichiers...");
		
		Vector<File> opasFile = new Vector<File>();
		Vector<File> geoFile = new Vector<File>();
		Vector<File> lplnFile = new Vector<File>();
		Vector<File> fplFile = new Vector<File>();
		Vector<File> plnsFile = new Vector<File>();
		Vector<File> sqlitePlnsFile = new Vector<File>();
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
		
		PropertyChangeListener readerListener = new PropertyChangeListener() {

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

		if(opasFile.size()>0){
			try{
				OPASTracksLayer layer = new OPASTracksLayer(new TracksModel());
				layer.setPrecision(Double.parseDouble(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_PRECISION, "0.01")));
				this.wwd.toggleLayer(layer, true);
				//lecture et création des tracks à la volée
				OPASReader reader = new OPASReader(opasFile, layer.getModel(), readerListener);
				if(reader.getModel().getAllTracks().size() > 0){
					//changement du style en fonction de la conf
					if(reader.getModel().getAllTracks().size()< Integer.parseInt(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_SEUIL, "20"))){
						layer.setStyle(TrajectoriesLayer.STYLE_CURTAIN);
					}
					layer.setName(reader.getName());
					this.addTrajectoriesView(reader, layer);
				} else {
					//aucune trajectoire trouvée dans les fichiers
					opasFile.clear();
				}
			} catch (PointNotFoundException e) {
				e.printStackTrace();
			}
		}
		if(geoFile.size()>0){
			try{
				GEOTracksLayer layer = new GEOTracksLayer(new TracksModel());
				layer.setPrecision(Double.parseDouble(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_PRECISION, "0.01")));
				this.wwd.toggleLayer(layer, true);
				//lecture et création des tracks à la volée
				GEOReader reader = new GEOReader(geoFile, layer.getModel(), readerListener, filters, disjunctive);
				if(reader.getModel().getAllTracks().size() > 0){
					//changement du style en fonction de la conf
					if(reader.getModel().getAllTracks().size()< Integer.parseInt(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_SEUIL, "20"))){
						layer.setStyle(TrajectoriesLayer.STYLE_CURTAIN);
					}
					layer.setName(reader.getName());
					this.addTrajectoriesView(reader, layer);
				} else {
					//aucune trajectoire trouvée dans les fichiers
					geoFile.clear();
				}
			} catch (PointNotFoundException e) {
				e.printStackTrace();
			}
		}
		if(lplnFile.size()>0){
			try {
				LPLNTracksLayer layer = new LPLNTracksLayer(new TracksModel());
				LPLNReader reader = new LPLNReader(lplnFile, layer.getModel(), readerListener);
				this.wwd.toggleLayer(layer, true);
				if(reader.getModel().getAllTracks().size() > 0){
					this.addTrajectoriesView(reader, layer);
				} else {
					lplnFile.clear();
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
				FPLReader fplR = new FPLReader(fplFile, layer.getModel(), readerListener);
				this.wwd.toggleLayer(layer, true);
				String msgErreur = fplR.getErrorMessage();
				if(!msgErreur.isEmpty())
					JOptionPane.showMessageDialog(null, msgErreur, "Erreur lors de la lecture du plan de vol", JOptionPane.ERROR_MESSAGE);
				if(fplR.getModel().getAllTracks().size()>0)
					this.addTrajectoriesView(fplR, layer);
				else 
					fplFile.clear();
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
			if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
				database = fileChooser.getSelectedFile();
				try{
					PLNSTracksLayer layer = new PLNSTracksLayer(new PLNSTracksModel());
					this.wwd.toggleLayer(layer, true);
					PLNSReader reader = new PLNSReader(plnsFile.toArray(new File[]{}), database, (PLNSTracksModel)layer.getModel(), readerListener);
					this.addTrajectoriesView(reader, layer);
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
					this.wwd.toggleLayer(layer, true);
					
					final ProgressMonitor progress = new ProgressMonitor(this, "Import des trajectoires", "", 0, 100);
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
					this.addTrajectoriesView(new PLNSReader(f, (PLNSTracksModel) layer.getModel(), readerListener), layer);
				}
			} catch (PointNotFoundException e) {
				Logging.logger().warning("Point non trouvé : "+e.getName());
				JOptionPane.showMessageDialog(null, "<html><b>Problème :</b><br />Impossible de trouver certains points du fichier ("+e.getName()+").<br /><br />" +
						"<b>Solution :</b><br />Vérifiez qu'une base de données STIP existe et qu'elle est cohérente avec le fichier de trajectoires.</html>",
						"Erreur", JOptionPane.ERROR_MESSAGE);
			}
		}
		current = -1;
		progressMonitorT.close();
		if(opasFile.size() == 0 && geoFile.size() == 0 && lplnFile.size() == 0 
				&& fplFile.size()==0 && plnsFile.size() == 0 && sqlitePlnsFile.size() == 0){
			Logging.logger().warning("Aucun fichier trajectoire trouvé.");
			JOptionPane.showMessageDialog(null, "<html><b>Problème :</b><br />Aucun fichier trajectoire trouvé.<br /><br />" +
					"<b>Solution :</b><br />Vérifiez que les fichiers sélectionnés sont bien dans un format pris en compte.</html>", "Erreur", JOptionPane.ERROR_MESSAGE);
		}
	
	}
	
	public void addTrajectoriesView(final TrackFilesReader reader, final TrajectoriesLayer layer){
		final TrajectoriesView content = new TrajectoriesView(wwd, reader, layer, context);
		this.wwd.toggleLayer(layer, true);
		int i = 0;
		if(control.getSingleDockable(reader.getName()) != null){
			do{
				i++;
			} while (control.getSingleDockable(reader.getName()+"-"+i) != null);
		}
		DefaultSingleCDockable dockable = new DefaultSingleCDockable(i==0?reader.getName():reader.getName()+"-"+i);
		dockable.setTitleText(reader.getName());

		dockable.setLocation(locationDatas);
		dockable.setCloseable(true);
		dockable.add(content);
		control.addDockable(dockable);
		dockable.setVisible(true);
		
		dockable.addCDockableStateListener(new CDockableStateListener() {
			
			@Override
			public void visibilityChanged(CDockable dockable) {
					wwd.removeLayer(layer);
					//force close instead of just changing the visibility
					dockable = null;
					layer.getModel().dispose();
					layer.dispose();
			}
				
			@Override
			public void extendedModeChanged(CDockable dockable, ExtendedMode mode) {}
		});
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
				try {
					project.loadProject(file, wwd, getThis(), false);
				} catch (CompatibilityVersionException e) {
					if(JOptionPane.showConfirmDialog(null, "<html>Le fichier que souhaitez importer n'est pas compatible avec la version de Videso que vous utilisez.<br/>" +
							"Souhaitez vous tout de même l'importer ?<br/><br/>" +
							"<b>Avertissement : </b>L'import d'un fichier non compatible peut faire planter l'application.<br/><br/>" +
							"<i>Information : </i> Version du fichier : "+e.getMessage()+"</html>",
							"Version du fichier incompatible.", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE)
							== JOptionPane.YES_OPTION) {
						project.loadProject(file, wwd, getThis(), true);
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
				return null;
			}

		}.execute();
	}
	
	private MainWindow getThis(){
		return this;
	}

//	public void setDrawToolbar(boolean selected) {
//		if(drawToolbar == null){
//			this.drawToolbar = new DrawToolbar(wwd);
//			this.drawToolbar.setFloatable(true);
//		}
//		if(selected){
//			this.toolbars.add(drawToolbar, BorderLayout.PAGE_START);
//			this.validate();
//		} else {
//			this.toolbars.remove(drawToolbar);
//			this.validate();
//		}
//	}
}
