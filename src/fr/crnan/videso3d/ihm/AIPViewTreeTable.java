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

import javax.swing.BorderFactory;
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
import fr.crnan.videso3d.aip.AIPController;
import fr.crnan.videso3d.ihm.components.DataView;
import fr.crnan.videso3d.ihm.components.FilteredTreeTableModel;
import fr.crnan.videso3d.ihm.components.RegexViewFilter;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class AIPViewTreeTable extends JPanel implements DataView {


	private AIPController controller;

	private JTextField filtre = new JTextField(20);

	public AIPViewTreeTable(AIPController aipController) {
		this.controller = aipController;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		try {
			if(DatabaseManager.getCurrentAIP() != null) { //si pas de bdd, ne pas créer la vue

				JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());

				//ajout d'un filtre
				JPanel filterPanel = new JPanel();
				filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));
				filterPanel.add(Box.createVerticalGlue());
				filterPanel.add(new Label("Filtre : "));
				filterPanel.add(filtre);


				panel.add(filterPanel, BorderLayout.NORTH);

				panel.add(this.buildTree());

				this.add(panel);
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

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	private Component buildTree() {

		JPanel volumes = new JPanel();

		volumes.setLayout(new BorderLayout());
		volumes.setBorder(BorderFactory.createTitledBorder("Zones"));

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		this.fillRootNode(root);

		final JXTreeTable treeTable = new JXTreeTable();
		
		final AbstractTreeTableModel model = new FilteredTreeTableModel(root);
		model.addTreeModelListener(new TreeModelListener() {

			@Override
			public void treeStructureChanged(TreeModelEvent e) {
				if(filtre.getText().isEmpty()){
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
				String type = ((Couple<String, Boolean>)((DefaultMutableTreeNode)(e.getPath()[1])).getUserObject()).getFirst();
				Couple<String, Boolean> source = (Couple<String, Boolean>) ((DefaultMutableTreeNode)e.getTreePath().getLastPathComponent()).getUserObject();

				if(source.getSecond()){
					controller.showObject(AIPController.string2type(type), source.getFirst());
				} else {
					controller.hideObject(AIPController.string2type(type), source.getFirst());
				}

			}
		});
		treeTable.setTreeTableModel(model);
		treeTable.setRootVisible(false);

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

		volumes.add(new JScrollPane(treeTable));
		return volumes;
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
