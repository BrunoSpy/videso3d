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

package fr.crnan.videso3d.databases.stip;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.ProgressSupport;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.Balise3D;
import fr.crnan.videso3d.graphics.DatabaseBalise2D;
import fr.crnan.videso3d.graphics.DatabaseBalise3D;
import fr.crnan.videso3d.graphics.DatabaseRoute2D;
import fr.crnan.videso3d.graphics.DatabaseRoute3D;
import fr.crnan.videso3d.graphics.Route.Space;
import fr.crnan.videso3d.graphics.Route2D;
import fr.crnan.videso3d.graphics.Route3D;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.graphics.VPolygon;
import fr.crnan.videso3d.layers.Balise2DLayer;
import fr.crnan.videso3d.layers.Balise3DLayer;
import fr.crnan.videso3d.layers.FilterableAirspaceLayer;
import fr.crnan.videso3d.layers.Routes2DLayer;
import fr.crnan.videso3d.layers.Routes3DLayer;
import fr.crnan.videso3d.layers.TextLayer;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;

/**
 * Contrôle l'affichage et la construction des éléments 3D
 * @author Bruno Spyckerelle
 * @version 0.2.0
 */
public class StipController extends ProgressSupport implements VidesoController {

	private VidesoGLCanvas wwd;
	
	/**
	 * Layer contenant les routes
	 */
	private Routes3DLayer routes3D;
	private Routes2DLayer routes2D;

	/**
	 * Layer pour les itis
	 */
	private RenderableLayer itisLayer;
	private HashMap<String, Route2D> itis = new HashMap<String, Route2D>();
	/**
	 * Layer pour les trajets
	 */
	private RenderableLayer trajLayer;
	private HashMap<String, Route2D> trajs = new HashMap<String, Route2D>();
	/**
	 * Layer pour les connexions
	 */
	private RenderableLayer connexLayer;
	private HashMap<String, Route2D> connexions = new HashMap<String, Route2D>();
	
	/**
	 * 
	 * Layers pour les balises publiées
	 */
	private Balise3DLayer balisesPub3D = new Balise3DLayer("Balises 3D publiées");
	private Balise2DLayer balisesPub2D = new Balise2DLayer("Balises publiées");
	/**
	 * Layers pour les balises non publiées
	 */
	private Balise2DLayer balisesNP2D = new Balise2DLayer("Balises non publiées");
	private Balise3DLayer balisesNP3D = new Balise3DLayer("Balises 3D non publiées");
	/**
	 * Layer contenant les secteurs
	 */
	private FilterableAirspaceLayer secteursLayer = new FilterableAirspaceLayer();
	{secteursLayer.setName("Secteurs");
	secteursLayer.setEnableAntialiasing(true);}		
	
	/**
	 * Layer de texte pour afficher les coordonnées des secteurs.
	 */
	private TextLayer textLayer = new TextLayer("Coord. volumes STIP");
	
	private HashMap<String, Secteur3D> secteurs = new HashMap<String, Secteur3D>();
	/**
	 * Liste nominale des balises
	 */
	private HashMap<String, Balise2D> balises2D = new HashMap<String, Balise2D>();
	private HashMap<String, Balise3D> balises3D = new HashMap<String, Balise3D>();
	
	//Attributs pour le highlight
	private Object highlight;
	private Object lastAttrs;
	
	private String routeEnCreation = "";
	
	/**
	 * Constantes
	 */
	public final static int ROUTES = 0;
	public final static int BALISES = 1;
	public final static int SECTEUR = 2;
	public final static int ITI = 3;
	public final static int CONNEXION = 4;
	public final static int TRAJET = 5;
	public final static int CONSIGNE = 6;
	public final static int BALINT = 7;
	
	public StipController(VidesoGLCanvas wwd){
		this.wwd = wwd;
		this.buildStip();
	}

	@Override
	public void unHighlight(int type, String name) {}

	@Override
	public void addLayer(String name, Layer layer) {}

	@Override
	public void removeLayer(String name, Layer layer) {}

	@Override
	public void toggleLayer(Layer layer, Boolean state) {
		this.wwd.toggleLayer(layer, state);
	}

