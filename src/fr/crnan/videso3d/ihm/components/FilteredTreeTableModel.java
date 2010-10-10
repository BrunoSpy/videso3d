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


import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import fr.crnan.videso3d.Couple;

/**
 * TreeTableModel suitable for SkyView and AIP GUI
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class FilteredTreeTableModel extends AbstractTreeTableModel {

	private String[] titles = {"Objet", "Afficher"};

	private Class[] types = {String.class, Boolean.class};

	private ViewFilter filter;

	public FilteredTreeTableModel(DefaultMutableTreeNode root) {
		super(root);
	}

	public void setViewFilter(ViewFilter filter){
		this.filter = filter;
		this.modelSupport.fireTreeStructureChanged(new TreePath(this.getRoot()));
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

	@Override
	public Object getChild(Object parent, int index) {
		if(parent instanceof DefaultMutableTreeNode){
			if(filter != null){
				int count = 0;
				for(int i = 0; i < ((DefaultMutableTreeNode)parent).getChildCount();i++){
					Object child = ((DefaultMutableTreeNode)parent).getChildAt(i);
					if(!isLeaf(child) || this.filter.isShown((DefaultMutableTreeNode) child)){
						if(count++ == index){
							return child;
						}
					} else {
						Object child2 = getChild(child, index - count);
						if(child2 != null){
							return child2;
						}
						count += getChildCount(child);
					}
				}
			} else {
				return ((DefaultMutableTreeNode)parent).getChildAt(index);
			}
		} 
		return null;
	}

	@Override
	public int getChildCount(Object parent) {
		if(parent instanceof DefaultMutableTreeNode){
			if(filter != null) {
				int count = 0;
				for(int i = 0; i < ((DefaultMutableTreeNode)parent).getChildCount();i++){
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) ((DefaultMutableTreeNode)parent).getChildAt(i);
					if(!isLeaf(child) || this.filter.isShown(child)){
						count++;
					} else {
						count += getChildCount(child);
					}
				}
				return count;
			} else {
				return ((DefaultMutableTreeNode)parent).getChildCount();
			}				
		} else {
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.AbstractTreeTableModel#setValueAt(java.lang.Object, java.lang.Object, int)
	 */
	@Override
	public void setValueAt(Object value, Object node, int column) {
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
		if(column == 1){
			//ne pas changer la valuer des nodes non affichés
			if(filter != null){
				if(!treeNode.isLeaf()){
					((Couple<String, Boolean>) treeNode.getUserObject()).setSecond((Boolean)value);
					for(int i=0;i<treeNode.getChildCount();i++){
						DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
						setValueAt(value, child, column);
					}
				} else {
					if(this.filter.isShown((DefaultMutableTreeNode) node)){
						((Couple<String, Boolean>) treeNode.getUserObject()).setSecond((Boolean)value);
						this.modelSupport.fireChildChanged(new TreePath(treeNode.getPath()), 0, treeNode);
					}
				}
			} else {
				((Couple<String, Boolean>) treeNode.getUserObject()).setSecond((Boolean)value);
				if(!treeNode.isLeaf()){
					for(int i=0;i<treeNode.getChildCount();i++){
						DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
						setValueAt(value, child, column);
					}
				} else {
					this.modelSupport.fireChildChanged(new TreePath(treeNode.getPath()), 0, treeNode);
				}
			}
		}
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		// TODO Auto-generated method stub
		return 0;
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

}
