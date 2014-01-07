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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;

import javax.swing.BoxLayout;

import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.formats.fpl.FPLFileFilter;
import fr.crnan.videso3d.formats.geo.GEOFileFilter;
import fr.crnan.videso3d.formats.lpln.LPLNFileFilter;
import fr.crnan.videso3d.formats.opas.OPASFileFilter;
import fr.crnan.videso3d.formats.plns.PLNSFileFilter;
import fr.crnan.videso3d.graphics.PolygonAnnotation;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import fr.crnan.videso3d.ihm.components.VFileChooser;
import fr.crnan.videso3d.ihm.components.VSpinner;
import fr.crnan.videso3d.trajectography.TracksModel;
import fr.crnan.videso3d.trajectography.TrajectoryFileFilter;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.airspaces.Airspace;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;


/**
 * IHM to filter and import trajectories
 * @author Bruno Spyckerelle
 * @version 0.1.2
 */
public class TrajectoriesImportUI extends JDialog {
	
	private final ButtonGroup typeBtnGrp = new ButtonGroup();
	private JTextField adep;
	private JTextField adest;
	private JRadioButton rdbtnOu;
	private JRadioButton rdbtnEt;
	private JRadioButton filterYes; 
	private JTextField modeA;
	
	private JComboBox<PolygonAnnotation> polygonComboBox;
	
	private VSpinner heureDebut;
	private VSpinner minuteDebut;
	private VSpinner secondesDebut;
	private VSpinner heureFin;
	private VSpinner minutesFin;
	private VSpinner secondesFin;
	
	private JCheckBox enableHeureDebut;
	private JCheckBox enableHeureFin;
	private JCheckBox enableRapidite;
	private JLabel toolTipRapidite;
	private final ButtonGroup filterbtnGrp = new ButtonGroup();
	
