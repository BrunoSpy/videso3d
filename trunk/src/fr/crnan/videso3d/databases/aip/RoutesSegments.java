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

package fr.crnan.videso3d.databases.aip;

import java.util.ArrayList;

import fr.crnan.videso3d.Couple;

/**
 * Classe permettant de stocker les noms des segments avec les routes auxquelles ils appartiennent, ainsi que les balises aux extremités de 
 * chaque segment.
 * @author A. Vidal
 * @author Bruno Spyckerelle
 * @version 0.1.2
 */
public class RoutesSegments{

	private ArrayList<Route> routes = new ArrayList<Route>();
	
	public ArrayList<Route> getRoutes(){
		return this.routes;
	}
	
	/**
	 * Ajoute un segment dans la liste correspondant à sa route
	 * @param segmentName Le nom du segment
	 * @param routeName Le nom de la route à laquelle le segment appartient
	 * @param routeType Le type de la route
	 * @param visible true si la route est affichée. Ce paramètre n'est pris en compte que pour le premier segment 
	 * et sera valable pour l'ensemble de la route.
	 */
	public void addSegment(String segmentName, String bal1, String bal2, String routeName, int routeType, int pkRoute, boolean visible){
		Segment segment = new Segment(segmentName, bal1, bal2);
		boolean routeExists = false;
		for(Route r : routes){
			if(r.getName().equals(routeName)){
				r.add(segment);
			}
		}
		if(!routeExists){
			Route route = new Route(routeName, routeType, pkRoute, visible);
			route.add(segment);
			routes.add(route);
		}
	}
	
	

	/**
	 * 
	 * @param routeName Le nom de la route.
	 * @return La liste des <code>Segment</code> qui composent la route.
	 */
	public Route getSegmentOfRoute(String routeName){
		for(Route r : routes){
			if(r.getName().equals(routeName)){
				return r;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param pkRoute L'identifiant de la route.
	 * @return La liste des <code>Segment</code> qui composent la route.
	 */
	public Route getSegmentOfRoute(int pkRoute){
		for(Route r : routes){
			if(r.getPk()==pkRoute){
				return r;
			}
		}
		return null;
	}
	
	/**
	 * Enlève la route <code>routeName</code> de la liste des routes.
	 * @param routeName Le nom de la route en question.
	 */
	public void removeRoute(String routeName){
		for(Route r : routes){
			if(r.getName().equals(routeName)){
				routes.remove(r);
			}
		}
	}
	
	
	
	public class Route extends ArrayList<Segment>{
		private String name;
		private int pk;
		private boolean visible;
		private boolean navFixsVisible = false;
		private int type;
		
		public Route(String name, int routeType, int pk, boolean visible){
			this.name = name;
			this.type = routeType;
			this.pk  = pk;
			this.visible = visible;
		}
		
		public String getName(){
			return name;
		}
		
		public int getType(){
			return this.type;
		}
		
		public int getPk(){
			return pk;
		}
		
		public boolean isVisible(){
			return visible;
		}
		
		public boolean areNavFixsVisible(){
			return navFixsVisible;
		}
		
		public void setVisible(boolean visible){
			this.visible = visible;
		}
		
		public void setNavFixsVisible(boolean navFixsVisible){
			this.navFixsVisible = navFixsVisible;
		}
		
		
	}
	
	public class Segment extends Couple<String, String>{
		private String name;
		
		public Segment(String name, String bal1, String bal2){
			super(bal1, bal2);
			this.name = name;
		}
		
		public String getName(){
			return name;
		}
	}
	
	
}
