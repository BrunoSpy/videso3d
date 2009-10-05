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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.globes.FlatGlobeCautra;
import fr.crnan.videso3d.util.VidesoStatusBar;

import gov.nasa.worldwind.BasicModel;

/**
 * Fenêtre principale
 * @author Bruno Spyckerelle
 * @version 0.3
 */
@SuppressWarnings("serial")
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
	public MainWindow(final DatabaseManager db){
		
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
		
	
		//fermeture des connections aux bases de données avant de quitter afin de ne pas perdre les dernières transactions
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				db.closeAll();
			}
		});
		
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
						final String type = (String)evt.getNewValue();
						//mises à jour en background
						new SwingWorker<String, Integer>(){
							@Override
							protected String doInBackground() throws Exception {
								if(type.equals("STIP")){
									//mise à jour de la vue 3D
									wwd.buildStip();
									//mise à jour de l'explorateur de données
									dataExplorer.updateStipView();
								} else if (type.equals("EXSA")){
									//mise à jour de l'explorateur de données
									dataExplorer.updateStrView();
									//mise à jour de la vue 3D
									wwd.removeMosaiques();
								} else if(type.equals("STPV")){
									dataExplorer.updateStpvView();
									wwd.removeMosaiques();
								}
								return null;
							}
						}.execute();
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
				db.closeAll();
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
		toolbar.add(new JLabel("Recherche : "));
		
		JTextField search = new JTextField(10);
		search.setToolTipText("Rechercher un élément Stip affiché");
		LinkedList<String> results = new LinkedList<String>();
		try {
			Statement st = this.db.getCurrentStip();
			ResultSet rs = st.executeQuery("select name from balises UNION select name from routes UNION select nom from secteurs");
			while(rs.next()){
				results.add(rs.getString(1));
			}
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		AutoCompleteDecorator.decorate(search, results, false);
		
		search.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				wwd.hightlight(((JTextField)e.getSource()).getText());
			}
		});
		
		toolbar.add(search);
		
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
