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
import java.util.Iterator;
import java.util.LinkedList;

import fr.crnan.videso3d.graphics.Route;
import fr.crnan.videso3d.graphics.Route3D;
import gov.nasa.worldwind.layers.AirspaceLayer;
/**
 * Layer destiné à afficher les routes<br />
 * Permet d'afficher sélectivement une ou plusieurs routes, selon leur nom ou leur type
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class Routes3DLayer extends AirspaceLayer implements RoutesLayer {

	/**
	 * Liste des routes PDR
	 */
	private HashMap<String, Route3D> pdr = new HashMap<String, Route3D>();
	/**
	 * Liste des AWY
	 */
	private HashMap<String, Route3D> awy = new HashMap<String, Route3D>();
	/**
	 * Liste des PDR actives
	 */
	private LinkedList<Route3D> pdrActives = new LinkedList<Route3D>();
	/**
	 * Liste des AWY actives
	 */
	private LinkedList<Route3D> awyActives = new LinkedList<Route3D>();

	/**
	 * Nouveau Layer
	 * @param name Nom du layer
	 */
	public Routes3DLayer(String name){
		this.setName(name);
		this.setEnableAntialiasing(true);
	}

	public void addRoutePDR(Route route, String name){
		if(route instanceof Route3D) {
			pdr.put(name, (Route3D) route);
		}
	}
	
	public void addRouteAwy(Route route, String name){
		if(route instanceof Route3D) {
			awy.put(name, (Route3D) route);
		} 
	}
	
	public Route3D getRoutePDR(String name){
		return pdr.get(name);
	}
	
	public Route3D getRouteAwy(String name){
		return awy.get(name);
	}
	
	@Override
	public void removeAllAirspaces(){
		super.removeAllAirspaces();
		pdr.clear();
		awy.clear();
		pdrActives.clear();
		awyActives.clear();
	}
	
	public void displayAllRoutes(){
		displayAllRoutesAwy();
		displayAllRoutesPDR();
	}
	
	public void hideAllRoutes(){
		super.removeAllAirspaces();
		pdrActives.clear();
		awyActives.clear();
	}
	
	public void displayAllRoutesPDR(){
		Iterator<Route3D> iterator = pdr.values().iterator();
		while(iterator.hasNext()){
			displayRoutePDR(iterator.next());
		}
	}
	
	public void displayAllRoutesAwy(){
		Iterator<Route3D> iterator = awy.values().iterator();
		while(iterator.hasNext()){
			displayRouteAwy(iterator.next());
		}
	}
	
	public void hideAllRoutesPDR(){
		Iterator<Route3D> iterator = pdrActives.iterator();
		while(iterator.hasNext()){
			removeAirspace(iterator.next());
		}
		pdrActives.clear();
	}
	
	public void hideAllRoutesAWY(){
		Iterator<Route3D> iterator = awyActives.iterator();
		while(iterator.hasNext()){
			removeAirspace(iterator.next());
		}
		awyActives.clear();
	}
	
	private void displayRoutePDR(Route3D r){
		addAirspace(r);
		pdrActives.add(r);
	}
	
	public void displayRoutePDR(String route){
		displayRoutePDR(pdr.get(route));
	}
	
	public void hideRoutePDR(String route){
		Route3D r = pdr.get(route);
		removeAirspace(r);
		pdrActives.remove(r);
	}
	
	private void displayRouteAwy(Route3D r){
		addAirspace(r);
		awyActives.add(r);
	}
	
	public void displayRouteAwy(String route){
		displayRouteAwy(awy.get(route));
	}
	
	public void hideRouteAwy(String route){
		Route3D r = awy.get(route);
		removeAirspace(r);
		awyActives.remove(r);
	}

}
