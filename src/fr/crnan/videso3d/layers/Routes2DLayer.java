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

import java.util.HashMap;
import java.util.HashSet;

import fr.crnan.videso3d.graphics.Route;
import fr.crnan.videso3d.graphics.Route2D;
import fr.crnan.videso3d.graphics.Route.Type;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.RenderableLayer;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class Routes2DLayer extends RenderableLayer implements RoutesLayer {

	/**
	 * Liste des routes
	 */
	private HashMap<String, Route2D> routes = new HashMap<String, Route2D>();

	private HashSet<Route2D> displayedRoutes = new HashSet<Route2D>();

	public Routes2DLayer(String name){
		this.setName(name);
	}
	
	
	@Override
	public void addRoute(Route route, String name) {
		if(route instanceof Route2D) {
			routes.put(name, (Route2D) route);
		}
	}

	@Override
	public void displayAllRoutes() {
		for(Route2D r : routes.values()){
			this.displayRoute(r);
		}
	}

	private void displayRoute(Route2D r){
		if(!this.displayedRoutes.contains(r)){
			displayedRoutes.add(r);
			this.addRenderable(r);
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}
	
	private void displayAllRoutes(Type t){
		for(Route2D r : routes.values()){
			if(!displayedRoutes.contains(r)){
				if(r.getType().compareTo(t) == 0) {
					this.displayRoute(r);
				}
			}
		}
	}
	
	@Override
	public void displayAllRoutesAwy() {
		this.displayAllRoutes(Type.FIR);
	}
	
	@Override
	public void displayAllRoutesPDR() {
		this.displayAllRoutes(Type.UIR);
	}
	
	@Override
	public void displayRoute(String route) {
		this.displayRoute(this.routes.get(route));
	}

	@Override
	public Route getRoute(String name) {
		return routes.get(name);
	}
	
	@Override
	public void hideAllRoutes() {
		this.removeAllRenderables();
		this.displayedRoutes.clear();
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	private void hideAllRoutes(Type t) {
		HashSet<Route2D> temp = new HashSet<Route2D>(this.displayedRoutes);
		for(Route2D r : temp){
			if(r.getType().compareTo(t) == 0){
				this.displayedRoutes.remove(r);
				this.removeRenderable(r);
			}
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
		}

	@Override
	public void hideAllRoutesPDR() {
		this.hideAllRoutes(Type.UIR);
	}
	
	@Override
	public void hideAllRoutesAWY() {
		this.hideAllRoutes(Type.FIR);
	}

	@Override
	public void hideRoute(String route) {
		Route2D r = routes.get(route);
		this.hideRoute(r);
	}

	private void hideRoute(Route2D r){
		if(this.displayedRoutes.contains(r)){
			this.displayedRoutes.remove(r);
			this.removeRenderable(r);
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	@Override
	public void highlight(String name) {
		Route2D r = this.routes.get(name);
		r.highlight(true);
		this.displayRoute(r);
	}


	@Override
	public void unHighlight(String name) {
		Route2D r = (Route2D) this.getRoute(name);
		r.highlight(false);
	}


}
