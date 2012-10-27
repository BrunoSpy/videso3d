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


import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;

import fr.crnan.videso3d.ihm.components.TitledPanel;
import javax.swing.JSplitPane;
import javax.swing.JButton;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ButtonGroup;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
/**
 * Dialog to create a new request to the SQLite PLNS database
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class PLNSChartCreateUI extends JDialog {

	private boolean request = false;
	private int chartType = 0;
	private JTextArea reqArea;
	private JTextField titleField;
	private final ButtonGroup typeGraphGroup = new ButtonGroup();
	private JTextField ordonneesField;
	private JTextField abscissesField;
	
	public PLNSChartCreateUI(final Component parent){
		this.setTitle("Créer une nouvelle requête");
		
		this.setModal(true);
		
		this.setPreferredSize(new Dimension(600, 450));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.2);
		splitPane.setOneTouchExpandable(true);
		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		JPanel helpPanel = new JPanel();
		helpPanel.setPreferredSize(new Dimension(200, 400));
		splitPane.setLeftComponent(helpPanel);
		helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.Y_AXIS));
		helpPanel.add(new TitledPanel("Aide"));
		JLabel label = new JLabel("<html><br />"+
				"Dans la base <em>plns</em>, les tables suivantes (suivies de leurs champs) sont disponibles :<ul>" +
				"<li>balises (idpln, fl, heure)</li>" +
				"<li>plns (date, heure_dep, indicatif, code, code_prev, adep, adest, rfl, type, lp, cat_vol)</li>" +
				"<li>secteurs (idpln, secteur)</li>" +
				"<li>sls (idpln, sl)</li>" +
				"</ul>" +
				"<br />" +
				"Pour les graphes de type \"camembert\", la requête SQL doit renvoyer deux colonnes, la première représentant les catégories.<br/>" +
				"<br />" +
				"Pour les graphes de séries et les histogrammes, la requête doit renvoyer au moins deux colonnes, la première représentant l'axe des X, chaque colonne suivante sera utilisée pour une série.<br />" +
				"<br />"+
				"</html>");
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		helpPanel.add(label);
		
		Component verticalStrut = Box.createVerticalStrut(500);
		helpPanel.add(verticalStrut);
		
		JPanel requestPanel = new JPanel();
		splitPane.setRightComponent(requestPanel);
		requestPanel.setLayout(new BoxLayout(requestPanel, BoxLayout.Y_AXIS));
		
		JPanel typePanel = new TitledPanel("1. Type de graphique :");
		
		
		JPanel panel = new JPanel();
		panel.setAlignmentY(Component.TOP_ALIGNMENT);
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JRadioButton rdbtnHistogramme = new JRadioButton("Histogramme");
		rdbtnHistogramme.setActionCommand("2");
		typeGraphGroup.add(rdbtnHistogramme);
		panel.add(rdbtnHistogramme);
		
		JRadioButton rdbtnCamenbert = new JRadioButton("Camenbert");
		rdbtnCamenbert.setActionCommand("1");
		typeGraphGroup.add(rdbtnCamenbert);
		panel.add(rdbtnCamenbert);
		
		JRadioButton rdbtnSeries = new JRadioButton("Séries");
		rdbtnSeries.setActionCommand("0");
		typeGraphGroup.add(rdbtnSeries);
		panel.add(rdbtnSeries);
		
		ActionListener radioListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				chartType = new Integer(e.getActionCommand());
			}
		};
		rdbtnCamenbert.addActionListener(radioListener);
		rdbtnHistogramme.addActionListener(radioListener);
		rdbtnSeries.addActionListener(radioListener);
		
		typePanel.add(panel, BorderLayout.CENTER);
		
		requestPanel.add(typePanel);
		
		JPanel titlePanel = new TitledPanel("2. Titre du graphique :");
		titleField = new JTextField();
		titlePanel.add(titleField, BorderLayout.CENTER);
		
		requestPanel.add(titlePanel);
		
		JPanel axisTitles = new TitledPanel("3. Titre des axes :");
		
		
		requestPanel.add(axisTitles);
		
		JPanel axisContent = new JPanel();
		axisTitles.add(axisContent, BorderLayout.CENTER);
		
		JLabel lblAbscisses = new JLabel("Abscisses : ");
		
		JLabel lblOrdonnes = new JLabel("Ordonnées : ");
		
		ordonneesField = new JTextField();
		ordonneesField.setColumns(10);
		
		abscissesField = new JTextField();
		abscissesField.setColumns(10);
		GroupLayout gl_axisContent = new GroupLayout(axisContent);
		gl_axisContent.setHorizontalGroup(
			gl_axisContent.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_axisContent.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_axisContent.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_axisContent.createSequentialGroup()
							.addComponent(lblAbscisses)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(abscissesField, GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE))
						.addGroup(gl_axisContent.createSequentialGroup()
							.addComponent(lblOrdonnes)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(ordonneesField, GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
							.addGap(1))))
		);
		gl_axisContent.setVerticalGroup(
			gl_axisContent.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_axisContent.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_axisContent.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblAbscisses)
						.addComponent(abscissesField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_axisContent.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblOrdonnes)
						.addComponent(ordonneesField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
		);
		axisContent.setLayout(gl_axisContent);

		
		JPanel reqPanel = new TitledPanel("3. Requête : ");
		
		
		reqArea = new JTextArea();
		reqPanel.add(reqArea, BorderLayout.CENTER);
		
		requestPanel.add(reqPanel);
		
		JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		Component horizontalGlue = Box.createHorizontalGlue();
		buttonPanel.add(horizontalGlue);
		
		JButton validate = new JButton("Créer graphique");
		buttonPanel.add(validate);
		
		validate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				request = true;
				dispose();
			}
		});
		
		JButton cancel = new JButton("Annuler");
		buttonPanel.add(cancel);
		
		cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		this.pack();
	}
	/**
	 * Shows the dialog and returns <code>true</code> if a request is valid
	 * @param parent
	 * @return boolean
	 */
	public boolean showDialog(Component parent){
		this.setVisible(true);
		return request;
	}
	/**
	 * 
	 * @return 0 for a XYChart, 1 for a PieChart and 2 for a CategoryChart
	 */
	public int getChartType(){
		return chartType;
	}
	
	public String getRequest(){
		return reqArea != null ? reqArea.getText() : "";
	}
	
	public String getChartTitle(){
		return titleField != null ? titleField.getText() : "";
	}
	
	public String getAbscissesTitle(){
		return abscissesField.getText();
	}
	
	public String getOrdonneesTitle(){
		return ordonneesField.getText();
	}
}
