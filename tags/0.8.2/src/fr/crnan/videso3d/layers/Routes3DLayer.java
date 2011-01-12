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
import fr.crnan.videso3d.graphics.Route3D;
import fr.crnan.videso3d.graphics.Route.Space;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.AirspaceLayer;
/**
 * Layer destiné à afficher les routes<br />
 * Permet d'afficher sélectivement une ou plusieurs routes, selon leur nom ou leur type
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class Routes3DLayer extends AirspaceLayer implements RoutesLayer {
	
	/**
	 * Ensemble des routes
	 */
	private HashMap<String, Route3D> routes = new HashMap<String, Route3D>();
	
	private HashSet<Route3D> displayedRoutes = new HashSet<Route3D>();
	
	public Routes3DLayer(String string) {
		this.setName(string);
	}

	@Override
	public void addRoute(Route route, String name) {
		if(route instanceof Route3D)
			this.routes.put(name, (Route3D) route);
	}

	@Override
	public void displayAllRoutes() {
		for(Route3D r : routes.values()){
			this.displayRoute(r);
		}
	}

	private void displayAllRoutes(Space t){
		for(Route3D r : routes.values()){
			if(!displayedRoutes.contains(r)){
				if(r.getSpace().compareTo(t) == 0) {
					this.displayRoute(r);
				}
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
		if(!this.displayedRoutes.contains(r)){
			displayedRoutes.add(r);
			this.addAirspace(r);
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}
	
	@Override
	public void displayRoute(String route) {
		Route3D r = routes.get(route);
		this.displayRoute(r);
	}

	@Override
	public Route getRoute(String name) {
		return this.routes.get(name);
	}

	@Override
	public void hideAllRoutes() {
		this.displayedRoutes.clear();
		this.removeAllAirspaces();
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	private void hideRoute(Route3D r){
		if(this.displayedRoutes.contains(r)){
			this.displayedRoutes.remove(r);
			this.removeAirspace(r);
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}
	
	private void hideAllRoutes(Space t){
		//copie temporaire pour pouvoir modifier displayedRoutes
		//sinon on a un accès concurrent impossible
		HashSet<Route3D> temp = new HashSet<Route3D>(this.displayedRoutes);
		for(Route3D r : temp){
			if(r.getSpace().compareTo(t) == 0){
				this.displayedRoutes.remove(r);
				this.removeAirspace(r);
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
		this.hideRoute(this.routes.get(route));
	}

	@Override
	public void highlight(String name) {
		Route3D r = this.routes.get(name);
		r.highlight(true);
		this.displayRoute(r);
	}

	@Override
	public void unHighlight(String name) {
		Route3D r = this.routes.get(name);
		r.highlight(false);
	}
	
	

	

}
