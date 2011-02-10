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

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import org.jdesktop.swingx.JXMultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout;
import org.jdesktop.swingx.MultiSplitLayout.Node;
import org.jdesktop.swingx.MultiSplitLayout.Leaf;
import org.jdesktop.swingx.MultiSplitLayout.Divider;
import org.jdesktop.swingx.MultiSplitLayout.Split;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class VerticalMultipleSplitPanes extends JXMultiSplitPane {

	private List<Node> children;
	
	private Split modelRoot = new Split();
	
	public VerticalMultipleSplitPanes() {
		this.modelRoot.setRowLayout(false);
		this.getMultiSplitLayout().setModel(modelRoot);
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#add(java.awt.Component)
	 */
	@Override
	public Component add(Component c) {
		int size = 0;
		if(children == null){
			size = 0;
			children = new LinkedList<MultiSplitLayout.Node>();
			children.add(new Leaf("leaf"+size));
		} else {
			size = children.size();
			children.add(new Divider());
			Leaf newLeaf = new Leaf("leaf"+size);
			children.add(newLeaf);
		}
		modelRoot.setChildren(children);
		super.add(c, "leaf"+size);
		return c;
	} 	
	
}
