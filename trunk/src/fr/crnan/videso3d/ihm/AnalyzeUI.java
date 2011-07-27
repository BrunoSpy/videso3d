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
import java.awt.CardLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

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
 * @version 0.3.0
 */
public final class AnalyzeUI extends JFrame {

	private static AnalyzeUI instance = null;

	private JTabbedPane tabPane = new JTabbedPane();

	private ContextPanel context = new ContextPanel();

	private JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, context, tabPane);

	private JLabel nombreResultats = new JLabel();
	
	private JPanel searchPanelContainer = new JPanel(new CardLayout(0, 0));
	private JPanel topPanel;
	
	private AdvancedSearchPanel itiSearch, baliseSearch = null;

	private JButton advancedSearch;

	private SearchPanel searchPanel;
	
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
	 * @param numero Numéro de la liaison privilégiée recherchée (optionnel)
	 */
	public final static void showResults(boolean advanced, String type, String... criteria){
		
		final ResultPanel content = getInstance().createResultPanel(advanced, type, getInstance().tabPane, criteria);
		content.setContext(getInstance().context);
		content.addPropertyChangeListener(ResultPanel.PROPERTY_RESULT, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				int index = getInstance().tabPane.indexOfComponent(content);
				getInstance().tabPane.setTitleAt(index,	getInstance().tabPane.getTitleAt(index) + " ("+evt.getNewValue()+")");
				getInstance().nombreResultats.setText(evt.getNewValue().toString());
			}
		});

		getInstance().tabPane.addTab(content.getTitleTab(), content);
		
		ButtonTabComponent buttonTab = new ButtonTabComponent(getInstance().tabPane);
		getInstance().tabPane.setTabComponentAt(getInstance().tabPane.indexOfComponent(content), buttonTab);
		getInstance().tabPane.setSelectedIndex(getInstance().tabPane.indexOfComponent(content));

		getInstance().setVisible(true);

		//si type balise, on affiche les infos contextuelles sur cette balise
		if(type.equals("balise")){
			try {
				if(DatabaseManager.getCurrentStip().executeQuery("select * from balises where name = '"+criteria[0]+"'").next()) {
					getInstance().context.showInfo(Type.STIP, StipController.BALISES, criteria[0]);
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
		showResults(false, type, balise, "", "");
	}

	private AnalyzeUI(){
		super();
		getContentPane().setLayout(new BorderLayout());

		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/videso3d.png")));

		this.setTitle("Videso - Analyse ("+Videso3D.VERSION+")");

		topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		getContentPane().add(topPanel, BorderLayout.PAGE_START);

		topPanel.add(searchPanelContainer);
		
		advancedSearch = new JButton("Recherche avancée");
		advancedSearch.setEnabled(false);
		
		searchPanel = new SearchPanel();
		searchPanel.getTypeComboBox().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
					advancedSearch.setEnabled(isAdvancedSearchAvailable((String) ((JComboBox)e.getSource()).getSelectedItem()));
			}
		});
		
		this.searchPanelContainer.add(searchPanel, "default");
		
		Box searchButtonBox = Box.createVerticalBox();
		advancedSearch.addActionListener(new ActionListener() {
			
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(((JButton)e.getSource()).getText().equals("Recherche avancée")){
					setAdvancedSearchPanel(searchPanel.getType());
					advancedSearch.setText("Recherche simplifiée");
				} else {
					setDefaultSearch();
				}
			}
		});
		searchButtonBox.add(Box.createVerticalStrut(5));
		searchButtonBox.add(advancedSearch);
		searchButtonBox.add(Box.createVerticalGlue());
		
		topPanel.add(searchButtonBox);
				
		getContentPane().add(this.createStatusBar(), BorderLayout.PAGE_END);

		splitpane.setOneTouchExpandable(true);
		splitpane.setContinuousLayout(true);

		this.getContentPane().add(splitpane);
		this.pack();
		//FullScreen
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);

	}	

	private void setDefaultSearch(){
		((CardLayout) searchPanelContainer.getLayout()).show(searchPanelContainer, "default");
		topPanel.setPreferredSize(searchPanel.getPreferredSize());
		topPanel.validate();
		advancedSearch.setText("Recherche avancée");
	}
	
	private void setDefaultSearch(String type){
		searchPanel.getTypeComboBox().setSelectedItem(type);
		setDefaultSearch();
	}
	
	private void setAdvancedSearchPanel(String type){
		getAdvancedSearchPanel(type).getTypeComboBox().setSelectedItem(type);
		topPanel.setPreferredSize(getAdvancedSearchPanel(type).getPreferredSize());
		((CardLayout) searchPanelContainer.getLayout()).show(searchPanelContainer, type);
		topPanel.validate();
	}
	
	
	private AdvancedSearchPanel getAdvancedSearchPanel(String type){
		
		ActionListener typeComboBoxListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String type = (String) ((JComboBox)e.getSource()).getSelectedItem();
				if(isAdvancedSearchAvailable(type)){
					setAdvancedSearchPanel(type);
				} else {
					setDefaultSearch(type);
				}
			}
		};
		
		if(type.equals("iti")){
			if(itiSearch == null){
				itiSearch = new ItiSearchPanel();
				searchPanelContainer.add(itiSearch, "iti");
				itiSearch.getTypeComboBox().addActionListener(typeComboBoxListener);
			}
			return itiSearch;
		} else if(type.equals("route")){
			return null;
		} else if(type.equals("trajet")){
			return null;
		} else if(type.equals("balise")){
			if(baliseSearch == null){
				
			}
			return null;
		} else if(type.equals("connexion")){
			return null;
		} else if(type.equals("stars")){
			return null;
		}else if(type.equals("liaison privilégiée")){
			return null;
		} else {
			return null;
		}
	}
	
	private boolean isAdvancedSearchAvailable(String type){
		return type.equals("iti");
	}
	
	private ResultPanel createResultPanel(boolean advanced, final String type, JTabbedPane tabPane, final String... criteria){

		if(type.equals("iti")){
			return new ItiPanel(advanced, criteria);
		} else if(type.equals("route")){
			return new RoutePanel(advanced, criteria);
		} else if(type.equals("trajet")){
			return new TrajetPanel(advanced, criteria);
		} else if(type.equals("balise")){
			return new BaliseResultPanel(criteria[0]);
		} else if(type.equals("connexion")){
			return new ConnexPanel(advanced, criteria);
		} else if(type.equals("stars")){
			return new StarPanel(advanced, criteria);
		}else if(type.equals("liaison privilégiée")){
			return new LiaisonPanel(criteria[2], tabPane);
		}

		return null;
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
		statusBar.add(new JLabel(" | "));
		statusBar.add(new JLabel("Version Stpv : " + versionStpv));

		statusBar.add(new JLabel(" | "));
		statusBar.add(new JLabel("Nombre de résultats : "));
		statusBar.add(nombreResultats);

		return statusBar;
	}
	
	
	public static ContextPanel getContextPanel(){
		return getInstance().context;
	}
	
}
