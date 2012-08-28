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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatasManager.Type;
import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.aip.AIP;
import fr.crnan.videso3d.databases.aip.AIPController;
import fr.crnan.videso3d.ihm.components.FilteredMultiTreeTableView;
import fr.crnan.videso3d.ihm.components.FilteredTreeTableModel;
import fr.crnan.videso3d.ihm.components.TitleTwoButtons;

/**
 * @author Bruno Spyckerelle
 * @version 0.3.2
 */
public class AIPView extends FilteredMultiTreeTableView {

	private HashMap<String, DefaultMutableTreeNode> zones = new HashMap<String, DefaultMutableTreeNode>();
	private HashMap<String, DefaultMutableTreeNode> routes = new HashMap<String, DefaultMutableTreeNode>();
	private HashMap<String, DefaultMutableTreeNode> aerodromes = new HashMap<String, DefaultMutableTreeNode>();
	private HashMap<String, DefaultMutableTreeNode> balises = new HashMap<String, DefaultMutableTreeNode>();
	
	private FilteredTreeTableModel zonesModel;
	private FilteredTreeTableModel routesModel;
	private FilteredTreeTableModel aerodromesModel;
	private FilteredTreeTableModel balisesModel;
	
	
	public AIPView() {

		try {
			if(DatabaseManager.getCurrentAIP() != null) { //si pas de bdd, ne pas créer la vue

				DefaultMutableTreeNode zonesRoot = new DefaultMutableTreeNode("root");
				this.fillZonesRootNode(zonesRoot);
				this.zonesModel = new FilteredTreeTableModel(zonesRoot);
				this.addTableTree(this.zonesModel, "Espaces", null);
				
				DefaultMutableTreeNode routesRoot = new DefaultMutableTreeNode("root");
				this.fillRoutesRootNode(routesRoot);
				this.routesModel = new FilteredTreeTableModel(routesRoot);
				this.addTableTree(this.routesModel, "", createTitleRoutes());
				
				DefaultMutableTreeNode aerodromesRoot = new DefaultMutableTreeNode("root");
				this.fillAerodromesRootNode(aerodromesRoot);
				this.aerodromesModel = new FilteredTreeTableModel(aerodromesRoot);
				this.addTableTree(this.aerodromesModel, "Terrains", null);

				DefaultMutableTreeNode navFixRoot = new DefaultMutableTreeNode("root");
				this.fillBalisesRootNode(navFixRoot);
				this.balisesModel = new FilteredTreeTableModel(navFixRoot);
				this.addTableTree(this.balisesModel, "", createTitleBalises());

				
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}


	@Override
	public AIPController getController() {
		return (AIPController) DatasManager.getController(DatasManager.Type.AIP);
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
								DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(new Couple<String, Boolean>(shortName, false));
								node.add(node2);
								zones.put(t+shortName, node2);
							}
						}else{
							DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(new Couple<String, Boolean>(rs.getString(1), false));
							node.add(node2);
							zones.put(t+rs.getString(1), node2);
						}
					}
				}else{
					while(rs.next()){
						DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(new Couple<String, Boolean>(rs.getString(1), false));
						node.add(node2);
						zones.put(t+rs.getString(1), node2);
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
					DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(new Couple<String, Boolean>(rs.getString(1), false));
					node.add(node2);
					routes.put(rs.getString(1), node2);
				}
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	private void fillBalisesRootNode(DefaultMutableTreeNode root){
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
					DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(new Couple<String, Boolean>(rs.getString(1), false));
					node.add(node2);
					balises.put(t+rs.getString(1), node2);
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
				String name = rs0.getString(1)+" -- "+rs0.getString(2);
				DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(new Couple<String, Boolean>(name, false));
				aerodromes.add(node2);
				this.aerodromes.put(name, node2);
			}
			ResultSet rs1 = st.executeQuery("select nom from Aerodromes where type=1 order by nom");
			while(rs1.next()){
				DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(new Couple<String, Boolean>(rs1.getString(1), false));
				alti.add(node2);
				this.aerodromes.put(rs1.getString(1), node2);
			}
			ResultSet rs2 = st.executeQuery("select nom from Aerodromes where type=2 order by nom");
			while(rs2.next()){
				DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(new Couple<String, Boolean>(rs2.getString(1), false));
				prive.add(node2);
				this.aerodromes.put(rs2.getString(1), node2);
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
	private JPanel createTitleBalises(){
		
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


	private void setValueAt(int type, String name, boolean selected){
		if(type <= 20){
			this.zonesModel.setValueAt(selected, this.zones.get(AIP.type2String(type)+name), 1);
		}else if(type > 20 && type < 30){
			this.routesModel.setValueAt(selected, this.routes.get(name), 1);
		}else if(type >=30 && type < 40){
			this.balisesModel.setValueAt(selected, this.balises.get(AIP.type2String(type)+name), 1);
		}else if(type>=40){
			this.aerodromesModel.setValueAt(selected, this.aerodromes.get(name), 1);
		}
	}
	
	@Override
	public void showObject(int type, String name) {
		setValueAt(type, name, true);
	}

	@Override
	public void hideObject(int type, String name) {
		//Si jamais c'est le nom d'un segment qui est passé en paramètre, il faut enlever le numéro pour retrouver le nom de la route
		if(type == AIP.AWY || type == AIP.PDR){
			String[] splittedName = name.split("-");
			if(splittedName.length==3)
				name = (splittedName[0]+"-"+splittedName[1]).trim();
			else if(splittedName.length==2)
				name = splittedName[0].trim();
		}
		setValueAt(type, name, false);
	}
	
}
