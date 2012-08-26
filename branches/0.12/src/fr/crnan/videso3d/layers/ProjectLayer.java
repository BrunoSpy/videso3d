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

package fr.crnan.videso3d.layers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fr.crnan.videso3d.databases.edimap.Carte;
import fr.crnan.videso3d.graphics.Balise;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.Balise3D;
import fr.crnan.videso3d.graphics.RestorableUserFacingText;
import fr.crnan.videso3d.project.Project;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.GeographicText;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.airspaces.Airspace;

/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class ProjectLayer extends LayerSet {
	
	private PriorityRenderableLayer xmlRenderables = null;
	private Balise2DLayer xmlBalises = null;
	private Balise3DLayer xmlBalises3D = null;
	private FilterableAirspaceLayer xmlAirspaces = null;
	private TextLayer xmlTexts = null;
	
	private Project project;
	
	public ProjectLayer(Project project){
		this.setName(project.getName());
		
		xmlRenderables = new PriorityRenderableLayer();
		this.add(xmlRenderables);
		
		xmlBalises = new Balise2DLayer("Balises 2D");
		this.add(xmlBalises);
		
		xmlBalises3D = new Balise3DLayer("Balises 3D");
		this.add(xmlBalises3D);
		
		xmlAirspaces = new FilterableAirspaceLayer();
		this.add(xmlAirspaces);
		
		xmlTexts = new TextLayer("Texts");
		this.add(xmlTexts);
		
		this.project = project;
		
		this.project.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals(Project.OBJECT_ADDED)){
					ProjectLayer.this.addObject(evt.getNewValue());
				} else if(evt.getPropertyName().equals(Project.OBJECT_REMOVED)) {
					ProjectLayer.this.removeObject(evt.getNewValue());
				}
			}
		});
		
		//initialize layer
		for(Carte c : this.project.getCartes()){
			this.addObject(c);
		}
		for(Airspace a : this.project.getAirspaces()){
			this.addObject(a);
		}
		for(Renderable r : this.project.getRenderables()){
			this.addObject(r);
		}
		for(Balise2D b : this.project.getBalises2D()){
			this.addObject(b);
		}
		for(Balise3D b : this.project.getBalises3D()){
			this.addObject(b);
		}
		for(GeographicText t : this.project.getTexts()){
			this.addObject(t);
		}
		for(Layer l : this.project.getLayers()){
			this.addObject(l);
		}
	}

	public void addObject(Object o){
		if(o instanceof Carte){
			((Carte) o).setLayers(xmlRenderables, xmlAirspaces, xmlTexts);
			((Carte) o).setVisible(true);
		} else if(o instanceof Airspace){
			xmlAirspaces.addAirspace((Airspace) o);
		} else if(o instanceof Renderable){
			xmlRenderables.addRenderable((Renderable) o);
		} else if(o instanceof Balise2D){
			xmlBalises.addBalise((Balise) o);
			xmlBalises.showBalise((Balise) o);
		} else if(o instanceof Balise3D){
			xmlBalises3D.addBalise((Balise) o);
			xmlBalises3D.showBalise((Balise) o);
		} else if(o instanceof RestorableUserFacingText){
			xmlTexts.addGeographicText((GeographicText) o);
		}else if(o instanceof Layer) {
			this.add((Layer)o);
		}
	}
	
	public void removeObject(Object o){
		if(o instanceof Carte){
			((Carte) o).setVisible(false);
		} else if(o instanceof Airspace){
			xmlAirspaces.removeAirspace((Airspace) o);
		} else if(o instanceof Renderable){
			xmlRenderables.removeRenderable((Renderable) o);
		} else if(o instanceof Balise2D){
			xmlBalises.removeBalise((Balise) o);
		} else if(o instanceof Balise3D){
			xmlBalises3D.removeBalise((Balise) o);
		} else if(o instanceof RestorableUserFacingText){
			xmlTexts.removeGeographicText((GeographicText) o);
		}else if(o instanceof Layer) {
			this.remove((Layer)o);
		}
	}

}
