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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;

import javax.swing.BoxLayout;

import fr.crnan.videso3d.formats.fpl.FPLFileFilter;
import fr.crnan.videso3d.formats.geo.GEOFileFilter;
import fr.crnan.videso3d.formats.lpln.LPLNFileFilter;
import fr.crnan.videso3d.formats.opas.OPASFileFilter;
import fr.crnan.videso3d.formats.plns.PLNSFileFilter;
import fr.crnan.videso3d.ihm.components.TitledPanel;
import fr.crnan.videso3d.ihm.components.VFileChooser;
import fr.crnan.videso3d.ihm.components.VSpinner;
import fr.crnan.videso3d.trajectography.TracksModel;
import fr.crnan.videso3d.trajectography.TrajectoryFileFilter;

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;
import java.awt.Component;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;


/**
 * IHM to filter and import trajectories
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class TrajectoriesImportUI extends JDialog {
	
	private final ButtonGroup typeBtnGrp = new ButtonGroup();
	private JTextField adep;
	private JTextField adest;
	private JRadioButton rdbtnOu;
	private JRadioButton rdbtnEt;
	private JRadioButton filterYes; 
	private JTextField modeA;
	
	private VSpinner heureDebut;
	private VSpinner minuteDebut;
	private VSpinner secondesDebut;
	private VSpinner heureFin;
	private VSpinner minutesFin;
	private VSpinner secondesFin;
	
	private JCheckBox enableHeureDebut;
	private JCheckBox enableHeureFin;
	
	private final ButtonGroup filterbtnGrp = new ButtonGroup();
	
	public TrajectoriesImportUI(final MainWindow mainWindow) {
			
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
		
		GroupLayout gl_panel = new GroupLayout(filterContent);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(lblActiverLesFiltres)
						.addGroup(gl_panel.createSequentialGroup()
							.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
								.addComponent(lblAroportArrive, Alignment.LEADING)
								.addComponent(lblAroportDpart, Alignment.LEADING)
								.addComponent(lblFiltrerLesTrajectoires, Alignment.LEADING)
								.addComponent(lblModeA, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
								.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
									.addComponent(lblHeureDebut)
									.addPreferredGap(ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
									.addComponent(enableHeureDebut))
								.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
									.addComponent(lblHeureDeFin)
									.addPreferredGap(ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
									.addComponent(enableHeureFin)
									.addPreferredGap(ComponentPlacement.RELATED)))
							.addGap(6)
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel.createSequentialGroup()
									.addComponent(heureFin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(lblSep3)
									.addComponent(minutesFin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(lblSep4)
									.addComponent(secondesFin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_panel.createSequentialGroup()
									.addComponent(heureDebut, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(lblSep1)
									.addComponent(minuteDebut, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(lblSep2)
									.addComponent(secondesDebut, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addComponent(adest, GroupLayout.PREFERRED_SIZE, 108, GroupLayout.PREFERRED_SIZE)
								.addComponent(modeA, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE)
								.addComponent(rdbtnEt)
								.addComponent(filterYes)
								.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
									.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
										.addComponent(filterNo)
										.addComponent(rdbtnOu))
									.addComponent(adep, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE))))))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel.createSequentialGroup()
									.addComponent(filterNo)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(rdbtnOu))
								.addGroup(gl_panel.createSequentialGroup()
									.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
										.addComponent(lblActiverLesFiltres)
										.addComponent(filterYes))
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_panel.createSequentialGroup()
											.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
												.addComponent(lblFiltrerLesTrajectoires)
												.addComponent(rdbtnEt))
											.addPreferredGap(ComponentPlacement.RELATED)
											.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
												.addComponent(adep, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(lblAroportDpart)))
										.addGroup(gl_panel.createSequentialGroup()
											.addGap(58)
											.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
												.addComponent(lblAroportArrive)
												.addComponent(adest, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
										.addComponent(modeA, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(lblModeA))))
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
										.addComponent(heureDebut, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(lblSep1)
										.addComponent(minuteDebut, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(lblSep2)
										.addComponent(secondesDebut, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
								.addGroup(gl_panel.createSequentialGroup()
									.addGap(12)
									.addComponent(lblHeureDebut)))
							.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addGroup(Alignment.TRAILING, gl_panel.createSequentialGroup()
							.addContainerGap()
							.addComponent(enableHeureDebut)
							.addGap(10)))
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
							.addComponent(heureFin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblSep3)
							.addComponent(minutesFin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblSep4)
							.addComponent(secondesFin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(6)
							.addComponent(lblHeureDeFin))
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(4)
							.addComponent(enableHeureFin)))
					.addContainerGap())
		);
		gl_panel.linkSize(SwingConstants.HORIZONTAL, new Component[] {adep, adest, modeA});
		filterContent.setLayout(gl_panel);
		
	
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
				if(fileChooser.showOpenDialog(getThis()) == VFileChooser.APPROVE_OPTION){
					
					new SwingWorker<String, Integer>(){
						@Override
						protected String doInBackground() throws Exception {
							try {
								mainWindow.addTrajectoriesViews(fileChooser.getSelectedFiles(), getFilters(), isDisjuntive());
							} catch(Exception e1){
								e1.printStackTrace();
							}
							return null;
						}
					}.execute();

				}
				getThis().dispose();
			}
		});
		
		
		buttons.add(validate);
		JButton cancel = new JButton("Annuler");
		cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getThis().dispose();
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
			if(!modeA.getText().isEmpty()) filters.add(new TrajectoryFileFilter(TracksModel.FIELD_TYPE_MODE_A, modeA.getText()));
			if(enableHeureDebut.isSelected()){
				filters.add(new TrajectoryFileFilter(TracksModel.FIELD_TYPE_TIME_BEGIN,
													heureDebut.getValue()+":"+minuteDebut.getValue()+":"+secondesDebut.getValue()));
			}
			if(enableHeureFin.isSelected()){
				filters.add(new TrajectoryFileFilter(TracksModel.FIELD_TYPE_TIME_END,
												heureFin.getValue()+":"+minutesFin.getValue()+":"+secondesFin.getValue()));
			}
			return filters;
		} else {
			return null;
		}
	}
	
	private JDialog getThis(){
		return this;
	}
}
