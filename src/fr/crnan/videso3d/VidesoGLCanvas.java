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

import java.awt.Component;
import java.awt.Cursor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ProgressMonitor;

import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.globes.EarthFlatCautra;
import fr.crnan.videso3d.globes.FlatGlobeCautra;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.layers.FrontieresStipLayer;
import fr.crnan.videso3d.graphics.Route3D;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.graphics.Route3D.Type;
import fr.crnan.videso3d.layers.BaliseMarkerLayer;
import fr.crnan.videso3d.layers.MosaiqueLayer;
import fr.crnan.videso3d.layers.RoutesLayer;
import fr.crnan.videso3d.layers.TextLayer;
import fr.crnan.videso3d.stip.Secteur;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.examples.util.LayerManagerLayer;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.LatLonGraticuleLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.SkyColorLayer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.util.UnitsFormat;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.util.measure.MeasureToolController;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwind.view.orbit.FlatOrbitView;
/**
 * Extension de WorldWindCanvas prenant en compte la création d'éléments 3D
 * @author Bruno Spyckerelle
 * @version 0.4
 */
@SuppressWarnings("serial")
public class VidesoGLCanvas extends WorldWindowGLCanvas {

	/**
	 * Layer contenant les routes
	 */
	private RoutesLayer routes;
	
	/**
	 * Layers pour les balises publiées
	 */
	private TextLayer balisesPubTexts = new TextLayer("Balises publiées");
	private BaliseMarkerLayer balisesPubMarkers = new BaliseMarkerLayer();
	/**
	 * Layers pour les balises non publiées
	 */
	private TextLayer balisesNPTexts = new TextLayer("Balises non publiées");
	private BaliseMarkerLayer balisesNPMarkers = new BaliseMarkerLayer();
	/**
	 * Layer contenant les secteurs
	 */
	private AirspaceLayer secteursLayer = new AirspaceLayer();
	{secteursLayer.setName("Secteurs");}
	/**
	 * Layer contenant les annotations
	 */
	private AnnotationLayer annotationLayer;
	/**
	 * Layer pour les frontières
	 */
	private FrontieresStipLayer frontieres;
	/**
	 * Projection 2D
	 */
	private FlatGlobeCautra flatGlobe;
	private Globe roundGlobe;
	private String projection;
	/**
	 * Outil de mesure (alidade)
	 */
	private MeasureTool measureTool;
	/**
	 * Liste des layers Mosaiques
	 */
	private HashMap<String, MosaiqueLayer> mosaiquesLayer = new HashMap<String, MosaiqueLayer>();
	/**
	 * Liste des objets affichés
	 */
	private HashMap<String, Balise2D> balises = new HashMap<String, Balise2D>();
	private Object highlight;
	private Object lastAttrs;
	private Layer lastLayer;
	private AirspaceLayer selectedAirspaces = new AirspaceLayer();
	
	
	private DatabaseManager db;
	
	/**
	 * Initialise les différents objets graphiques
	 */
	public void initialize(DatabaseManager db){
		this.db = db;
		
		this.addSelectListener(new AirspaceListener(this));		

		//Latitudes et longitudes
		Layer latlon = new LatLonGraticuleLayer();
		latlon.setEnabled(false);
		this.getModel().getLayers().add(latlon);
				
		//on screen layer manager
		LayerManagerLayer layerManager = new LayerManagerLayer(this);
		layerManager.setEnabled(false); //réduit par défaut
		this.getModel().getLayers().add(layerManager);
		
		//layer d'accueil des objets séléctionnés
		this.getModel().getLayers().add(selectedAirspaces);
		
		this.toggleLayer(balisesNPMarkers, false);
		this.toggleLayer(balisesNPTexts, false);
		this.toggleLayer(balisesPubMarkers, false);
		this.toggleLayer(balisesPubTexts, false);
		
		
		if (isFlatGlobe())
		{
			this.flatGlobe = (FlatGlobeCautra)this.getModel().getGlobe();
			this.roundGlobe = new Earth();
		}
		else
		{
			this.flatGlobe = new EarthFlatCautra();
			this.roundGlobe = this.getModel().getGlobe();
		}
		
		this.buildStip();	
		
		//position de départ centrée sur la France
		this.getView().goTo(Position.fromDegrees(47, 0, 2500e3), 25e5);
		
	}
	
