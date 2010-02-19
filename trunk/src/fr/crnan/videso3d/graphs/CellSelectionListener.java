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

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;

import fr.crnan.videso3d.ihm.ContextPanel;
/**
 * Listener de cellule JGraph.<br />
 * Permet de mettre à jour la fenêtre contextuelle en fonction de la cellule sélectionnée
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class CellSelectionListener implements mxIEventListener {

	private mxGraph graph;

	private ContextPanel context;
	/**
	 * Constructeur pour écouter la sélection d'une cellule
	 * @param graph
	 * @param stack
	 * @param context
	 */
	public CellSelectionListener(mxGraph graph, ContextPanel context){
		super();
		this.graph = graph;
		this.context = context;
	}


	@Override
	public void invoke(Object sender, mxEventObject evt) {

		mxCell cell = (mxCell) graph.getSelectionCell();
		if(cell != null && cell.getValue() instanceof CellContent){
			CellContent content = (CellContent) cell.getValue();
			if(content.getType().equals(CellContent.TYPE_BALISE)){
				context.showBalise(((CellContent)cell.getValue()).getName());
			} else if(content.getType().equals(CellContent.TYPE_ITI)){
				context.showIti(((CellContent)cell.getValue()).getId());
			} else if(content.getType().equals(CellContent.TYPE_TRAJET)){
				context.showTrajet(((CellContent)cell.getValue()).getId());
			} else if(content.getType().equals(CellContent.TYPE_ROUTE)){
				context.showRoute(((CellContent)cell.getValue()).getId());
			}
		}
	}

}
