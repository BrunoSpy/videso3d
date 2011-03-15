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

import javax.swing.tree.DefaultMutableTreeNode;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.ihm.components.FilteredMultiTreeTableView;
import fr.crnan.videso3d.ihm.components.FilteredTreeTableModel;
import fr.crnan.videso3d.skyview.SkyViewController;

/**
 * Interface de sélection de données SkyView
 * @author Bruno Spyckerelle
 * @version 0.3.2
 */
public class SkyView extends FilteredMultiTreeTableView {

	public SkyView(){

	//	this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		//		panel.setBorder(BorderFactory.createTitledBorder(""));

//		this.add(Box.createVerticalGlue());
		try{
			if(DatabaseManager.getCurrentSkyView() != null){
				
				
				DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
				this.fillRootNode(root);
				FilteredTreeTableModel model = new FilteredTreeTableModel(root);

				this.addTableTree(model, null, null);
				
			}
		} catch (SQLException e){
			e.printStackTrace();
		}

		//this.add(Box.createVerticalGlue());		
	}

	@Override
	public SkyViewController getController() {
		return (SkyViewController) DatasManager.getController(Type.SkyView);
	}


	private void fillRootNode(DefaultMutableTreeNode root) {

		try {
			Statement st = DatabaseManager.getCurrentSkyView();
			//ajout des routes
			DefaultMutableTreeNode routes = new DefaultMutableTreeNode(new Couple<String, Boolean>("Routes", false));
			root.add(routes);
			ResultSet rs = st.executeQuery("select distinct icao from airway order by icao");
			LinkedList<String> icao = new LinkedList<String>();
			while(rs.next()){
				icao.add(rs.getString(1));
			}
			for(String oaci : icao){
				rs = st.executeQuery("select distinct ident from airway where icao='"+oaci+"' order by ident");
				DefaultMutableTreeNode state = new DefaultMutableTreeNode(new Couple<String, Boolean>(oaci, false));
				routes.add(state);
				while(rs.next()){
					state.add(new DefaultMutableTreeNode(new Couple<String, Boolean>(rs.getString(1), false)));
				}
			}
			//ajout des waypoints
			DefaultMutableTreeNode waypoints = new DefaultMutableTreeNode(new Couple<String, Boolean>("Waypoints", false));
			root.add(waypoints);
			rs = st.executeQuery("select distinct icao from waypoint order by icao");
			icao.clear();
			while(rs.next()){
				icao.add(rs.getString(1));
			}
			for(String oaci : icao){
				rs = st.executeQuery("select ident from waypoint where icao='"+oaci+"' order by ident");
				DefaultMutableTreeNode point = new DefaultMutableTreeNode(new Couple<String, Boolean>(oaci, false));
				waypoints.add(point);
				while(rs.next()){
					point.add(new DefaultMutableTreeNode(new Couple<String, Boolean>(rs.getString(1), false)));
				}
			}
			//ajout des aéroports
			DefaultMutableTreeNode airports = new DefaultMutableTreeNode(new Couple<String, Boolean>("Airports", false));
			root.add(airports);
			rs = st.executeQuery("select distinct icao from airport order by icao");
			icao.clear();
			while(rs.next()){
				icao.add(rs.getString(1));
			}
			for(String oaci : icao){
				rs = st.executeQuery("select ident from airport where icao='"+oaci+"' order by ident");
				DefaultMutableTreeNode airport = new DefaultMutableTreeNode(new Couple<String, Boolean>(oaci, false));
				airports.add(airport);
				while(rs.next()){
					airport.add(new DefaultMutableTreeNode(new Couple<String, Boolean>(rs.getString(1), false)));
				}
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