	@Override
	public void removeAllLayers() {
		this.wwd.removeLayer(routes2D);
		this.wwd.removeLayer(routes3D);
		this.wwd.removeLayer(balisesNP2D);
		this.wwd.removeLayer(balisesPub2D);
		this.wwd.removeLayer(balisesNP3D);
		this.wwd.removeLayer(balisesPub3D);
		this.wwd.removeLayer(secteursLayer);
		this.wwd.removeLayer(itisLayer);
		this.wwd.removeLayer(trajLayer);
		this.wwd.removeLayer(connexLayer);
		this.wwd.removeLayer(textLayer);
	}
	
	@Override
	public void set2D(Boolean flat) {}
	
	@Override
	public void reset(){
		this.balisesNP2D.setLocked(false);
		this.balisesPub2D.setLocked(false);
		this.balisesNP2D.removeAllBalises();
		this.balisesPub2D.removeAllBalises();
		this.balisesNP3D.setLocked(false);
		this.balisesPub3D.setLocked(false);
		this.balisesNP3D.removeAllBalises();
		this.balisesPub3D.removeAllBalises();
		for(Secteur3D s : this.secteurs.values()){
			s.setVisible(false);
		}
		this.routes2D.hideAllRoutes();
		this.routes3D.hideAllRoutes();
		for(Renderable r : this.itisLayer.getRenderables()){
			((Route2D) r).setVisible(false);
		}
		for(Renderable r : this.trajLayer.getRenderables()){
			((Route2D) r).setVisible(false);
		}
		for(Renderable r : this.connexLayer.getRenderables()){
			((Route2D) r).setVisible(false);
		}
		textLayer.removeAllGeographicTexts();
	}

	@Override
	public void showObject(int type, String name) {
		switch (type) {
		case ROUTES://Route
			this.createRoute(name);
			this.routes2D.displayRoute(name);
			this.routes3D.displayRoute(name);
			break;
		case BALISES://Balises
			this.createBalise(name);
			this.balisesPub3D.showBalise(name, type);
			this.balisesNP3D.showBalise(name, type);
			this.balisesPub2D.showBalise(name, type);
			this.balisesNP2D.showBalise(name, type);
			break;
		case SECTEUR://secteur
			this.addSecteur3D(name);
			break;
		case ITI:
			if(this.itis.containsKey(name)){
				this.itis.get(name).setVisible(true);
			} else {
				this.createIti(name);
			}
			for(String balise : this.itis.get(name).getBalises()){
				this.showObject(BALISES, balise);
			}
			break;
		case TRAJET:
			if(this.trajs.containsKey(name)){
				this.trajs.get(name).setVisible(true);
			} else {
				this.createTrajet(name);
			}
			for(String balise : this.trajs.get(name).getBalises()){
				this.showObject(BALISES, balise);
			}
			break;
		case CONNEXION:
			if(this.connexions.containsKey(name)){
				this.connexions.get(name).setVisible(true);
			} else {
				this.createConnex(name);
			}
			for(String balise : this.connexions.get(name).getBalises()){
				this.showObject(BALISES, balise);
			}
			break;
		default:
			break;
		}
		//synchroniser la vue si l'appel n'a pas été fait par la vue
		DatasManager.getView(DatasManager.Type.STIP).showObject(type, name);
	}

	@Override
	public void hideObject(int type, String name) {
		switch (type) {
		case ROUTES://Routes
			this.routes2D.hideRoute(name);
			this.routes3D.hideRoute(name);
			hideRoutesBalises(name);
			break;
		case BALISES://Balises Pub
			this.balisesPub2D.hideBalise(name, type);
			this.balisesNP2D.hideBalise(name, type);
			this.balisesPub3D.hideBalise(name, type);
			this.balisesNP3D.hideBalise(name, type);
			break;
		case SECTEUR://secteur
			this.removeSecteur3D(name);
			break;
		case ITI:
			if(this.itis.containsKey(name))
				((Route2D) this.itis.get(name)).setVisible(false);
		case TRAJET:
			if(this.trajs.containsKey(name))
				((Route2D) this.trajs.get(name)).setVisible(false);
		case CONNEXION:
			if(this.connexions.containsKey(name))
				((Route2D) this.connexions.get(name)).setVisible(false);
		default:
			break;
		}		
		//synchroniser la vue si l'appel n'a pas été fait par la vue
		DatasManager.getView(DatasManager.Type.STIP).hideObject(type, name);
	}
	
