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
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdesktop.swingx.JXTreeTable;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.MultiValueMap;
import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.skyview.SkyViewController;
import fr.crnan.videso3d.ihm.components.FilteredMultiTreeTableView;
import fr.crnan.videso3d.ihm.components.FilteredTreeTableModel;
import fr.crnan.videso3d.ihm.components.FilteredTreeTableNode;

/**
 * Interface de sélection de données SkyView
 * @author Bruno Spyckerelle
 * @author Adrien Vidal
 * @version 0.3.3
 */
public class SkyView extends FilteredMultiTreeTableView {
	
	private MultiValueMap<String, DefaultMutableTreeNode> nodes = new MultiValueMap<String, DefaultMutableTreeNode>();

	private FilteredTreeTableModel model;
	
	public SkyView(){
		try{
			if(DatabaseManager.getCurrentSkyView() != null){
				DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
				this.fillRootNode(root);
				this.model = new FilteredTreeTableModel(root);
				this.addTableTree(this.model, null, null);
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
	}

	@Override
	public SkyViewController getController() {
		return (SkyViewController) DatasManager.getController(DatasManager.Type.SkyView);
	}

	


	@Override
	public void addTableTree(final FilteredTreeTableModel model, String title, JPanel titlePanel){

		super.models.add(model);
		//éléments de l'IHM
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		if(title != null && titlePanel == null){
			tablePanel.setBorder(BorderFactory.createTitledBorder(title));
		}else{
			tablePanel.setBorder(BorderFactory.createTitledBorder(""));
		}


		final JXTreeTable treeTable = new JXTreeTable();
		super.tables.add(new JXTreeTable());


		model.addTreeModelListener(new TreeModelListener() {

			@Override
			public void treeStructureChanged(TreeModelEvent e) {
				if(getFiltre().isEmpty()){
					treeTable.collapseAll();
				} else {
					treeTable.expandAll();
				}
			}


			@Override
			public void treeNodesRemoved(TreeModelEvent e) {}

			@Override
			public void treeNodesInserted(TreeModelEvent e) {}

			@Override
			public void treeNodesChanged(TreeModelEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getTreePath().getLastPathComponent();
				FilteredTreeTableNode source = (FilteredTreeTableNode)node.getUserObject();
				int type = getController().string2type(((FilteredTreeTableNode)((DefaultMutableTreeNode)(e.getPath()[1])).getUserObject()).getName());
				if(!node.isLeaf()){
					if(source.isVisible()){
						if(e.getPath().length==3)
							((SkyViewController)getController()).showAllWaypoints(((FilteredTreeTableNode)((DefaultMutableTreeNode)(e.getPath()[2])).getUserObject()).getName());
						else
							((SkyViewController)getController()).showAllWaypoints(null);
					} else {
						if(e.getPath().length==3)
							((SkyViewController)getController()).hideAllWaypoints(((FilteredTreeTableNode)((DefaultMutableTreeNode)(e.getPath()[2])).getUserObject()).getName());
						else
							((SkyViewController)getController()).hideAllWaypoints(null);
					}
				}else{
					if(source.isVisible()){
						getController().showObject(type, source.getName());
					} else {
						getController().hideObject(type, source.getName());
					}
				}
			}
		});
	}

	
	
	
	
	private void fillRootNode(DefaultMutableTreeNode root) {
		try {
			Statement st = DatabaseManager.getCurrentSkyView();
			//Ajout des routes
			DefaultMutableTreeNode routes = new DefaultMutableTreeNode(new FilteredTreeTableNode("Routes", false));
			root.add(routes);
			//HashMap ayant pour clés les codes OACI des pays, et en valeurs un couple contenant le noeud correspondant au pays 
			//et la liste des routes déjà trouvées pour ce pays
			HashMap<String, Couple<DefaultMutableTreeNode,LinkedList<String>>> awyStateNodesMap = 
					new HashMap<String, Couple<DefaultMutableTreeNode,LinkedList<String>>>();
			ResultSet rs = st.executeQuery("select ident, icao from airway order by ident");
			while(rs.next()){
				String stateName = rs.getString(2);
				Couple<DefaultMutableTreeNode,LinkedList<String>> stateNode = awyStateNodesMap.get(stateName);
				//Pour chaque route trouvée, on regarde si le noeud du pays a déjà été créé, et si non, on le crée.
				if(stateNode==null){
					stateNode = new Couple<DefaultMutableTreeNode,LinkedList<String>> (new DefaultMutableTreeNode(new FilteredTreeTableNode(stateName, false)), new LinkedList<String>());
					awyStateNodesMap.put(stateName, stateNode);
				}

				String routeName = rs.getString(1);
				//On vérifie que cette route n'a pas déjà été ajoutée au noeud du pays
				if(!stateNode.getSecond().contains(routeName)){
					DefaultMutableTreeNode routeNode = new DefaultMutableTreeNode(new FilteredTreeTableNode(routeName, false));
					//on rajoute le noeud de la route au noeud du pays
					stateNode.getFirst().add(routeNode);
					//on rajoute le nom de la route dans la liste des routes trouvées pour ce pays afin d'éviter les doublons
					stateNode.getSecond().add(routeName);
					nodes.put(routeName, routeNode);
				}
			}
			rs.close();
			//Tri des noeuds des pays : on crée une liste avec tous les codes
			LinkedList<String> sortedStatesNames = new LinkedList<String>();
			for(String stateName : awyStateNodesMap.keySet()){
				sortedStatesNames.add(stateName);
			}
			//On trie cette liste
			Collections.sort(sortedStatesNames);
			//Ce qui nous permet d'ajouter les noeuds des pays dans le bon ordre.
			for(String state : sortedStatesNames){
				routes.add(awyStateNodesMap.get(state).getFirst());
			}
			
			//Ajout des waypoints
			DefaultMutableTreeNode waypoints = new DefaultMutableTreeNode(new FilteredTreeTableNode("Waypoints", false));
			root.add(waypoints);
			fillTypeNode(SkyViewController.TYPE_WAYPOINT,waypoints, st);
			
			//Ajout des aéroports
			DefaultMutableTreeNode airports = new DefaultMutableTreeNode(new FilteredTreeTableNode("Airports", false));
			root.add(airports);
			fillTypeNode(SkyViewController.TYPE_AIRPORT,airports, st);
			
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void fillTypeNode(Integer type, DefaultMutableTreeNode typeNode, Statement st){
		//HashMap ayant pour clés les codes des pays et pour valeurs les noeuds associés.
		HashMap<String, DefaultMutableTreeNode> wptStateNodesMap = new HashMap<String, DefaultMutableTreeNode>();
		String query = "select ident, icao from";
		if(type==SkyViewController.TYPE_WAYPOINT)
			query+=" waypoint";
		else if(type==SkyViewController.TYPE_AIRPORT)
			query+=" airport";
		query+=" order by ident";
		ResultSet rs;
		try {
			rs = st.executeQuery(query);
			while(rs.next()){
				String stateName = rs.getString(2);
				DefaultMutableTreeNode stateNode = wptStateNodesMap.get(stateName);
				//Si le noeud corredspondant au pays "stateName" n'existe pas, on le crée 
				if(stateNode==null){
					stateNode = new DefaultMutableTreeNode(new FilteredTreeTableNode(stateName, false));
					wptStateNodesMap.put(stateName, stateNode);
				}
				//On crée le noeud du waypoint et on l'ajoute au noeud de son pays
				String wptName = rs.getString(1);
				DefaultMutableTreeNode wptNode = new DefaultMutableTreeNode(new FilteredTreeTableNode(wptName, false));
				stateNode.add(wptNode);
				nodes.put(wptName, wptNode);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//Tri des noeuds de pays par ordre alphabétique : on crée une liste contenant tous les codes
		LinkedList<String> sortedStatesNames = new LinkedList<String>();
		for(String stateName : wptStateNodesMap.keySet()){
			sortedStatesNames.add(stateName);
		}
		//On trie cette liste
		Collections.sort(sortedStatesNames);
		//Et on ajoute les noeuds des pays ainsi triés à leur racine (waypoints ou airports)
		for(String state : sortedStatesNames){
			typeNode.add(wptStateNodesMap.get(state));
		}
	}

	
	@Override
	public void showObject(int type, String name) {
		this.model.setValueAt(true, this.nodes.get(name), 1);
	}

	@Override
	public void hideObject(int type, String name) {
		this.model.setValueAt(false, this.nodes.get(name), 1);
	}

	public static boolean isSkyViewFile(File file) {
		return file.getName().toLowerCase().endsWith("mdb");
	}
	
	private String getFiltre(){
		return super.filtre.getText();
	}

}
