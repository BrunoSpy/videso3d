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

package fr.crnan.videso3d.skyview;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.geom.LatLonUtils;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.DatabaseBalise2D;
import fr.crnan.videso3d.graphics.DatabaseRoute2D;
import fr.crnan.videso3d.graphics.Route;
import fr.crnan.videso3d.graphics.Route2D;
import fr.crnan.videso3d.layers.Balise2DLayer;
import fr.crnan.videso3d.layers.Routes2DLayer;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;

/**
 * Gestion des données SkyView
 * @author Bruno Spyckerelle
 * @version 0.1.5
 */
public class SkyViewController implements VidesoController {

	private VidesoGLCanvas wwd;
	
	private Routes2DLayer routes = new Routes2DLayer("Routes SkyView");
	private Balise2DLayer airports = new Balise2DLayer("Aéroports SkyView");
	private Balise2DLayer waypoints = new Balise2DLayer("Balises SkyView");
	
	private HashSet<String> routesList = new HashSet<String>();
	
	public final static int TYPE_ROUTE = 0;
	public final static int TYPE_WAYPOINT = 1;
	public final static int TYPE_AIRPORT = 2;
	
	public SkyViewController(VidesoGLCanvas wwd){
		this.wwd = wwd;
		this.wwd.firePropertyChange("step", "", "Création des éléments SkyView");
		this.toggleLayer(routes, true);
		this.toggleLayer(airports, true);
		this.toggleLayer(waypoints, true);
	}
	
	@Override
	public void highlight(int type, String name) {
		this.showObject(type, name);
		Object object = null;
		switch(type){
		case TYPE_ROUTE : 
			object = routes.getRoute(name+"0");
			break;
		case TYPE_WAYPOINT :
			object = waypoints.getBalise(name,type);
			break;
		case TYPE_AIRPORT :
			object = airports.getBalise(name, type);
			break;
		default : break;
		}
		this.wwd.centerView(object);
	}

	@Override
	public void unHighlight(int type, String name) {}

	@Override
	public void addLayer(String name, Layer layer) {}

	@Override
	public void removeLayer(String name, Layer layer) {}

	@Override
	public void removeAllLayers() {
		this.wwd.removeLayer(routes);
		this.wwd.removeLayer(airports);
		this.wwd.removeLayer(waypoints);
	}

	@Override
	public void toggleLayer(Layer layer, Boolean state) {
		this.wwd.toggleLayer(layer, state);
	}

