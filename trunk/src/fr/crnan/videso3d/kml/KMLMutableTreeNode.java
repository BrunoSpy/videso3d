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
package fr.crnan.videso3d.kml;

import fr.crnan.videso3d.ihm.components.FilteredTreeTableNode;
import gov.nasa.worldwind.ogc.kml.KMLAbstractContainer;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.KMLNetworkLink;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class KMLMutableTreeNode extends FilteredTreeTableNode implements MutableTreeNode  {

	protected MutableTreeNode parent;
	protected List<MutableTreeNode> children;
	protected KMLAbstractFeature feature;
	
	public KMLMutableTreeNode(String name, boolean visible, KMLAbstractFeature kmlFeature){
		super(name, visible);
		this.feature = kmlFeature;
		
	}
	
	
	
    /**
     * Creates a new <code>KMLMutableTreeNode</code> from the specified <code>feature</code>. This maps the feature type
     * to a node type as follows: <ul> <li>KML container to <code>KMLContainerMutableTreeNode</code>.</li> <li>KML network link
     * to <code>KMLNetworkLinkMutableTreeNode</code>.</li> <li>All other KML features to <code>KMLMutableTreeNode</code>.</li> </ul>
     *
     * @param feature the KML feature to create a new <code>KMLMutableTreeNode</code> for.
     *
     * @return a new <code>MutableTreeNode</code>.
     *
     * @throws IllegalArgumentException if the <code>feature</code> is <code>null</code>.
     */
    public static MutableTreeNode fromKMLFeature(KMLAbstractFeature feature) {
        if (feature == null)
        {
            String message = Logging.getMessage("nullValue.FeatureIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        if (feature instanceof KMLNetworkLink)
            return new KMLNetworkLinkMutableTreeNode((KMLNetworkLink) feature);
        else if (feature instanceof KMLAbstractContainer)
            return new KMLContainerMutableTreeNode((KMLAbstractContainer) feature);
        else 
            return new KMLMutableTreeNode(feature.getName(), true, feature);
    }
	
	@Override
	public Enumeration<MutableTreeNode> children() {
		return new Vector<MutableTreeNode>(children).elements();
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		return this.children.get(childIndex);
	}

	@Override
	public int getChildCount() {
		if(this.children == null){
			return 0;
		} else {
			return this.children.size();
		}
	}

	@Override
	public int getIndex(TreeNode node) {
		return this.children.indexOf(node);
	}

	@Override
	public void insert(MutableTreeNode child, int index) {
		if (this.children == null)
            this.children = new ArrayList<MutableTreeNode>();
		
		this.children.add(index, child);
		child.setParent(this);
	}

	public void insert(MutableTreeNode child){
		if(this.children == null)
			this.children = new ArrayList<MutableTreeNode>();
		this.insert(child, this.children.size());
	}
	
	@Override
	public void remove(int index) {
		 if (this.children != null) {
			 MutableTreeNode child = this.children.remove(index);
			 if (child != null && child.getParent() == this)
			 child.setParent(null); 
		 }
		 
		 
	}

	@Override
	public void remove(MutableTreeNode node) {
		if (this.children != null) {
			 this.children.remove(node);
			 if (node != null && node.getParent() == this)
			 node.setParent(null); 
		 }
		
	}
	
	public void removeAllChildren(){
		if (this.children == null)
            return;
		
		Iterator<MutableTreeNode> iterator = this.children.iterator();
        if (!iterator.hasNext())
            return;

        while (iterator.hasNext()) {
            MutableTreeNode child = iterator.next();
            iterator.remove();

            child.setParent(null);
        }

	}

	@Override
	public void removeFromParent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParent(MutableTreeNode newParent) {
		this.parent = newParent;
	}

	@Override
	public void setUserObject(Object object) {
		
	}

	@Override
	public TreeNode getParent() {
		return this.parent;
	}

	@Override
	public boolean isLeaf() {
		return true;
	}


    /**
     * Indicates the KML feature this node represents.
     *
     * @return this node's KML feature.
     */
    public KMLAbstractFeature getFeature()
    {
        return this.feature;
    }

    @Override
    public String toString(){
    	return this.getFeature().getName();
    }

}
