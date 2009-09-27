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
import java.util.List;

import javax.swing.ProgressMonitor;

import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.FrontieresStipLayer;
import fr.crnan.videso3d.graphics.Route3D;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.graphics.Route3D.Type;
import fr.crnan.videso3d.layers.BaliseMarkerLayer;
import fr.crnan.videso3d.layers.MosaiqueLayer;
import fr.crnan.videso3d.layers.TextLayer;
import fr.crnan.videso3d.stip.Secteur;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
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
	/**
	 * Layer pour les frontières
	 */
	private FrontieresStipLayer frontieres;
	
	/**
	 * Liste des layers Mosaiques
	 */
	private HashMap<String, MosaiqueLayer> mosaiquesLayer = new HashMap<String, MosaiqueLayer>();
	
	private DatabaseManager db;
	
	/**
	 * Initialise les différents objets graphiques
	 */
	public void initialize(DatabaseManager db){
		this.db = db;
		
	//	this.toggleFrontieres(true);
				
		this.buildStip();	

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
	/*----------------- Gestion des frontières ---------------------*/
	/*--------------------------------------------------------------*/
	private void toggleFrontieres(Boolean toggle){
		if(toggle){
			if(frontieres == null) frontieres = new FrontieresStipLayer();
			this.getModel().getLayers().add(frontieres);
			this.redraw();
		} else {
			this.getModel().getLayers().remove(frontieres);
			this.redraw();
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
			ResultSet rs = st.executeQuery("select secteurs.nom, secteurs.numero, cartesect.flinf, cartesect.flsup from secteurs, cartesect where secteurs.numero = cartesect.sectnum and secteurs.nom ='"+name+"'");
			Integer i = 0;
			while(rs.next()){
				Secteur3D secteur3D = new Secteur3D(name, rs.getInt("flinf"), rs.getInt("flsup"));
				Secteur secteur = new Secteur(name, rs.getInt("numero"), this.db.getCurrentStip());
				secteur.setConnectionPays(this.db.getCurrent(DatabaseManager.Type.PAYS));
				secteur3D.setLocations(secteur.getContour(rs.getInt("flsup")));
				this.addToSecteursLayer(secteur3D);
				secteurs.put(name+i.toString(), secteur3D);
				i++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void removeSecteur3D(String name){
		Integer i = 0;
		while(secteurs.containsKey(name+i.toString())){
			this.removeFromSecteursLayer(secteurs.get(name+i.toString()));
			secteurs.remove(name+i.toString());
			i++;
		}
		
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
				System.out.println("route : "+name);
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

	/**
	 * Construit ou met à jour les objets Stip
	 * Appelé lors de l'initialisation de la vue ou lors du changement de base de données Stip
	 */
	public void buildStip() {
		ProgressMonitor progress = new ProgressMonitor(null, 
				"Mise à jour des éléments STIP", "Suppression des éléments précédents", 0, 5);
		progress.setMillisToPopup(1);
		progress.setProgress(0);
		//Suppression des objets3D
		if(routesAwy != null) {
			routesAwy.removeAllAirspaces();
			this.getModel().getLayers().remove(routesAwy); 
		} else {
			routesAwy = new AirspaceLayer();
		}
		if(routesPDR != null) {
			routesPDR.removeAllAirspaces();
			this.getModel().getLayers().remove(routesPDR);
		} else {
			routesPDR = new AirspaceLayer();
		}
		if(balisesPubTexts != null) this.getModel().getLayers().remove(balisesPubTexts);
		balisesPubTexts = new TextLayer();
		if(balisesPubMarkers != null) this.getModel().getLayers().remove(balisesPubMarkers);
		balisesPubMarkers = new BaliseMarkerLayer();
		if(balisesNPTexts != null) this.getModel().getLayers().remove(balisesNPTexts);
		balisesNPTexts = new TextLayer();
		if(balisesNPMarkers != null) this.getModel().getLayers().remove(balisesNPMarkers);
		balisesNPMarkers = new BaliseMarkerLayer();
		if(secteursLayer != null) this.getModel().getLayers().remove(secteursLayer);
		secteursLayer = new AirspaceLayer();		
		try {
			if(this.db.getCurrentStip() != null) {
				progress.setNote("Création des balises publiées");
				progress.setProgress(1);
				//création des nuveaux objets
				this.buildBalises(db, 0);
				progress.setNote("Création des balises non publiées");
				progress.setProgress(2);
				this.buildBalises(db, 1);
				progress.setNote("Création des routes FIR");
				progress.setProgress(3);
				this.buildRoutes(db, "F");
				progress.setNote("Création des routes UIR");
				progress.setProgress(4);
				this.buildRoutes(db, "U");
				progress.setProgress(5);
				this.getModel().getLayers().add(secteursLayer);
				this.secteurs = new HashMap<String, Secteur3D>();				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.redraw();
	}

	/*--------------------------------------------------------------*/
	/*------------------ Gestion des mosaiques   -------------------*/
	/*--------------------------------------------------------------*/
	public void toggleMosaiqueLayer(String type, String name, Boolean toggle){
		if(mosaiquesLayer.containsKey(type+name)){
			MosaiqueLayer mos = mosaiquesLayer.get(type+name);
			this.toggleLayer(mos.getShapeLayer(), toggle);
			this.toggleLayer(mos.getTextLayer(), toggle);
		} else {
			if(toggle){
				Boolean grille = true;
				LatLonCautra origine = null; 
				Integer width = 0;
				Integer height = 0;
				Integer size = 0; 
				int hSens = 0; 
				int vSens = 0;
				int numSens = 0;
				List<Couple<Integer, Integer>> squares = null;
				Boolean numbers = true;
				ShapeAttributes attr = null;
				if(type.equals("mosaique")) {
					try {
						Statement st = this.db.getCurrentExsa();
						ResultSet rs = st.executeQuery("select * from centmosai where type ='"+name+"'");
						origine = LatLonCautra.fromCautra(rs.getDouble("xcautra"), rs.getDouble("ycautra"));
						width = rs.getInt("colonnes");
						height = rs.getInt("lignes");
						rs.close();
						st.close();
						size = 32;
						hSens = MosaiqueLayer.BOTTOM_UP;
						vSens = MosaiqueLayer.LEFT_RIGHT;
						numSens = MosaiqueLayer.VERTICAL_FIRST;
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} if (type.equals("capa")) {
					try {
						grille = false;
						squares = new LinkedList<Couple<Integer,Integer>>();
						Statement st = this.db.getCurrentExsa();
						String typeGrille = name.equals("VISSEC") ? "ADP" : "CCR"; //TODO comment faire pour les autres centres ??
						ResultSet rs = st.executeQuery("select * from centmosai where type ='"+typeGrille+"'");
						origine = LatLonCautra.fromCautra(rs.getDouble("xcautra"), rs.getDouble("ycautra"));
						width = rs.getInt("colonnes");
						height = rs.getInt("lignes");
						size = 32;
						hSens = MosaiqueLayer.BOTTOM_UP;
						vSens = MosaiqueLayer.LEFT_RIGHT;
						numSens = MosaiqueLayer.VERTICAL_FIRST;
						rs = st.executeQuery("select * from ficaafniv where abonne = '"+name+"'");
						rs.next();
						for(int i=1; i<= height*width; i++){
							if(rs.getInt("carre") == i){
								if(!rs.getBoolean("elimine")){
									squares.add(new Couple<Integer, Integer>(i, 0));
								}
								rs.next();
							} else {
								squares.add(new Couple<Integer, Integer>(i, 0));
							}
						}
						numbers = false;
						attr = new BasicShapeAttributes();
						attr.setInteriorMaterial(Material.YELLOW);
						attr.setInteriorOpacity(0.4);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (type.equals("dyn")){
					grille = false;
					squares = new LinkedList<Couple<Integer,Integer>>();
					try {
						Statement st = this.db.getCurrentExsa();
						ResultSet rs = st.executeQuery("select * from centmosai where type ='CCR'");
						origine = LatLonCautra.fromCautra(rs.getDouble("xcautra"), rs.getDouble("ycautra"));
						width = rs.getInt("colonnes");
						height = rs.getInt("lignes");
						size = 32;
						hSens = MosaiqueLayer.BOTTOM_UP;
						vSens = MosaiqueLayer.LEFT_RIGHT;
						numSens = MosaiqueLayer.VERTICAL_FIRST;
						numbers = false;
						attr = new BasicShapeAttributes();
						attr.setInteriorMaterial(Material.YELLOW);
						attr.setInteriorOpacity(0.4);
						grille = false;
						rs = st.executeQuery("select * from ficaafnic where abonne = '"+name+"'");
						while(rs.next()){
							squares.add(new Couple<Integer, Integer>(rs.getInt("carre"), 0));
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (type.equals("zocc")){
					grille = false;
					squares = new LinkedList<Couple<Integer,Integer>>();
					try {
						Statement st = this.db.getCurrentExsa();
						ResultSet rs = st.executeQuery("select * from centmosai where type ='CCR'");
						origine = LatLonCautra.fromCautra(rs.getDouble("xcautra"), rs.getDouble("ycautra"));
						width = rs.getInt("colonnes");
						height = rs.getInt("lignes");
						size = 32;
						hSens = MosaiqueLayer.BOTTOM_UP;
						vSens = MosaiqueLayer.LEFT_RIGHT;
						numSens = MosaiqueLayer.VERTICAL_FIRST;
						numbers = false;
						attr = new BasicShapeAttributes();
						attr.setInteriorMaterial(Material.YELLOW);
						attr.setInteriorOpacity(0.4);
						grille = false;
						rs = st.executeQuery("select * from centsczoc where zone = '"+name+"'");
						while(rs.next()){
							squares.add(new Couple<Integer, Integer>(rs.getInt("carre"), rs.getInt("souscarre")));
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (type.equals("vvf")){
					grille = false;
					squares = new LinkedList<Couple<Integer,Integer>>();
					try {
						Statement st = this.db.getCurrentExsa();
						ResultSet rs = st.executeQuery("select * from centmosai where type ='CCR'");
						origine = LatLonCautra.fromCautra(rs.getDouble("xcautra"), rs.getDouble("ycautra"));
						width = rs.getInt("colonnes");
						height = rs.getInt("lignes");
						size = 32;
						hSens = MosaiqueLayer.BOTTOM_UP;
						vSens = MosaiqueLayer.LEFT_RIGHT;
						numSens = MosaiqueLayer.VERTICAL_FIRST;
						numbers = false;
						attr = new BasicShapeAttributes();
						attr.setInteriorMaterial(Material.YELLOW);
						attr.setInteriorOpacity(0.4);
						attr.setOutlineMaterial(Material.YELLOW);
						grille = false;
						rs = st.executeQuery("select * from centscvvf where vvfs LIKE '%"+name+"%'");
						while(rs.next()){
							squares.add(new Couple<Integer, Integer>(rs.getInt("carre"), rs.getInt("souscarre")));
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				MosaiqueLayer mLayer = new MosaiqueLayer(grille, origine, width, height, size, hSens, vSens, numSens, squares, numbers, attr);
				mosaiquesLayer.put(type+name, mLayer);
				this.toggleLayer(mLayer.getShapeLayer(), toggle);
				this.toggleLayer(mLayer.getTextLayer(), toggle);
			}
		}
	}
	/**
	 * Supprime toutes les mosaiques de la vue
	 */
	public void removeMosaiques(){
		Iterator<MosaiqueLayer> iterator = mosaiquesLayer.values().iterator();
		while(iterator.hasNext()){
			MosaiqueLayer mos = iterator.next();
			this.toggleLayer(mos.getShapeLayer(), false);
			this.toggleLayer(mos.getTextLayer(), false);
		}
		mosaiquesLayer.clear();
	}
}