	@Override
	public void showObject(int type, String name) {
		switch (type) {
		case TYPE_ROUTE:
			if(!routesList.contains(name+0)){ //création de la route que si nécessaire
				List<Route2D> routes = this.createRoute(name);
				for(int i=0;i<routes.size();i++){
					this.routes.addRoute(routes.get(i), name+i);
					this.routesList.add(name+i);
				}
			}
			for(int i=0;routesList.contains(name+i);i++){
				this.routes.displayRoute(name+i);
			}
			break;
		case TYPE_AIRPORT:
			if(!airports.contains(name)){
				try {
					Statement st = DatabaseManager.getCurrentSkyView();
					ResultSet rs = st.executeQuery("select * from airport where ident='"+name+"'");
					if(rs.next()){
						Balise2D airport = new DatabaseBalise2D(name, 
												new Position(LatLonUtils.computeLatLonFromSkyviewString(rs.getString(8), rs.getString(9)), 0), 
												DatabaseManager.Type.SkyView,
												SkyViewController.TYPE_AIRPORT,
												airports.getTextLayer());
						airport.setAnnotation("<b>"+name+"</b><br /><br />"+rs.getString(4));
						airports.addBalise(airport);
					}
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			airports.showBalise(name, TYPE_AIRPORT);
			break;
		case TYPE_WAYPOINT:
			if(!waypoints.contains(name)){
				try{
					Statement st = DatabaseManager.getCurrentSkyView();
					ResultSet rs = st.executeQuery("select * from waypoint where ident='"+name+"'");
					if(rs.next()){
						DatabaseBalise2D waypoint = new DatabaseBalise2D(name, new Position(LatLonUtils.computeLatLonFromSkyviewString(rs.getString(7), rs.getString(8)), 0),
								DatabaseManager.Type.SkyView,
								SkyViewController.TYPE_WAYPOINT,
								waypoints.getTextLayer());
						waypoint.setAnnotation("<b>"+name+"</b><br /><br />"+rs.getString(4));
						waypoints.addBalise(waypoint);
					}
					st.close();
				} catch (SQLException e){
					e.printStackTrace();
				}
			}
			waypoints.showBalise(name, TYPE_WAYPOINT);
			break;
		default:
			break;
		}
		//synchroniser la vue si l'appel n'a pas été fait par la vue
		DatasManager.getView(Type.SkyView).showObject(type, name);
	}

	@Override
	public void hideObject(int type, String name) {
		switch (type) {
		case TYPE_ROUTE:
			for(int i=0;routesList.contains(name+i);i++){
				this.routes.hideRoute(name+i);
			}
			break;
		case TYPE_AIRPORT:
			this.airports.hideBalise(name, TYPE_AIRPORT);
			break;
		case TYPE_WAYPOINT:
			this.waypoints.hideBalise(name, TYPE_WAYPOINT);
			break;
		default:
			break;
		}
		//synchroniser la vue si l'appel n'a pas été fait par la vue
		DatasManager.getView(Type.SkyView).hideObject(type, name);
	}

	@Override
	public void set2D(Boolean flat) {}

	@Override
	public void reset() {
		this.routesList.clear();
		this.routes.removeAllRenderables();
		this.airports.removeAllBalises();
		this.waypoints.removeAllBalises();
	}



	private List<Route2D> createRoute(String ident){
		try {
			Statement st = DatabaseManager.getCurrentSkyView();
			ResultSet rs = st.executeQuery("select from_fix_ident, from_ident_icao, to_fix_ident, to_ident_icao, levl from airway where ident = '"+ident+"'");
			LinkedList<Couple<String,String>> points = new LinkedList<Couple<String,String>>();
			boolean first = true;
			String type = "";
			LinkedList<Couple<Couple<String,String>,Couple<String,String>>> couples = new LinkedList<Couple<Couple<String,String>,Couple<String,String>>>();
			while(rs.next()){
				//si c'est le premier résultat, on initialise la route
				if(first){
					first = false;
					points.add(new Couple<String, String>(rs.getString(1), rs.getString(2)));
					points.add(new Couple<String, String>(rs.getString(3), rs.getString(4)));
					type = rs.getString(5);
				} else {
					Couple<String, String> one = new Couple<String, String>(rs.getString(1), rs.getString(2));
					Couple<String, String> two = new Couple<String, String>(rs.getString(3), rs.getString(4));
					if(!this.insertSegment(points, one, two)){//si on n'a pas réussi à insérer le segment, on le garde pour rééssayer plus tard
						couples.add(new Couple<Couple<String, String>, Couple<String, String>>(one, two));
					}
				}
			}
			return createRoute(ident, type, points, couples);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	private List<Route2D> createRoute(String ident, 
			String type, 
			LinkedList<Couple<String,String>> points, 
			LinkedList<Couple<Couple<String,String>,Couple<String,String>>> couples){

		LinkedList<Route2D> routes2D = new LinkedList<Route2D>();
		LinkedList<LinkedList<Couple<String,String>>> routes = new LinkedList<LinkedList<Couple<String,String>>>();
		
		while(!couples.isEmpty()){
			int startLength = couples.size();
			Iterator<Couple<Couple<String, String>, Couple<String, String>>> iterator = couples.iterator();
			while(iterator.hasNext()){
				Couple<Couple<String, String>, Couple<String, String>> couple = iterator.next();
				if(this.insertSegment(points, couple.getFirst(), couple.getSecond())){
					iterator.remove();
				}
			}
			if(startLength == couples.size()){//plus de possibilités d'allonger le tronçon, enregistrer la route et commencer un deuxième tronçon
				routes.add((LinkedList<Couple<String, String>>) points.clone());
				points.clear();
				points.add(couples.getFirst().getFirst());
				points.add(couples.getFirst().getSecond());
				couples.removeFirst();
			}
		}
		//on enregistre le dernier tronçon
		routes.add((LinkedList<Couple<String, String>>) points.clone());
		//puis on crée les routes 2D

		try {
			Statement st = DatabaseManager.getCurrentSkyView();
			ResultSet rs;
			HashMap<String, Couple<String, String>> pointsRoute = new HashMap<String, Couple<String,String>>();
			StringBuilder queryWaypoint = new StringBuilder("select ident, latitude, longitude from waypoint where");
			StringBuilder queryNavAid = new StringBuilder("select ident, latitude, longitude from navaid where");
			for(LinkedList<Couple<String, String>> route : routes){
				for(Couple<String, String> p : route){
					if(p.getFirst().length()>3){
						queryWaypoint.append(" (ident='"+p.getFirst()+"' AND icao='"+p.getSecond()+"') OR");
					}else{
						queryNavAid.append(" (ident='"+p.getFirst()+"' AND icao='"+p.getSecond()+"') OR");
					}
				}
			}
			if(queryWaypoint.length()>56){
				queryWaypoint.delete(queryWaypoint.length()-3,queryWaypoint.length());
				rs = st.executeQuery(queryWaypoint.toString());
				while(rs.next()){
					pointsRoute.put(rs.getString(1), new Couple<String, String>(rs.getString(2), rs.getString(3)));
				}
			}
			if(queryNavAid.length()>56){
				queryNavAid.delete(queryNavAid.length()-3,queryNavAid.length());
				rs = st.executeQuery(queryNavAid.toString());
				while(rs.next()){
					pointsRoute.put(rs.getString(1), new Couple<String, String>(rs.getString(2), rs.getString(3)));
				}
			}
			for(LinkedList<Couple<String, String>> route : routes){
				DatabaseRoute2D r = new DatabaseRoute2D(ident, type.equals("H")? Route.Space.UIR : Route.Space.FIR,
						Type.SkyView,
						SkyViewController.TYPE_ROUTE
						);
				LinkedList<LatLon> loc = new LinkedList<LatLon>();
				LinkedList<String> balises = new LinkedList<String>();
				for(Couple<String, String> p : route){
					Couple<String, String> coords = pointsRoute.get(p.getFirst());
					LatLon latlon = LatLonUtils.computeLatLonFromSkyviewString(coords.getFirst(), coords.getSecond());
					loc.add(latlon);
					balises.add(p.getFirst());
				}
				r.setLocations(loc);
				r.setBalises(balises);
				routes2D.add(r);
			}
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return routes2D;
	}

	
	/**
	 * Essaye d'ajouter un segment à une route
	 * @param route
	 * @param one
	 * @param two
	 * @return
	 */
	private boolean insertSegment(LinkedList<Couple<String, String>> route, Couple<String, String> one, Couple<String, String> two){
		if(route.getFirst().getFirst().equals(one.getFirst())){
			route.addFirst(two);
			return true;
		} else if(route.getFirst().getFirst().equals(two.getFirst())){
			route.addFirst(one);
			return true;
		} else if(route.getLast().getFirst().equals(one.getFirst())){
			route.add(two);
			return true;
		} else if(route.getLast().getFirst().equals(two.getFirst())){
			route.add(one);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int string2type(String type) {
		if(type.equals("Routes")){
			return SkyViewController.TYPE_ROUTE;
		} else if(type.equals("Waypoints")){
			return SkyViewController.TYPE_WAYPOINT;
		} else if(type.equals("Airports")){
			return SkyViewController.TYPE_AIRPORT;
		}
		
		return 0;
	}

	/* (non-Javadoc)
	 * @see fr.crnan.videso3d.VidesoController#type2string(int)
	 */
	@Override
	public String type2string(int type) {
		switch (type) {
		case TYPE_ROUTE:
			return "Routes";
		case TYPE_AIRPORT:
			return "Airports";
		case TYPE_WAYPOINT:
			return "Waypoints";
		default:
			break;
		}
		return null;
	}

	public static int getNumberInitSteps() {
		return 1;
	}

	@Override
	public String toString(){
		return "SkyView";
	}
	
	@Override
	public Collection<Object> getObjects(int type) {
		return null;
	}
	
	@Override
	public void setColor(Color color, int type, String name) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public boolean isColorEditable(int type) {
		return false;
	}

	@Override
	public HashMap<Integer, List<String>> getSelectedObjectsReference() {
		HashMap<Integer, List<String>> objects = new HashMap<Integer, List<String>>();
		//routes
		ArrayList<String> routesUnique = new ArrayList<String>();
		for(String name : this.routes.getVisibleRoutes()){
			if(name.endsWith("0")){//only add the name of the route once
				routesUnique.add(name);
			}
		}
		if(!routesUnique.isEmpty())
			objects.put(SkyViewController.TYPE_ROUTE, routesUnique);
		//airports
		if(!this.airports.getVisibleBalisesNames().isEmpty())
			objects.put(TYPE_AIRPORT, this.airports.getVisibleBalisesNames());
		//waypoints
		if(!this.waypoints.getVisibleBalisesNames().isEmpty())
			objects.put(TYPE_WAYPOINT, this.waypoints.getVisibleBalisesNames());
		return objects;
	}

	@Override
	public Iterable<Restorable> getSelectedObjects() {
		ArrayList<Restorable> restorables = new ArrayList<Restorable>();
		//routes
		for(String r : this.routes.getVisibleRoutes()){
			restorables.add(this.routes.getRoute(r));
		}
		//airports
		restorables.addAll(this.airports.getVisibleBalises());
		//waypoints
		restorables.addAll(this.waypoints.getVisibleBalises());
		return restorables;
	}
	
		
	
	/**
	 * Affiche tous les waypoints du pays passé en paramètre. Si <code>state</code> est null, affiche les waypoints de tous les pays.
	 * @param pays Le pays dont on veut afficher les waypoints.
	 */
	public void showAllWaypoints(String pays){
		try{
			Statement st = DatabaseManager.getCurrentSkyView();
			String query = "select ident, name, latitude, longitude from waypoint";
			if(pays!=null)
				query+=" where icao='"+pays+"'";
			ResultSet rs = st.executeQuery(query);
			while(rs.next()){
				String ident = rs.getString(1);
				DatabaseBalise2D waypoint = new DatabaseBalise2D(ident, new Position(LatLonUtils.computeLatLonFromSkyviewString(rs.getString(3), rs.getString(4)), 0),
						DatabaseManager.Type.SkyView,
						SkyViewController.TYPE_WAYPOINT,
						waypoints.getTextLayer());
				waypoint.setAnnotation("<b>"+ident+"</b><br /><br />"+rs.getString(2));
				waypoints.addBaliseActive(waypoint);
			}
			waypoints.showAll();
			st.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}

	public void hideAllWaypoints(String pays) {
		try{
			if(pays!=null){
				Statement st = DatabaseManager.getCurrentSkyView();
				ResultSet rs = st.executeQuery("select ident from waypoint where icao='"+pays+"'");
				while(rs.next()){
					waypoints.hideBalise(rs.getString(1), TYPE_WAYPOINT);
				}
			}else{
				waypoints.hideAll();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	
	@Override
	public boolean areLocationsVisible(int type, String name) {
		if(type==TYPE_WAYPOINT)
			return waypoints.getBalise(name, type).isLocationVisible();
		if(type==TYPE_AIRPORT)
			return airports.getBalise(name, type).isLocationVisible();
		if(type==TYPE_ROUTE){}
			//TODO
		return false;
	}


	@Override
	public void setLocationsVisible(int type, String name, boolean b) {
		if(type==TYPE_WAYPOINT)
			waypoints.getBalise(name, type).setLocationVisible(b);
		if(type==TYPE_AIRPORT)
			airports.getBalise(name, type).setLocationVisible(b);
		if(type==TYPE_ROUTE){}
			//TODO
	}
	
}
