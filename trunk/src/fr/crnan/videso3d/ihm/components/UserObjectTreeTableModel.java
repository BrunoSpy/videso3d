
package fr.crnan.videso3d.ihm.components;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class UserObjectTreeTableModel extends FilteredTreeTableModel {

	private DefaultMutableTreeNode userObjectNode;
	
	public UserObjectTreeTableModel(DefaultMutableTreeNode root) {
		super(root);
		userObjectNode = new DefaultMutableTreeNode(new FilteredTreeTableNode("Objects utilisateurs", true));
		((DefaultMutableTreeNode)this.getRoot()).add(userObjectNode);
		
	}

	public void addObjectNode(DefaultMutableTreeNode node){
		this.userObjectNode.add(node);
		this.modelSupport.fireTreeStructureChanged(new TreePath(userObjectNode));
	}
	
	public void addProject(DefaultMutableTreeNode node){
		((DefaultMutableTreeNode)this.getRoot()).add(node);
		this.modelSupport.fireTreeStructureChanged(new TreePath(userObjectNode));
	}
	
}
