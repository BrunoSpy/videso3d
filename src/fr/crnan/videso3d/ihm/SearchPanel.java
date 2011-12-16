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

import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;
import javax.swing.JButton;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.ihm.components.JUpperCaseComboBox;
import fr.crnan.videso3d.ihm.components.TypeComboBox;

/**
 * Default search panel
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class SearchPanel extends JPanel {
	private JUpperCaseComboBox searchField1;
	private JUpperCaseComboBox searchField2;
	private JTextField numLiaison;
	private JPanel defaultSearchPanel;
	private JComboBox typeBox;
	private JButton btnRechercher;
	private JPanel liaisonPrivilegieePanel;
	private TypeComboBox typeBox2;
	private JButton btnRechercher2;	
	
	public SearchPanel() {
		
		this.setLayout(new CardLayout(0, 0));
		
		this.setPreferredSize(new Dimension(0,40));
		
		defaultSearchPanel = new JPanel();
		add(defaultSearchPanel, "default");
		
		
		typeBox = new TypeComboBox();
		typeBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				if(((JComboBox)e.getSource()).getSelectedItem().equals("liaison privilégiée")){
					((CardLayout) getLayout()).show(getThis(), "liaison");
				} else {
					((CardLayout) getLayout()).show(getThis(), "default");
				}

				typeBox2.setSelectedItem(((JComboBox)e.getSource()).getSelectedItem());
			}
		});
		
		//Liste des balises pour l'autocomplétion
		Vector<String> results = getAllStipItems();
		
		searchField1 = new JUpperCaseComboBox(new DefaultComboBoxModel(results));
		searchField1.setEditable(true);
		searchField1.setToolTipText("<html>Nom de la balise ou du terrain recherché.<br />Exemple : LF* renverra toutes les informations sur les terrains français.</html>");
		AutoCompleteDecorator.decorate(searchField1);
		
		@SuppressWarnings("unchecked")
		Vector<String> results2 = (Vector<String>) results.clone();
		searchField2 = new JUpperCaseComboBox(new DefaultComboBoxModel(results2));
		searchField2.setEditable(true);
		AutoCompleteDecorator.decorate(searchField2);
		
		btnRechercher = new JButton("Rechercher");
		
		liaisonPrivilegieePanel = new JPanel();
		add(liaisonPrivilegieePanel, "liaison");
		
		typeBox2 = new TypeComboBox();
		typeBox2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				if(((JComboBox)e.getSource()).getSelectedItem().equals("liaison privilégiée")){
					((CardLayout) getLayout()).show(getThis(), "liaison");
				} else {
					((CardLayout) getLayout()).show(getThis(), "default");
				}

				typeBox.setSelectedItem(((JComboBox)e.getSource()).getSelectedItem());
			}
		});

		numLiaison = new JTextField();
		numLiaison.setColumns(10);
		btnRechercher2 = new JButton("Rechercher");
		
		this.applyLayout();
		
		//Ajout des tooltips en fonction du type de recherche
		typeBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String item = (String)((JComboBox)e.getSource()).getSelectedItem();
				if(item.equals("balise")){
					searchField1.setToolTipText("<html>Nom de la balise ou du terrain recherché.<br />Exemple : LF* renverra toutes les informations sur les terrains français.</html>");
					searchField2.setToolTipText("Sans objet.");
				} else if(item.equals("iti")){
					searchField1.setToolTipText("<html>Première balise recherchée.<br />Recherche aussi dans les entrées des itis." +
							"<br /><i>Astuce : </i>Pour rechercher tous les itis qui contiennent une balise commençant par OM, mettre OM*.</html>");
					searchField2.setToolTipText("<html>Deuxième balise recherchée.<br />Recherche aussi dans les sorties des itis.</html>");
				} else if(item.equals("trajet")){
					searchField1.setToolTipText("<html>Première balise recherchée." +
							"<br /><i>Astuce : </i>Pour rechercher tous les trajets qui contiennent une balise commençant par OM, mettre OM*.</html>");
					searchField2.setToolTipText("<html>Deuxième balise recherchée.</html>");
				} else if(item.equals("route")){
					searchField1.setToolTipText("<html>Première balise recherchée.<br />Recherche aussi dnas les nom de route si il n'y a pas de deuxième balise recherchée.<br />" +
							"<br /><i>Astuce : </i>Pour rechercher toutes les routes qui contiennent une balise commençant par OM, mettre OM*.</html>");
					searchField2.setToolTipText("<html>Deuxième balise recherchée.</html>");
				}
			}
		});

		//un appui sur la touche entrée lance la recherche
		searchField1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if("comboBoxEdited".equals(e.getActionCommand())){
					btnRechercher.doClick();
				}
			}
		});
		searchField2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if("comboBoxEdited".equals(e.getActionCommand())){
					btnRechercher.doClick();
				}
			}
		});
		numLiaison.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				btnRechercher2.doClick();
			}
		});
		
		//Action sur les boutons rechercher
		ActionListener rechercheListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!searchField1.getSelectedItem().toString().isEmpty() || !searchField2.getSelectedItem().toString().isEmpty() ||!numLiaison.getText().isEmpty()){
					AnalyzeUI.showResults(false, typeBox.getSelectedItem().toString(),
										  searchField1.getSelectedItem().toString(),
										  searchField2.getSelectedItem().toString(),
										  numLiaison.getText());
				}
			}
		};
		
		btnRechercher.addActionListener(rechercheListener);
		btnRechercher2.addActionListener(rechercheListener);
	}
	
	private void applyLayout(){
		JLabel lblObjetsRecherchs = new JLabel("Objets recherchés : ");
		JLabel lblContenant = new JLabel("contenant");
		JLabel lblEt = new JLabel("et");
		JLabel lblNumro = new JLabel("numéro");
		JLabel lblObjetsRecherchs2 = new JLabel("Objets recherchés : ");
		
		GroupLayout gl_defaultSearchPanel = new GroupLayout(defaultSearchPanel);
		gl_defaultSearchPanel.setHorizontalGroup(
			gl_defaultSearchPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_defaultSearchPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblObjetsRecherchs)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(typeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblContenant)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(searchField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblEt)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(searchField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnRechercher)
					.addContainerGap(290, Short.MAX_VALUE))
		);
		gl_defaultSearchPanel.setVerticalGroup(
			gl_defaultSearchPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_defaultSearchPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_defaultSearchPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblObjetsRecherchs)
						.addComponent(typeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblContenant)
						.addComponent(searchField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblEt)
						.addComponent(searchField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnRechercher))
					.addContainerGap(70, Short.MAX_VALUE))
		);
		defaultSearchPanel.setLayout(gl_defaultSearchPanel);
		
		GroupLayout gl_liaisonPrivilegieePanel = new GroupLayout(liaisonPrivilegieePanel);
		gl_liaisonPrivilegieePanel.setHorizontalGroup(
			gl_liaisonPrivilegieePanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_liaisonPrivilegieePanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblObjetsRecherchs2)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(typeBox2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblNumro)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(numLiaison, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnRechercher2, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(180, Short.MAX_VALUE))
		);
		gl_liaisonPrivilegieePanel.setVerticalGroup(
			gl_liaisonPrivilegieePanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_liaisonPrivilegieePanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_liaisonPrivilegieePanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblObjetsRecherchs2)
						.addComponent(typeBox2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNumro)
						.addComponent(numLiaison, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnRechercher2))
					.addContainerGap(25, Short.MAX_VALUE))
		);
		liaisonPrivilegieePanel.setLayout(gl_liaisonPrivilegieePanel);
	}
	
	private JPanel getThis(){
		return this;
	}
	
	public String getType(){
		return (String) typeBox.getSelectedItem();
	}
	
	public JComboBox getTypeComboBox(){
		return typeBox;
	}
	
	/**
	 * 
	 * @return Un vecteur contenant tous les noms des balises et des routes STIP, avec une chaîne de caratères vide en première position.
	 */
	public Vector<String> getAllStipItems(){
		Vector<String> results = new Vector<String>();
		results.add("");
		try {
			Statement st = DatabaseManager.getCurrentStip();
			if(st != null){
				ResultSet rs = st.executeQuery("select name from balises UNION select name from routes" /*UNION select nom from secteurs*/);
				while(rs.next()){
					results.add(rs.getString(1));
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return results;
	}
	
	public void updateSearchBoxes(){
		Vector<String> results1 = getAllStipItems();
		searchField1.setModel(new DefaultComboBoxModel(results1));
		@SuppressWarnings("unchecked")
		Vector<String> results2 = (Vector<String>) results1.clone();
		searchField2.setModel(new DefaultComboBoxModel(results2));
		
		
	}
}
