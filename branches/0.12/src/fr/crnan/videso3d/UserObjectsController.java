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
package fr.crnan.videso3d;

import fr.crnan.videso3d.graphics.Balise;
import fr.crnan.videso3d.graphics.Balise3D;
import fr.crnan.videso3d.layers.Balise3DLayer;
import fr.crnan.videso3d.project.Project;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
/**
 * Controls the objects created by the user
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class UserObjectsController implements VidesoController {
	
	/**
	 * Projects loaded
	 */
	private List<Project> projects;
	
	/**
	 * User generated objects
	 */
	private HashMap<Integer, Object> objects;

	private RenderableLayer renderableLayer;
	private Balise3DLayer balise3DLayer;
	
	private VidesoGLCanvas wwd;
	
	public UserObjectsController(VidesoGLCanvas wwd){
		this.wwd=  wwd;
		
		renderableLayer = new RenderableLayer();
		renderableLayer.setName("Renderable User Objects");
		this.wwd.toggleLayer(renderableLayer, true);
		
		balise3DLayer = new Balise3DLayer("User Generated Points");
		this.wwd.toggleLayer(balise3DLayer, true);
	}
	
	
	/**
	 * 
	 * @param o
	 * @return True if this controller manages <code>o</code>
	 */
	public boolean manages(Object o){
		return objects.containsValue(o);
	}
	
	public void removeObject(Object o){
		
	}
	
	/**
	 * Add an object to the adequate layer<br />
	 * In order to be saved in a project, objects have to be {@link Restorable}
	 * @param o 3D object
	 */
	public void addObject(Restorable o){
		if(o instanceof Renderable){
			if(renderableLayer == null){
				renderableLayer = new RenderableLayer();
				renderableLayer.setName("Objets personnalisés");
				this.toggleLayer(renderableLayer, true);
			}
			renderableLayer.addRenderable((Renderable) o);
		} else if(o instanceof Balise3D){
			if(balise3DLayer == null){
				balise3DLayer = new Balise3DLayer("Points personnalisés");
				balise3DLayer.setPickEnabled(true);
				this.toggleLayer(balise3DLayer, true);
			}
			balise3DLayer.addBalise((Balise) o);
		}
	}
	
	/**
	 * 
	 * @return True if objects independant of any project
	 */
	public boolean hasUserObjects(){
		return (renderableLayer != null && renderableLayer.getRenderables().iterator().hasNext()) ||
				(balise3DLayer != null && balise3DLayer.getVisibleBalises().size() > 0);
	}
	
	/**
	 * @return All visible and restorable objects added by user and not part of a project
	 */
	public List<Restorable> getUserObjects(){
		List<Restorable> objects = new ArrayList<Restorable>();
		if(renderableLayer != null){
			for(Renderable r : renderableLayer.getRenderables()){
				if(r instanceof Restorable){
					objects.add((Restorable) r);
				}
			}
		}
		if(balise3DLayer != null){
			for(Balise3D b : balise3DLayer.getVisibleBalises()){
				objects.add(b);
			}
		}
		return objects;
	}
	
	/**
	 * Supprime tous les objets ajoutés par l'utilisateur
	 */
	public void deleteAllUserObjects(){
		if(renderableLayer != null)
			renderableLayer.removeAllRenderables();
		if(balise3DLayer != null)
			balise3DLayer.eraseAllBalises();
	}
	
	@Override
	public void highlight(int type, String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unHighlight(int type, String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addLayer(String name, Layer layer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeLayer(String name, Layer layer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAllLayers() {
		// TODO Auto-generated method stub

	}

	@Override
	public void toggleLayer(Layer layer, Boolean state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showObject(int type, String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void hideObject(int type, String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public int string2type(String type) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String type2string(int type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void set2D(Boolean flat) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Object> getObjects(int type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setColor(Color color, int type, String name) {
		// TODO Auto-generated method stub

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

	@Override
	public boolean areLocationsVisible(int type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setLocationsVisible(int type, String name, boolean b) {
		// TODO Auto-generated method stub

	}

}
