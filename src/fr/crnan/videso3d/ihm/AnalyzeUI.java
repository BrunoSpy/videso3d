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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.graphs.GraphStyle;
import fr.crnan.videso3d.graphs.Iti;
import fr.crnan.videso3d.graphs.ResultPanel;
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



	private ResultPanel createResultPanel(String type, String search){

		mxGraph graph = new mxGraph();
		mxCell group = null;
		mxStackLayout stack = null;
		
		graph.getSelectionModel().addListener(mxEvent.CHANGE, context);
		
		graph.setCellsCloneable(false);
		graph.setCellsEditable(false);
		graph.setCellsResizable(false);
		graph.setCellsDisconnectable(false);
		
		group = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, "Groupe", 0, 0, 500, 500, GraphStyle.groupStyle);

		
		Set<Object> groups = new HashSet<Object>();
		((mxCell)group).setConnectable(false);
		stack = new mxStackLayout(graph, false);
		
		if(type.equals("iti")){
			try {
				Statement st = DatabaseManager.getCurrentStip();
				ResultSet rs = st.executeQuery("select iditi from balitis where balise='"+search+"'");
				while(rs.next()){
					mxCell iti = (mxCell)Iti.addIti(graph, group, rs.getInt(1));
					groups.add(iti);
					graph.addListener(mxEvent.CELLS_FOLDED, new CellFoldedListener(iti, stack, graph, group));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			
		}
		graph.updateGroupBounds(groups.toArray(), graph.getGridSize());

		stack.execute(group);

		graph.updateCellSize(group);

		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		graphComponent.setBorder(null);
		ResultPanel result = new ResultPanel(graphComponent);
		result.setBorder(null);
		return result;
	}

	private JToolBar createToolbar(){

		JToolBar toolbar = new JToolBar();

		toolbar.add(new JLabel(" Rechercher : "));

		String[] types = {"balise", "iti"};

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
				context.setGraph(content.getGraph());
				tabPane.addTab(t+" "+s, content);
				tabPane.addChangeListener(new ChangeListener() {
					
					@Override
					public void stateChanged(ChangeEvent arg0) {
						context.setGraph(((ResultPanel)tabPane.getSelectedComponent()).getGraph());
					}
				});
				
				ButtonTabComponent buttonTab = new ButtonTabComponent(tabPane);
				tabPane.setTabComponentAt(tabPane.indexOfComponent(content), buttonTab);
			}
		});

		toolbar.add(newSearch);

		return toolbar;

	}

	private JPanel createStatusBar(){
		JPanel statusBar = new JPanel();
		statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
		JLabel stip = new JLabel("Version Stip : " + "1.12.12:45.45.45");
		statusBar.add(stip);
		statusBar.add(new JLabel(" | "));
		statusBar.add(new JLabel("Version Stpv : " + "45.12.13"));

		return statusBar;
	}

	/* *********************************************** */
	/* ***************** Listeners ******************* */
	/* *********************************************** */

	private class CellFoldedListener implements mxIEventListener {

		private mxCell cell;

		private mxGraph graph;
		
		private mxStackLayout stack;

		private mxCell group;
		
		public CellFoldedListener(mxCell cell, mxStackLayout stack, mxGraph graph, mxCell group){
			super();
			this.cell = cell;
			this.graph = graph;
			this.stack = stack;
			this.group = group;
		}

		@Override
		public void invoke(Object sender, mxEventObject evt) {
			if(cell.isCollapsed()){
				cell.setStyle(GraphStyle.groupStyleFolded);
				cell.getGeometry().setWidth(cell.getGeometry().getAlternateBounds().getWidth());
			} else {
				cell.setStyle(GraphStyle.groupStyle);
			}
			stack.execute(group);
			graph.updateCellSize(group);
		}

	}
}
