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


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

/**
 * TreeTableModel suitable for STIP, SkyView and AIP GUI
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class FilteredTreeTableModel extends AbstractTreeTableModel {

	private String[] titles = {"Objet", "Afficher"};

	private Class[] types = {String.class, Boolean.class};

	private ViewFilter filter;
	
	private int count;
	
	private PropertyChangeSupport support;
	
	public FilteredTreeTableModel(DefaultMutableTreeNode root) {
		super(root);
		this.support = new PropertyChangeSupport(this);
		
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
			if(o instanceof FilteredTreeTableNode){
				if(column == 0){
					value = ((FilteredTreeTableNode)o).getName();
				} else if(column ==1) {
					value = ((FilteredTreeTableNode)o).isVisible();
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

	public int getLeafsCount(Object parent, boolean value){
		if(parent instanceof DefaultMutableTreeNode){
			if(filter != null) {
				int count = 0;
				for(int i = 0; i < ((DefaultMutableTreeNode)parent).getChildCount();i++){
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) ((DefaultMutableTreeNode)parent).getChildAt(i);
					if(isLeaf(child) && this.filter.isShown(child) && ((FilteredTreeTableNode) child.getUserObject()).isVisible() == value){
						count++;
					} else {
						count += getLeafsCount(child, value);
					}
				}
				return count;
			} else {
				int count = 0;
				for(int i = 0; i < ((DefaultMutableTreeNode)parent).getChildCount();i++){
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) ((DefaultMutableTreeNode)parent).getChildAt(i);
					if(isLeaf(child)&& ((FilteredTreeTableNode) child.getUserObject()).isVisible() == value){
						count++;
					} else {
						count += getLeafsCount(child, value);
					}
				}
				return count;
				
			}				
		} else {
			return 0;
		}
	}
	
	public List<DefaultMutableTreeNode> getChildList(Object parent){
		List<DefaultMutableTreeNode> list = new ArrayList<DefaultMutableTreeNode>();
		if(parent instanceof DefaultMutableTreeNode) {
			if(((DefaultMutableTreeNode) parent).isLeaf())
				list.add((DefaultMutableTreeNode) parent);
			else {
				for(int i = 0; i < ((DefaultMutableTreeNode)parent).getChildCount();i++){
					list.addAll(getChildList(((DefaultMutableTreeNode)parent).getChildAt(i)));
				}
			}
			return list;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.AbstractTreeTableModel#setValueAt(java.lang.Object, java.lang.Object, int)
	 */
	@Override
	public void setValueAt(final Object value, final Object node, final int column) {
		new SwingWorker<Integer, Integer>() {
			protected Integer doInBackground() throws Exception {
				setValueAt(value, node, column, true, true);
				return 1;
			};
		}.execute();
	}

	private void setValueAt(Object value, Object node, int column, boolean first, boolean fireEvent){
		
		if(column == 1){
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
			Boolean old = ((FilteredTreeTableNode)((DefaultMutableTreeNode)treeNode).getUserObject()).isVisible();
			//do nothing if old value equals new value
			if(old == value)
				return;
			
			if(first) {
				count = 0;
				if((Boolean) value)
					this.support.firePropertyChange("change", -1, this.getLeafsCount(node, !((Boolean) value)));
			}
			//ne pas changer la valeur des nodes non affichés
			if(filter != null){
				if(!treeNode.isLeaf()){
					((FilteredTreeTableNode) treeNode.getUserObject()).setVisible((Boolean)value);
					for(int i=0;i<treeNode.getChildCount();i++){
						DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
						setValueAt(value, child, column, false, true);
					}
				} else {
					if(this.filter.isShown((DefaultMutableTreeNode) node)){
						((FilteredTreeTableNode) treeNode.getUserObject()).setVisible((Boolean)value);
						count++;
						if((Boolean) value) 
							this.support.firePropertyChange("progress", count-1, count);
						this.modelSupport.fireChildChanged(new TreePath(treeNode.getPath()), 0, treeNode);
					}
				}
			} else {
				((FilteredTreeTableNode) treeNode.getUserObject()).setVisible((Boolean)value);
				if(!treeNode.isLeaf()){
					try{
						if(fireEvent)
							this.modelSupport.fireChildChanged(new TreePath(treeNode.getPath()), 0, treeNode);

						String nodeName = ((FilteredTreeTableNode) treeNode.getUserObject()).getName();
						if(nodeName.equals("Waypoints"))
							fireEvent = false;
						else if(treeNode.getParent() instanceof FilteredTreeTableNode){
							if(((FilteredTreeTableNode)((DefaultMutableTreeNode)treeNode.getParent()).getUserObject()).getName().equals("Waypoints"))
								fireEvent = false;
						}
						for(int i=0;i<treeNode.getChildCount();i++){
							DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
							setValueAt(value, child, column, false, fireEvent);
						}
					}catch(Exception e){e.printStackTrace();}
				} else {
					count++;
					if((Boolean) value) 
						this.support.firePropertyChange("progress", count-1, count);
					if(fireEvent)
						this.modelSupport.fireChildChanged(new TreePath(treeNode.getPath()), 0, treeNode);	
				}
			}
		}
	}
	
	@Override
	public int getIndexOfChild(Object parent, Object child) {
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

	/**
	 * Delete the filter and clear the selection.
	 */
	public void clearSelection(){
		this.setViewFilter(null);

		Enumeration<Object> e = ((DefaultMutableTreeNode)this.getRoot()).children();
		while(e.hasMoreElements()){
			this.clearSelection((DefaultMutableTreeNode)e.nextElement());
		}
	}

	private void clearSelection(DefaultMutableTreeNode node){
		this.setValueAt(false, node, 1);
		if(!node.isLeaf()){
			Enumeration<Object> e = ((DefaultMutableTreeNode)node).children();
			while(e.hasMoreElements()){
				this.clearSelection((DefaultMutableTreeNode)e.nextElement());
			}
		}
	}
	
	public void addPropertyChangeListener(PropertyChangeListener l){
		support.addPropertyChangeListener(l);
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener l){
		support.addPropertyChangeListener(propertyName, l);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener l){
		support.removePropertyChangeListener(l);
	}
	
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener l){
		support.removePropertyChangeListener(propertyName, l);
	}
	
}
