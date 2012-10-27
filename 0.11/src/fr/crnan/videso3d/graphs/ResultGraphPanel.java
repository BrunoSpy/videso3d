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
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventSource.mxIEventListener;

import fr.crnan.videso3d.ihm.ContextPanel;
import fr.crnan.videso3d.ihm.ResultPanel;
/**
 * Panneau de résultats représentés sous forme de graphe
 * @author Bruno Spyckerelle
 * @version 0.1.2
 */
public abstract class ResultGraphPanel extends ResultPanel  {

	protected final VGraph graph = new VGraph();

	protected final mxStackLayout stack = new mxStackLayout(graph, false);

	protected final mxHierarchicalLayout layout = new mxHierarchicalLayout(graph, SwingConstants.WEST);

	protected JProgressBar progressBar = new JProgressBar();
	
	protected mxIEventListener selectionListener;
	
	/**
	 * 
	 * @param advanced True if using advanced search
	 * @param criteria List of search criteria
	 */
	public ResultGraphPanel(boolean advanced, final String... criteria){
		layout.setParallelEdgeSpacing(5.0);
		layout.setInterRankCellSpacing(20.0);//edge size
		layout.setInterHierarchySpacing(10.0);
		layout.setIntraCellSpacing(10.0);//espace entre deux lignes

		this.progressBar.setVisible(false);

		this.setBorder(null);
		this.setLayout(new BorderLayout());

		this.createGraphComponent(advanced, criteria);
	}

	/**
	 * Crée le graphe proprement dit
	 * @param balise
	 */
	protected abstract void createGraphComponent(boolean advanced, final String... criteria);
	
	protected VGraph getGraph(){
		return this.graph;
	}
	
	@Override
	public void setContext(ContextPanel context) {
		selectionListener = new CellSelectionListener(graph, context);
		// mise à jour du panel contextuel
		graph.getSelectionModel().addListener(mxEvent.CHANGE, selectionListener);
	}

	public void tabSelected(){
		selectionListener.invoke(null, null);
	}
}
