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
package fr.crnan.videso3d.graphs;

import java.awt.BorderLayout;

import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxEvent;
import com.mxgraph.view.mxGraph;

import fr.crnan.videso3d.ihm.ContextPanel;
/**
 * Panneau de résultats représentés sous forme de graphe
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public abstract class ResultGraphPanel extends ResultPanel  {

	protected final mxGraph graph = new mxGraph();

	protected final mxStackLayout stack = new mxStackLayout(graph, false);

	protected final mxHierarchicalLayout layout = new mxHierarchicalLayout(graph, SwingConstants.WEST);

	protected JProgressBar progressBar = new JProgressBar();

	/**
	 * Construit le panneau
	 * @param balise Balise recherchée
	 * @param balise2 
	 */
	public ResultGraphPanel(final String balise, String balise2){
		layout.setParallelEdgeSpacing(5.0);
		layout.setInterRankCellSpacing(20.0);//edge size
		layout.setInterHierarchySpacing(10.0);

		graph.setCellsCloneable(false);
		graph.setCellsEditable(false);
		graph.setCellsResizable(false);
		graph.setCellsDisconnectable(false);

		this.progressBar.setVisible(false);

		this.setBorder(null);
		this.setLayout(new BorderLayout());

		this.createGraphComponent(balise, balise2);
	}

	/**
	 * Crée le graphe proprement dit
	 * @param balise
	 * @return
	 */
	protected abstract void createGraphComponent(final String balise, final String balise2);

	protected String forgeSql(String balise){
		int length = balise.length();
		if(balise.charAt(length-1) == '*') {
			return "LIKE '"+balise.substring(0, length-1)+"%'";
		} else {
			return "= '"+balise+"'";
		}
	}
		
	protected boolean nameMatch(String balise, String name){
		int length = balise.length();
		if(!balise.isEmpty() && balise.charAt(length-1) == '*') {
			return name.startsWith(balise.substring(0, length-1));
		} else {
			return name.equals(balise); 
		}
	}
	
	protected mxGraph getGraph(){
		return this.graph;
	}
	
	@Override
	public void setContext(ContextPanel context) {
		// mise à jour du panel contextuel
		graph.getSelectionModel().addListener(mxEvent.CHANGE, new CellSelectionListener(graph, context));
	}

}
