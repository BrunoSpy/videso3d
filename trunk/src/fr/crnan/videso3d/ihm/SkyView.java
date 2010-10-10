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
import java.awt.Component;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.ihm.components.DataView;
import fr.crnan.videso3d.ihm.components.FilteredTreeTableModel;
import fr.crnan.videso3d.ihm.components.RegexViewFilter;
import fr.crnan.videso3d.skyview.SkyViewController;

/**
 * Interface de sélection de données SkyView
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class SkyView extends JPanel implements DataView{

	private VidesoController controller;

	private JPanel panel = new JPanel();
	private JTextField filtre = new JTextField(20);

	public SkyView(VidesoController controller){
		this.controller = controller;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		//		panel.setBorder(BorderFactory.createTitledBorder(""));

		this.add(Box.createVerticalGlue());
		try{
			if(DatabaseManager.getCurrentSkyView() != null){
				this.add(this.buildTree());
			}
		} catch (SQLException e){
			e.printStackTrace();
		}

		//this.add(Box.createVerticalGlue());		
	}

	@Override
	public VidesoController getController() {
		return this.controller;
	}

	@Override
	public void reset() {


	}

	private Component buildTree() {
		panel.setLayout(new BorderLayout());

		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));
		filterPanel.add(Box.createVerticalGlue());
		filterPanel.add(new Label("Filtre : "));
		filterPanel.add(filtre);

		panel.add(filterPanel, BorderLayout.NORTH);

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		this.fillRootNode(root);

		final AbstractTreeTableModel model = new FilteredTreeTableModel(root);
		model.addTreeModelListener(new TreeModelListener() {

			@Override
			public void treeStructureChanged(TreeModelEvent e) {}

			@Override
			public void treeNodesRemoved(TreeModelEvent e) {}

			@Override
			public void treeNodesInserted(TreeModelEvent e) {}

			@Override
			public void treeNodesChanged(TreeModelEvent e) {
				String type = ((Couple<String, Boolean>)((DefaultMutableTreeNode)(e.getPath()[1])).getUserObject()).getFirst();
				Couple<String, Boolean> source = (Couple<String, Boolean>) ((DefaultMutableTreeNode)e.getTreePath().getLastPathComponent()).getUserObject();
				if(type.equals("Routes")){
					if(source.getSecond()){
						controller.showObject(SkyViewController.TYPE_ROUTE, source.getFirst());
					} else {
						controller.hideObject(SkyViewController.TYPE_ROUTE, source.getFirst());
					}
				} else if(type.equals("Waypoints")){
					if(source.getSecond()){
						controller.showObject(SkyViewController.TYPE_WAYPOINT, source.getFirst());
					} else {
						controller.hideObject(SkyViewController.TYPE_WAYPOINT, source.getFirst());
					}
				} else if(type.equals("Airports")){
					if(source.getSecond()){
						controller.showObject(SkyViewController.TYPE_AIRPORT, source.getFirst());
					} else {
						controller.hideObject(SkyViewController.TYPE_AIRPORT, source.getFirst());
					}
				}
			}
		});
		final JXTreeTable treeRoutesTable = new JXTreeTable(model);
		treeRoutesTable.setRootVisible(false);

		filtre.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(filtre.getText().isEmpty()){
					((FilteredTreeTableModel) model).setViewFilter(null);
				} else {
					((FilteredTreeTableModel) model).setViewFilter(new RegexViewFilter(filtre.getText()));
				}
			}
		});

		panel.add(new JScrollPane(treeRoutesTable));
		return panel;
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

}
