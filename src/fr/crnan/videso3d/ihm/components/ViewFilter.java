package fr.crnan.videso3d.ihm.components;

import javax.swing.tree.DefaultMutableTreeNode;

public interface ViewFilter {

	boolean isShown(DefaultMutableTreeNode node);
	
}
