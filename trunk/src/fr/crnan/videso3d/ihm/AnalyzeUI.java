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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.graphs.ItiPanel;
import fr.crnan.videso3d.graphs.ResultPanel;
import fr.crnan.videso3d.graphs.RoutePanel;
import fr.crnan.videso3d.graphs.TrajetPanel;
/**
 * Fenêtre d'analyse des données Stip et Stpv.<br />
 * Cette classe est un singleton afin de n'être ouverte qu'une fois maximum.
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public final class AnalyzeUI extends JFrame {

	private static AnalyzeUI instance = null;
	
	private JTabbedPane tabPane = new JTabbedPane();

	private ContextPanel context = new ContextPanel();

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
	
	public final static void showResults(String type, String balise1, String balise2){
		ResultPanel content = getInstance().createResultPanel(type, balise1, balise2);
		content.setContext(getInstance().context);
		content.addPropertyChangeListener(ResultPanel.PROPERTY_RESULT, new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				getInstance().nombreResultats.setText(evt.getNewValue().toString());
			}
		});
		JScrollPane scrollContent = new JScrollPane(content);
		scrollContent.setBorder(null);
		getInstance().tabPane.addTab(type+" "+balise1+(balise2.isEmpty() ? "" : "+"+balise2), scrollContent);

		ButtonTabComponent buttonTab = new ButtonTabComponent(getInstance().tabPane);
		getInstance().tabPane.setTabComponentAt(getInstance().tabPane.indexOfComponent(scrollContent), buttonTab);
		getInstance().tabPane.setSelectedIndex(getInstance().tabPane.indexOfComponent(scrollContent));
		
		getInstance().setVisible(true);
	}
	
	private AnalyzeUI(){
		super();
		this.setLayout(new BorderLayout());

		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/videso3d.png")));
		
		this.setTitle("Videso - Analyse (0.7.0)");
		
		this.add(this.createToolbar(), BorderLayout.PAGE_START);

		this.add(this.createStatusBar(), BorderLayout.PAGE_END);

		JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, context, tabPane);
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
		}
		return null;
	}

	private JToolBar createToolbar(){

		JToolBar toolbar = new JToolBar();

		toolbar.add(new JLabel(" Rechercher : "));

		String[] types = {/*"balise", "balint",*/ "iti", "trajet", "route"/*, "connexion"*/};

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
		AutoCompleteDecorator.decorate(search);

		toolbar.add(search);

		toolbar.add(new JLabel(" et : "));

		//nouvelle liste de résultat avec un résultat vide en première position
		LinkedList<String> results2 = (LinkedList<String>) results.clone();
		results2.addFirst("");
		final JComboBox search2 = new JComboBox((results2).toArray());
		search2.setEditable(true);
		AutoCompleteDecorator.decorate(search2);

		toolbar.add(search2);

		JButton newSearch = new JButton("Nouvelle recherche");
		newSearch.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String t = type.getSelectedItem().toString();
				String s = search.getSelectedItem().toString();
				String s2 = search2.getSelectedItem().toString();
				ResultPanel content = createResultPanel(t, s, s2);
				content.setContext(context);
				content.addPropertyChangeListener(ResultPanel.PROPERTY_RESULT, new PropertyChangeListener() {
					
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						nombreResultats.setText(evt.getNewValue().toString());
					}
				});
				JScrollPane scrollContent = new JScrollPane(content);
				scrollContent.setBorder(null);
				tabPane.addTab(t+" "+s+(s2.isEmpty() ? "" : "+"+s2), scrollContent);

				ButtonTabComponent buttonTab = new ButtonTabComponent(tabPane);
				tabPane.setTabComponentAt(tabPane.indexOfComponent(scrollContent), buttonTab);
				tabPane.setSelectedIndex(tabPane.indexOfComponent(scrollContent));
			}
		});

		toolbar.add(newSearch);

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
		statusBar.add(new JLabel(" | "));
		statusBar.add(new JLabel("Version Stpv : " + versionStpv));

		statusBar.add(new JLabel(" | "));
		statusBar.add(new JLabel("Nombre de résultats : "));
		statusBar.add(nombreResultats);
		
		return statusBar;
	}


}
