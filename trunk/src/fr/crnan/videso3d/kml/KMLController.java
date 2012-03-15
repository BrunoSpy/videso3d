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

import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import fr.crnan.videso3d.ProgressSupport;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.examples.kml.KMLApplicationController;
import gov.nasa.worldwindx.examples.util.BalloonController;
import gov.nasa.worldwindx.examples.util.HotSpotController;

/**
 * 
 * @author Mickael Papail
 *
 */
public class KMLController extends ProgressSupport implements VidesoController{
	private String filePath;
	private VidesoGLCanvas wwd;
	private KML kml;
	protected BalloonController balloonController;
	protected HotSpotController hotSpotController;
	protected gov.nasa.worldwind.ogc.kml.impl.KMLController kmlControl;
	protected KMLApplicationController kmlAppEventController;
	protected KMLRoot documentRoot; 	
	
	
	public KMLController(VidesoGLCanvas wwd) {

		this.wwd = wwd;
		kml = new KML(); 
		documentRoot = kml.getKmlRoot();
		documentRoot.setField(AVKey.DISPLAY_NAME, kml.getKmlRoot().toString());
		// Add a controller to handle input events on the layer selector and on browser balloons.
        this.hotSpotController = new HotSpotController(this.wwd);
        
        // Add a controller to handle common KML application events.
        
        this.kmlAppEventController = new KMLApplicationController(this.wwd);
        this.balloonController = new BalloonController(this.wwd);       
        this.addKMLLayer(documentRoot);        
        this.kmlAppEventController.setBalloonController(balloonController);       
        this.balloonController = new BalloonController(this.wwd) {
        	 @Override
             protected void addDocumentLayer(KMLRoot document)
             {
              addKMLLayer(documentRoot);
             }										
        };										
	}
		
	protected void addKMLLayer(KMLRoot document)
    {
		// this.wwd.firePropertyChange("step", "", "Création de l'arborescence KML");
		// Create a KMLController to adapt the KMLRoot to the World Wind renderable interface.
		
		kmlControl =   new gov.nasa.worldwind.ogc.kml.impl.KMLController(kml.getKmlRoot());
	
        // Adds a new layer containing the KMLRoot to the end of the WorldWindow's layer list. This
        // retrieve's the layer name from the KMLRoot's DISPLAY_NAME field.
        RenderableLayer layer = new RenderableLayer();
        layer.setName("Layer KML");        
        layer.setName((String) kml.getKmlRoot().getField(AVKey.DISPLAY_NAME));                     
        //System.out.println("Le nom du kml root est "+(String) kml.getKmlRoot().getField(AVKey.DISPLAY_NAME));
        layer.addRenderable(kmlControl);
        this.wwd.getModel().getLayers().add(layer);
         
        // Adds a new layer tree node for the KMLRoot to the on-screen layer tree, and makes the new node visible
        // in the tree. This also expands any tree paths that represent open KML containers or open KML network
        // links.
        /*
        KMLLayerTreeNode layerNode = new KMLLayerTreeNode(layer, kmlRoot);
        this.layerTree.getModel().addLayer(layerNode);
        this.layerTree.makeVisible(layerNode.getPath());
        layerNode.expandOpenContainers(this.layerTree);
		*/

        // Listens to refresh property change events from KML network link nodes. Upon receiving such an event this
        // expands any tree paths that repre)sent open KML containers. When a KML network link refreshes, its tree
        // node replaces its children with new nodes created form the refreshed content, then sends a refresh
        // property change event through the layer tree. By expanding open containers after a network link refresh,
        // we ensure that the network link tree view appearance is consistent with the KML specification.
        /*
        layerNode.addPropertyChangeListener(AVKey.RETRIEVAL_STATE_SUCCESSFUL, new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent event)
            {
                if (event.getSource() instanceof KMLNetworkLinkTreeNode)
                    ((KMLNetworkLinkTreeNode) event.getSource()).expandOpenContainers(layerTree);
            }
        });
    	*/
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
	
	@Override
	public void addLayer(String name, Layer layer) {
		// TODO Auto-generated method stub						
	}
	@Override
	public Collection<Object> getObjects(int type) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void hideObject(int type, String name) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void highlight(int type, String name) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removeAllLayers() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removeLayer(String name, Layer layer) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void set2D(Boolean flat) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setColor(Color color, int type, String name) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void showObject(int type, String name) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int string2type(String type) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void toggleLayer(Layer layer, Boolean state) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String type2string(int type) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void unHighlight(int type, String name) {
		// TODO Auto-generated method stub
		
	}
	public static int getNumberInitSteps() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isColorEditable(int type) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public HashMap<Integer, List<String>> getSelectedObjectsReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Restorable> getSelectedObjects() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Not implemented in this controller
	 */
	@Override
	public boolean areLocationsVisible(int type, String name) {
		return false;
	}

	/**
	 * Not implemented in this controller
	 */
	@Override
	public void setLocationsVisible(int type, String name, boolean b) {
	}
	
}



