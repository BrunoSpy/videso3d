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


/*
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
import java.awt.BorderLayout;
import java.io.File;
import java.net.URL;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingEvent;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingListener;

import javax.swing.JPanel;
import javax.swing.tree.TreePath;

import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.kml.KMLMutableTreeNode;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;

/**
 * EXPERIMENTAL
 * @author Mickael Papail
 * @author Bruno Spyckerelle
 * @version 0.2.0
 */
public class KMLView extends JPanel{

	private KMLRoot kmlRoot;
	private KMLController controller;
	private RenderableLayer layer;
		
	public KMLView(KMLRoot root, final VidesoGLCanvas wwd){
		this.kmlRoot = root;
			
		controller = new KMLController(kmlRoot);
		layer = new RenderableLayer();
		layer.setName("KML Layer");
		
		layer.addRenderable(controller);
		
		wwd.toggleLayer(layer, true);
		
		KMLMutableTreeNode rootNode = KMLMutableTreeNode.fromKMLFeature(this.kmlRoot.getFeature());
		
		CheckboxTree tree = new CheckboxTree(rootNode);
		tree.setRootVisible(false);
		tree.getCheckingModel().addCheckingPath(new TreePath(rootNode));
		tree.addTreeCheckingListener(new TreeCheckingListener() {
			
			@Override
			public void valueChanged(TreeCheckingEvent e) {
				KMLMutableTreeNode kmlNode = (KMLMutableTreeNode) e.getPath().getLastPathComponent();
				kmlNode.getFeature().setVisibility(e.isCheckedPath());
				wwd.redrawNow();
			}
		});
		
		this.add(tree, BorderLayout.CENTER);
        
	}
	
	public Layer getLayer(){
		return this.layer;
	}
	
	public String getTitle(){
		return (String) kmlRoot.getField(AVKey.DISPLAY_NAME);
	}
	
	protected static String formName(Object kmlSource, KMLRoot kmlRoot)
    {
        KMLAbstractFeature rootFeature = kmlRoot.getFeature();

        if (rootFeature != null && !WWUtil.isEmpty(rootFeature.getName()))
            return rootFeature.getName();

        if (kmlSource instanceof File)
            return ((File) kmlSource).getName();

        if (kmlSource instanceof URL)
            return ((URL) kmlSource).getPath();

        if (kmlSource instanceof String && WWIO.makeURL((String) kmlSource) != null)
            return WWIO.makeURL((String) kmlSource).getPath();

        return "KML Layer";
    }
}