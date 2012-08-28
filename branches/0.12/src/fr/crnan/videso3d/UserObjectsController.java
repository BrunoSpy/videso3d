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
import fr.crnan.videso3d.graphics.VidesoObject;
import fr.crnan.videso3d.graphics.editor.PolygonEditorsManager;
import fr.crnan.videso3d.graphics.editor.ShapeEditorsManager;
import fr.crnan.videso3d.ihm.UserObjectsView;
import fr.crnan.videso3d.layers.Balise3DLayer;
import fr.crnan.videso3d.layers.ProjectLayer;
import fr.crnan.videso3d.project.Project;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AbstractShape;
import gov.nasa.worldwind.render.Highlightable;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.Polygon;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
/**
 * Controls the objects created by the user<br />
 * A view has to be created before the controller.
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
	private List<Restorable> userObjects;

	/**
	 * Every objects : either user generated or linked to a project
	 */
	private List<Object> objects;
	
	private RenderableLayer renderableLayer;
	private Balise3DLayer balise3DLayer;
	private AirspaceLayer airspaceLayer;
	
	private VidesoGLCanvas wwd;
	private UserObjectsView view;
	
	public UserObjectsController(VidesoGLCanvas wwd, UserObjectsView view){
		this.wwd=  wwd;
		this.view = view;
		
		this.userObjects = new ArrayList<Restorable>();
		this.projects = new ArrayList<Project>();
		this.objects = new ArrayList<Object>();
		
		renderableLayer = new RenderableLayer();
		renderableLayer.setName("Renderable User Objects");
		this.wwd.toggleLayer(renderableLayer, true);
		
		balise3DLayer = new Balise3DLayer("User Generated Points");
		balise3DLayer.setPickEnabled(true);
		this.wwd.toggleLayer(balise3DLayer, true);
		
		airspaceLayer = new AirspaceLayer();
		airspaceLayer.setName("User Generated Airspaces");
		this.wwd.toggleLayer(airspaceLayer, true);
	}
	
	public int getID(Object o){
		return this.objects.indexOf(o);
	}
		
	/**
	 * 
	 * @param o
	 * @return True if this controller manages <code>o</code>
	 */
	public boolean manages(Object o){
		return objects.contains(o);
	}
	
	public void removeObject(Object o){
		if(userObjects.contains(o)){
			if(o instanceof Airspace){
				airspaceLayer.removeAirspace((Airspace)o);
				PolygonEditorsManager.stopEditAirspace((Polygon) o);
			} else	if(o instanceof Renderable){
				renderableLayer.removeRenderable((Renderable) o);
			} else if(o instanceof Balise3D){
				balise3DLayer.removeBalise((Balise) o);
			}
			this.userObjects.remove(o);
		} else {
			for(Project p : this.projects){
				p.removeObject(o);
			}
		}
		objects.remove(o);
		
		this.view.remove(o);
	}
	
	public void addProject(Project project){
		this.projects.add(project);
		ProjectLayer layer = new ProjectLayer(project);
		project.setProjectLayer(layer);
		this.wwd.toggleLayer(layer, true);
		this.objects.addAll(project.getAllObjects());
		
		this.view.addProject(project);
	}
	
	/**
	 * Add an object to the adequate layer<br />
	 * In order to be saved in a project, objects have to be {@link Restorable}
	 * @param o 3D object
	 */
	public void addObject(Restorable o){
		if(o instanceof VidesoObject){
			if(o instanceof Airspace){
				airspaceLayer.addAirspace((Airspace)o);
			} else	if(o instanceof Renderable){
				renderableLayer.addRenderable((Renderable) o);
			} else if(o instanceof Balise3D){
				balise3DLayer.addBalise((Balise) o);
			}
			this.userObjects.add(o);
			this.objects.add(o);
			this.view.addObject((VidesoObject) o);
		} else {
			throw new IllegalArgumentException("Object must be of class VidesoObject");
		}
	}
	
	/**
	 * 
	 * @return True if objects independant of any project
	 */
	public boolean hasUserObjects(){
		return (renderableLayer != null && renderableLayer.getRenderables().iterator().hasNext()) ||
				(balise3DLayer != null && balise3DLayer.getVisibleBalises().size() > 0) ||
				(airspaceLayer != null && airspaceLayer.getAirspaces().iterator().hasNext());
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
		if(airspaceLayer != null){
			for(Airspace r : airspaceLayer.getAirspaces()){
				if(r instanceof Restorable && r.isVisible()){
					objects.add((Restorable) r);
				}
			}
		}
		return objects;
	}
	
	/**
	 * Supprime tous les objets ajoutés par l'utilisateur
	 */
	public void deleteAllUserObjects(){
		for(Object o : this.userObjects){
			this.removeObject(o);
		}
	}
	
	@Override
	public void highlight(int type, String name) {
		Object o = this.objects.get(type);
		if(o instanceof Highlightable){
			((Highlightable) o).setHighlighted(true);
			this.wwd.redraw();
		}
	}

	@Override
	public void unHighlight(int type, String name) {
		Object o = this.objects.get(type);
		if(o instanceof Highlightable){
			((Highlightable) o).setHighlighted(false);
			this.wwd.redraw();
		}

	}

	@Override
	public void addLayer(String name, Layer layer) {
	}

	@Override
	public void removeLayer(String name, Layer layer) {
	}

	@Override
	public void removeAllLayers() {
	}

	@Override
	public void toggleLayer(Layer layer, Boolean state) {
		this.wwd.toggleLayer(layer, state);
	}

	@Override
	public void showObject(int type, String name) {
		Object o = this.objects.get(type);
		if(o != null){
			if(this.userObjects.contains(o)){
				if(o instanceof Airspace) {
					((Airspace) o).setVisible(true);
					this.airspaceLayer.firePropertyChange(AVKey.LAYER, null, this.airspaceLayer);
				} else if(o instanceof Renderable){
					this.renderableLayer.addRenderable((Renderable) o);
					this.renderableLayer.firePropertyChange(AVKey.LAYER, null, this.renderableLayer);
				} else if(o instanceof Balise3D) {
					((Balise3D) o).setVisible(true);
				}
			} else {
				for(Project p : projects){
					p.getProjectLayer().setVisible(o, true);
				}
			}
		}
	}

	@Override
	public void hideObject(int type, String name) {
		Object o = this.objects.get(type);
		if(o != null){
			if(this.userObjects.contains(o)){
				if(o instanceof Airspace) {
					((Airspace) o).setVisible(false);
					this.airspaceLayer.firePropertyChange(AVKey.LAYER, null, this.airspaceLayer);
					PolygonEditorsManager.stopEditAirspace((Polygon) o);
				} else if(o instanceof Renderable){
					this.renderableLayer.removeRenderable((Renderable) o);
					if(o instanceof AbstractShape)
						ShapeEditorsManager.stopEditShape((AbstractShape) o);
					this.renderableLayer.firePropertyChange(AVKey.LAYER, null, this.renderableLayer);
				} else if(o instanceof Balise3D) {
					((Balise3D) o).setVisible(false);
				}
			} else {
				for(Project p : projects){
					p.getProjectLayer().setVisible(o, false);
				}
			}
		}
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
		return null;
	}

	@Override
	public Iterable<Restorable> getSelectedObjects() {
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
