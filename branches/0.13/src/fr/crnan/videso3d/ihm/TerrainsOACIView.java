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
import java.util.HashMap;
import javax.swing.tree.DefaultMutableTreeNode;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.terrainsoaci.TerrainsOaciController;
import fr.crnan.videso3d.ihm.components.FilteredMultiTreeTableView;
import fr.crnan.videso3d.ihm.components.FilteredTreeTableModel;
import fr.crnan.videso3d.ihm.components.FilteredTreeTableNode;

/**
 * Sélecteur de terrains OACI
 * @author David Granado
 * @version 0.0.1
 */
@SuppressWarnings("serial")
public class TerrainsOACIView extends FilteredMultiTreeTableView {

	private HashMap<String, DefaultMutableTreeNode> terrains = new HashMap<String, DefaultMutableTreeNode>();
	
	private FilteredTreeTableModel terrainsModel;
	
	public TerrainsOACIView() {
		try {
			if(DatabaseManager.getCurrentTerrainsOACI() != null) { //si pas de bdd, ne pas créer la vue
				DefaultMutableTreeNode terrainsRoot = new DefaultMutableTreeNode("root");
				this.fillTerrainsRootNode(terrainsRoot);
				this.terrainsModel = new FilteredTreeTableModel(terrainsRoot);
				this.addTableTree(this.terrainsModel, "Terrains OACI", null);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public TerrainsOaciController getController(){
		return (TerrainsOaciController) DatasManager.getController(DatasManager.Type.TerrainsOACI);
	}
	
	private void fillTerrainsRootNode(DefaultMutableTreeNode root){
		try{
			Statement st = DatabaseManager.getCurrentTerrainsOACI();
			ResultSet rs = st.executeQuery("select distinct country from terrainsoaci order by country");
			while(rs.next()){
				DefaultMutableTreeNode country = new DefaultMutableTreeNode(new FilteredTreeTableNode(rs.getString("country"), false));
				try {
					Statement st2 = DatabaseManager.getCurrentTerrainsOACI();
					ResultSet rs2 = st2.executeQuery("select * from terrainsoaci where country='" + rs.getString("country") + "'order by idoaci");
					while(rs2.next()){
						DefaultMutableTreeNode node = new DefaultMutableTreeNode(new FilteredTreeTableNode(rs2.getString("idoaci"), false));
						this.terrains.put(rs2.getString("idoaci"), node);
						country.add(node);
					}
					rs2.close();
					st2.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				root.add(country);
			}
			rs.close();
			st.close();
//			Ci dessous, le code qui permettait d'utiliser l'ancien format de fichier terrain OACI
//			DefaultMutableTreeNode terr = new DefaultMutableTreeNode(new FilteredTreeTableNode("Terrains OACI", false));
//			root.add(terr);
//			ResultSet rs = st.executeQuery("select idoaci from terrainsoaci order by idoaci");
//			while(rs.next()){
//				DefaultMutableTreeNode node = new DefaultMutableTreeNode(new FilteredTreeTableNode(rs.getString(1), false));
//				terrains.put(rs.getString(1), node);
//				terr.add(node);
//			}
//			rs.close();
//			st.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void reset() {
		super.reset();

	}

	@Override
	public void showObject(int type, String name) {
		this.terrainsModel.setValueAt(true,  this.terrains.get(name), 1);
	}
	
	public void showObject(String idoaci) {
		showObject(0, idoaci);
	}

	@Override
	public void hideObject(int type, String name) {
		this.terrainsModel.setValueAt(false, this.terrains.get(name), 1);
	}
	
	public void hideObject(String idoaci) {
		hideObject(0, idoaci);
	}

}
