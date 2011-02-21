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

import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.aip.AIPController;
import fr.crnan.videso3d.ihm.components.FilteredMultiTreeTableView;
import fr.crnan.videso3d.ihm.components.FilteredTreeTableModel;
import fr.crnan.videso3d.ihm.components.TitleTwoButtons;

/**
 * @author Bruno Spyckerelle
 * @version 0.3.2
 */
public class AIPView extends FilteredMultiTreeTableView {

	public AIPView() {

		try {
			if(DatabaseManager.getCurrentAIP() != null) { //si pas de bdd, ne pas créer la vue

				DefaultMutableTreeNode ZonesRoot = new DefaultMutableTreeNode("root");
				this.fillZonesRootNode(ZonesRoot);
				FilteredTreeTableModel ZonesModel = new FilteredTreeTableModel(ZonesRoot);
				this.addTableTree(ZonesModel, "Espaces", null);
				
				DefaultMutableTreeNode RoutesRoot = new DefaultMutableTreeNode("root");
				this.fillRoutesRootNode(RoutesRoot);
				FilteredTreeTableModel RoutesModel = new FilteredTreeTableModel(RoutesRoot);
				this.addTableTree(RoutesModel, "", createTitleRoutes());
				
				DefaultMutableTreeNode AerodromesRoot = new DefaultMutableTreeNode("root");
				this.fillAerodromesRootNode(AerodromesRoot);
				FilteredTreeTableModel AerodromesModel = new FilteredTreeTableModel(AerodromesRoot);
				this.addTableTree(AerodromesModel, "Terrains", null);

				DefaultMutableTreeNode NavFixRoot = new DefaultMutableTreeNode("root");
				this.fillNavFixRootNode(NavFixRoot);
				FilteredTreeTableModel NavFixModel = new FilteredTreeTableModel(NavFixRoot);
				this.addTableTree(NavFixModel, "", createTitleNavFix());

				
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}


	@Override
	public AIPController getController() {
		return (AIPController) DatasManager.getController(Type.AIP);
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
	
	
	private void fillNavFixRootNode(DefaultMutableTreeNode root){
		try{
			Statement st = DatabaseManager.getCurrentAIP();
			ResultSet rs = st.executeQuery("select distinct type from NavFix order by type");
			LinkedList<String> types = new LinkedList<String>();
			while(rs.next()){
				types.add(rs.getString(1));
			}
			for(String t : types){
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(new Couple<String, Boolean>(t, false));
				root.add(node);
				rs = st.executeQuery("select distinct nom from NavFix where type = '"+t+"' order by nom");
				while(rs.next()){
					node.add(new DefaultMutableTreeNode(new Couple<String, Boolean>(rs.getString(1), false)));
				}
			}
			rs.close();
			st.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	private void fillAerodromesRootNode(DefaultMutableTreeNode root){
		try{
			Statement st = DatabaseManager.getCurrentAIP();
			DefaultMutableTreeNode aerodromes = new DefaultMutableTreeNode(new Couple<String, Boolean>("Aérodromes",false));
			DefaultMutableTreeNode alti = new DefaultMutableTreeNode(new Couple<String, Boolean>("Altisurfaces",false));
			DefaultMutableTreeNode prive = new DefaultMutableTreeNode(new Couple<String, Boolean>("Terrains privés",false));

			ResultSet rs0 = st.executeQuery("select code, nom from Aerodromes where type=0 order by code");
			while(rs0.next()){
				aerodromes.add(new DefaultMutableTreeNode(new Couple<String, Boolean>(rs0.getString(1)+" -- "+rs0.getString(2), false)));
			}
			ResultSet rs1 = st.executeQuery("select nom from Aerodromes where type=1 order by nom");
			while(rs1.next()){
				alti.add(new DefaultMutableTreeNode(new Couple<String, Boolean>(rs1.getString(1), false)));
			}
			ResultSet rs2 = st.executeQuery("select nom from Aerodromes where type=2 order by nom");
			while(rs2.next()){
				prive.add(new DefaultMutableTreeNode(new Couple<String, Boolean>(rs2.getString(1), false)));
			}
			
			rs0.close();rs1.close();rs2.close();
			st.close();
			root.add(aerodromes);
			root.add(alti);
			root.add(prive);
		}catch(SQLException e){
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
				getController().toggleLayer(getController().getRoutes2DLayer(), state);
				getController().toggleLayer(getController().getRoutes3DLayer(), !state);
			}
		});
		
		return titlePanel; 
	}
	
	/**
	 * Titre du panel Routes.<br />
	 * Contient un sélecteur pour choisir la méthode de représentation (2D/3D).
	 * @return JPanel
	 */
	//TODO factoriser avec createTitleRoutes de StipView
	private JPanel createTitleNavFix(){
		
		TitleTwoButtons titlePanel = new TitleTwoButtons("Navigation Fix", "2D", "3D", true);
		titlePanel.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				Boolean state = e.getStateChange() == ItemEvent.SELECTED;
				getController().setBalisesLayer3D(!state);
			}
		});
		
		return titlePanel; 
	}
	
}
