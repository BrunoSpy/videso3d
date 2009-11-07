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
import java.util.LinkedList;

import fr.crnan.videso3d.graphics.Route;
import fr.crnan.videso3d.graphics.Route2D;
import gov.nasa.worldwind.layers.SurfaceShapeLayer;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class Routes2DLayer extends SurfaceShapeLayer implements RoutesLayer {

	/**
	 * Liste des routes PDR
	 */
	private HashMap<String, Route2D> pdr = new HashMap<String, Route2D>();
	/**
	 * Liste des AWY
	 */
	private HashMap<String, Route2D> awy = new HashMap<String, Route2D>();
	/**
	 * Liste des PDR actives
	 */
	private LinkedList<Route2D> pdrActives = new LinkedList<Route2D>();
	/**
	 * Liste des AWY actives
	 */
	private LinkedList<Route2D> awyActives = new LinkedList<Route2D>();

	public Routes2DLayer(String name){
		this.setName(name);
	}
	
	
	@Override
	public void addRouteAwy(Route route, String name) {
		if(route instanceof Route2D) {
			awy.put(name, (Route2D) route);
		}
	}

	@Override
	public void addRoutePDR(Route route, String name) {
		if(route instanceof Route2D) {
			pdr.put(name, (Route2D) route);
		}
	}

	@Override
	public void displayAllRoutes() {
		displayAllRoutesAwy();
		displayAllRoutesPDR();
	}

	@Override
	public void displayAllRoutesAwy() {
		for(Route2D route : awy.values()){
			displayRouteAwy(route);
		}
	}

	private void displayRouteAwy(Route2D r){
		addRenderable(r);
		awyActives.add(r);
	}
	
	@Override
	public void displayAllRoutesPDR() {
		for(Route2D route : pdr.values()){
			displayRoutePDR(route);
		}
	}

	private void displayRoutePDR(Route2D r){
		addRenderable(r);
		pdrActives.add(r);
	}
	
	@Override
	public void displayRouteAwy(String route) {
		displayRouteAwy(awy.get(route));
	}

	@Override
	public void displayRoutePDR(String route) {
		displayRoutePDR(pdr.get(route));
	}

	@Override
	public Route getRouteAwy(String name) {
		return awy.get(name);
	}

	@Override
	public Route getRoutePDR(String name) {
		return pdr.get(name);
	}

	public void removeAllRenderables(){
		super.removeAllRenderables();
		pdr.clear();
		awy.clear();
		pdrActives.clear();
		awyActives.clear();
	}
	
	@Override
	public void hideAllRoutes() {
		super.removeAllRenderables();
		pdrActives.clear();
		awyActives.clear();
	}

	@Override
	public void hideAllRoutesAWY() {
		for(Route2D route : awyActives){
			removeRenderable(route);
		}
		awyActives.clear();
	}

	@Override
	public void hideAllRoutesPDR() {
		for(Route2D route : pdrActives){
			removeRenderable(route);
		}
		pdrActives.clear();
	}

	@Override
	public void hideRouteAwy(String route) {
		Route2D r = awy.get(route);
		if(awyActives.contains(r)){
			removeRenderable(r);
			awyActives.remove(r);
		}
	}

	@Override
	public void hideRoutePDR(String route) {
		Route2D r = pdr.get(route);
		if(pdrActives.contains(r)){
			removeRenderable(r);
			pdrActives.remove(r);
		}
	}

}
