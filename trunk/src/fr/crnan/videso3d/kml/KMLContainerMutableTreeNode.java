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

import javax.swing.tree.MutableTreeNode;

import gov.nasa.worldwind.ogc.kml.KMLAbstractContainer;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class KMLContainerMutableTreeNode extends KMLMutableTreeNode {

	public KMLContainerMutableTreeNode(KMLAbstractFeature kmlFeature) {
		super(kmlFeature);
		
		for (KMLAbstractFeature child : this.getFeature().getFeatures())  {
            if (child != null){
            	MutableTreeNode featureNode = KMLMutableTreeNode.fromKMLFeature(child);
            	if(featureNode != null)
            		this.insert(featureNode);
            }
                
        }
	}

    /**
     * Indicates the KML container this node represents.
     *
     * @return this node's KML container.
     */
    @Override
    public KMLAbstractContainer getFeature() {
        return (KMLAbstractContainer) super.getFeature();
    }
	
    @Override
    public boolean isLeaf(){
    	return false;
    }
}
