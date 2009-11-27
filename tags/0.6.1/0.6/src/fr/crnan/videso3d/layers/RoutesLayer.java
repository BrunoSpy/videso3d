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

import fr.crnan.videso3d.graphics.Route;
/**
 * Layer destiné à afficher les routes<br />
 * Permet d'afficher sélectivement une ou plusieurs routes, selon leur nom ou leur type
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public interface RoutesLayer {

	public void addRoutePDR(Route route, String name);
	
	public void addRouteAwy(Route route, String name);
	
	public Route getRoutePDR(String name);
	
	public Route getRouteAwy(String name);
		
	public void displayAllRoutes();
	
	public void hideAllRoutes();
	
	public void displayAllRoutesPDR();
	
	public void displayAllRoutesAwy();
	
	public void hideAllRoutesPDR();
	
	public void hideAllRoutesAWY();
		
	public void displayRoutePDR(String route);
	
	public void hideRoutePDR(String route);
	
	public void displayRouteAwy(String route);
	
	public void hideRouteAwy(String route);
}

