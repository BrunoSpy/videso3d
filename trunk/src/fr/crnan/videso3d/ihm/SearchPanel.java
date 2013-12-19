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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JFileChooser;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;
import javax.swing.JButton;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.formats.plns.PLNSFileFilter;
import fr.crnan.videso3d.ihm.components.JUpperCaseComboBox;
import fr.crnan.videso3d.ihm.components.TypeComboBox;
import fr.crnan.videso3d.ihm.components.VFileChooser;

/**
 * Default search panel
 * @author Bruno Spyckerelle
 * @version 0.1.2
 */
public class SearchPanel extends JPanel {
	private JUpperCaseComboBox searchField1;
	private JUpperCaseComboBox searchField2;
	private JTextField numLiaison;
	private JPanel defaultSearchPanel;
	private JComboBox<String> typeBox;
	private JButton btnRechercher;
	private JPanel liaisonPrivilegieePanel;
	private TypeComboBox typeBoxLP;
	private JButton btnRechercher2;	
	private JPanel plnsSearchPanel;
	private TypeComboBox typeBoxPLNS;
	private JPanel sectSearchPanel;
	private TypeComboBox typeBoxSect;
	private JUpperCaseComboBox searchFieldSect;
	private JButton btnRechercher3;	
	private JTextField choosePLNS;
	private JButton analyserPLNS;
	
