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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.aip.AIPController;
import fr.crnan.videso3d.ihm.components.FilteredMultiTreeTableView;
import fr.crnan.videso3d.ihm.components.FilteredTreeTableModel;
import fr.crnan.videso3d.ihm.components.TitleTwoButtons;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.3
 */
public class AIPView extends FilteredMultiTreeTableView {

	private AIPController controller;

	public AIPView(AIPController aipController) {
		this.controller = aipController;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		try {
			if(DatabaseManager.getCurrentAIP() != null) { //si pas de bdd, ne pas créer la vue
				
				DefaultMutableTreeNode ZonesRoot = new DefaultMutableTreeNode("root");
				this.fillZonesRootNode(ZonesRoot);
				FilteredTreeTableModel ZonesModel = new FilteredTreeTableModel(ZonesRoot);
				this.addTableTree(ZonesModel, "Zones", null);
				
				DefaultMutableTreeNode RoutesRoot = new DefaultMutableTreeNode("root");
				this.fillRoutesRootNode(RoutesRoot);
				FilteredTreeTableModel RoutesModel = new FilteredTreeTableModel(RoutesRoot);
				this.addTableTree(RoutesModel, "", createTitleRoutes());
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
	
	private void fillZonesRootNode(DefaultMutableTreeNode root){

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
				if(t.equals("CTL")){
					HashSet<String> secteurs = new HashSet<String>();
					while(rs.next()){
						String name = rs.getString(1);
						if(name.contains(" ")){
							String shortName = name.split("\\s+")[0];
							if(secteurs.add(shortName)){
								node.add(new DefaultMutableTreeNode(new Couple<String, Boolean>(shortName, false)));
							}
						}else{
							node.add(new DefaultMutableTreeNode(new Couple<String, Boolean>(rs.getString(1), false)));
						}
					}
				}else{
					while(rs.next()){
						node.add(new DefaultMutableTreeNode(new Couple<String, Boolean>(rs.getString(1), false)));
					}
				}
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	private void fillRoutesRootNode(DefaultMutableTreeNode root){
		try {
			Statement st = DatabaseManager.getCurrentAIP();
			ResultSet rs = st.executeQuery("select distinct type from routes order by type");
			LinkedList<String> types = new LinkedList<String>();
			while(rs.next()){
				types.add(rs.getString(1));
			}
			for(String t : types){
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(new Couple<String, Boolean>(t, false));
				root.add(node);
				rs = st.executeQuery("select nom from routes where type = '"+t+"' order by nom");
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
	
	/**
	 * Titre du panel Routes.<br />
	 * Contient un sélecteur pour choisir la méthode de représentation (2D/3D).
	 * @return JPanel
	 */
	//TODO factoriser avec createTitleRoutes de StipView
	private JPanel createTitleRoutes(){
		
		TitleTwoButtons titlePanel = new TitleTwoButtons("Routes", "2D", "3D", true);
		titlePanel.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				Boolean state = e.getStateChange() == ItemEvent.SELECTED;
				controller.toggleLayer(controller.getRoutes2DLayer(), state);
				controller.toggleLayer(controller.getRoutes3DLayer(), !state);
			}
		});
		
		return titlePanel; 
	}
	
	
}