	public AnnotationLayer getAnnotationLayer(){
		if(annotationLayer == null){
			annotationLayer = new AnnotationLayer();
			this.getModel().getLayers().add(annotationLayer);
		}
		return annotationLayer;
	}
	
	/**
	 * Affiche ou non un Layer<br />
	 * Ajouter le {@link Layer} aux layers du modèle si il n'en fait pas partie
	 * @param layer {@link Layer} à afficher/enlever
	 * @param state {@link Boolean}
	 */
	public void toggleLayer(Layer layer, Boolean state){
		if (layer != null) {
			if(!this.getModel().getLayers().contains(layer)){
				this.getModel().getLayers().add(layer);
			}
			layer.setEnabled(state);
		}
	}
	
	/*--------------------------------------------------------------*/
	/*---------------------- Outil de mesure -----------------------*/
	/*--------------------------------------------------------------*/
	
	public MeasureTool getMeasureTool(){
		if(measureTool == null){
			measureTool = new MeasureTool(this);
			measureTool.setController(new MeasureToolController());
			measureTool.setMeasureShape(MeasureTool.SHAPE_LINE);
			measureTool.setFollowTerrain(true);
			measureTool.setShowAnnotation(true);
			measureTool.setUnitsFormat(new UnitsFormat(UnitsFormat.NAUTICAL_MILES, UnitsFormat.SQUARE_KILOMETERS));
		}
		return measureTool;
	}
	
