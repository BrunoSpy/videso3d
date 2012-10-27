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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import fr.crnan.videso3d.graphics.Route;
import fr.crnan.videso3d.graphics.Route3D;
import fr.crnan.videso3d.graphics.Route.Space;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.AirspaceLayer;
/**
 * Layer destiné à afficher les routes<br />
 * Permet d'afficher sélectivement une ou plusieurs routes, selon leur nom ou leur type
 * @author Bruno Spyckerelle
 * @version 0.3.3
 */
public class Routes3DLayer extends AirspaceLayer implements RoutesLayer {
	
	/**
	 * Ensemble des routes
	 */
	private HashMap<String, Route3D> routes = new HashMap<String, Route3D>();
	
	
	public Routes3DLayer(String string) {
		this.setName(string);
	}

	@Override
	public void addRoute(Route route, String name) {
		if(route instanceof Route3D) {
			this.routes.put(name, (Route3D) route);
			this.addAirspace((Route3D)route);
		}
	}

	@Override
	public void displayAllRoutes() {
		for(Route3D r : routes.values()){
			this.displayRoute(r);
		}
	}

	private void displayAllRoutes(Space t){
		for(Route3D r : routes.values()){
			if(r.getSpace().compareTo(t) == 0) {
				this.displayRoute(r);
			}
		}
	}
	
	@Override
	public void displayAllRoutesAwy() {
		this.displayAllRoutes(Space.FIR);
	}

	@Override
	public void displayAllRoutesPDR() {
		this.displayAllRoutes(Space.UIR);
	}

	private void displayRoute(Route3D r){
		r.setVisible(true);
		this.firePropertyChange(AVKey.LAYER, null, this);
	}
	
	@Override
	public void displayRoute(String route) {
		if(this.routes.containsKey(route)){
			this.displayRoute(this.routes.get(route));
		}
	}

	@Override
	public Route getRoute(String name) {
		return this.routes.get(name);
	}

	@Override
	public void hideAllRoutes() {
		for(Route3D r : routes.values()){
			r.setVisible(false);
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	private void hideRoute(Route3D r){
		r.setVisible(false);
		r.setLocationsVisible(false);
		this.firePropertyChange(AVKey.LAYER, null, this);
	}
	
	private void hideAllRoutes(Space t){
		//copie temporaire pour pouvoir modifier displayedRoutes
		//sinon on a un accès concurrent impossible
		for(Route3D r : routes.values()){
			if(r.getSpace().compareTo(t) == 0){
				r.setVisible(false);
			}
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}
	
	@Override
	public void hideAllRoutesAWY() {
		this.hideAllRoutes(Space.FIR);
	}

	@Override
	public void hideAllRoutesPDR() {
		this.hideAllRoutes(Space.UIR);
	}

	@Override
	public void hideRoute(String route) {
		if(this.routes.containsKey(route)) {
			this.hideRoute(this.routes.get(route));
		}
	}

	@Override
	public void highlight(String name) {
		if(this.routes.containsKey(name)){
			Route3D r = this.routes.get(name);
			r.setHighlighted(true);
			this.displayRoute(r);
		}
	}

	@Override
	public void unHighlight(String name) {
		if(this.routes.containsKey(name)){
			this.routes.get(name).setHighlighted(false);
		}
	}
	
	@Override
	public List<String> getVisibleRoutes() {
		List<String> routesList = new ArrayList<String>();
		for(Entry<String, Route3D> entry : routes.entrySet()){
			if(entry.getValue().isVisible()){
				routesList.add(entry.getKey());
			}
		}
		return routesList;
	}

	

}
