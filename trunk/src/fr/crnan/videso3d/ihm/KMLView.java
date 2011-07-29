package fr.crnan.videso3d.ihm;


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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.kml.KMLController;
import fr.crnan.videso3d.ihm.components.FilteredMultiTreeTableView;
import fr.crnan.videso3d.ihm.components.FilteredTreeTableModel;
import fr.crnan.videso3d.ihm.components.TitleTwoButtons;
import gov.nasa.worldwind.util.layertree.LayerTree;


/**
 * @author Mickael Papail
 * @version 0.1
 */
public class KMLView extends FilteredMultiTreeTableView {

	protected LayerTree layertree;	
	
	public KMLView() {

		try {
			if(DatabaseManager.getCurrentKML() != null) { //si pas de bdd, ne pas creer la vue
			/*	
				 gov.nasa.worldwind.examples.util.layertree.KMLLayerTreeNode layerNode = new KMLLayerTreeNode(layer, kmlRoot);
		            this.layerTree.getModel().addLayer(layerNode);
		            this.layerTree.makeVisible(layerNode.getPath());
		            layerNode.expandOpenContainers(this.layerTree);*/		
				DefaultMutableTreeNode kmlTree = new DefaultMutableTreeNode("root");				
				this.fillKmlTree(kmlTree);			
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
		
	private void fillKmlTree(DefaultMutableTreeNode root){		
	
	}	
	
	@Override
	public KMLController getController() {
		return (KMLController) DatasManager.getController(Type.KML);
	}
	
	@Override
	public void showObject(int type, String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hideObject(int type, String name) {
		// TODO Auto-generated method stub
		
	}
	
}
