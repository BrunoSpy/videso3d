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
import com.mxgraph.view.mxGraph;

import fr.crnan.videso3d.databases.DatabaseManager.Type;
import fr.crnan.videso3d.databases.stip.StipController;
import fr.crnan.videso3d.databases.stpv.StpvController;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.2
 */
public class VGraph extends mxGraph {

	public VGraph(){
		super();
		this.setCellsCloneable(false);
		this.setCellsEditable(false);
		this.setCellsResizable(false);
		this.setCellsDisconnectable(false);
	}
	
	@Override
	public boolean isCellFoldable(Object cell, boolean arg1) {

		if(cell instanceof mxCell){
			Object v = ((mxCell) cell).getValue();
			if(v instanceof CellContent ){
				int t = ((CellContent) v).getType();
				Type base = ((CellContent) v).getBase();
				//les routes et les itis ne doivent pas pouvoir être réduits
				if((base.equals(Type.STIP) && t == StipController.ROUTES) ||
						(base.equals(Type.STIP) && t == StipController.ITI) ||
						(base.equals(Type.STIP) && t == StipController.TRAJET) ||
						(base.equals(Type.STIP) && t == StipController.CONNEXION) ||
						(base.equals(Type.STPV) && t == StpvController.STAR)){
					return false;
				}
			}
		}
		
		return super.isCellFoldable(cell, arg1);
	}

	
	
}
