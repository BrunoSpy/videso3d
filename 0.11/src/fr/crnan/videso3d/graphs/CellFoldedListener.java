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


import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;

/**
 * Listener de cellule JGraph.<br />
 * Permet de mettre à jour la fenêtre contextuelle en fonction de la cellule sélectionnée<br />
 * et de mettre à jour le layout lors du repli d'un groupe.
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class CellFoldedListener implements mxIEventListener {

	private mxCell cell = null;
	
	private mxStackLayout stack;
	
	/**
	 * Constructeur pour écouter le repli d'une cellule
	 * @param cell
	 * @param stack
	 */
	public CellFoldedListener(mxCell cell, mxStackLayout stack){
		super();
		this.stack = stack;
		this.cell = cell;
	}

	@Override
	public void invoke(Object sender, mxEventObject evt) {
		
		if(cell.isCollapsed()){
			cell.setStyle(GraphStyle.groupStyleFolded);
			cell.getGeometry().setWidth(cell.getGeometry().getAlternateBounds().getWidth());
		} else {
			cell.setStyle(GraphStyle.groupStyle);
		}
		((mxGraph)sender).setCellsLocked(false);
		((mxGraph)sender).getModel().beginUpdate();
		stack.execute(((mxGraph)sender).getDefaultParent());
		((mxGraph)sender).getModel().endUpdate();
		((mxGraph)sender).setCellsLocked(true);

	}

}
