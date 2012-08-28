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

package fr.crnan.videso3d.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import fr.crnan.videso3d.databases.edimap.Carte;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.Balise3D;
import fr.crnan.videso3d.graphics.RestorableUserFacingText;
import fr.crnan.videso3d.layers.ProjectLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.GeographicText;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.util.Logging;

/**
 * Videso Project<br />
 * Manages access to its objects and its properties.<br />
 * Fires a PropertyChangeEvent when an object is added or removed<br />
 * Created by a {@link ProjectManager}
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class Project {

	private boolean onlyLinks;
	
	private boolean databasesIncluded;
	
	private String name;
		
	//List of each type of objects
	private List<Carte> cartes = new ArrayList<Carte>();
	private List<Airspace> airspaces = new ArrayList<Airspace>();
	private List<Renderable> renderables = new ArrayList<Renderable>();
	private List<Balise2D> balises2D = new ArrayList<Balise2D>();
	private List<Balise3D> balises3D = new ArrayList<Balise3D>();
	private List<GeographicText> texts = new ArrayList<GeographicText>();
	private List<SurfaceImage> images = new ArrayList<SurfaceImage>();
	private List<Layer> layers = new ArrayList<Layer>();
	
	
	//Propertychange Support
	private PropertyChangeSupport support = new PropertyChangeSupport(this);
	
	private ProjectLayer projectLayer;
	
	/**
	 * Property fired when an object is added.<br />
	 * The value sent with is the object itself.
	 */
	public static final String OBJECT_ADDED = "object.added";
	
	/**
	 * Property fired when an object is removed.<br />
	 * The value sent with is the object itself.
	 */
	public static final String OBJECT_REMOVED = "object.removed";
	
	public Project(String name){
		this.name = name;;
	}
	
	public String getName(){
		return this.name;
	}

	/**
	 * @return True if the project has only links to databases objects
	 */
	public boolean isOnlyLinks() {
		return onlyLinks;
	}

	/**
	 * @param onlyLinks True if the project has only links to objects
	 */
	public void setOnlyLinks(boolean onlyLinks) {
		this.onlyLinks = onlyLinks;
	}

	/**
	 * @return True if the project embeds databases files
	 */
	public boolean isDatabasesIncluded() {
		return databasesIncluded;
	}

	/**
	 * @param databasesIncluded
	 */
	public void setDatabasesIncluded(boolean databasesIncluded) {
		this.databasesIncluded = databasesIncluded;
	}

	public void addObject(Object o){
		if(o instanceof Carte){
			addCarte((Carte) o);
		} else if(o instanceof Airspace){
			addAirspace((Airspace) o);
		} else if(o instanceof Renderable){
			addRenderable((Renderable) o);
		} else if(o instanceof Balise2D){
			addBalise2D((Balise2D) o);
		} else if(o instanceof Balise3D){
			addBalise3D((Balise3D) o);
		} else if(o instanceof RestorableUserFacingText){
			addText((GeographicText) o);
		}else if(o instanceof Layer) {
			addLayer((Layer) o);
		} else {
			Logging.logger().warning("Unsupported class "+o.getClass());
		}
	}
	
	public void removeObject(Object o){
		if(o instanceof Carte){
			removeCarte((Carte) o);
		} else if(o instanceof Airspace){
			removeAirspace((Airspace) o);
		} else if(o instanceof Renderable){
			removeRenderable((Renderable) o);
		} else if(o instanceof Balise2D){
			removeBalise2D((Balise2D) o);
		} else if(o instanceof Balise3D){
			removeBalise3D((Balise3D) o);
		} else if(o instanceof RestorableUserFacingText){
			removeText((GeographicText) o);
		}else if(o instanceof Layer) {
			removeLayer((Layer) o);
		} else {
			Logging.logger().warning("Unsupported class "+o.getClass());
			throw new IllegalArgumentException();
		}
	}
	
	public void addCarte(Carte carte){
		this.cartes.add(carte);
		this.firePropertyChange(OBJECT_ADDED, null, carte);
	}
	
	public void removeCarte(Carte carte){
		this.cartes.remove(carte);
		this.firePropertyChange(OBJECT_REMOVED, null, carte);
	}
	
	public List<Carte> getCartes() {
		return cartes;
	}

	public void addAirspace(Airspace airspace){
		this.airspaces.add(airspace);
		this.firePropertyChange(OBJECT_ADDED, null, airspace);
	}
	
	public void removeAirspace(Airspace airspace){
		this.airspaces.remove(airspace);
		this.firePropertyChange(OBJECT_REMOVED, null, airspace);
	}
	
	public List<Airspace> getAirspaces() {
		return airspaces;
	}

	public void addRenderable(Renderable renderable){
		this.renderables.add(renderable);
		this.firePropertyChange(OBJECT_ADDED, null, renderable);
	}
	
	public void removeRenderable(Renderable renderable){
		this.renderables.remove(renderable);
		this.firePropertyChange(OBJECT_REMOVED, null, renderable);
	}
	
	public List<Renderable> getRenderables() {
		return renderables;
	}

	public void addBalise2D(Balise2D balise2d){
		this.balises2D.add(balise2d);
		this.firePropertyChange(OBJECT_ADDED, null, balise2d);
	}
	
	public void removeBalise2D(Balise2D balise2d){
		this.balises2D.remove(balise2d);
		this.firePropertyChange(OBJECT_REMOVED, null, balise2d);
	}
	
	public List<Balise2D> getBalises2D() {
		return balises2D;
	}
	
	public void addBalise3D(Balise3D balise3d){
		this.balises3D.add(balise3d);
		this.firePropertyChange(OBJECT_ADDED, null, balise3d);
	}
	
	public void removeBalise3D(Balise3D balise3d){
		this.balises2D.remove(balise3d);
		this.firePropertyChange(OBJECT_REMOVED, null, balise3d);
	}
	
	public List<Balise3D> getBalises3D() {
		return balises3D;
	}

	public void addText(GeographicText text){
		this.texts.add(text);
		this.firePropertyChange(OBJECT_ADDED, null, text);
	}
	
	public void removeText(GeographicText text){
		this.texts.remove(text);
		this.firePropertyChange(OBJECT_REMOVED, null, text);
	}
	
	public List<GeographicText> getTexts() {
		return texts;
	}

	public void addImage(SurfaceImage image){
		this.images.add(image);
		this.firePropertyChange(OBJECT_ADDED, null, image);
	}

	public void removeImage(SurfaceImage image){
		this.images.remove(image);
		this.firePropertyChange(OBJECT_REMOVED, null, image);
	}
	
	public List<SurfaceImage> getImages() {
		return images;
	}
	
	public void addLayer(Layer layer){
		this.layers.add(layer);
		this.firePropertyChange(OBJECT_ADDED, null, layer);
	}
	
	public void removeLayer(Layer layer){
		this.layers.remove(layer);
		this.firePropertyChange(OBJECT_REMOVED, null, layer);
	}
	
	public List<Layer> getLayers(){
		return this.layers;
	}
	
	public List<Object> getAllObjects(){
		List<Object> objects = new ArrayList<Object>();
		for(Carte c : this.getCartes()){
			objects.add(c);
		}
		for(Airspace a : this.getAirspaces()){
			objects.add(a);
		}
		for(Renderable r : this.getRenderables()){
			objects.add(r);
		}
		for(Balise2D b : this.getBalises2D()){
			objects.add(b);
		}
		for(Balise3D b : this.getBalises3D()){
			objects.add(b);
		}
		for(GeographicText t : this.getTexts()){
			objects.add(t);
		}
		for(Layer l : this.getLayers()){
			objects.add(l);
		}
		return objects;
	}
	
	/**
	 * Set the {@link ProjectLayer} that will display this project<br />
	 * Don't confuse with {@link #addLayer(Layer)} that will add a layer object to this project
	 * @param layer
	 */
	public void setProjectLayer(ProjectLayer layer){
		if(this.projectLayer != null || !layer.getProject().equals(this)){
			throw new IllegalArgumentException("Project layer already set or project layer linked to another project");
		}
		this.projectLayer = layer;
	}
	
	public ProjectLayer getProjectLayer(){
		return this.projectLayer;
	}
	
	/* *********** Property Change *********** */
	
	public void firePropertyChange(PropertyChangeEvent event){
		this.support.firePropertyChange(event);
	}
	
	public void firePropertyChange(String name, Object oldValue, Object newValue){
		this.support.firePropertyChange(name, oldValue, newValue);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener l){
		this.support.addPropertyChangeListener(l);
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener l){
		this.support.addPropertyChangeListener(propertyName, l);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener l){
		this.support.removePropertyChangeListener(l);
	}
	
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener l){
		this.support.removePropertyChangeListener(propertyName, l);
	}
}
