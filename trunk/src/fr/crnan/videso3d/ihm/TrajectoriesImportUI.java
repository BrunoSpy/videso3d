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
import fr.crnan.videso3d.ihm.components.VFileChooser;
import fr.crnan.videso3d.trajectography.TracksModel;
import fr.crnan.videso3d.trajectography.TrajectoryFileFilter;

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingWorker;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;

/**
 * IHM to filter and import trajectories
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class TrajectoriesImportUI extends JDialog {
	
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JTextField adep;
	private JTextField adest;
	private JRadioButton rdbtnOu;
	
	public TrajectoriesImportUI(final MainWindow mainWindow) {
			
		this.setTitle("Importer et filtrer des trajectoires");
		
		getContentPane().setLayout(new BorderLayout());
		
//		JPanel filter = new JPanel();
//		filter.setLayout(new BorderLayout());
//		filter.add(new TitledPanel("1. Filtrer les trajectoires"), BorderLayout.NORTH);

		JPanel filterContent = new JPanel();

		JLabel lblFiltrerLesTrajectoires = new JLabel("Type de filtre :");

		JRadioButton rdbtnEt = new JRadioButton("Et");
		buttonGroup.add(rdbtnEt);

		rdbtnOu = new JRadioButton("Ou");
		rdbtnOu.setSelected(true);
		buttonGroup.add(rdbtnOu);
		
		JLabel lblAroportDpart = new JLabel("Aéroport départ :");
		
		adep = new JTextField();
		adep.setColumns(10);
		
		JLabel lblAroportArrive = new JLabel("Aéroport arrivée :");
		
		adest = new JTextField();
		adest.setColumns(10);
		GroupLayout gl_panel = new GroupLayout(filterContent);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(lblFiltrerLesTrajectoires)
						.addComponent(lblAroportDpart)
						.addComponent(lblAroportArrive))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(adep, GroupLayout.PREFERRED_SIZE, 108, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(rdbtnEt)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(rdbtnOu))
						.addComponent(adest, 108, 108, 108))
					.addContainerGap(239, Short.MAX_VALUE))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblFiltrerLesTrajectoires)
						.addComponent(rdbtnEt)
						.addComponent(rdbtnOu))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(adep, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblAroportDpart))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(adest, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblAroportArrive))
					)
		);
		filterContent.setLayout(gl_panel);
		
//		filter.add(filterContent, BorderLayout.CENTER);
		
		
		getContentPane().add(filterContent, BorderLayout.CENTER);
		
//		JPanel files = new JPanel();
//		files.setLayout(new BorderLayout());
//		files.add(new TitledPanel("2. Choisir les fichiers à importer"), BorderLayout.NORTH);
		
//		JPanel filesContent = new JPanel();
		
//		files.add(filesContent, BorderLayout.CENTER);
		
//		getContentPane().add(files);
		
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
		buttons.add(cancel);
		
		getContentPane().add(buttons, BorderLayout.SOUTH);
				
		this.pack();
		
	}
	
	private boolean isDisjuntive(){
		return rdbtnOu.isSelected();
	}
	
	private List<TrajectoryFileFilter> getFilters(){
		List<TrajectoryFileFilter> filters = new ArrayList<TrajectoryFileFilter>();
		if(!adep.getText().isEmpty()) filters.add(new TrajectoryFileFilter(TracksModel.FIELD_ADEP, adep.getText()));
		if(!adest.getText().isEmpty()) filters.add(new TrajectoryFileFilter(TracksModel.FIELD_ADEST, adest.getText()));
		return filters;
	}
	
	private JDialog getThis(){
		return this;
	}
}
