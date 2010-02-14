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
 * Fenêtre d'analyse des données Stip et Stpv
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class AnalyzeUI extends JFrame {

	private JTabbedPane tabPane = new JTabbedPane();

	private ContextPanel context = new ContextPanel();

	public AnalyzeUI(){
		super();
		this.setLayout(new BorderLayout());

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

	private ResultPanel createResultPanel(final String type, final String search){

		if(type.equals("iti")){
			return new ItiPanel(search);
		} else if(type.equals("route")){
			return new RoutePanel(search);
		} else if(type.equals("trajet")){
			return new TrajetPanel(search);
		}
		return null;
	}

	private JToolBar createToolbar(){

		JToolBar toolbar = new JToolBar();

		toolbar.add(new JLabel(" Rechercher : "));

		String[] types = {"balise", "iti", "trajet", "route"};

		final JComboBox type = new JComboBox(types);

		toolbar.add(type);

		toolbar.add(new JLabel(" contenant : "));


		LinkedList<String> results = new LinkedList<String>();
		try {
			Statement st = DatabaseManager.getCurrentStip();
			if(st != null){
				ResultSet rs = st.executeQuery("select name from balises UNION select name from routes UNION select nom from secteurs");
				while(rs.next()){
					results.add(rs.getString(1));
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		final JComboBox search = new JComboBox(results.toArray());
		search.setToolTipText("Rechercher un élément Stip affiché");
		search.setEditable(true);
		AutoCompleteDecorator.decorate(search);

		toolbar.add(search);

		JButton newSearch = new JButton("Nouvelle recherche");
		newSearch.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String t = type.getSelectedItem().toString();
				String s = search.getSelectedItem().toString();
				ResultPanel content = createResultPanel(t, s);
				content.setContext(context);
				JScrollPane scrollContent = new JScrollPane(content);
				scrollContent.setBorder(null);
				tabPane.addTab(t+" "+s, scrollContent);

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

		return statusBar;
	}


}