	@Override
	public boolean isColorEditable(int type){
		switch (type) {
		case ROUTES://Routes
			return false;
		case BALISES://Balises Pub
			return false;
		case SECTEUR://secteur
			return true;
		default:
			return false;
		}		
	}

	@Override
	public void setColor(Color color, int type, String name) {
		switch (type) {
		case ROUTES://Routes
			
			break;
		case BALISES://Balises Pub
			
			break;
		case SECTEUR://secteur
			int i = 0;
			while(this.secteurs.containsKey(name+i)){
				this.secteurs.get(name+i).getAttributes().setMaterial(new Material(color));
				i++;
			}
			this.firePropertyChange(AVKey.LAYER, null, this.secteursLayer);
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
	}
	
	/**
	 * Crée les balises 2D et 3D si besoin
	 * @param name Nom dela balise à créer
	 */
	private void createBalise(String name){
		if(!balises2D.containsKey(name)) {
			try {
				Statement st = DatabaseManager.getCurrentStip();
				ResultSet rs = st.executeQuery("select * from balises where name = '" + name+"'");
				
				Balise2D balise2d = null;
				Balise3D balise3d = null;
				
				if(rs.next()){
					
					String annotation = "<p><b>Balise "+rs.getString("name") +"</b></p>";
					annotation += "<p>Commentaire : "+rs.getString("definition")+"<br />";
					int plafond = -1;
					int plafondMax = 10;
					String secteur = null;
					for(int i = 9; i>= 1; i--){
						int plancher = rs.getInt("limit"+i);
						if(plancher != -1){
							if(secteur!=null){
								annotation += "\nDu "+plafond+" au "+plancher+" : "+secteur+"<br />";
							}	
							if (plafond > plafondMax) plafondMax = plafond;
							plafond = plancher;
							secteur = rs.getString("sect"+i);
						}
					}
					if(secteur != null) annotation += "\nDu "+plafond+" au "+0+" : "+secteur+"<br />";
					annotation += "</p>";

					if(rs.getBoolean("publicated")) {
						balise3d = new DatabaseBalise3D(rs.getString("name"), Position.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude"), plafondMax*30.48), annotation, DatasManager.Type.STIP, StipController.BALISES);
						balisesPub3D.addBalise(balise3d);
						balise2d = new DatabaseBalise2D(rs.getString("name"), Position.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude"), 100.0), annotation, DatasManager.Type.STIP, StipController.BALISES, balisesPub2D.getTextLayer());
						balisesPub2D.addBalise(balise2d);	
					} else {
						balise3d = new DatabaseBalise3D(rs.getString("name"), Position.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude"), plafondMax*30.48), annotation, DatasManager.Type.STIP, StipController.BALISES);
						balisesNP3D.addBalise(balise3d);
						balise2d = new DatabaseBalise2D(rs.getString("name"), Position.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude"), 100.0), annotation, DatasManager.Type.STIP, StipController.BALISES, balisesNP2D.getTextLayer());
						balisesNP2D.addBalise(balise2d);
					}
					//lien nominal
					this.balises2D.put(rs.getString("name"), balise2d);
					this.balises3D.put(rs.getString("name"), balise3d);
					
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void createBalise(List<String> names){
		for(String name : names){
			this.createBalise(name);
		}
	}
	
	/*--------------------------------------------------------------*/
	/*----------------- Gestion des secteurs STIP ------------------*/
	/*--------------------------------------------------------------*/
		
	/**
	 * Ajoute tous les {@link Secteur3D} formant le secteur <code>name</code>
	 * @param name Nom du secteur à ajouter
	 */
	private void addSecteur3D(String name){
		if(!secteurs.containsKey(name+0)){
			this.createSecteur(name);
		}
		Integer i = 0;
		while(secteurs.containsKey(name+i.toString())){
			(secteurs.get(name+i.toString())).setVisible(true);
			i++;
		}
		this.secteursLayer.firePropertyChange(AVKey.LAYER, null, this.secteursLayer);
	}
	
	private void createSecteur(String name){
		if(!secteurs.containsKey(name+0)){
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
					Secteur3D secteur3D = new Secteur3D(name, rs.getInt("flinf"), rs.getInt("flsup"),StipController.SECTEUR, DatasManager.Type.STIP, textLayer);
					Secteur secteur = new Secteur(name, rs.getInt("numero"), DatabaseManager.getCurrentStip());
					secteur.setConnectionPays(DatabaseManager.getCurrent(DatasManager.Type.PAYS));
					secteur3D.setLocations(secteur.getContour(rs.getInt("flsup")));
					secteur3D.setNormalAttributes(attrs);
					secteur3D.setVisible(false);
					this.addToSecteursLayer(secteur3D);
					secteurs.put(name+i.toString(), secteur3D);
					i++;
				}
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Enlève tous les {@link Secteur3D} formant le secteur <code>name</code>
	 * @param name Nom du secteur à supprimer
	 */
	private void removeSecteur3D(String name){
		Integer i = 0;
		while(secteurs.containsKey(name+i.toString())){
			secteurs.get(name+i.toString()).setVisible(false);
			secteurs.get(name+i.toString()).setLocationsVisible(false);
			i++;
		}
		this.secteursLayer.firePropertyChange(AVKey.LAYER, null, this.secteursLayer);
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
		this.secteursLayer.firePropertyChange(AVKey.LAYER, null, this.secteursLayer);
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
	}

	private void createRoute(String name){
		if(!routeEnCreation.equals(name)){
			routeEnCreation = name;
			if(this.routes2D.getRoute(name) == null) {
				DatabaseRoute3D route3D = new DatabaseRoute3D(DatasManager.Type.STIP, StipController.ROUTES);
				DatabaseRoute2D route2D = new DatabaseRoute2D(DatasManager.Type.STIP, StipController.ROUTES);
				List<LatLon> loc = new ArrayList<LatLon>();
				List<Integer> sens = new ArrayList<Integer>();
				List<String> balises = new ArrayList<String>();
				try {
					Statement st = DatabaseManager.getCurrentStip();
					ResultSet rs = st.executeQuery("select espace from routes where name = '"+name+"'");
					String type = rs.getString(1);
					if(type.equals("F")) {
						route3D.setSpace(Space.FIR);
						route2D.setSpace(Space.FIR);
					}
					if(type.equals("U")) {
						route3D.setSpace(Space.UIR);
						route2D.setSpace(Space.UIR);
					}
					rs = st.executeQuery("select * from routebalise, balises where route = '"+name+"' and routebalise.balise = balises.name and appartient = 1");
					while(rs.next()){
						loc.add(LatLon.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude")));
						balises.add(rs.getString("balise"));
						String s = rs.getString("sens");
						if(s.equals("+")){
							sens.add(Route3D.LEG_FORBIDDEN);
						} else if(s.equals(">")){
							sens.add(Route3D.LEG_DIRECT);
						} else if(s.equals("<")){
							sens.add(Route3D.LEG_INVERSE);
						} else {
							sens.add(Route3D.LEG_AUTHORIZED);
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				route3D.setLocations(loc, sens);
				route3D.setName(name);
				route3D.setBalises(balises);
				route2D.setLocations(loc, sens);
				route2D.setBalises(balises);
				route2D.setName(name);
				this.routes3D.addRoute(route3D, name);
				this.routes2D.addRoute(route2D, name);
			}
			routeEnCreation = "";
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
	 * @param name Le nom de la route
	 * @param withLocations true si on veut afficher les coordonnées des balises, false sinon
	 */
	public void showRoutesBalises(String name, boolean withLocations){
		List<String> balises = routes3D.getRoute(name).getBalises();
		this.createBalise(balises);
		balisesNP3D.showBalises(balises, BALISES);
		balisesPub3D.showBalises(balises, BALISES);
		balisesNP2D.showBalises(balises, BALISES);
		balisesPub2D.showBalises(balises, BALISES);
		if(withLocations){
			for(String b : balises){
				setLocationsVisible(BALISES, b, true);
			}
		}
	}
	
	public void hideRoutesBalises(String name){
		List<String> balises = routes3D.getRoute(name).getBalises();
		balisesNP2D.hideBalises(balises, BALISES);
		balisesPub2D.hideBalises(balises, BALISES);
		balisesNP3D.hideBalises(balises, BALISES);
		balisesPub3D.hideBalises(balises, BALISES);
	}
	
	/*--------------------------------------------------------------*/
	/*-------------------- Gestion des itis STIP -------------------*/
	/*--------------------------------------------------------------*/
	

	/**
	 * Crée et affiche l'iti correspondant
	 * @param name Id de l'iti
	 */
	public void createIti(String name) {
		Integer id = new Integer(name);
		DatabaseRoute2D iti = new DatabaseRoute2D(name, DatasManager.Type.STIP, ITI);
		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from itis, balitis where itis.id ='"+id+"' and itis.id = balitis.iditi");
			ArrayList<String> balises = new ArrayList<String>();
			ArrayList<Integer> sens = new ArrayList<Integer>();
			ArrayList<LatLon> pos = new ArrayList<LatLon>();
			while(rs.next()){
				balises.add(rs.getString("balise"));
			}
			for(String balise : balises){
				rs = st.executeQuery("select * from balises, balitis where balitis.balid = balises.id" +
																			" and balises.name ='"+balise+"'" +
																			" and balitis.appartient = 1" +
																			" and balitis.iditi ='"+id+"'");
				if(rs.next()){
					pos.add(LatLon.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude")));
					sens.add(Route3D.LEG_AUTHORIZED);
				}
			}
			iti.setLocations(pos, sens);
			iti.setBalises(balises);
			rs = st.executeQuery("select * from itis where itis.id ='"+id+"'");
			iti.setAnnotation("<html><b>Iti</b><br /><br />De "+rs.getString("entree")+" vers "+rs.getString("sortie")+".<br />" +
					"Du niveau "+rs.getString("flsup")+" au niveau "+rs.getString("flinf"));
			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.itis.put(name, iti);
		this.itisLayer.addRenderable(iti);
		this.itisLayer.firePropertyChange(AVKey.LAYER, null, this.itisLayer);
	}
	
	/**
	 * Crée et affiche le trajet correspondant
	 * @param name Id du trajet
	 */
	public void createTrajet(String name) {
		Integer id = new Integer(name);
		DatabaseRoute2D trajet = new DatabaseRoute2D(name, DatasManager.Type.STIP, TRAJET);
		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from trajets, baltrajets where trajets.id ='"+id+"' and trajets.id = baltrajets.idtrajet");
			ArrayList<String> balises = new ArrayList<String>();
			ArrayList<Integer> sens = new ArrayList<Integer>();
			ArrayList<LatLon> pos = new ArrayList<LatLon>();
			while(rs.next()){
				balises.add(rs.getString("balise"));
				sens.add(Route3D.LEG_AUTHORIZED);
			}
			for(String balise : balises){
				rs = st.executeQuery("select * from balises where name ='"+balise+"'");
				pos.add(LatLon.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude")));
			}
			trajet.setLocations(pos, sens);
			trajet.setBalises(balises);
			rs = st.executeQuery("select * from trajets where trajets.id ='"+id+"'");
			String cond1 = rs.getString("cond1");
			String cond2 = rs.getString("cond2");
			String conditions = !cond1.matches(" *")? (cond2!=null?"Conditions : "+cond1+" "+rs.getString("etat1")+", "+cond2+" "+rs.getString("etat2") :"Condition : "+cond1+" "+rs.getString("etat1") ):"";
			trajet.setAnnotation("<html><b>Trajet</b><br /><br />De "+rs.getString("eclatement")+" vers "+rs.getString("raccordement")+".<br />" +
					"Plafond FL"+rs.getString("fl")+"<br/>"+conditions);
			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.trajs.put(name, trajet);
		this.trajLayer.addRenderable(trajet);
		this.trajLayer.firePropertyChange(AVKey.LAYER, null, this.trajLayer);
	}
	/**
	 * Crée et affiche la connexion correspondante
	 * @param name Id de la connexion
	 */
	public void createConnex(String name) {
		Integer id = new Integer(name);
		DatabaseRoute2D connex = new DatabaseRoute2D(name, DatasManager.Type.STIP, CONNEXION);
		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from connexions, balconnexions where connexions.id ='"+id+"' and connexions.id = balconnexions.idconn");
			ArrayList<String> balises = new ArrayList<String>();
			ArrayList<Integer> sens = new ArrayList<Integer>();
			ArrayList<LatLon> pos = new ArrayList<LatLon>();
			while(rs.next()){
				balises.add(rs.getString("balise"));
				sens.add(Route3D.LEG_AUTHORIZED);
			}
			rs = st.executeQuery("select * from connexions where connexions.id ='"+id+"'");
			if(rs.next()){
				String DepartOuArrivee = "";
				String baliConnex = rs.getString("connexion");
				if(rs.getString("type").equals("A")){
					balises.add(0, baliConnex);
					DepartOuArrivee = "arrivée";
				}else{
					balises.add(baliConnex);
					DepartOuArrivee = "départ";
				}
				connex.setAnnotation("<html><b>Connexion "+DepartOuArrivee+"</b><br /><br />Terrain : "+rs.getString("terrain")+"<br />Balise de connexion : "+baliConnex+"<br/>" +
						"Du niveau "+rs.getString("flinf")+" au niveau "+rs.getString("flsup"));
				ResultSet rs2 = null;
				for(String balise : balises){
					rs2 = st.executeQuery("select * from balises where name ='"+balise+"'");
					pos.add(LatLon.fromDegrees(rs2.getDouble("latitude"), rs2.getDouble("longitude")));
				}
				rs2.close();
				connex.setLocations(pos, sens);
				connex.setBalises(balises);
			}
			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.connexions.put(name, connex);
		this.connexLayer.addRenderable(connex);
		this.connexLayer.firePropertyChange(AVKey.LAYER, null, this.connexLayer);
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
		if(balisesPub2D != null) {
			this.toggleLayer(balisesPub2D, true);
			balisesPub2D.removeAllBalises();
		}
		if(balisesNP2D != null) {
			this.toggleLayer(balisesNP2D, true);
			balisesNP2D.removeAllBalises();
		}
		if(balisesPub3D != null) {
			this.toggleLayer(balisesPub3D, false);
			balisesPub3D.removeAllBalises();
		}
		if(balisesNP3D != null) {
			this.toggleLayer(balisesNP3D, false);
			balisesNP3D.removeAllBalises();
		}
		if(secteursLayer != null) {
			secteursLayer.removeAllAirspaces();
			this.toggleLayer(secteursLayer, true);
		} else {
			secteursLayer = new FilterableAirspaceLayer();
			secteursLayer.setName("Secteurs");
			secteursLayer.setEnableAntialiasing(true);
			this.toggleLayer(secteursLayer, true);
			this.toggleLayer(textLayer, true);
		}
		if(itisLayer != null){
			itisLayer.removeAllRenderables();
			this.toggleLayer(itisLayer, true);
			itis.clear();
		} else {
			itisLayer = new RenderableLayer();
			itisLayer.setName("Stip Itis");
			this.toggleLayer(itisLayer, true);
		}
		if(trajLayer != null){
			trajLayer.removeAllRenderables();
			this.toggleLayer(trajLayer, true);
			trajs.clear();
		} else {
			trajLayer = new RenderableLayer();
			trajLayer.setName("Stip Trajets");
			this.toggleLayer(trajLayer, true);
		}
		if(connexLayer != null){
			connexLayer.removeAllRenderables();
			this.toggleLayer(connexLayer, true);
			connexions.clear();
		} else {
			connexLayer = new RenderableLayer();
			connexLayer.setName("Stip connex");
			this.toggleLayer(connexLayer, true);
		}
		try {
			if(DatabaseManager.getCurrentStip() != null) {
				//création des nouveaux objets
				buildBalises(0);
				buildBalises(1);
				buildRoutes("F");
				buildRoutes("U");
				this.toggleLayer(secteursLayer, true);
				this.toggleLayer(textLayer, true);
				secteurs.clear();				
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
	public void highlight(int type, String text) {
		this.showObject(type, text);
		
		switch (type) {
		case ROUTES:
			Route3D airspace = (Route3D) routes3D.getRoute(text);

			this.unHighlightPrevious(airspace);

			routes2D.highlight(text);
			routes3D.highlight(text);

			//création des balises si besoin
			this.showRoutesBalises(text, false);			

			this.wwd.getView().goTo(airspace.getReferencePosition(), 1e6);

			highlight = airspace;
			break;
		case SECTEUR:
			this.unHighlightPrevious(text);
			Secteur3D secteur = secteurs.get(text+0);
			lastAttrs = secteur == null ? new BasicAirspaceAttributes() : secteur.getAttributes(); //nécessaire à cause des secteurs fictifs qui n'ont pas de dessin
			AirspaceAttributes attrs = new BasicAirspaceAttributes((AirspaceAttributes) lastAttrs);
			attrs.setOutlineMaterial(Material.YELLOW);
			this.setAttributesToSecteur(text, attrs);
			highlight = text;
			if(secteur != null) this.wwd.getView().goTo(secteur.getReferencePosition(), 1e6);
			break;
		case BALISES:
			this.createBalise(text);
			Balise2D balise = (Balise2D) balises2D.get(text);
			Balise3D balise3d = (Balise3D) balises3D.get(text);
			balise.setHighlighted(true);
			this.unHighlightPrevious(balise);
			this.unHighlightPrevious(balise3d);
			highlight = balise;
			this.wwd.getView().goTo(balise.getPosition(), 4e5);
			break;
		case ITI:
			Route2D iti = this.itis.get(text);
			if(iti != null){
				this.showObject(ITI, text);
		//		this.wwd.centerView(iti);
			}
			break;
		case TRAJET:
			Route2D trajet = this.trajs.get(text);
			if(trajet != null){
				this.showObject(TRAJET, text);
		//		this.wwd.centerView(trajet);
			}
			break;
		case CONNEXION:
			Route2D connex = this.connexions.get(text);
			if(connex != null){
				this.showObject(CONNEXION, text);
		//		this.wwd.centerView(trajet);
			}
			break;
		default:
			break;
		}
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
			} else if(highlight instanceof fr.crnan.videso3d.graphics.Balise){
				((fr.crnan.videso3d.graphics.Balise)highlight).setHighlighted(false);
				highlight = null;
			}
		}
	}

	@Override
	public int string2type(String type) {
		if(type.equals("Balise") || type.equals("Publiées") || type.equals("Balise publiée") ||
				type.equals("Non publiées") || type.equals("Balise non publiée")){
			return BALISES;
		} else if(type.equals("AWY") || type.equals("PDR") || type.equals("Routes")) {
			return ROUTES;
		} else if(type.equals("Secteurs") || type.equals("Secteur") || type.equals("Paris")
				 || type.equals("Reims") || type.equals("Aix") || type.equals("Bordeaux") || type.equals("Brest") || type.equals("Autres")){
			return SECTEUR;
		} else if(type.equals("Itis") || type.equals("Iti")) {
			return ITI;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see fr.crnan.videso3d.VidesoController#type2string(int)
	 */
	@Override
	public String type2string(int type) {
		switch (type) {
		case ROUTES:
			return "Routes";
		case SECTEUR:
			return "Secteur";
		case BALISES:
			return "Balise";
		case ITI:
			return "Iti";
		default:
			break;
		}
		return null;
	}
	
	public String toString(){
		return "Stip";
	}

	public void setBalisesLayer3D(Boolean state) {
		this.toggleLayer(balisesNP2D, !state);
		this.toggleLayer(balisesPub2D, !state);
		this.toggleLayer(balisesNP3D, state);
		this.toggleLayer(balisesPub3D, state);
	}


	public static int getNumberInitSteps() {
		return 5;
	}

	/**
	 * 
	 * @param name of the Stip sector
	 * @return All {@link VPolygon} of the sector
	 */
	public List<VPolygon> getPolygons(String name) {
		List<VPolygon> polygons = new ArrayList<VPolygon>();
		int i = 0;
		while(secteurs.containsKey(name+i)){
			polygons.add(secteurs.get(name+i));
			i++;
		}
		return polygons;
	}

	@Override
	public Collection<Object> getObjects(int type) {
		switch (type) {
		case SECTEUR:
			try {
				Statement st = DatabaseManager.getCurrentStip();
				ResultSet rs = st.executeQuery("select count(*) from secteurs");
				this.fireTaskStarts(rs.getInt(1));
				rs = st.executeQuery("select nom from secteurs");
				int i = 1;
				while(rs.next()){
					this.createSecteur(rs.getString(1));
					this.fireTaskProgress(i++);
				}
				st.close();
				return new ArrayList<Object>(secteurs.values());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			break;

		default:
			break;
		}
		return null;
	}

	@Override
	public HashMap<Integer, List<String>> getSelectedObjectsReference() {
		HashMap<Integer, List<String>> objects = new HashMap<Integer, List<String>>();
		//SECTEUR
		List<String> secteurs = new ArrayList<String>();
		for(String s : this.secteurs.keySet()){
			if(this.secteurs.get(s).isVisible()){
				secteurs.add(s.substring(0, s.length()-1));
			}
		}
		if(!secteurs.isEmpty()) objects.put(SECTEUR, secteurs);
		//BALISES
		List<String> balis = new ArrayList<String>();
		if(this.balisesNP2D.isEnabled()){
			balis.addAll(balisesNP2D.getVisibleBalisesNames());
			balis.addAll(balisesPub2D.getVisibleBalisesNames());
		} else {
			balis.addAll(balisesNP3D.getVisibleBalisesNames());
			balis.addAll(balisesPub3D.getVisibleBalisesNames());
		}
		if(!balis.isEmpty()) objects.put(BALISES, balis);
		//ROUTES
		List<String> routes = new ArrayList<String>();
		routes.addAll(routes2D.getVisibleRoutes());
		if(!routes.isEmpty()) objects.put(ROUTES, routes);
		return objects;
	}

	@Override
	public Iterable<Restorable> getSelectedObjects() {
		List<Restorable> restorables = new LinkedList<Restorable>();
		//SECTEUR
		for(Entry<String, Secteur3D> s : this.secteurs.entrySet()){
			if(s.getValue().isVisible()){
				restorables.add(s.getValue());
			}
		}
		//Balise
		if(this.balisesNP2D.isEnabled()){
			restorables.addAll(this.balisesNP2D.getVisibleBalises());
			restorables.addAll(this.balisesPub2D.getVisibleBalises());
		} else {
			restorables.addAll(this.balisesNP3D.getVisibleBalises());
			restorables.addAll(this.balisesPub3D.getVisibleBalises());
		}
		//Routes
		if(this.routes3D.isEnabled()){
			for(String r : this.routes3D.getVisibleRoutes()){
				restorables.add(this.routes3D.getRoute(r));
			}
		} else {
			for(String r : this.routes2D.getVisibleRoutes()){
				restorables.add(this.routes2D.getRoute(r));
			}
		}
		return restorables;
	}

	@Override
	public boolean areLocationsVisible(int type, String name) {
		switch(type){
		case BALISES :
			return balises2D.get(name).isLocationVisible();
		case SECTEUR :
			return secteurs.get(name+0).areLocationsVisible();
		case ROUTES :
			return routes2D.getRoute(name).areLocationsVisible();
		}
		return false;
	}

	@Override
	public void setLocationsVisible(int type, String name, boolean visible) {
		switch(type){
		case BALISES :
			balises2D.get(name).setLocationVisible(visible);
			balises3D.get(name).setLocationVisible(visible);
			break;
		case SECTEUR : 
			Integer i = 0;
			while(secteurs.containsKey(name+i.toString())){
				secteurs.get(name+i.toString()).setLocationsVisible(visible);
				i++;
			}
			break;
		case ROUTES :
			if(visible)
				showRoutesBalises(name, true);
			else{
				hideRoutesBalises(name);
			}
			routes2D.getRoute(name).setLocationsVisible(visible);
			routes3D.getRoute(name).setLocationsVisible(visible);
		}
	}

	
	
}
