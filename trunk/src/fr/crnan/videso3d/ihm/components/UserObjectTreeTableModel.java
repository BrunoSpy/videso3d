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

import fr.crnan.videso3d.kml.KMLMutableTreeNode;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class UserObjectTreeTableModel extends FilteredTreeTableModel {

	private DefaultMutableTreeNode userObjectNode;
	
	public UserObjectTreeTableModel(DefaultMutableTreeNode root) {
		super(root);
		userObjectNode = new DefaultMutableTreeNode(new FilteredTreeTableNode("Objets utilisateurs", true));
		((DefaultMutableTreeNode)this.getRoot()).add(userObjectNode);
		
	}

	public void addObjectNode(DefaultMutableTreeNode node){
		this.userObjectNode.add(node);
		this.modelSupport.fireTreeStructureChanged(new TreePath(userObjectNode));
	}
	
	public void removeObjectNode(DefaultMutableTreeNode node){
		this.userObjectNode.remove(node);
		this.modelSupport.fireTreeStructureChanged(new TreePath(userObjectNode));
	}
	
	public void addProject(DefaultMutableTreeNode node){
		((DefaultMutableTreeNode)this.getRoot()).add(node);
		this.modelSupport.fireTreeStructureChanged(new TreePath(userObjectNode));
	}
	
	public void addKMLNode(DefaultMutableTreeNode node){
		((DefaultMutableTreeNode) this.getRoot()).add(node);
		this.modelSupport.fireTreeStructureChanged(new TreePath(userObjectNode));
	}
}
