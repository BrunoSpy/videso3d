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

package fr.crnan.videso3d.ihm.components;

import java.awt.BorderLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

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

import fr.crnan.videso3d.Couple;


/**
 * Panel de sélection de données avec plusieurs panels filtrables
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public abstract class FilteredMultiTreeTableView extends JPanel implements DataView {

	private JTextField filtre = new JTextField(20);

	private JPanel container = new JPanel();

	private List<JXTreeTable> tables = new LinkedList<JXTreeTable>();
	private List<FilteredTreeTableModel> models = new LinkedList<FilteredTreeTableModel>();
	
	public FilteredMultiTreeTableView(){
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		//ajout d'un filtre
		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));
		filterPanel.add(Box.createVerticalGlue());
		filterPanel.add(new Label("Filtre : "));
		filterPanel.add(filtre);
		
		panel.add(filterPanel, BorderLayout.NORTH);
		
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		
		panel.add(container);
		
		this.add(panel);
	}

	public void addTableTree(final FilteredTreeTableModel model, String title){
		
		models.add(model);	
		
		//éléments de l'IHM
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		if(title != null) tablePanel.setBorder(BorderFactory.createTitledBorder(title));
		
		final JXTreeTable treeTable = new JXTreeTable();
		tables.add(treeTable);

		
		
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
				@SuppressWarnings("unchecked")
				String type = ((Couple<String, Boolean>)((DefaultMutableTreeNode)(e.getPath()[1])).getUserObject()).getFirst();
				Couple<String, Boolean> source = (Couple<String, Boolean>) ((DefaultMutableTreeNode)e.getTreePath().getLastPathComponent()).getUserObject();
				if(source.getSecond()){
					getController().showObject(getController().string2type(type), source.getFirst());
				} else {
					getController().hideObject(getController().string2type(type), source.getFirst());
				}

			}
			
		});
				
		treeTable.setTableHeader(null);
		treeTable.setRootVisible(false);
		treeTable.setTreeTableModel(model);
		
		//Ajout du filtre
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
		
		tablePanel.add(new JScrollPane(treeTable));
		container.add(tablePanel);
		
		
		treeTable.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
				if(e.getClickCount()==2){
					int row = treeTable.rowAtPoint(e.getPoint());  
					Object[] path = treeTable.getPathForRow(row).getPath();
					if(path.length>2){
						String type = ((Couple<String, Boolean>)((DefaultMutableTreeNode)path[1]).getUserObject()).getFirst();
						String name = (String) treeTable.getValueAt(row, 0);
						treeTable.setValueAt(true, row, 1);
						//TODO problème avec les données AIP : il faut rajouter le type devant le nom car highlight ne prend pas le 
						//type en paramètre.
						getController().highlight(getController().string2type(type)+" "+name);
					}
				}
			}
		});
	
	}

	@Override
	public void reset() {
		filtre.setText("");
		for(JXTreeTable t : tables){
			t.collapseAll();
		}
		for(FilteredTreeTableModel m : models){
			m.clearSelection();
		}
	}
	
	
	
}
