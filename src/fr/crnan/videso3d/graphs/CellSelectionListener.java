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

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatasManager.Type;
import fr.crnan.videso3d.databases.stip.StipController;
import fr.crnan.videso3d.databases.stpv.StpvController;
import fr.crnan.videso3d.ihm.ContextPanel;
/**
 * Listener de cellule JGraph.<br />
 * Permet de mettre à jour la fenêtre contextuelle en fonction de la cellule sélectionnée
 * @author Bruno Spyckerelle
 * @version 0.1.2
 */
public class CellSelectionListener implements mxIEventListener {

	private mxGraph graph;

	private ContextPanel context;
	/**
	 * Constructeur pour écouter la sélection d'une cellule
	 * @param graph
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
			switch (content.getBase()) {
			case STIP:
				switch (content.getType()) {
				case StipController.ROUTES:
					context.showInfo(DatasManager.Type.STIP, StipController.ROUTES, content.getName());
					break;
				case StipController.BALISES:
					context.showInfo(DatasManager.Type.STIP, StipController.BALISES, content.getName());
					break;
				case StipController.ITI:
					context.showInfo(DatasManager.Type.STIP, StipController.ITI, new Integer(content.getId()).toString());
					context.setTitle("Informations sur "+content.getName());
					break;
				case StipController.TRAJET:
					context.showInfo(DatasManager.Type.STIP, StipController.TRAJET, new Integer(content.getId()).toString());
					context.setTitle("Informations sur "+content.getName());
					break;
				case StipController.CONNEXION:
					context.showInfo(DatasManager.Type.STIP, StipController.CONNEXION, new Integer(content.getId()).toString());
					context.setTitle("Informations sur "+content.getName());
					break;
				default:
					break;
				}
				break;
			case STPV:
				switch (content.getType()) {
				case StpvController.STAR:
					context.showInfo(DatasManager.Type.STPV, StpvController.STAR, new Integer(content.getId()).toString());
					context.setTitle("Informations sur "+content.getName());
					break;

				default:
					break;
				}
				break;
			default:
				break;
			}

		}else{
			context.showInfo(null, 0, null);
		}
	}

}
