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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;

import fr.crnan.videso3d.ihm.components.JUpperCaseTextField;
import fr.crnan.videso3d.ihm.components.TypeComboBox;

/**
 * Advanced search panel for ITI
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class ItiSearchPanel extends AdvancedSearchPanel {
	private JTextField entreeField;
	private JTextField sortieField;
	private JTextField infField;
	private JTextField supField;
	private JTextField balisesField;
	private JButton btnNewButton;
	private JTextField balise1Field;
	private JTextField balise2Field;
	private JComboBox infComboBox;
	private JComboBox supComboBox;

	
	/**
	 * Create the panel.
	 */
	public ItiSearchPanel() {

		this.setPreferredSize(new Dimension(680, 180));

		ActionListener rechercheActionListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String[] balises = balisesField.getText().split(",");
				String[] criteria = new String[8+balises.length];
				
				criteria[0] = entreeField.getText();
				criteria[1] = sortieField.getText();
				criteria[2] = infField.getText();
				criteria[3] = supField.getText();
				criteria[4] = infComboBox.getSelectedItem().toString();
				criteria[5] = supComboBox.getSelectedItem().toString();
				criteria[6] = balise1Field.getText();
				criteria[7] = balise2Field.getText();
				for(int i = 8;i<8+balises.length;i++){
					criteria[i] = balises[i-8];
				}
				
				AnalyzeUI.showResults(true, "iti", criteria);
			}
		};
		
		entreeField = new JUpperCaseTextField();
		entreeField.setColumns(10);
		entreeField.addActionListener(rechercheActionListener);

		sortieField = new JUpperCaseTextField();
		sortieField.setColumns(10);
		sortieField.addActionListener(rechercheActionListener);

		btnNewButton = new JButton("Rechercher");
		btnNewButton.addActionListener(rechercheActionListener);

		String[] operateurs = {"=", ">=", "<="};
		infComboBox = new JComboBox(operateurs);
		supComboBox = new JComboBox(operateurs);

		infField = new JTextField();
		infField.setColumns(10);
		infField.addActionListener(rechercheActionListener);

		supField = new JTextField();
		supField.setColumns(10);
		supField.addActionListener(rechercheActionListener);
		
		balisesField = new JUpperCaseTextField();
		balisesField.setColumns(10);
		balisesField.addActionListener(rechercheActionListener);
		balisesField.setToolTipText("Iti comprenant toutes ces balises, séparées par une virgule.");
		
		balise1Field = new JUpperCaseTextField();
		balise1Field.setColumns(10);
		balise1Field.addActionListener(rechercheActionListener);
		balise1Field.setToolTipText("Uniquement les itis dont la première balise est celle-ci.");
		
		balise2Field = new JUpperCaseTextField();
		balise2Field.setColumns(10);
		balise2Field.addActionListener(rechercheActionListener);		
		balise2Field.setToolTipText("Uniquement les itis dont la dernière balise est celle-ci.");
		
		comboBox = new TypeComboBox();
		comboBox.setSelectedItem("iti");

		this.applyLayout();

	}

	private void applyLayout() {
		JLabel lblEntre = new JLabel("Entrée : ");
		JLabel lblSortie = new JLabel("Sortie : ");
		JLabel lblNiveauInf = new JLabel("Niveau Inf : ");
		JLabel lblNiveauSup = new JLabel("Niveau Sup : ");
		JLabel lblBalises = new JLabel("Balises :");
		JLabel lblObjetsRecherchs = new JLabel("Objets recherchés :");
		
		JLabel lblBalise = new JLabel("Première balise : ");
		JLabel lblBalise_1 = new JLabel("Dernière balise :");

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(lblEntre)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblNiveauInf)
									.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(infComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED))
								.addComponent(lblBalise)
								.addComponent(lblBalises))
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(balisesField, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
								.addGroup(groupLayout.createSequentialGroup()
									.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
										.addComponent(balise1Field, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
										.addComponent(infField, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
										.addComponent(entreeField, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE))
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
										.addGroup(groupLayout.createSequentialGroup()
											.addComponent(lblNiveauSup)
											.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
											.addComponent(supComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addComponent(lblSortie)
										.addComponent(lblBalise_1))
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(supField, GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
										.addComponent(sortieField, GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
										.addComponent(balise2Field, GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)))))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblObjetsRecherchs)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(252)))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btnNewButton)
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(btnNewButton)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblObjetsRecherchs)
								.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblEntre)
								.addComponent(entreeField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblSortie)
								.addComponent(sortieField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNiveauInf)
								.addComponent(infField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(supField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNiveauSup)
								.addComponent(infComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(supComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblBalise)
								.addComponent(balise1Field, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblBalise_1)
								.addComponent(balise2Field, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(balisesField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblBalises))))
					.addContainerGap(12, Short.MAX_VALUE))
		);
		setLayout(groupLayout);
	}
}
