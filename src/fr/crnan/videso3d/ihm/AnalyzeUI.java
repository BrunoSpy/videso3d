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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.Videso3D;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.graphs.ConnexPanel;
import fr.crnan.videso3d.graphs.ItiPanel;
import fr.crnan.videso3d.graphs.RoutePanel;
import fr.crnan.videso3d.graphs.StarPanel;
import fr.crnan.videso3d.graphs.TrajetPanel;
import fr.crnan.videso3d.ihm.components.ButtonTabComponent;
import fr.crnan.videso3d.stip.StipController;
/**
 * Fenêtre d'analyse des données Stip et Stpv.<br />
 * Cette classe est un singleton afin de n'être ouverte qu'une fois maximum.
 * @author Bruno Spyckerelle
 * @version 0.2.2
 */
public final class AnalyzeUI extends JFrame {

	private static AnalyzeUI instance = null;

	private JTabbedPane tabPane = new JTabbedPane();

	private ContextPanel context = new ContextPanel();

	private JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, context, tabPane);

	private JLabel nombreResultats = new JLabel();

	public final static AnalyzeUI getInstance(){
		if(instance == null){
			instance = new AnalyzeUI();
		}
		return instance;
	}

	public static void showAnalyzeUI(){
		getInstance().setVisible(true);
	}

	/**
	 * Ajoute un tab de résultats et ouvre la fenêtre si besoin
	 * @param type Type de recherche
	 * @param balise1 Première balise cherchée
	 * @param balise2 Deuxième balise cherchée (optionnel)
	 */
	public final static void showResults(String type, String balise1, String balise2){
		ResultPanel content = getInstance().createResultPanel(type, balise1, balise2);
		content.setContext(getInstance().context);
		content.addPropertyChangeListener(ResultPanel.PROPERTY_RESULT, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				getInstance().nombreResultats.setText(evt.getNewValue().toString());
			}
		});

		getInstance().tabPane.addTab(type+" "+balise1+(balise2.isEmpty() ? "" : "+"+balise2), content);

		ButtonTabComponent buttonTab = new ButtonTabComponent(getInstance().tabPane);
		getInstance().tabPane.setTabComponentAt(getInstance().tabPane.indexOfComponent(content), buttonTab);
		getInstance().tabPane.setSelectedIndex(getInstance().tabPane.indexOfComponent(content));

		getInstance().setVisible(true);

		//si type balise, on affiche les infos contextuelles sur cette balise
		if(type.equals("balise")){
			try {
				if(DatabaseManager.getCurrentStip().executeQuery("select * from balises where name = '"+balise1+"'").next()) {
					getInstance().context.showInfo(Type.STIP, StipController.BALISES, balise1);
				} 
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Ajoute un tab de résultats et ouvre la fenêtre si besoin
	 * @param type Type de recherche
	 * @param balise Balise cherchée
	 */
	public final static void showResults(String type, String balise){
		showResults(type, balise, "");
	}

	private AnalyzeUI(){
		super();
		this.setLayout(new BorderLayout());

		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/videso3d.png")));

		this.setTitle("Videso - Analyse ("+Videso3D.VERSION+")");

		this.add(this.createToolbar(), BorderLayout.PAGE_START);

		this.add(this.createStatusBar(), BorderLayout.PAGE_END);

		splitpane.setOneTouchExpandable(true);
		splitpane.setContinuousLayout(true);

		this.getContentPane().add(splitpane);
		this.pack();
		//FullScreen
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);

	}	

	private ResultPanel createResultPanel(final String type, final String search, final String search2){

		if(type.equals("iti")){
			return new ItiPanel(search, search2);
		} else if(type.equals("route")){
			return new RoutePanel(search, search2);
		} else if(type.equals("trajet")){
			return new TrajetPanel(search, search2);
		} else if(type.equals("balise")){
			return new BaliseResultPanel(search);
		} else if(type.equals("connexion")){
			return new ConnexPanel(search, search2);
		} else if(type.equals("stars")){
			return new StarPanel(search, search2);
		}
		return null;
	}

	private JToolBar createToolbar(){

		JToolBar toolbar = new JToolBar();

		toolbar.add(new JLabel(" Rechercher : "));

		String[] types = {"balise", /*"balint",*/ "iti", "trajet", "route", "connexion", "stars"};

		final JComboBox type = new JComboBox(types);

		toolbar.add(type);

		toolbar.add(new JLabel(" contenant : "));


		LinkedList<String> results = new LinkedList<String>();
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
		final JComboBox search = new JComboBox(results.toArray());
		search.setEditable(true);
		search.setToolTipText("<html>Nom de la balise ou du terrain recherché.<br />Exemple : LF* renverra toutes les informations sur les terrains français.</html>");
	
		AutoCompleteDecorator.decorate(search);

		toolbar.add(search);

		toolbar.add(new JLabel(" et : "));

		//nouvelle liste de résultat avec un résultat vide en première position
		LinkedList<String> results2 = (LinkedList<String>) results.clone();
		results2.addFirst("");
		final JComboBox search2 = new JComboBox((results2).toArray());
		search2.setEditable(true);
		search2.setToolTipText("Sans objet.");
		AutoCompleteDecorator.decorate(search2);

		toolbar.add(search2);

		final JButton newSearch = new JButton("Nouvelle recherche");
		newSearch.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(!search.getSelectedItem().toString().isEmpty() || !search2.getSelectedItem().toString().isEmpty()){
					showResults(type.getSelectedItem().toString(), search.getSelectedItem().toString(), search2.getSelectedItem().toString());
				}
			}
		});

		toolbar.add(newSearch);


		//Ajout des tooltips en fonction du type de recherche
		type.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String item = (String)((JComboBox)e.getSource()).getSelectedItem();
				if(item.equals("balise")){
					search.setToolTipText("<html>Nom de la balise ou du terrain recherché.<br />Exemple : LF* renverra toutes les informations sur les terrains français.</html>");
					search2.setToolTipText("Sans objet.");
				} else if(item.equals("iti")){
					search.setToolTipText("<html>Première balise recherchée.<br />Recherche aussi dans les entrées des itis." +
					"<br /><i>Astuce : </i>Pour rechercher tous les itis qui contiennent une balise commençant par OM, mettre OM*.</html>");
					search2.setToolTipText("<html>Deuxième balise recherchée.<br />Recherche aussi dans les sorties des itis.</html>");
				} else if(item.equals("trajet")){
					search.setToolTipText("<html>Première balise recherchée." +
					"<br /><i>Astuce : </i>Pour rechercher tous les trajets qui contiennent une balise commençant par OM, mettre OM*.</html>");
					search2.setToolTipText("<html>Deuxième balise recherchée.</html>");
				} else if(item.equals("route")){
					search.setToolTipText("<html>Première balise recherchée.<br />Recherche aussi dnas les nom de route si il n'y a pas de deuxième balise recherchée.<br />" +
					"<br /><i>Astuce : </i>Pour rechercher toutes les routes qui contiennent une balise commençant par OM, mettre OM*.</html>");
					search2.setToolTipText("<html>Deuxième balise recherchée.</html>");
				}
			}
		});

		//un appui sur la touche entrée lance la recherche
		search.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if("comboBoxEdited".equals(e.getActionCommand())){
					newSearch.doClick();
				}
			}
		});
		search2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if("comboBoxEdited".equals(e.getActionCommand())){
					newSearch.doClick();
				}
			}
		});
		
		return toolbar;

	}

	private JPanel createStatusBar(){
		String versionStip = "";
		String versionStpv = "";
		try {
			Statement st = DatabaseManager.getCurrent(Type.Databases);
			ResultSet rs = st.executeQuery("select * from databases where selected = '1' and type = 'STIP'");
			if(rs.next()) versionStip = rs.getString(2);
			rs = st.executeQuery("select * from databases where selected = '1' and type = 'STPV'");
			if(rs.next()) versionStpv = rs.getString(2);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		JPanel statusBar = new JPanel();
		statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
		JLabel stip = new JLabel("Version Stip : " + versionStip);
		statusBar.add(stip);
		statusBar.add(new JSeparator());
		statusBar.add(new JLabel("Version Stpv : " + versionStpv));

		statusBar.add(new JSeparator());
		statusBar.add(new JLabel("Nombre de résultats : "));
		statusBar.add(nombreResultats);

		return statusBar;
	}

	public static ContextPanel getContextPanel(){
		return getInstance().context;
	}
	
}
