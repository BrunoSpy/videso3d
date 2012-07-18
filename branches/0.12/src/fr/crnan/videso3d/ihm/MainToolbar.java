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


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.stream.XMLStreamException;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.ihm.components.DataView;
import fr.crnan.videso3d.ihm.components.DropDownButton;
import fr.crnan.videso3d.ihm.components.DropDownToggleButton;
import fr.crnan.videso3d.ihm.components.Omnibox;
import fr.crnan.videso3d.ihm.components.VFileChooser;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwindx.examples.util.ScreenShotAction;

/**
 * Toolbar of the main window
 * @author Bruno Spyckerelle
 * @version 0.1.6
 */
public class MainToolbar extends JToolBar {

	private VidesoGLCanvas wwd;
	private MainWindow mainWindow;
	private Omnibox omniBox;

	public MainToolbar(final MainWindow parent, VidesoGLCanvas ww, Omnibox box) {
		this.setFloatable(true);
		
		this.wwd = ww;
		this.mainWindow = parent;
		this.omniBox = box;
		
		//Configuration
		final JButton config = new JButton(new ImageIcon(getClass().getResource("/resources/configure.png")));
		config.setToolTipText("Configurer les paramètres généraux de l'application");
		config.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new ConfigurationUI().setVisible(true);
			}
		});
		this.add(config);

		this.addSeparator();

		//Drawings
		final JToggleButton draw = new JToggleButton(new ImageIcon(getClass().getResource("/resources/applications-graphics_22.png")));
		draw.setToolTipText("Afficher la barre d'outils de dessin");
		draw.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				parent.setDrawToolbar(draw.isSelected());
			}
		});

		this.add(draw);

		this.addSeparator();	

		//Analyse
		final JButton analyze = new JButton(new ImageIcon(getClass().getResource("/resources/datas_analyze_22.png")));
		analyze.setToolTipText("Analyser les données Stip/Stpv");
		analyze.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				AnalyzeUI.showAnalyzeUI();
			}
		});
		this.add(analyze);

		//Ajouter données
		JButton datas = new JButton(new ImageIcon(getClass().getResource("/resources/database_22.png")));
		datas.setToolTipText("Ajouter/supprimer des données");
		datas.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new DatabaseManagerUI().setVisible(true);
			}
		});
		this.add(datas);

		this.addSeparator();

		final JButton loadProject = new JButton(new ImageIcon(getClass().getResource("/resources/load_project_22.png")));
		loadProject.setToolTipText("Charger un projet");
		loadProject.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final VFileChooser fileChooser = new VFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if(fileChooser.showOpenDialog(loadProject) == JFileChooser.APPROVE_OPTION){
					parent.loadProject(fileChooser.getSelectedFile());
				}
			}
		});
		this.add(loadProject);

		final JButton saveProject = new JButton(new ImageIcon(getClass().getResource("/resources/save_project_22.png")));
		saveProject.setToolTipText("Enregistrer le projet");
		saveProject.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new ProjectManagerUI(mainWindow, wwd);
			}
		});

		this.add(saveProject);
		
		//Screenshot
		JButton snapshot = new JButton(new ImageIcon(getClass().getResource("/resources/snapshot.png")));
		snapshot.setToolTipText("Capture d'écran de la vue 3D");
		snapshot.addActionListener(new ScreenShotAction(wwd));
		this.add(snapshot);
		
		this.addSeparator();
		//ajout d'un kml
		final JButton kml = new JButton(new ImageIcon(getClass().getResource("/resources/add_kml_22.png")));
		kml.setToolTipText("Importer un fichier KML");
		kml.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				VFileChooser fileChooser = new VFileChooser();
				fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("KML/KMZ File", "kml", "kmz"));
				if(fileChooser.showOpenDialog(mainWindow)== JFileChooser.APPROVE_OPTION){
					try {
						KMLRoot kmlRoot = KMLRoot.createAndParse(fileChooser.getSelectedFile());
						kmlRoot.setField(AVKey.DISPLAY_NAME, KMLView.formName(fileChooser.getSelectedFile(), kmlRoot));
						mainWindow.addKMLView(new KMLView(kmlRoot, wwd));
					} catch (IOException e) {
						e.printStackTrace();
					} catch (XMLStreamException e) {
						e.printStackTrace();
					}
				}
			}
		});
		this.add(kml);
		
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
				ArrayList<String> imgFilter = new ArrayList<String>();
				for(String s : ImageIO.getReaderFormatNames()){
					imgFilter.add(s);
				}
				imgFilter.add("gtif");
				imgFilter.add("gtiff");
				imgFilter.add("geotiff");
				file.addChoosableFileFilter(
						new FileNameExtensionFilter("Images", imgFilter.toArray(new String[]{})));
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
		images.addToToolBar(this);

		//Ajouter trajectoires
		final DropDownButton trajectoires = new DropDownButton(new ImageIcon(getClass().getResource("/resources/plus_traj_22.png")));

    final JMenuItem fileWithFilter = new JMenuItem("Fichier avec filtrage");
		fileWithFilter.setToolTipText("Filtrer puis importer des trajectoires depuis un fichier");
		fileWithFilter.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new TrajectoriesImportUI(mainWindow).setVisible(true);
			}
		});

		JMenuItem text = new JMenuItem("Texte");
		text.setToolTipText("Importer des trajectoires par copier/coller");
		text.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new FPLImportUI(mainWindow).setVisible(true);
			}
		});

    trajectoires.getPopupMenu().add(fileWithFilter);
		trajectoires.getPopupMenu().add(text);

		trajectoires.setToolTipText("Importer des trajectoires");
		trajectoires.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				fileWithFilter.doClick();
			}
		});
		trajectoires.addToToolBar(this);
		
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

		this.add(reset);

		this.addSeparator();
		
		//afficher une échelle verticale	
		final JToggleButton verticalScaleBar = new JToggleButton(new ImageIcon(getClass().getResource("/resources/scale_22_2.png")));
		verticalScaleBar.setToolTipText("Afficher une échelle verticale");
		verticalScaleBar.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				wwd.activateVerticalScaleBar(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		this.add(verticalScaleBar);

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
		fond.addToToolBar(this);



		//Alidade
		final JToggleButton alidad = new JToggleButton("Alidade");
		alidad.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				wwd.switchMeasureTool(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		this.add(alidad);

		//Projections 
		new ProjectionDropDownButton(wwd).addToToolBar(this);
		this.addSeparator();

		//Vertical exaggeration
		JLabel label = new JLabel("Échelle verticale : ");
		this.add(label);
		this.add(new VerticalExaggerationSlider(wwd));		
		this.addSeparator();


		//recherche avec autocomplétion
		omniBox.addToToolbar(this);
		this.addSeparator();

		//Help button
		JButton aide = new JButton(new ImageIcon(getClass().getResource("/resources/bullet_about_22.png")));
		aide.setToolTipText("A propos...");
		aide.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JDialog help = new HelpDialog();
				help.setVisible(true);
			}
		});

		this.add(aide);

	}



}
