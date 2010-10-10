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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.tree.DefaultMutableTreeNode;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.aip.AIPController;
import fr.crnan.videso3d.ihm.components.FilteredMultiTreeTableView;
import fr.crnan.videso3d.ihm.components.FilteredTreeTableModel;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.3
 */
public class AIPViewTreeTable extends FilteredMultiTreeTableView {

	private AIPController controller;

	public AIPViewTreeTable(AIPController aipController) {
		this.controller = aipController;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		try {
			if(DatabaseManager.getCurrentAIP() != null) { //si pas de bdd, ne pas créer la vue
				
				DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
				this.fillRootNode(root);
				FilteredTreeTableModel model = new FilteredTreeTableModel(root);
				
				this.addTableTree(model, "Zones");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.add(Box.createVerticalGlue());
	}


	@Override
	public VidesoController getController() {
		return controller;
	}
	
	private void fillRootNode(DefaultMutableTreeNode root){

		try {
			Statement st = DatabaseManager.getCurrentAIP();
			ResultSet rs = st.executeQuery("select distinct type from volumes order by type");
			LinkedList<String> types = new LinkedList<String>();
			while(rs.next()){
				types.add(rs.getString(1));
			}

			for(String t : types){
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(new Couple<String, Boolean>(t, false));
				root.add(node);
				rs = st.executeQuery("select nom from volumes where type = '"+t+"' order by nom");
				while(rs.next()){
					node.add(new DefaultMutableTreeNode(new Couple<String, Boolean>(rs.getString(1), false)));
				}
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
