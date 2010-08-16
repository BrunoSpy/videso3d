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

package fr.crnan.videso3d.stip;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.Route2D;
import fr.crnan.videso3d.graphics.Route3D;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.graphics.Route.Type;
import fr.crnan.videso3d.layers.BaliseLayer;
import fr.crnan.videso3d.layers.Routes2DLayer;
import fr.crnan.videso3d.layers.Routes3DLayer;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;

/**
 * Contrôle l'affichage et la construction des éléments 3D
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class StipController implements VidesoController {

	private VidesoGLCanvas wwd;
	
	/**
	 * Layer contenant les routes
	 */
	private Routes3DLayer routes3D;
	private Routes2DLayer routes2D;
	/**
	 * 
	 * Layers pour les balises publiées
	 */
	private BaliseLayer balisesPub = new BaliseLayer("Balises publiées");
	/**
	 * Layers pour les balises non publiées
	 */
	private BaliseLayer balisesNP = new BaliseLayer("Balises non publiées");
	/**
	 * Layer contenant les secteurs
	 */
	private AirspaceLayer secteursLayer = new AirspaceLayer();
	{secteursLayer.setName("Secteurs");
	secteursLayer.setEnableAntialiasing(true);}	
	
	private HashMap<String, Secteur3D> secteurs = new HashMap<String, Secteur3D>();
	/**
	 * Liste nominale des balises
	 */
	private HashMap<String, Balise2D> balises = new HashMap<String, Balise2D>();
	//Attributs pour le highlight
	private Object highlight;
	private Object lastAttrs;
	
	/**
	 * Constantes
	 */
	public final static int ROUTES = 0;
	public final static int BALISES_PUB = 1;
	public final static int BALISES_NP = 2;
	public final static int SECTEUR = 3;
	
	
	public StipController(VidesoGLCanvas wwd){
		this.wwd = wwd;
		this.buildStip();
	}

	@Override
	public void unHighlight(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addLayer(String name, Layer layer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeLayer(String name, Layer layer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toggleLayer(Layer layer, Boolean state) {
		this.wwd.toggleLayer(layer, state);
	}

	@Override
	public void removeAllLayers() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void set2D(Boolean flat) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void reset(){
		this.balisesNP.removeAllBalises();
		this.balisesPub.removeAllBalises();
		this.secteurs.clear();
		this.secteursLayer.removeAllAirspaces();
		this.routes2D.hideAllRoutes();
		this.routes3D.hideAllRoutes();
	}

	@Override
	public void showObject(int type, String name) {
		switch (type) {
		case 0://Route
			
			break;
		case 1://Balises Pub
			
			break;
		case 2://Balises NP

			break;
		case 3://secteur
			if(!secteurs.containsKey(name+0)){//n'afficher le secteur que s'il n'est pas déjà affiché
				this.addSecteur3D(name);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void hideObject(int type, String name) {
		switch (type) {
		case 0://Routes
			
			break;
		case 1://Balises Pub

			break;
		case 2://Balises NP

			break;
		case 3://secteur
			this.removeSecteur3D(name);
			break;
		default:
			break;
		}		
	}
	
	
	/*--------------------------------------------------------------*/
	/*----------------- Gestion des balises STIP -------------------*/
	/*--------------------------------------------------------------*/
	
	/**
	 * Construit les balises Stip
	 * @param publicated Balises publéies ou non
	 */
	private void buildBalises(int publicated){
		switch (publicated) {
		case 0:
			this.wwd.firePropertyChange("step", "", "Création des balises publiées");
			break;
		case 1:
			this.wwd.firePropertyChange("step", "", "Création des balises non publiées");
			break;
		}
		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from balises where publicated = " + publicated);
			while(rs.next()){
				Balise2D balise = new Balise2D(rs.getString("name"), Position.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude"), 100.0));
				String annotation = "<p><b>Balise "+rs.getString("name") +"</b></p>";
				annotation += "<p>Commentaire : "+rs.getString("definition")+"<br />";
				int plafond = -1;
				String secteur = null;
				for(int i = 9; i>= 1; i--){
					int plancher = rs.getInt("limit"+i);
					if(plancher != -1){
						if(secteur!=null){
							annotation += "\nDu "+plafond+" au "+plancher+" : "+secteur+"<br />";
						}	
						plafond = plancher;
						secteur = rs.getString("sect"+i);
					}
				}
				if(secteur != null) annotation += "\nDu "+plafond+" au "+0+" : "+secteur+"<br />";
				annotation += "</p>";
				balise.setAnnotation(annotation);
				if(publicated == 1){
					balisesPub.addBalise(balise);
				} else {
					balisesNP.addBalise(balise);
				}
				//lien nominal
				this.balises.put(rs.getString("name"), balise);
			}
			
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public BaliseLayer getBalisesPubLayer(){
		return balisesPub;
	}
	public BaliseLayer getBalisesNPLayer(){
		return balisesNP;
	}
	
	/*--------------------------------------------------------------*/
	/*----------------- Gestion des secteurs STIP ------------------*/
	/*--------------------------------------------------------------*/
	
	/**
	 * Ajoute tous les {@link Secteur3D} formant le secteur <code>name</code>
	 * @param name Nom du secteur à ajouter
	 */
	private void addSecteur3D(String name){
		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select secteurs.nom, secteurs.numero, cartesect.flinf, cartesect.flsup from secteurs, cartesect where secteurs.numero = cartesect.sectnum and secteurs.nom ='"+name+"'");
			Integer i = 0;
			BasicAirspaceAttributes attrs = new BasicAirspaceAttributes();
			attrs.setDrawOutline(true);
			attrs.setMaterial(new Material(Color.CYAN));
			attrs.setOutlineMaterial(new Material(Pallet.makeBrighter(Color.CYAN)));
			attrs.setOpacity(0.2);
			attrs.setOutlineOpacity(0.9);
			attrs.setOutlineWidth(1.5);
			while(rs.next()){
				Secteur3D secteur3D = new Secteur3D(name, rs.getInt("flinf"), rs.getInt("flsup"));
				Secteur secteur = new Secteur(name, rs.getInt("numero"), DatabaseManager.getCurrentStip());
				secteur.setConnectionPays(DatabaseManager.getCurrent(DatabaseManager.Type.PAYS));
				secteur3D.setLocations(secteur.getContour(rs.getInt("flsup")));
				secteur3D.setAttributes(attrs);
				this.addToSecteursLayer(secteur3D);
				secteurs.put(name+i.toString(), secteur3D);
				i++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Enlève tous les {@link Secteur3D} formant le secteur <code>name</code>
	 * @param name Nom du secteur à supprimer
	 */
	private void removeSecteur3D(String name){
		Integer i = 0;
		while(secteurs.containsKey(name+i.toString())){
			this.removeFromSecteursLayer(secteurs.get(name+i.toString()));
			secteurs.remove(name+i.toString());
			i++;
		}
	}

	/**
	 * Change les attributs de tous les {@link Secteur3D} formant le secteur <code>name</code>
	 * @param name Nom du secteur à modifier
	 * @param attrs Attributs
	 */
	private void setAttributesToSecteur(String name, AirspaceAttributes attrs){
		Integer i = 0;
		while(secteurs.containsKey(name+i.toString())){
			secteurs.get(name+i.toString()).setAttributes(attrs);
			i++;
		}
	}
	
	private void addToSecteursLayer(Secteur3D secteur){
		this.secteursLayer.addAirspace(secteur);
		this.wwd.redraw();
	}
	
	private void removeFromSecteursLayer(Secteur3D secteur){
		this.secteursLayer.removeAirspace(secteur);
		this.wwd.redraw();
	}
	
	/*--------------------------------------------------------------*/
	/*------------------ Gestion des routes STIP -------------------*/
	/*--------------------------------------------------------------*/
	private void buildRoutes(String type) {
		switch (type.charAt(0)) {
		case 'F':
			this.wwd.firePropertyChange("step", "", "Création des routes FIR");
			break;
		case 'U':
			this.wwd.firePropertyChange("step", "", "Création des routes UIR");
			break;
		}
		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet routes = st.executeQuery("select name from routes where espace = '"+type+"'");
			LinkedList<String> routesNames = new LinkedList<String>();
			while(routes.next()){
				routesNames.add(routes.getString(1));
				
			}
			Iterator<String> iterator = routesNames.iterator();
			while(iterator.hasNext()){
				String name = iterator.next();
				Route3D route3D = new Route3D();
				Route2D route2D = new Route2D();
				if(type.equals("F")) {
					route3D.setType(Type.FIR);
					route2D.setType(Type.FIR);
				}
				if(type.equals("U")) {
					route3D.setType(Type.UIR);
					route2D.setType(Type.UIR);
				}
				ResultSet rs = st.executeQuery("select * from routebalise, balises where route = '"+name+"' and routebalise.balise = balises.name and appartient = 1");
				LinkedList<LatLon> loc = new LinkedList<LatLon>();
				LinkedList<Integer> sens = new LinkedList<Integer>();
				LinkedList<String> balises = new LinkedList<String>();
				while(rs.next()){
					loc.add(LatLon.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude")));
					balises.add(rs.getString("balise"));
					if(rs.getString("sens").equals("+")){
						sens.add(Route3D.LEG_FORBIDDEN);
					} else {
						sens.add(Route3D.LEG_AUTHORIZED);
					}
				}
				route3D.setLocations(loc, sens);
				route3D.setName(name);
				route3D.setBalises(balises);
				route2D.setLocations(loc);
				route2D.setBalises(balises);
				route2D.setName(name);
				this.routes3D.addRoute(route3D, name);
				this.routes2D.addRoute(route2D, name);
			}
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Routes3DLayer getRoutes3DLayer(){
		return routes3D;
	}

	public Routes2DLayer getRoutes2DLayer(){
		return routes2D;
	}
	/**
	 * Affiche les balises associées à la route
	 * @param name
	 */
	public void showRoutesBalises(String name){
		List<String> balises = routes3D.getRoute(name).getBalises();
		balisesNP.showBalises(balises);
		balisesPub.showBalises(balises);
	}
	
	public void hideRoutesBalises(String name){
		List<String> balises = routes3D.getRoute(name).getBalises();
		balisesNP.unshowBalises(balises);
		balisesPub.unshowBalises(balises);
	}
	
	/**
	 * Construit ou met à jour les objets Stip
	 * Appelé lors de l'initialisation de la vue ou lors du changement de base de données Stip
	 */
	private void buildStip() {
		this.wwd.firePropertyChange("step", "", "Suppression des objets 3D");

		//Suppression des objets
		if(routes3D != null) {
			routes3D.removeAllAirspaces();
		} else {
			routes3D = new Routes3DLayer("Routes Stip 3D");
			this.toggleLayer(routes3D, false); //affichage en 2D par défaut
		}
		if(routes2D != null){
			routes2D.removeAllRenderables();
		} else {
			routes2D = new Routes2DLayer("Routes Stip 2D");
			this.toggleLayer(routes2D, true);
		}
		if(balisesPub != null) {
			this.toggleLayer(balisesPub, true);
			balisesPub.removeAllBalises();
		}
		if(balisesNP != null) {
			this.toggleLayer(balisesNP, true);
			balisesNP.removeAllBalises();
		}
		if(secteursLayer != null) {
			secteursLayer.removeAllAirspaces();
			this.toggleLayer(secteursLayer, true);
		} else {
			secteursLayer = new AirspaceLayer();
			secteursLayer.setName("Secteurs");
			secteursLayer.setEnableAntialiasing(true);
			this.toggleLayer(secteursLayer, true);
		}
		try {
			if(DatabaseManager.getCurrentStip() != null) {
				//création des nouveaux objets
				buildBalises(0);
				buildBalises(1);
				buildRoutes("F");
				buildRoutes("U");
				this.toggleLayer(secteursLayer, true);
				secteurs = new HashMap<String, Secteur3D>();				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.wwd.redraw();
	}

	/**
	 * Centre la vue et met en valeur un objet
	 * @param text Nom de l'objet à afficher
	 */
	@Override
	public void highlight(String text) {
//		if(text.trim().isEmpty()){
//			if(highlight != null) {
//				if((highlight instanceof Route3D) && lastAttrs != null){
//					((Airspace)highlight).setAttributes((AirspaceAttributes) lastAttrs);
//					selectedAirspaces.removeAllAirspaces();
//				} else if (highlight instanceof String){ //cas des secteurs
//					this.setAttributesToSecteur((String) highlight, (AirspaceAttributes) lastAttrs);
//					this.removeSecteur3D((String) highlight);
//				} else if(highlight instanceof Balise2D){
//					((Balise2D) highlight).highlight(false);
//					if(lastLayer != null) this.toggleLayer(lastLayer, false);
//					lastLayer = null;
//				}
//				lastAttrs = null;
//				highlight = null;
//			}
//		} else {
			try {
				Statement st = DatabaseManager.getCurrentStip();
				//on recherche le type
				ResultSet rs = st.executeQuery("select * from routes where routes.name = '"+text+"'");
				if(rs.next()){
					Route3D airspace = (Route3D) routes3D.getRoute(text);
					
					this.unHighlightPrevious(airspace);
					
					routes2D.highlight(text);
					routes3D.highlight(text);
					
					//ajout des balises
					balisesNP.showBalises(airspace.getBalises());
					balisesPub.showBalises(airspace.getBalises());
					
					this.toggleLayer(balisesNP, true);
					this.toggleLayer(balisesPub, true);
					
					this.wwd.getView().goTo(airspace.getReferencePosition(), 1e6);
					
					highlight = airspace;
					
					return;
				}
				rs = st.executeQuery("select * from secteurs where nom = '"+text+"'");
				if(rs.next()){
					if(!secteurs.containsKey(text+0)){
						this.addSecteur3D(text);
					}
					this.unHighlightPrevious(text);
					Secteur3D airspace = secteurs.get(text+0);
					lastAttrs = airspace == null ? new BasicAirspaceAttributes() : airspace.getAttributes(); //nécessaire à cause des secteurs fictifs qui n'ont pas de dessin
					AirspaceAttributes attrs = new BasicAirspaceAttributes((AirspaceAttributes) lastAttrs);
					attrs.setOutlineMaterial(Material.YELLOW);
					this.setAttributesToSecteur(text, attrs);
					highlight = text;
					if(airspace != null) this.wwd.getView().goTo(airspace.getReferencePosition(), 1e6);
					return;
				}
				rs = st.executeQuery("select * from balises where name = '"+text+"'");
				if(rs.next()){
					Balise2D airspace = (Balise2D) balises.get(text);
					airspace.highlight(true);
					this.unHighlightPrevious(airspace);
					highlight = airspace;
					if (rs.getInt("publicated") == 1) {
						balisesPub.showBalise(airspace);
					}
					if (rs.getInt("publicated") == 0) {
						balisesNP.showBalise(airspace);
					} 
					this.wwd.getView().goTo(airspace.getPosition(), 4e5);
					return;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
//		}
	}
	
	private void unHighlightPrevious(Object previous){
		if(highlight != null){
			if(highlight == previous) {
				return;
			} else if (highlight instanceof Route3D){
				routes2D.unHighlight(((Route3D) highlight).getName());
				routes3D.unHighlight(((Route3D) highlight).getName());
				highlight = null;
			} else if(highlight instanceof String){
				this.setAttributesToSecteur((String) highlight, (AirspaceAttributes) lastAttrs);
				highlight = null;
			} else if(highlight instanceof Balise2D){
				((Balise2D)highlight).highlight(false);
				highlight = null;
			}
		}
	}
	
}