	public void switchMeasureTool(Boolean bool){
		this.getMeasureTool().setArmed(bool);
		//Changement du curseur
		((Component) this).setCursor(!measureTool.isArmed() ? Cursor.getDefaultCursor()
                : Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		if(!bool){
			this.getMeasureTool().clear();
		}
	}
	
	/*--------------------------------------------------------------*/
	/*----------------- Gestion des projections --------------------*/
	/*--------------------------------------------------------------*/
	
	/**
	 * Change la projection
	 * @param projection Nom de la projection (parmi {@link FlatGlobeCautra})
	 */
	public void setProjection(String projection){
		this.projection = projection;
		if(flatGlobe != null) this.flatGlobe.setProjection(projection);
		if(isFlatGlobe()) this.redraw();
	}
	
    public boolean isFlatGlobe()
    {
        return this.getModel().getGlobe() instanceof FlatGlobeCautra;
    }
	
    public String getProjection(){
    	return projection;
    }
    /**
     * Active la vue 2D
     * @param flat
     */
    public void enableFlatGlobe(boolean flat)
    {
        if(isFlatGlobe() == flat)
            return;

        if(!flat)
        {
            // Switch to round globe
            this.getModel().setGlobe(roundGlobe) ;
            // Switch to orbit view and update with current position
            FlatOrbitView flatOrbitView = (FlatOrbitView)this.getView();
            BasicOrbitView orbitView = new BasicOrbitView();
            orbitView.setCenterPosition(flatOrbitView.getCenterPosition());
            orbitView.setZoom(flatOrbitView.getZoom( ));
            orbitView.setHeading(flatOrbitView.getHeading());
            orbitView.setPitch(flatOrbitView.getPitch());
            this.setView(orbitView);
            // Change sky layer
            LayerList layers = this.getModel().getLayers();
            for(int i = 0; i < layers.size(); i++)
            {
                if(layers.get(i) instanceof SkyColorLayer)
                    layers.set(i, new SkyGradientLayer());
            }
        }
        else
        {
            // Switch to flat globe
            this.getModel().setGlobe(flatGlobe);
            flatGlobe.setProjection(this.getProjection());
            // Switch to flat view and update with current position
            BasicOrbitView orbitView = (BasicOrbitView)this.getView();
            FlatOrbitView flatOrbitView = new FlatOrbitView();
            flatOrbitView.setCenterPosition(orbitView.getCenterPosition());
            flatOrbitView.setZoom(orbitView.getZoom( ));
            flatOrbitView.setHeading(orbitView.getHeading());
            flatOrbitView.setPitch(orbitView.getPitch());
            this.setView(flatOrbitView);
            // Change sky layer
            LayerList layers = this.getModel().getLayers();
            for(int i = 0; i < layers.size(); i++)
            {
                if(layers.get(i) instanceof SkyGradientLayer)
                    layers.set(i, new SkyColorLayer());
            }
        }
        
        this.redraw();
    }
    
	/*--------------------------------------------------------------*/
	/*----------------- Gestion des frontières ---------------------*/
	/*--------------------------------------------------------------*/
	/**
	 * Affiche ou non le fond uni suivant les frontières Stip
	 * @param toggle
	 */
    public void toggleFrontieres(Boolean toggle){
			if(frontieres == null) frontieres = new FrontieresStipLayer();
			this.toggleLayer(frontieres, toggle);
	}
	
	/*--------------------------------------------------------------*/
	/*----------------- Gestion des balises STIP -------------------*/
	/*--------------------------------------------------------------*/
	
	/**
	 * Construit les balises Stip
	 * @param db Lien vers le gestionnaire de base de données
	 * @param publicated Balises publéies ou non
	 */
	private void buildBalises(DatabaseManager db, int publicated){
		try {
			Statement st = db.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from balises where publicated = " + publicated);
			while(rs.next()){
				Balise2D balise = new Balise2D(rs.getString("name"), Position.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude"), 100.0));
				if(publicated == 1){
					balise.addToLayer(balisesPubMarkers, balisesPubTexts);
				} else {
					balise.addToLayer(balisesNPMarkers, balisesNPTexts);
				}
				//lien nominal
				this.balises.put(rs.getString("name"), balise);
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
	/**
	 * Ajoute tous les {@link Secteur3D} formant le secteur <code>name</code>
	 * @param name Nom du secteur à ajouter
	 */
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
	/**
	 * Enlève tous les {@link Secteur3D} formant le secteur <code>name</code>
	 * @param name Nom du secteur à supprimer
	 */
	public void removeSecteur3D(String name){
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
	public void setAttributesToSecteur(String name, AirspaceAttributes attrs){
		Integer i = 0;
		while(secteurs.containsKey(name+i.toString())){
			secteurs.get(name+i.toString()).setAttributes(attrs);
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
				ResultSet rs = st.executeQuery("select * from routebalise, balises where route = '"+name+"' and routebalise.balise = balises.name and appartient = 1");
				LinkedList<LatLon> loc = new LinkedList<LatLon>();
				while(rs.next()){
					loc.add(LatLon.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude")));
				}
				route.setLocations(loc);
				route.setName(name);
				if(type.equals("F")) this.routes.addRouteAwy(route, name);
				if(type.equals("U")) this.routes.addRoutePDR(route, name);
			}
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public RoutesLayer getRoutesLayer(){
		return routes;
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
		if(routes != null) {
			routes.removeAllAirspaces();
		} else {
			routes = new RoutesLayer("Routes Stip");
			this.toggleLayer(routes, true);
		}
		if(balisesPubTexts != null) {
			this.toggleLayer(balisesPubTexts, false);
			balisesPubTexts.removeAllGeographicTexts();
		}
		if(balisesPubMarkers != null) {
			this.toggleLayer(balisesPubMarkers, false);
			balisesPubMarkers = new BaliseMarkerLayer();
		}
		if(balisesNPTexts != null) {
			this.toggleLayer(balisesNPTexts, false);
			balisesNPTexts.removeAllGeographicTexts();
		}
		if(balisesNPMarkers != null) {
			this.toggleLayer(balisesNPMarkers, false);
			balisesNPMarkers = new BaliseMarkerLayer();
		}
		if(secteursLayer != null) {
			secteursLayer.removeAllAirspaces();
			this.toggleLayer(secteursLayer, true);
		} else {
			secteursLayer = new AirspaceLayer();
			secteursLayer.setName("Secteurs");
			this.toggleLayer(secteursLayer, true);
		}
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
				this.toggleLayer(secteursLayer, true);
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

	/**
	 * Ajoute/enlève une mosaique
	 * @param type Type de la mosaique
	 * @param name Nom de la mosaique
	 * @param toggle Affiche si vrai
	 */
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
				} else if (type.equals("stpv")){
					try {
						Statement st = this.db.getCurrentStpv();
						ResultSet rs = st.executeQuery("select * from mosaique where type ='"+name+"'");
						origine = LatLonCautra.fromCautra(rs.getDouble("xcautra")-512, rs.getDouble("ycautra")-512);
						width = rs.getInt("nombre");
						height = rs.getInt("nombre");
						size = rs.getInt("carre");
						hSens = MosaiqueLayer.BOTTOM_UP;
						vSens = MosaiqueLayer.LEFT_RIGHT;
						numSens = MosaiqueLayer.HORIZONTAL_FIRST;
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

	/*--------------------------------------------------------------*/
	/*------------------ Gestion du highlight ----------------------*/
	/*--------------------------------------------------------------*/
	/**
	 * Centre la vue et met en valeur un objet
	 * @param text Nom de l'objet à afficher
	 */
	public void highlight(String text) {
		if(text.isEmpty()){
			if(highlight != null) {
				if((highlight instanceof Route3D) && lastAttrs != null){
					((Airspace)highlight).setAttributes((AirspaceAttributes) lastAttrs);
					selectedAirspaces.removeAllAirspaces();
				} else if (highlight instanceof String){ //cas des secteurs
					this.setAttributesToSecteur((String) highlight, (AirspaceAttributes) lastAttrs);
					this.removeSecteur3D((String) highlight);
				} else if(highlight instanceof Balise2D){
					((Balise2D) highlight).highlight(false);
					if(lastLayer != null) this.toggleLayer(lastLayer, false);
					lastLayer = null;
				}
				lastAttrs = null;
				highlight = null;
			}
		} else {
			try {
				Statement st = this.db.getCurrentStip();
				//on recherche le type
				ResultSet rs = st.executeQuery("select * from routes where routes.name = '"+text+"'");
				if(rs.next()){
					Route3D airspace;
					if(rs.getString("espace").equals("F")){
						airspace = routes.getRouteAwy(text);
					} else {
						airspace = routes.getRoutePDR(text);
					}
					this.unHighlightPrevious(airspace);
					lastAttrs = airspace.getAttributes();
					AirspaceAttributes attrs = new BasicAirspaceAttributes((AirspaceAttributes) lastAttrs);
					attrs.setMaterial(Material.YELLOW);
					attrs.setOutlineMaterial(Material.YELLOW);
					attrs.setOutlineWidth(2.0);
					airspace.setAttributes(attrs);
					highlight = airspace;
					selectedAirspaces.addAirspace((Airspace) highlight);
					this.getView().goTo(airspace.getReferencePosition(), 1e6);
					return;
				}
				rs = st.executeQuery("select * from secteurs where nom = '"+text+"'");
				if(rs.next()){
					if(!secteurs.containsKey(text+0)){
						this.addSecteur3D(text);
					}
					this.unHighlightPrevious(text);
					Secteur3D airspace = secteurs.get(text+0);
					lastAttrs = airspace.getAttributes();
					AirspaceAttributes attrs = new BasicAirspaceAttributes((AirspaceAttributes) lastAttrs);
					attrs.setOutlineMaterial(Material.YELLOW);
					this.setAttributesToSecteur(text, attrs);
					highlight = text;
					this.getView().goTo(airspace.getReferencePosition(), 1e6);
					return;
				}
				rs = st.executeQuery("select * from balises where name = '"+text+"'");
				if(rs.next()){
					Balise2D airspace = (Balise2D) balises.get(text);
					airspace.highlight(true);
					this.unHighlightPrevious(airspace);
					highlight = airspace;
					if (rs.getInt("publicated") == 1 && !balisesPubTexts.isEnabled()) {
						lastLayer = balisesPubTexts;
						this.toggleLayer(balisesPubTexts, true);
					}
					if (rs.getInt("publicated") == 0 && !balisesNPTexts.isEnabled() ) {
						lastLayer = balisesNPTexts;
						this.toggleLayer(balisesNPTexts, true);
					} 
					this.getView().goTo(airspace.getPosition(), 5e5);
					return;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void unHighlightPrevious(Object previous){
		if(highlight != null){
			if(highlight == previous) {
				return;
			} else if (highlight instanceof Route3D){
				((Route3D)highlight).setAttributes((AirspaceAttributes) lastAttrs);
				highlight = null;
				selectedAirspaces.removeAllAirspaces();
			} else if(highlight instanceof String){
				this.setAttributesToSecteur((String) highlight, (AirspaceAttributes) lastAttrs);
				this.removeSecteur3D((String) highlight);
				highlight = null;
			} else if(highlight instanceof Balise2D){
				((Balise2D)highlight).highlight(false);
				if(lastLayer != null) this.toggleLayer(lastLayer, false);
				highlight = null;
				lastLayer = null;
			}
		}
	}
}
