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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.ihm.components.DataView;

/**
 * Interface de sélection de données SkyView
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class SkyView extends JPanel implements DataView{
	
	private VidesoController controller;
	
	private JPanel panel = new JPanel();
	
	public SkyView(VidesoController controller){
		this.controller = controller;
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		panel.setBorder(BorderFactory.createTitledBorder(""));
		
		try{
			if(DatabaseManager.getCurrentSkyView() != null){
				this.add(this.buildRoutes());
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
		
		this.add(Box.createVerticalGlue());		
	}

	@Override
	public VidesoController getController() {
		return this.controller;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	
	private Component buildRoutes() {
		panel.setLayout(new BorderLayout());
		AbstractTreeTableModel model = new SkyViewRouteTreeModel();
		final JXTreeTable treeRoutesTable = new JXTreeTable(model);
		treeRoutesTable.setRootVisible(false);
		panel.add(new JScrollPane(treeRoutesTable));
		return panel;
	}
	
	/* ********************************************************* */
	/* **************** Table models *************************** */
	/* ********************************************************* */
	
	private class SkyViewRouteTreeModel extends AbstractTreeTableModel {

		private String[] titles = {"Objet", "Afficher"};
		
		private Class[] types = {String.class, Boolean.class};
				
		public SkyViewRouteTreeModel(){
			super(new DefaultMutableTreeNode("root"));
			try {
				Statement st = DatabaseManager.getCurrentSkyView();
				//ajout des routes
				DefaultMutableTreeNode routes = new DefaultMutableTreeNode(new Couple<String, Boolean>("Routes", false));
				((DefaultMutableTreeNode) getRoot()).add(routes);
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
				((DefaultMutableTreeNode) getRoot()).add(waypoints);
				rs = st.executeQuery("select distinct icao from waypoint order by icao");
				icao.clear();
				while(rs.next()){
					icao.add(rs.getString(1));
				}
				for(String oaci : icao){
					rs = st.executeQuery("select ident from waypoint order by ident");
					DefaultMutableTreeNode point = new DefaultMutableTreeNode(new Couple<String, Boolean>(oaci, false));
					waypoints.add(point);
					while(rs.next()){
						point.add(new DefaultMutableTreeNode(new Couple<String, Boolean>(rs.getString(1), false)));
					}
				}
				//ajout des aéroports
				DefaultMutableTreeNode airports = new DefaultMutableTreeNode(new Couple<String, Boolean>("Airports", false));
				((DefaultMutableTreeNode) getRoot()).add(airports);
				rs = st.executeQuery("select distinct icao from airport order by icao");
				icao.clear();
				while(rs.next()){
					icao.add(rs.getString(1));
				}
				for(String oaci : icao){
					rs = st.executeQuery("select ident from airport order by ident");
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
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(Object node, int column) {
			Object value = null;
			if(node instanceof DefaultMutableTreeNode){
				Object o = ((DefaultMutableTreeNode)node).getUserObject();
				if(o instanceof Couple){
					if(column == 0){
						value = ((Couple<String, Boolean>)o).getFirst();
					} else if(column ==1) {
						value = ((Couple<String, Boolean>)o).getSecond();
					}
				}
			}
			return value;
		}

		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.treetable.AbstractTreeTableModel#isCellEditable(java.lang.Object, int)
		 */
		@Override
		public boolean isCellEditable(Object node, int column) {
			if(column == 1) {
				return true;
			} else {
				return false;
			}
		}

		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.treetable.AbstractTreeTableModel#setValueAt(java.lang.Object, java.lang.Object, int)
		 */
		@Override
		public void setValueAt(Object value, Object node, int column) {
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
			if(column == 1){
				((Couple<String, Boolean>) treeNode.getUserObject()).setSecond((Boolean)value);
				if(!treeNode.isLeaf()){
					for(int i=0;i<treeNode.getChildCount();i++){
						DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
						setValueAt(value, child, column);
						this.modelSupport.fireChildChanged(new TreePath(treeNode.getPath()), i, child);
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.treetable.AbstractTreeTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int column) {
			return types[column];
		}

		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.treetable.AbstractTreeTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int column) {
			if(column < titles.length) {
				return titles[column];
			} else {
				return "";
			}
		}

		@Override
		public Object getChild(Object parent, int index) {
			if(parent instanceof DefaultMutableTreeNode){
				return ((DefaultMutableTreeNode)parent).getChildAt(index);
			} else {
				return null;
			}
		}

		@Override
		public int getChildCount(Object parent) {
			if(parent instanceof DefaultMutableTreeNode){
				return ((DefaultMutableTreeNode)parent).getChildCount();
			} else {
				return 0;
			}
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			// TODO Auto-generated method stub
			return 0;
		}
		

	}
	
}