	public SearchPanel() {
		
		this.setLayout(new CardLayout(0, 0));
		
		this.setPreferredSize(new Dimension(0,40));
		
		//différents types de panneau de recherche
		defaultSearchPanel = new JPanel();
		add(defaultSearchPanel, "default");
		//recherche spécifique pour les LPs
		liaisonPrivilegieePanel = new JPanel();
		add(liaisonPrivilegieePanel, "liaison");
		//recherche dans une base PLNS
		plnsSearchPanel = new JPanel();
		add(plnsSearchPanel, "plns");
		//recherche d'un secteur
		sectSearchPanel = new JPanel();
		add(sectSearchPanel, "secteur");
				
		typeBox = new TypeComboBox();
		typeBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				if(((JComboBox)e.getSource()).getSelectedItem().equals("liaison privilégiée")){
					((CardLayout) getLayout()).show(SearchPanel.this, "liaison");
				} else if(((JComboBox)e.getSource()).getSelectedItem().equals("base PLNS...")) {
					((CardLayout) getLayout()).show(SearchPanel.this, "plns");
				} else if(((JComboBox)e.getSource()).getSelectedItem().equals("secteur")) {
					((CardLayout) getLayout()).show(SearchPanel.this, "secteur");
				} else {
					((CardLayout) getLayout()).show(SearchPanel.this, "default");
				}
				typeBoxLP.setSelectedItem(((JComboBox)e.getSource()).getSelectedItem());
				typeBoxPLNS.setSelectedItem(((JComboBox)e.getSource()).getSelectedItem());
				typeBoxSect.setSelectedItem(((JComboBox)e.getSource()).getSelectedItem());

			}
		});
		
		//Liste des balises pour l'autocomplétion
		Vector<String> results = getAllStipItems();
		
		searchField1 = new JUpperCaseComboBox(new DefaultComboBoxModel<String>(results));
		searchField1.setEditable(true);
		searchField1.setToolTipText("<html>Nom de la balise ou du terrain recherché.<br />Exemple : LF* renverra toutes les informations sur les terrains français.</html>");
		AutoCompleteDecorator.decorate(searchField1);
		
		@SuppressWarnings("unchecked")
		Vector<String> results2 = (Vector<String>) results.clone();
		searchField2 = new JUpperCaseComboBox(new DefaultComboBoxModel<String>(results2));
		searchField2.setEditable(true);
		AutoCompleteDecorator.decorate(searchField2);
		btnRechercher = new JButton("Rechercher");
		
		
		typeBoxPLNS = new TypeComboBox();
		typeBoxPLNS.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				if(e.getSource() instanceof JComboBox<?>){
					JComboBox<String> source = (JComboBox<String>) e.getSource();
					
					if(source.getSelectedItem().equals("liaison privilégiée")){
						((CardLayout) getLayout()).show(SearchPanel.this, "liaison");
					} else if(source.getSelectedItem().equals("base PLNS...")) {
						((CardLayout) getLayout()).show(SearchPanel.this, "plns");
					} else if(source.getSelectedItem().equals("secteur")) {
						((CardLayout) getLayout()).show(SearchPanel.this, "secteur");
					} else {
						((CardLayout) getLayout()).show(SearchPanel.this, "default");
					}
					typeBoxLP.setSelectedItem(source.getSelectedItem());
					typeBoxSect.setSelectedItem(source.getSelectedItem());
					typeBox.setSelectedItem(source.getSelectedItem());
				}
			}
		});
		choosePLNS = new JTextField(20);
		choosePLNS.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {}
			
			@Override
			public void mousePressed(MouseEvent arg0) {}
			
			@Override
			public void mouseExited(MouseEvent arg0) {}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {	}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				VFileChooser file = new VFileChooser();
				file.setFileSelectionMode(JFileChooser.FILES_ONLY);
				file.setMultiSelectionEnabled(true);
				file.addChoosableFileFilter(new PLNSFileFilter());
				if(file.showOpenDialog(null) == VFileChooser.APPROVE_OPTION){
					String choosePLNSText = "";
					for(File f : file.getSelectedFiles()){
						choosePLNSText+=";"+f.getAbsolutePath();
					}
					choosePLNSText = choosePLNSText.substring(1);
					choosePLNS.setText(choosePLNSText);
				}
			}
		});
		analyserPLNS = new JButton("Analyser");
		
		
		typeBoxLP = new TypeComboBox();
		typeBoxLP.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				if(((JComboBox)e.getSource()).getSelectedItem().equals("liaison privilégiée")){
					((CardLayout) getLayout()).show(SearchPanel.this, "liaison");
				} else if(((JComboBox)e.getSource()).getSelectedItem().equals("base PLNS...")) {
					((CardLayout) getLayout()).show(SearchPanel.this, "plns");
				} else if(((JComboBox)e.getSource()).getSelectedItem().equals("secteur")) {
					((CardLayout) getLayout()).show(SearchPanel.this, "secteur");
				} else {
					((CardLayout) getLayout()).show(SearchPanel.this, "default");
				}
				typeBoxPLNS.setSelectedItem(((JComboBox)e.getSource()).getSelectedItem());
				typeBoxSect.setSelectedItem(((JComboBox)e.getSource()).getSelectedItem());
				typeBox.setSelectedItem(((JComboBox)e.getSource()).getSelectedItem());
			}
		});

		numLiaison = new JTextField();
		numLiaison.setColumns(10);
		numLiaison.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnRechercher2.doClick();
			}
		});
		btnRechercher2 = new JButton("Rechercher");
		
		
		typeBoxSect = new TypeComboBox();
		typeBoxSect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				if(((JComboBox)e.getSource()).getSelectedItem().equals("liaison privilégiée")){
					((CardLayout) getLayout()).show(SearchPanel.this, "liaison");
				} else if(((JComboBox)e.getSource()).getSelectedItem().equals("base PLNS...")) {
					((CardLayout) getLayout()).show(SearchPanel.this, "plns");
				} else if(((JComboBox)e.getSource()).getSelectedItem().equals("secteur")) {
					((CardLayout) getLayout()).show(SearchPanel.this, "secteur");
				} else {
					((CardLayout) getLayout()).show(SearchPanel.this, "default");
				}
				typeBoxPLNS.setSelectedItem(((JComboBox)e.getSource()).getSelectedItem());
				typeBoxLP.setSelectedItem(((JComboBox)e.getSource()).getSelectedItem());
				typeBox.setSelectedItem(((JComboBox)e.getSource()).getSelectedItem());
			}
		});

		//Liste des balises pour l'autocomplétion
		Vector<String> resultsSect = getAllSecteurs();

		searchFieldSect = new JUpperCaseComboBox(new DefaultComboBoxModel<String>(resultsSect));
		searchFieldSect.setEditable(true);
		searchFieldSect.setToolTipText("<html>Nom du secteur.</html>");
		AutoCompleteDecorator.decorate(searchFieldSect);
		searchFieldSect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if("comboBoxEdited".equals(e.getActionCommand())){
					btnRechercher3.doClick();
				}
			}
		});
		btnRechercher3 = new JButton("Rechercher");
		

		
		
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
							"<br /><i>Astuces : </i><br />- Pour rechercher tous les itis qui contiennent une balise commençant par OM, mettre OM*." +
							"<br />- Pour rechercher les itis contenant un travers BALISE, taper BALISE/" +
							"<br />- Pour chercher uniquement les itis contenant une balise non travers, taper BALISE\\</html>");
					searchField2.setToolTipText("<html>Deuxième balise recherchée.<br />Recherche aussi dans les sorties des itis.</html>");
				} else if(item.equals("trajet")){
					searchField1.setToolTipText("<html>Première balise recherchée." +
							"<br /><i>Astuces : </i><br />- Pour rechercher tous les trajets qui contiennent une balise commençant par OM, mettre OM*." +
							"<br />- Pour rechercher les trajets contenant un travers BALISE, taper BALISE/" +
							"<br />- Pour chercher uniquement les trajets contenant une balise non travers, taper BALISE\\</html>");
					searchField2.setToolTipText("<html>Deuxième balise recherchée.</html>");
				} else if(item.equals("route")){
					searchField1.setToolTipText("<html>Première balise recherchée.<br />Recherche aussi dans les noms de route si il n'y a pas de deuxième balise recherchée.<br />" +
							"<br /><i>Astuces : </i><br />- Pour rechercher toutes les routes qui contiennent une balise commençant par OM, mettre OM*." +
							"<br />- Pour rechercher les routes contenant un travers BALISE, taper BALISE/" +
							"<br />- Pour chercher uniquement les routes contenant une balise non travers, taper BALISE\\</html>");
					searchField2.setToolTipText("<html>Deuxième balise recherchée.</html>");
				} else if(item.equals("connexion")){
					searchField1.setToolTipText("<html>Première balise recherchée.<br />" +
							"<br /><i>Astuces : </i><br />- Pour rechercher toutes les connexions qui contiennent une balise commençant par OM, mettre OM*." +
							"<br />- Pour rechercher les connexions contenant un travers BALISE, taper BALISE/" +
							"<br />- Pour chercher uniquement les connexions contenant une balise non travers, taper BALISE\\</html>");
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
		
		
		
		//Action sur les boutons rechercher
		ActionListener rechercheListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!searchField1.getSelectedItem().toString().isEmpty() || !searchField2.getSelectedItem().toString().isEmpty() || !numLiaison.getText().isEmpty()
						|| !choosePLNS.getText().isEmpty() || !searchFieldSect.getSelectedItem().toString().isEmpty()){
					AnalyzeUI.showResults(false, typeBox.getSelectedItem().toString(),
										  searchField1.getSelectedItem().toString(),
										  searchField2.getSelectedItem().toString(),
										  numLiaison.getText(), 
										  choosePLNS.getText(),
										  searchFieldSect.getSelectedItem().toString());
				}
			}
		};
		
		btnRechercher.addActionListener(rechercheListener);
		btnRechercher2.addActionListener(rechercheListener);
		btnRechercher3.addActionListener(rechercheListener);
		analyserPLNS.addActionListener(rechercheListener);
		
		
	}
	
	private void applyLayout(){
		JLabel lblObjetsRecherchs = new JLabel("Objets à analyser : ");
		JLabel lblContenant = new JLabel("contenant");
		JLabel lblEt = new JLabel("et");
		JLabel lblNumro = new JLabel("numéro");
		JLabel lblBigramme = new JLabel("bigramme");
		JLabel lblObjetsRecherchs2 = new JLabel("Objets à analyser : ");
		JLabel lblObjetsRecherchs3 = new JLabel("Objets à analyser : ");
		JLabel lblObjetsRecherchs4 = new JLabel("Objets à analyser : ");
		
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
					.addComponent(typeBoxLP, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
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
						.addComponent(typeBoxLP, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNumro)
						.addComponent(numLiaison, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnRechercher2))
					.addContainerGap(25, Short.MAX_VALUE))
		);
		liaisonPrivilegieePanel.setLayout(gl_liaisonPrivilegieePanel);
		
		GroupLayout gl_PLNSPanel = new GroupLayout(plnsSearchPanel);
		gl_PLNSPanel.setHorizontalGroup(
				gl_PLNSPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_PLNSPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblObjetsRecherchs3)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(typeBoxPLNS, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(choosePLNS, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(analyserPLNS, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(180, Short.MAX_VALUE))
		);
		gl_PLNSPanel.setVerticalGroup(
				gl_PLNSPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_PLNSPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_PLNSPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblObjetsRecherchs3)
						.addComponent(typeBoxPLNS, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(choosePLNS, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(analyserPLNS))
					.addContainerGap(25, Short.MAX_VALUE))
		);
		plnsSearchPanel.setLayout(gl_PLNSPanel);
		
		
		GroupLayout gl_sectSearchPanel = new GroupLayout(sectSearchPanel);
		gl_sectSearchPanel.setHorizontalGroup(
				gl_sectSearchPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_sectSearchPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblObjetsRecherchs4)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(typeBoxSect, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblBigramme)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(searchFieldSect, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnRechercher3, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(180, Short.MAX_VALUE))
		);
		gl_sectSearchPanel.setVerticalGroup(
				gl_sectSearchPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_sectSearchPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_sectSearchPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblObjetsRecherchs4)
						.addComponent(typeBoxSect, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblBigramme)
						.addComponent(searchFieldSect, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnRechercher3))
					.addContainerGap(25, Short.MAX_VALUE))
		);
		sectSearchPanel.setLayout(gl_sectSearchPanel);
	}
	
	public String getType(){
		return (String) typeBox.getSelectedItem();
	}
	
	public JComboBox<String> getTypeComboBox(){
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
	
	/**
	 * 
	 * @return Un vecteur contenant tous les noms des secteurs STIP, avec une chaîne de caratères vide en première position.
	 */
	public Vector<String> getAllSecteurs(){
		Vector<String> results = new Vector<String>();
		results.add("");
		try {
			Statement st = DatabaseManager.getCurrentStip();
			if(st != null){
				ResultSet rs = st.executeQuery("select nom from secteurs order by nom");
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
		searchField1.setModel(new DefaultComboBoxModel<String>(results1));
		@SuppressWarnings("unchecked")
		Vector<String> results2 = (Vector<String>) results1.clone();
		searchField2.setModel(new DefaultComboBoxModel<String>(results2));
		
		
	}
}
