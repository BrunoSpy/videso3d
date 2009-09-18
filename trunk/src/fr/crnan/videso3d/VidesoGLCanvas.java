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

package fr.crnan.videso3d;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.Route3D;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.graphics.Route3D.Type;
import fr.crnan.videso3d.layers.BaliseMarkerLayer;
import fr.crnan.videso3d.layers.TextLayer;
import fr.crnan.videso3d.stip.Secteur;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.Layer;
/**
 * Extension de WorldWindCanvas prenant en compte la création d'éléments 3D
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class VidesoGLCanvas extends WorldWindowGLCanvas {

	/**
	 * Layer contenant les routes UIR
	 */
	private AirspaceLayer routesAwy;
	/**
	 * Layer contenant les routes FIR
	 */
	private AirspaceLayer routesPDR;
	
	/**
	 * Layers pour les balises publiées
	 */
	private TextLayer balisesPubTexts = new TextLayer();
	private BaliseMarkerLayer balisesPubMarkers = new BaliseMarkerLayer();
	/**
	 * Layers pour les balises non publiées
	 */
	private TextLayer balisesNPTexts = new TextLayer();
	private BaliseMarkerLayer balisesNPMarkers = new BaliseMarkerLayer();
	/**
	 * Layer contenant les secteurs
	 */
	private AirspaceLayer secteursLayer = new AirspaceLayer();
	
	private DatabaseManager db;
	
	/**
	 * Initialise les différents objets graphiques
	 */
	public void initialize(DatabaseManager db){
		this.db = db;
		
		this.buildRoutes(db, "F"); 
		this.buildRoutes(db, "U");
		this.buildBalises(db, 1);
		this.buildBalises(db, 0);
		
		this.getModel().getLayers().add(secteursLayer);
	}
	
	/**
	 * Affiche ou non un Layer
	 * @param layer {@link Layer} à afficher/enlever
	 * @param state {@link Boolean}
	 */
	public void toggleLayer(Layer layer, Boolean state){
		if(state){
			if (layer != null) this.getModel().getLayers().add(layer);
		} else {
			if (layer != null) this.getModel().getLayers().remove(layer);
		}
	}
	
	/*--------------------------------------------------------------*/
	/*----------------- Gestion des balises STIP -------------------*/
	/*--------------------------------------------------------------*/
	private void buildBalises(DatabaseManager db, int publicated){
	
		try {
			Statement st = db.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from balises where publicated = " + publicated);
			while(rs.next()){
				Balise2D balise = new Balise2D(rs.getString("name"), LatLon.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude")));
				if(publicated == 1){
					balise.addToLayer(balisesPubMarkers, balisesPubTexts);
				} else {
					balise.addToLayer(balisesNPMarkers, balisesNPTexts);
				}
			}
			
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Layer getBalisesPubMarkers(){
		return balisesPubMarkers;
	}
	public Layer getBalisesPubTexts(){
		return balisesPubTexts;
	}
	public Layer getBalisesNPMarkers(){
		return balisesNPMarkers;
	}
	public Layer getBalisesNPTexts(){
		return balisesNPTexts;
	}
	
	/*--------------------------------------------------------------*/
	/*----------------- Gestion des secteurs STIP ------------------*/
	/*--------------------------------------------------------------*/
	
	private HashMap<String, Secteur3D> secteurs = new HashMap<String, Secteur3D>();
	
	public void addSecteur3D(String name){
		try {
			Statement st = this.db.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from secteurs where nom ='"+name+"'");
			while(rs.next()){
				Secteur3D secteur3D = new Secteur3D(name, rs.getInt("flinf"), rs.getInt("flsup"));
				Secteur secteur = new Secteur(name, rs.getInt("numero"), this.db.getCurrentStip());
				secteur.setConnectionPays(this.db.getCurrent(DatabaseManager.Type.PAYS));
				secteur3D.setLocations(secteur.getContour(rs.getInt("flsup")));
				this.addToSecteursLayer(secteur3D);
				secteurs.put(name, secteur3D);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void removeSecteur3D(String name){
		this.removeFromSecteursLayer(secteurs.get(name));
		secteurs.remove(name);
	}
	
	public void addToSecteursLayer(Secteur3D secteur){
		this.secteursLayer.addAirspace(secteur);
		this.redraw();
	}
	
	public void removeFromSecteursLayer(Secteur3D secteur){
		this.secteursLayer.removeAirspace(secteur);
		this.redraw();
	}
	
	/*--------------------------------------------------------------*/
	/*------------------ Gestion des routes STIP -------------------*/
	/*--------------------------------------------------------------*/
	private void buildRoutes(DatabaseManager db, String type) {
		try {
			Statement st = db.getCurrentStip();
			ResultSet routes = st.executeQuery("select name from routes where espace = '"+type+"'");
			LinkedList<String> routesNames = new LinkedList<String>();
			while(routes.next()){
				routesNames.add(routes.getString(1));
				
			}
			Iterator<String> iterator = routesNames.iterator();
			while(iterator.hasNext()){
				String name = iterator.next();
				Route3D route = new Route3D();
				if(type.equals("F")) route.setType(Type.FIR);
				if(type.equals("U")) route.setType(Type.UIR);
				ResultSet rs = st.executeQuery("select * from routebalise, balises where route = '"+name+"' and routebalise.balise = balises.name and appartient = 1");
				LinkedList<LatLon> loc = new LinkedList<LatLon>();
				while(rs.next()){
					loc.add(LatLon.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude")));
				}
				route.setLocations(loc);
				route.setName(name);
				if(type.equals("F")) this.addToRoutesAwy(route);
				if(type.equals("U")) this.addToRoutesPDR(route);
			}
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public AirspaceLayer getRoutesAwy() {
		return routesAwy;
	}

	public void setRoutesAwy(AirspaceLayer routesAwy) {
		this.routesAwy = routesAwy;
	}

	public void addToRoutesAwy(Route3D route){
		if(routesAwy == null){
			routesAwy = new AirspaceLayer();
			routesAwy.setName("AWY");
		}
		routesAwy.addAirspace(route);
	}
	
	public AirspaceLayer getRoutesPDR() {
		return routesPDR;
	}

	public void setRoutesPDR(AirspaceLayer routesPDR) {
		this.routesPDR = routesPDR;
	}
	
	public void addToRoutesPDR(Route3D route){
		if(routesPDR == null){
			routesPDR = new AirspaceLayer();
			routesPDR.setName("PDR");
		}
		routesPDR.addAirspace(route);
	}
	
}