	public TrajectoriesImportUI(final MainWindow mainWindow, VidesoGLCanvas wwd) {
			
		this.setTitle("Importer et filtrer des trajectoires");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		JPanel filterPanel = new JPanel();
		getContentPane().add(filterPanel);
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));
		
		filterPanel.add(new TitledPanel("1. Filtrer les trajectoires"));
		
		JPanel filterContent = new JPanel();

		JLabel lblFiltrerLesTrajectoires = new JLabel("Type de filtre :");

		rdbtnEt = new JRadioButton("Et");
		rdbtnEt.setEnabled(false);
		typeBtnGrp.add(rdbtnEt);

		rdbtnOu = new JRadioButton("Ou");
		rdbtnOu.setSelected(true);
		rdbtnOu.setEnabled(false);
		typeBtnGrp.add(rdbtnOu);
		
		JLabel lblAroportDpart = new JLabel("Aéroport départ :");
		
		adep = new JTextField();
		adep.setEnabled(false);
		adep.setColumns(10);
		
		JLabel lblAroportArrive = new JLabel("Aéroport arrivée :");
		
		adest = new JTextField();
		adest.setColumns(10);
		adest.setEnabled(false);
		
		JLabel lblPolygones = new JLabel("Polygones :");
		
		polygonComboBox = new JComboBox<PolygonAnnotation>(){
			
		};
		
		DefaultComboBoxModel<PolygonAnnotation> polygons = new DefaultComboBoxModel<PolygonAnnotation>();
		polygons.addElement(null);
		for(Layer layer : wwd.getModel().getLayers()){
			if(layer instanceof AirspaceLayer){
				for(Airspace a : ((AirspaceLayer) layer).getAirspaces()){
					if(a instanceof PolygonAnnotation){
						if(a.isVisible())
							polygons.addElement((PolygonAnnotation) a);
					}
				}
			} 
		}
		
		polygonComboBox.setModel(polygons);
		polygonComboBox.setEnabled(false);
		
		JLabel lblModeA = new JLabel("Mode A :");
		
		modeA = new JTextField();
		modeA.setColumns(10);
		modeA.setEnabled(false);
		
		JLabel lblActiverLesFiltres = new JLabel("Activer les filtres :");
		
		filterYes = new JRadioButton("Oui");
		filterbtnGrp.add(filterYes);
		
		JRadioButton filterNo = new JRadioButton("Non");
		
		filterbtnGrp.add(filterNo);
		
		filterYes.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
					rdbtnEt.setEnabled(filterYes.isSelected());
					rdbtnOu.setEnabled(filterYes.isSelected());
					adep.setEnabled(filterYes.isSelected());
					adest.setEnabled(filterYes.isSelected());
					modeA.setEnabled(filterYes.isSelected());
					polygonComboBox.setEnabled(filterYes.isSelected());
					enableHeureDebut.setEnabled(filterYes.isSelected());
					heureDebut.setEnabled(filterYes.isSelected() && enableHeureDebut.isSelected());
					minuteDebut.setEnabled(filterYes.isSelected() && enableHeureDebut.isSelected());
					secondesDebut.setEnabled(filterYes.isSelected() && enableHeureDebut.isSelected());
					enableHeureFin.setEnabled(filterYes.isSelected());
					heureFin.setEnabled(filterYes.isSelected() && enableHeureFin.isSelected());
					minutesFin.setEnabled(filterYes.isSelected() && enableHeureFin.isSelected());
					secondesFin.setEnabled(filterYes.isSelected() && enableHeureFin.isSelected());

			}
		});
		
		filterNo.setSelected(true);
		
		
		
		JLabel lblHeureDeFin = new JLabel("Heure de fin :");
		
		JLabel lblHeureDebut = new JLabel("Heure de début :");
		
		heureDebut = new VSpinner(0, 23);
		heureDebut.setEnabled(false);
		heureDebut.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if(enableHeureFin.isSelected()){
					if(((SpinnerNumberModel)((JSpinner)e.getSource()).getModel()).getNumber().intValue() >
					((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()){
						heureFin.setValue(heureDebut.getValue());
					} 
					if((((SpinnerNumberModel)(heureDebut).getModel()).getNumber().intValue() ==
							((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()) &&
							(((SpinnerNumberModel)(minuteDebut).getModel()).getNumber().intValue() >
							((SpinnerNumberModel)(minutesFin).getModel()).getNumber().intValue())){
						minutesFin.setValue(minuteDebut.getValue());
					}
					if((((SpinnerNumberModel)(heureDebut).getModel()).getNumber().intValue() ==
							((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()) &&
							(((SpinnerNumberModel)(minuteDebut).getModel()).getNumber().intValue() ==
							((SpinnerNumberModel)(minutesFin).getModel()).getNumber().intValue()) &&
							(((SpinnerNumberModel)(secondesDebut).getModel()).getNumber().intValue() >
							((SpinnerNumberModel)(secondesFin).getModel()).getNumber().intValue())){
						secondesFin.setValue(secondesDebut.getValue());
					}
				}
			}
		});

		JLabel lblSep1 = new JLabel(":");

		minuteDebut = new VSpinner(0,59);
		minuteDebut.setEnabled(false);
		minuteDebut.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if(enableHeureFin.isSelected()){
					if((((SpinnerNumberModel)(heureDebut).getModel()).getNumber().intValue() ==
							((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()) &&
							(((SpinnerNumberModel)(minuteDebut).getModel()).getNumber().intValue() >
							((SpinnerNumberModel)(minutesFin).getModel()).getNumber().intValue())){
						minutesFin.setValue(minuteDebut.getValue());
					}
					if((((SpinnerNumberModel)(heureDebut).getModel()).getNumber().intValue() ==
							((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()) &&
							(((SpinnerNumberModel)(minuteDebut).getModel()).getNumber().intValue() ==
							((SpinnerNumberModel)(minutesFin).getModel()).getNumber().intValue()) &&
							(((SpinnerNumberModel)(secondesDebut).getModel()).getNumber().intValue() >
							((SpinnerNumberModel)(secondesFin).getModel()).getNumber().intValue())){
						secondesFin.setValue(secondesDebut.getValue());
					}
				}
			}
		});

		JLabel lblSep2 = new JLabel(":");

		secondesDebut = new VSpinner(0,59);
		secondesDebut.setEnabled(false);
		secondesDebut.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if(enableHeureFin.isSelected()){
					if((((SpinnerNumberModel)(heureDebut).getModel()).getNumber().intValue() ==
							((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()) &&
							(((SpinnerNumberModel)(minuteDebut).getModel()).getNumber().intValue() ==
							((SpinnerNumberModel)(minutesFin).getModel()).getNumber().intValue()) &&
							(((SpinnerNumberModel)(secondesDebut).getModel()).getNumber().intValue() >
							((SpinnerNumberModel)(secondesFin).getModel()).getNumber().intValue())){
						secondesFin.setValue(secondesDebut.getValue());
					}
				}
			}
		});

		heureFin = new VSpinner(0,23);
		heureFin.setEnabled(false);
		heureFin.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if(enableHeureDebut.isSelected()){
					if(((SpinnerNumberModel)(heureDebut).getModel()).getNumber().intValue() >
					((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()){
						heureDebut.setValue(heureFin.getValue());
					}
					if((((SpinnerNumberModel)(heureDebut).getModel()).getNumber().intValue() ==
							((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()) &&
							(((SpinnerNumberModel)(minuteDebut).getModel()).getNumber().intValue() >
							((SpinnerNumberModel)(minutesFin).getModel()).getNumber().intValue())){
						minuteDebut.setValue(minutesFin.getValue());
					}
					if((((SpinnerNumberModel)(heureDebut).getModel()).getNumber().intValue() ==
							((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()) &&
							(((SpinnerNumberModel)(minuteDebut).getModel()).getNumber().intValue() ==
							((SpinnerNumberModel)(minutesFin).getModel()).getNumber().intValue()) &&
							(((SpinnerNumberModel)(secondesDebut).getModel()).getNumber().intValue() >
							((SpinnerNumberModel)(secondesFin).getModel()).getNumber().intValue())){
						secondesDebut.setValue(secondesFin.getValue());
					}
				}
			}
		});

		JLabel lblSep3 = new JLabel(":");
		
		minutesFin = new VSpinner(0,59);
		minutesFin.setEnabled(false);
		minutesFin.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if(enableHeureDebut.isSelected()){
					if((((SpinnerNumberModel)(heureDebut).getModel()).getNumber().intValue() ==
					((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()) &&
					(((SpinnerNumberModel)(minuteDebut).getModel()).getNumber().intValue() >
					((SpinnerNumberModel)(minutesFin).getModel()).getNumber().intValue())){
						minuteDebut.setValue(minutesFin.getValue());
					}
					if((((SpinnerNumberModel)(heureDebut).getModel()).getNumber().intValue() ==
							((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()) &&
							(((SpinnerNumberModel)(minuteDebut).getModel()).getNumber().intValue() ==
							((SpinnerNumberModel)(minutesFin).getModel()).getNumber().intValue()) &&
							(((SpinnerNumberModel)(secondesDebut).getModel()).getNumber().intValue() >
							((SpinnerNumberModel)(secondesFin).getModel()).getNumber().intValue())){
								secondesDebut.setValue(secondesFin.getValue());
							}
				}
			}
		});
		
		JLabel lblSep4 = new JLabel(":");
		
		secondesFin = new VSpinner(0,59);
		secondesFin.setEnabled(false);
		secondesFin.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if(enableHeureDebut.isSelected()){
					if((((SpinnerNumberModel)(heureDebut).getModel()).getNumber().intValue() ==
					((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()) &&
					(((SpinnerNumberModel)(minuteDebut).getModel()).getNumber().intValue() ==
					((SpinnerNumberModel)(minutesFin).getModel()).getNumber().intValue()) &&
					(((SpinnerNumberModel)(secondesDebut).getModel()).getNumber().intValue() >
					((SpinnerNumberModel)(secondesFin).getModel()).getNumber().intValue())){
						secondesDebut.setValue(secondesFin.getValue());
					}
				}
			}
		});
		
		enableHeureDebut = new JCheckBox("");
		enableHeureDebut.setEnabled(false);	
		enableHeureDebut.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				heureDebut.setEnabled(enableHeureDebut.isSelected());
				minuteDebut.setEnabled(enableHeureDebut.isSelected());
				secondesDebut.setEnabled(enableHeureDebut.isSelected());
				enableRapidite.setEnabled(enableHeureDebut.isSelected()||enableHeureFin.isSelected());
				toolTipRapidite.setEnabled(enableRapidite.isEnabled());
				if(!enableHeureDebut.isSelected() && !enableHeureFin.isSelected())
					enableRapidite.setSelected(false);
				if(((SpinnerNumberModel)(heureDebut).getModel()).getNumber().intValue() >
				((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()){
					heureDebut.setValue(heureFin.getValue());
				}
				if((((SpinnerNumberModel)(heureDebut).getModel()).getNumber().intValue() ==
						((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()) &&
						(((SpinnerNumberModel)(minuteDebut).getModel()).getNumber().intValue() >
						((SpinnerNumberModel)(minutesFin).getModel()).getNumber().intValue())){
					minuteDebut.setValue(minutesFin.getValue());
				}
				if((((SpinnerNumberModel)(heureDebut).getModel()).getNumber().intValue() ==
						((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()) &&
						(((SpinnerNumberModel)(minuteDebut).getModel()).getNumber().intValue() ==
						((SpinnerNumberModel)(minutesFin).getModel()).getNumber().intValue()) &&
						(((SpinnerNumberModel)(secondesDebut).getModel()).getNumber().intValue() >
						((SpinnerNumberModel)(secondesFin).getModel()).getNumber().intValue())){
					secondesDebut.setValue(secondesFin.getValue());
				}
			}
		});
		
		enableHeureFin = new JCheckBox("");
		enableHeureFin.setEnabled(false);
		enableHeureFin.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				heureFin.setEnabled(enableHeureFin.isSelected());
				minutesFin.setEnabled(enableHeureFin.isSelected());
				secondesFin.setEnabled(enableHeureFin.isSelected());	
				enableRapidite.setEnabled(enableHeureDebut.isSelected()||enableHeureFin.isSelected());
				toolTipRapidite.setEnabled(enableRapidite.isEnabled());
				if(!enableHeureDebut.isSelected() && !enableHeureFin.isSelected())
					enableRapidite.setSelected(false);
				if(((SpinnerNumberModel)(heureDebut).getModel()).getNumber().intValue() >
				((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()){
					heureFin.setValue(heureDebut.getValue());
				} 
				if((((SpinnerNumberModel)(heureDebut).getModel()).getNumber().intValue() ==
						((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()) &&
						(((SpinnerNumberModel)(minuteDebut).getModel()).getNumber().intValue() >
						((SpinnerNumberModel)(minutesFin).getModel()).getNumber().intValue())){
					minutesFin.setValue(minuteDebut.getValue());
				}
				if((((SpinnerNumberModel)(heureDebut).getModel()).getNumber().intValue() ==
						((SpinnerNumberModel)(heureFin).getModel()).getNumber().intValue()) &&
						(((SpinnerNumberModel)(minuteDebut).getModel()).getNumber().intValue() ==
						((SpinnerNumberModel)(minutesFin).getModel()).getNumber().intValue()) &&
						(((SpinnerNumberModel)(secondesDebut).getModel()).getNumber().intValue() >
						((SpinnerNumberModel)(secondesFin).getModel()).getNumber().intValue())){
					secondesFin.setValue(secondesDebut.getValue());
				}
			}
		});
		
		enableRapidite = new JCheckBox("Import rapide");
		enableRapidite.setEnabled(false);
		enableRapidite.setToolTipText("Dégrade la précision : certaines trajectoires peuvent être tronquées au début ou à la fin, " +
				"d'une durée allant jusqu'à une minute");
		
		toolTipRapidite = new JLabel(new ImageIcon(getClass().getResource("/resources/attention.png")));
		toolTipRapidite.setToolTipText("Dégrade la précision : certaines trajectoires peuvent être tronquées au début ou à la fin, " +
				"d'une durée allant jusqu'à une minute");
		toolTipRapidite.setEnabled(false);
		
		GroupLayout gl_filterContent = new GroupLayout(filterContent);
		gl_filterContent.setHorizontalGroup(
			gl_filterContent.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_filterContent.createSequentialGroup()
					.addGap(10)
					.addGroup(gl_filterContent.createParallelGroup(Alignment.LEADING, false)
						.addGroup(gl_filterContent.createSequentialGroup()
							.addGroup(gl_filterContent.createParallelGroup(Alignment.LEADING)
								.addComponent(lblHeureDeFin)
								.addComponent(lblHeureDebut))
							.addGap(6)
							.addGroup(gl_filterContent.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_filterContent.createSequentialGroup()
									.addComponent(enableRapidite)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(toolTipRapidite))
								.addGroup(gl_filterContent.createSequentialGroup()
									.addGroup(gl_filterContent.createParallelGroup(Alignment.LEADING)
										.addComponent(enableHeureFin)
										.addGroup(gl_filterContent.createSequentialGroup()
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(enableHeureDebut)))
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_filterContent.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_filterContent.createSequentialGroup()
											.addComponent(heureFin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
											.addComponent(lblSep3)
											.addComponent(minutesFin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
											.addComponent(lblSep4)
											.addComponent(secondesFin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addGroup(gl_filterContent.createSequentialGroup()
											.addComponent(heureDebut, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
											.addComponent(lblSep1)
											.addComponent(minuteDebut, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
											.addComponent(lblSep2)
											.addComponent(secondesDebut, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))))
						.addGroup(gl_filterContent.createSequentialGroup()
							.addGroup(gl_filterContent.createParallelGroup(Alignment.LEADING)
								.addComponent(lblModeA)
								.addComponent(lblAroportArrive)
								.addComponent(lblAroportDpart)
								.addComponent(lblPolygones)
								.addComponent(lblActiverLesFiltres)
								.addGroup(gl_filterContent.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(lblFiltrerLesTrajectoires)))
							.addGroup(gl_filterContent.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_filterContent.createSequentialGroup()
									.addComponent(rdbtnEt)
									.addGap(18)
									.addGroup(gl_filterContent.createParallelGroup(Alignment.LEADING)
										.addComponent(filterNo)
										.addComponent(rdbtnOu)))
								.addComponent(filterYes)
								.addGroup(gl_filterContent.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_filterContent.createParallelGroup(Alignment.LEADING)
										.addComponent(adest)
										.addComponent(adep, GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
										.addComponent(modeA)
										.addComponent(polygonComboBox, 0, 145, Short.MAX_VALUE))))))
					.addContainerGap())
		);
		gl_filterContent.setVerticalGroup(
			gl_filterContent.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_filterContent.createSequentialGroup()
					.addGap(4)
					.addGroup(gl_filterContent.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblActiverLesFiltres)
						.addComponent(filterYes)
						.addComponent(filterNo))
					.addGroup(gl_filterContent.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_filterContent.createSequentialGroup()
							.addGap(4)
							.addGroup(gl_filterContent.createParallelGroup(Alignment.BASELINE)
								.addComponent(rdbtnEt)
								.addComponent(rdbtnOu)))
						.addGroup(gl_filterContent.createSequentialGroup()
							.addGap(8)
							.addComponent(lblFiltrerLesTrajectoires)))
					.addGap(4)
					.addGroup(gl_filterContent.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblAroportDpart)
						.addComponent(adep, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(4)
					.addGroup(gl_filterContent.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblAroportArrive)
						.addComponent(adest, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(4)
					.addGroup(gl_filterContent.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblModeA)
						.addComponent(modeA, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(4)
					.addGroup(gl_filterContent.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPolygones)
						.addComponent(polygonComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(4)
					.addGroup(gl_filterContent.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_filterContent.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblHeureDebut)
							.addComponent(heureDebut, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblSep1)
							.addComponent(minuteDebut, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblSep2)
							.addComponent(secondesDebut, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(enableHeureDebut))
					.addGap(10)
					.addGroup(gl_filterContent.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_filterContent.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblHeureDeFin)
							.addComponent(heureFin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblSep3)
							.addComponent(minutesFin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblSep4)
							.addComponent(secondesFin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(enableHeureFin))
					.addGap(4)
					.addGroup(gl_filterContent.createParallelGroup(Alignment.TRAILING)
						.addComponent(toolTipRapidite)
						.addComponent(enableRapidite))
					.addContainerGap())
		);
		filterContent.setLayout(gl_filterContent);
		
	
		getContentPane().add(filterContent);
			
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.add(Box.createHorizontalGlue());
		JButton validate = new JButton("Choisir les fichiers");
		
		validate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final VFileChooser fileChooser = new VFileChooser();
				fileChooser.setFileSelectionMode(VFileChooser.FILES_ONLY);
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.addChoosableFileFilter(new OPASFileFilter());
				fileChooser.addChoosableFileFilter(new LPLNFileFilter());
				fileChooser.addChoosableFileFilter(new FPLFileFilter());
				fileChooser.addChoosableFileFilter(new PLNSFileFilter());
				fileChooser.addChoosableFileFilter(new GEOFileFilter());
				if(fileChooser.showOpenDialog(TrajectoriesImportUI.this) == VFileChooser.APPROVE_OPTION){
					mainWindow.addTrajectoriesViews(
							fileChooser.getSelectedFiles(), 
							getFilters(), 
							isDisjuntive(), 
							enableRapidite.isSelected());
				}
				TrajectoriesImportUI.this.dispose();
			}
		});
		
		
		buttons.add(validate);
		JButton cancel = new JButton("Annuler");
		cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TrajectoriesImportUI.this.dispose();
			}
		});
		
		JPanel titleSecondStep = new JPanel();
		getContentPane().add(titleSecondStep);
		titleSecondStep.setLayout(new BorderLayout(0, 0));
		
		titleSecondStep.add(new TitledPanel("2. Sélectionner les fichiers à importer"), BorderLayout.NORTH);
		buttons.add(cancel);
		
		getContentPane().add(buttons);
				
		this.pack();
		
	}
	
	private boolean isDisjuntive(){
		return rdbtnOu.isSelected();
	}
	
	private List<TrajectoryFileFilter> getFilters(){
		if(filterYes.isSelected()){
			List<TrajectoryFileFilter> filters = new ArrayList<TrajectoryFileFilter>();

			if(!adep.getText().isEmpty()) filters.add(new TrajectoryFileFilter(TracksModel.FIELD_ADEP, adep.getText()));
			if(!adest.getText().isEmpty()) filters.add(new TrajectoryFileFilter(TracksModel.FIELD_ADEST, adest.getText()));
			if(!modeA.getText().isEmpty()) filters.add(new TrajectoryFileFilter(TracksModel.FIELD_MODE_A, modeA.getText()));
			if(enableHeureDebut.isSelected()){
				filters.add(new TrajectoryFileFilter(TracksModel.FIELD_TIME_BEGIN,
													heureDebut.getValue()+":"+minuteDebut.getValue()+":"+secondesDebut.getValue()));
			}
			if(enableHeureFin.isSelected()){
				filters.add(new TrajectoryFileFilter(TracksModel.FIELD_TIME_END,
												heureFin.getValue()+":"+minutesFin.getValue()+":"+secondesFin.getValue()));
			}
			if(polygonComboBox.getSelectedItem() != null){
				filters.add(new TrajectoryFileFilter(TracksModel.FIELD_POLYGON, (PolygonAnnotation)polygonComboBox.getSelectedItem()));
			}
			return filters;
		} else {
			return null;
		}
	}
}
