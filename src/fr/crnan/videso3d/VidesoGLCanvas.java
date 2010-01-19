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
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.formats.geo.GEOReader;
import fr.crnan.videso3d.formats.geo.GEOTrack;
import fr.crnan.videso3d.formats.lpln.LPLNReader;
import fr.crnan.videso3d.formats.lpln.LPLNTrack;
import fr.crnan.videso3d.formats.opas.OPASReader;
import fr.crnan.videso3d.formats.opas.OPASTrack;
import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.globes.EarthFlatCautra;
import fr.crnan.videso3d.globes.FlatGlobeCautra;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.layers.FrontieresStipLayer;
import fr.crnan.videso3d.graphics.Radar;
import fr.crnan.videso3d.graphics.Route2D;
import fr.crnan.videso3d.graphics.Route3D;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.graphics.Route.Type;
import fr.crnan.videso3d.layers.BaliseLayer;
import fr.crnan.videso3d.layers.GEOTracksLayer;
import fr.crnan.videso3d.layers.LPLNTracksLayer;
import fr.crnan.videso3d.layers.MosaiqueLayer;
import fr.crnan.videso3d.layers.OPASTracksLayer;
import fr.crnan.videso3d.layers.Routes3DLayer;
import fr.crnan.videso3d.layers.Routes2DLayer;
import fr.crnan.videso3d.layers.TrajectoriesLayer;
import fr.crnan.videso3d.stip.Secteur;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.data.DataDescriptor;
import gov.nasa.worldwind.examples.util.LayerManagerLayer;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.layers.LatLonGraticuleLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.SkyColorLayer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.SurfaceShapeLayer;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceShape;
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
 * @version 0.6
 */
@SuppressWarnings("serial")
public class VidesoGLCanvas extends WorldWindowGLCanvas {

	/**
	 * Layer contenant les routes
	 */
	private Routes3DLayer routes3D;
	private Routes2DLayer routes2D;
	/**
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
	/**
	 * Layer pour les radars
	 */
	private SurfaceShapeLayer radarsLayer = new SurfaceShapeLayer();
	{radarsLayer.setName("Radars");}
	/**
	 * Liste des radars affichés
	 */
	private HashMap<String, SurfaceShape> radars = new HashMap<String, SurfaceShape>();
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
	 * Liste des layers Edimap
	 */
	private List<Layer> edimapLayers = new LinkedList<Layer>();
	
	/**
	 * Liste des objets affichés
	 */
	private HashMap<String, Balise2D> balises = new HashMap<String, Balise2D>();
	private Object highlight;
	private Object lastAttrs;
	private Layer lastLayer;
	private AirspaceLayer selectedAirspaces = new AirspaceLayer();
		
	/**
	 * Initialise les différents objets graphiques
	 */
	public void initialize(){		
		
		//Proxy
		Configuration.initializeProxy();
		
		this.addSelectListener(new AirspaceListener(this));		

//		//Latitudes et longitudes
		Layer latlon = new LatLonGraticuleLayer();
		latlon.setEnabled(false);
		this.getModel().getLayers().add(latlon);
//				
//		//on screen layer manager
		LayerManagerLayer layerManager = new LayerManagerLayer(this);
		layerManager.setEnabled(false); //réduit par défaut
		this.getModel().getLayers().add(0, layerManager);
						
		//mise à jour des calques de WorldWindInstalled
		firePropertyChange("step", "", "Ajout des layers installés");
		this.updateWWI();
		
		//layer d'accueil des objets séléctionnés
		this.getModel().getLayers().add(selectedAirspaces);
		
		this.toggleLayer(balisesNP, false);
		this.toggleLayer(balisesPub, false);
		this.toggleLayer(radarsLayer, true);
		
		
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
		this.getView().setEyePosition(Position.fromDegrees(47, 0, 2500e3));
		
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
			this.getModel().getLayers().addIfAbsent(layer);
			layer.setEnabled(state);
		}
	}

	/**
	 * Insère un layer suffisamment haut dans la liste pour être derrière les PlaceNames
	 * @param layer
	 */
    public void insertBeforePlacenames(Layer layer)
    {
        // Insert the layer into the layer list just before the placenames.
        int compassPosition = 0;
        LayerList layers = this.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l instanceof PlaceNameLayer)
                compassPosition = layers.indexOf(l);
        }
        layers.add(compassPosition, layer);
    }
	/**
	 * Mets à jour les layers installés dans WorldWindInstalled
	 */
	public void updateWWI(){
		//code inspired by gov.nasa.worldwind.examples.InstalledData.synchronizeLayers(Iterable<DataDescriptor> descriptors, LayerList layerList)
		
		File installLocation = null;
		for (java.io.File f : WorldWind.getDataFileStore().getLocations())
		{
			if (WorldWind.getDataFileStore().isInstallLocation(f.getPath()))
			{
				installLocation = f;
				break;
			}
		}
		java.util.List<? extends DataDescriptor> descriptors =
			WorldWind.getDataFileStore().findDataDescriptors(installLocation.getPath());		 
		
		java.util.List<DataDescriptor> installedDescriptors = new java.util.ArrayList<DataDescriptor>();
        for (DataDescriptor d : descriptors)
            installedDescriptors.add(d);

        java.util.List<Layer> uninstalledLayers = new java.util.ArrayList<Layer>();

        // Remove layers with DataDescriptors that are no longer installed, and remove DataDescriptors that
        // are already installed as a layer.
        for (Layer layer : this.getModel().getLayers())
        {
            Object o = layer.getValue("DataSourceDescriptor");
            if (o != null && o instanceof DataDescriptor)
            {
                DataDescriptor d = (DataDescriptor) o;
                // If the layer references an installed DataDesriptor, then we can eliminate it from the list
                // of new DataDescriptors.
                if (installedDescriptors.contains(d))
                    installedDescriptors.remove(d);
                // If the layer references a DataDescriptor that is no longer installed,
                // then remove that layer.
                else
                    uninstalledLayers.add(layer);
            }
        }

        // Remove layers for uninstalled DataDescriptors.
        for (Layer layer : uninstalledLayers)
            this.getModel().getLayers().remove(layer);

        // Add layers for installedDataDescriptors.
        for (DataDescriptor d : installedDescriptors) {
        	TiledImageLayer layer = new BasicTiledImageLayer(d);
			layer.setNetworkRetrievalEnabled(false);
			layer.setValue("DataSourceDescriptor", d);
			if (d.getName() != null)
				layer.setName(d.getName());
			this.insertBeforePlacenames(layer);
			layer.setEnabled(false); //don't show these layers by default 
        }
            
	}
	
	/*--------------------------------------------------------------*/
	/*---------------------- Outil de mesure -----------------------*/
	/*--------------------------------------------------------------*/
	
	public MeasureTool getMeasureTool(){
		if(measureTool == null){
			measureTool = new MeasureTool(this);
			measureTool.setController(new MeasureToolController());
			measureTool.setMeasureShapeType(MeasureTool.SHAPE_LINE);
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
	/*----------------- Gestion des layers Edimap ------------------*/
	/*--------------------------------------------------------------*/
    /**
     * Ajoute un calque Edimap à la liste. (indispensable pour pouvoir les supprimer ensuite)
     */
    public void addEdimapLayer(Layer layer){
    	edimapLayers.add(layer);
    }
    /**
     * Supprime tous les layers Edimap du globe
     */
    public void removeAllEdimapLayers(){
    	Iterator<Layer> layers = edimapLayers.iterator();
    	while(layers.hasNext()){
    		this.getModel().getLayers().remove(layers.next());
    	}
    	edimapLayers.clear();
    }
    
	/*--------------------------------------------------------------*/
	/*----------------- Gestion des balises STIP -------------------*/
	/*--------------------------------------------------------------*/
	
	/**
	 * Construit les balises Stip
	 * @param db Lien vers le gestionnaire de base de données
	 * @param publicated Balises publéies ou non
	 */
	private void buildBalises(int publicated){
		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from balises where publicated = " + publicated);
			while(rs.next()){
				Balise2D balise = new Balise2D(rs.getString("name"), Position.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude"), 100.0));
				String annotation = "Balise "+rs.getString("name") +" ("+rs.getString("definition")+")";
				int plancher = 0;
				for(int i = 1; i<= 9; i++){
					int plafond = rs.getInt("limit"+i);
					if(plafond != -1) annotation += "\nDu "+plancher+" au "+plafond+" : "+rs.getString("sect"+i);
					plancher = plafond;
				}
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
		}
		
	}
	
	public Layer getBalisesPubLayer(){
		return balisesPub;
	}
	public Layer getBalisesNPLayer(){
		return balisesNP;
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
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select secteurs.nom, secteurs.numero, cartesect.flinf, cartesect.flsup from secteurs, cartesect where secteurs.numero = cartesect.sectnum and secteurs.nom ='"+name+"'");
			Integer i = 0;
			while(rs.next()){
				Secteur3D secteur3D = new Secteur3D(name, rs.getInt("flinf"), rs.getInt("flsup"));
				Secteur secteur = new Secteur(name, rs.getInt("numero"), DatabaseManager.getCurrentStip());
				secteur.setConnectionPays(DatabaseManager.getCurrent(DatabaseManager.Type.PAYS));
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
	private void buildRoutes(String type) {
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
				while(rs.next()){
					loc.add(LatLon.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude")));
					if(rs.getString("sens").equals("+")){
						sens.add(Route3D.LEG_FORBIDDEN);
					} else {
						sens.add(Route3D.LEG_AUTHORIZED);
					}
				}
				route3D.setLocations(loc, sens);
				route3D.setName(name);
				route2D.setLocations(loc);
				route2D.setName(name);
				if(type.equals("F")){
					this.routes3D.addRouteAwy(route3D, name);
					this.routes2D.addRouteAwy(route2D, name);
				}
				if(type.equals("U")) {
					this.routes3D.addRoutePDR(route3D, name);
					this.routes2D.addRoutePDR(route2D, name);
				}
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
	 * Construit ou met à jour les objets Stip
	 * Appelé lors de l'initialisation de la vue ou lors du changement de base de données Stip
	 */
	public void buildStip() {
		firePropertyChange("step", "", "Suppression des objets 3D");
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
			this.toggleLayer(balisesPub, false);
			balisesPub.removeAllBalises();
		}
		if(balisesNP != null) {
			this.toggleLayer(balisesNP, false);
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
				//création des nuveaux objets
				firePropertyChange("step", "", "Création des balises publiées");
				this.buildBalises(0);
				firePropertyChange("step", "", "Création des balises non publiées");
				this.buildBalises(1);
				firePropertyChange("step", "", "Création des routes FIR");
				this.buildRoutes("F");
				firePropertyChange("step", "", "Création des routes UIR");
				this.buildRoutes("U");
				this.toggleLayer(secteursLayer, true);
				this.secteurs = new HashMap<String, Secteur3D>();				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.redraw();

	}

	/*--------------------------------------------------------------*/
	/*------------------ Gestion des radars      -------------------*/
	/*--------------------------------------------------------------*/
	/**
	 * Enlève tous les radars du globe
	 */
	public void removeRadars(){
		this.radars.clear();
		this.radarsLayer.removeAllRenderables();
	}
	/**
	 * Ajoute un radar sur le globe
	 * @param name
	 */
	public void addRadar(String name){
		try {
			Statement st = DatabaseManager.getCurrentExsa();
			ResultSet rs = st.executeQuery("select * from radrgener, radrtechn where radrgener.name = radrtechn.name and radrgener.name ='"+name+"'");
			if(rs.next()){
				Radar radar = new Radar(name, LatLon.fromDegrees(rs.getDouble("latitude"), rs.getDouble("longitude")), rs.getInt("portee"));
				radarsLayer.addRenderable(radar);
				radars.put(name, radar);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.redraw();
	}
	/**
	 * Enlève un radar du globe
	 * @param name
	 */
	public void removeRadar(String name){
		radarsLayer.removeRenderable(radars.get(name));
		radars.remove(name);
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
	 * @param flat Mosaique 2D ou 3D
	 */
	public void toggleMosaiqueLayer(String type, String name, Boolean toggle, Boolean flat){
		if(mosaiquesLayer.containsKey(type+name)){
			MosaiqueLayer mos = mosaiquesLayer.get(type+name);
			mos.set3D(!flat);
			this.toggleLayer(mos, toggle);
		} else {
			if(toggle){
				String annotationTitle = null;
				Boolean grille = true;
				LatLonCautra origine = null; 
				Integer width = 0;
				Integer height = 0;
				Integer size = 0; 
				int hSens = 0; 
				int vSens = 0;
				int numSens = 0;
				List<Couple<Integer, Integer>> squares = null;
				List<Couple<Double, Double>> altitudes = null;
				Boolean numbers = true;
				ShapeAttributes attr = null;
				AirspaceAttributes airspaceAttr = null;
				if(type.equals("mosaique")) {
					try {
						Statement st = DatabaseManager.getCurrentExsa();
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
						annotationTitle = "Filtrage capacitif "+name;
						grille = false;
						squares = new LinkedList<Couple<Integer,Integer>>();
						altitudes = new LinkedList<Couple<Double,Double>>();
						Statement st = DatabaseManager.getCurrentExsa();
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
									altitudes.add(new Couple<Double, Double>(rs.getInt("plancher")*30.48, rs.getInt("plafond")*30.48));
								}
								rs.next();
							} else {
								squares.add(new Couple<Integer, Integer>(i, 0));
								altitudes.add(new Couple<Double, Double>(-10.0, 660*30.48));
							}
						}
						numbers = false;
						airspaceAttr = new BasicAirspaceAttributes();
						airspaceAttr.setMaterial(Material.YELLOW);
						airspaceAttr.setOpacity(0.4);
						attr = new BasicShapeAttributes();
						attr.setInteriorMaterial(Material.YELLOW);
						attr.setInteriorOpacity(0.4);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (type.equals("dyn")){
					annotationTitle = "Filtrage dynamique "+name;
					grille = false;
					squares = new LinkedList<Couple<Integer,Integer>>();
					altitudes = new LinkedList<Couple<Double,Double>>();
					try {
						Statement st = DatabaseManager.getCurrentExsa();
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
						airspaceAttr = new BasicAirspaceAttributes();
						airspaceAttr.setMaterial(Material.YELLOW);
						airspaceAttr.setOpacity(0.4);
						grille = false;
						rs = st.executeQuery("select * from ficaafnic where abonne = '"+name+"'");
						while(rs.next()){
							squares.add(new Couple<Integer, Integer>(rs.getInt("carre"), 0));
							altitudes.add(new Couple<Double, Double>(rs.getInt("plancher")*30.48, rs.getInt("plafond")*30.48));
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (type.equals("zocc")){
					annotationTitle = "Zone d'occultation "+name;
					grille = false;
					squares = new LinkedList<Couple<Integer,Integer>>();
					altitudes = new LinkedList<Couple<Double,Double>>();
					try {
						Statement st = DatabaseManager.getCurrentExsa();
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
						airspaceAttr = new BasicAirspaceAttributes();
						airspaceAttr.setMaterial(Material.YELLOW);
						airspaceAttr.setOpacity(0.4);
						grille = false;
						rs = st.executeQuery("select * from centsczoc where zone = '"+name+"'");
						while(rs.next()){
							squares.add(new Couple<Integer, Integer>(rs.getInt("carre"), rs.getInt("souscarre")));
							altitudes.add(new Couple<Double, Double>(0.0, rs.getInt("plafond")*30.48));
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (type.equals("vvf")){
					annotationTitle = "VVF "+name;
					grille = false;
					squares = new LinkedList<Couple<Integer,Integer>>();
					altitudes = new LinkedList<Couple<Double,Double>>();
					try {
						Statement st = DatabaseManager.getCurrentExsa();
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
						airspaceAttr = new BasicAirspaceAttributes();
						airspaceAttr.setMaterial(Material.YELLOW);
						airspaceAttr.setOpacity(0.4);
						grille = false;
						rs = st.executeQuery("select * from centscvvf where vvfs LIKE '%"+name+"%'");
						while(rs.next()){
							squares.add(new Couple<Integer, Integer>(rs.getInt("carre"), rs.getInt("souscarre")));
							//récupérer plancher plafond correspondants
							String[] vvfs = rs.getString("vvfs").split("\\\\");
							int numVVF = 0;
							for(int i=0;i<vvfs.length;i++){
								if(vvfs[i].equals(name)) numVVF = i;
							}
							double plancher = new Double(rs.getString("planchers").split("\\\\")[numVVF])*30.48;
							double plafond = new Double(rs.getString("plafonds").split("\\\\")[numVVF])*30.48;
							altitudes.add(new Couple<Double, Double>(plancher, plafond));//TODO gérer les VVF multiples
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else if (type.equals("stpv")){
					try {
						Statement st = DatabaseManager.getCurrentStpv();
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
				MosaiqueLayer mLayer = new MosaiqueLayer(annotationTitle, grille, origine, width, height, size, hSens, vSens, numSens, squares, altitudes, numbers, attr, airspaceAttr);
				mosaiquesLayer.put(type+name, mLayer);
				mLayer.setName("Mosaïque "+type+" "+name);
				mLayer.set3D(!flat);
				this.toggleLayer(mLayer, toggle);
			}
		}
	}
	
	public void toggleMosaique2D(Boolean flat){
		Iterator<MosaiqueLayer> iterator = mosaiquesLayer.values().iterator();
		while(iterator.hasNext()){
			iterator.next().set3D(!flat);
		}
	}
	
	/**
	 * Supprime toutes les mosaiques de la vue
	 */
	public void removeMosaiques(){
		Iterator<MosaiqueLayer> iterator = mosaiquesLayer.values().iterator();
		while(iterator.hasNext()){
			this.toggleLayer(iterator.next(), false);
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
		if(text.trim().isEmpty()){
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
				Statement st = DatabaseManager.getCurrentStip();
				//on recherche le type
				ResultSet rs = st.executeQuery("select * from routes where routes.name = '"+text+"'");
				if(rs.next()){
					Route3D airspace;
					if(rs.getString("espace").equals("F")){
						airspace = routes3D.getRouteAwy(text);
					} else {
						airspace = routes3D.getRoutePDR(text);
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
					lastAttrs = airspace == null ? new BasicAirspaceAttributes() : airspace.getAttributes(); //nécessaire à cause des secteurs fictifs qui n'ont pas de dessin
					AirspaceAttributes attrs = new BasicAirspaceAttributes((AirspaceAttributes) lastAttrs);
					attrs.setOutlineMaterial(Material.YELLOW);
					this.setAttributesToSecteur(text, attrs);
					highlight = text;
					if(airspace != null) this.getView().goTo(airspace.getReferencePosition(), 1e6);
					return;
				}
				rs = st.executeQuery("select * from balises where name = '"+text+"'");
				if(rs.next()){
					Balise2D airspace = (Balise2D) balises.get(text);
					airspace.highlight(true);
					this.unHighlightPrevious(airspace);
					highlight = airspace;
					if (rs.getInt("publicated") == 1 && !balisesPub.isEnabled()) {
						lastLayer = balisesPub;
						this.toggleLayer(balisesPub, true);
					}
					if (rs.getInt("publicated") == 0 && !balisesNP.isEnabled() ) {
						lastLayer = balisesNP;
						this.toggleLayer(balisesNP, true);
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

	/**
	 * Ajoute les trajectoires à la vue
	 * @param file Fichier contenant les trajectoires
	 */
	public TrajectoriesLayer addTrajectoires(File file){
		if(OPASReader.isOpasFile(file)){
			return this.addTrajectoires(new OPASReader(file));
		} else if(GEOReader.isGeoFile(file)){
			return this.addTrajectoires(new GEOReader(file));
		} else if(LPLNReader.isLPLNFile(file)) {
			return this.addTrajectoires(new LPLNReader(file));
		}
		return null;
	}
	
	private TrajectoriesLayer addTrajectoires(LPLNReader lpln){
		LPLNTracksLayer trajLayer = new LPLNTracksLayer();
		this.toggleLayer(trajLayer, true);
		for(LPLNTrack track : lpln.getTracks()){
			trajLayer.addTrack(track);
		}
		return trajLayer;
	}
	
	/**
	 * Ajoute les trajectoires au format Elvira GEO
	 * @param geo
	 * @return {@link Layer}
	 */
	private TrajectoriesLayer addTrajectoires(GEOReader geo) {
		GEOTracksLayer trajLayer = new GEOTracksLayer();
		this.toggleLayer(trajLayer, true);

		for(GEOTrack track : geo.getTracks()){
			trajLayer.addTrack(track);
		}
		return trajLayer;
	}

	/**
	 * Ajoute les trajectoires au format OPAS
	 * @param opasReader
	 */
	public TrajectoriesLayer addTrajectoires(OPASReader opas) {
		OPASTracksLayer trajLayer = new OPASTracksLayer();
		this.toggleLayer(trajLayer, true);
		for(OPASTrack track : opas.getTracks()){
			trajLayer.addTrack(track);
		}
		return trajLayer;
	}

	/**
	 * Nombre d'étapes de l'initialisation (utile pour le splashscreen
	 * @return int
	 */
	public int getNumberInitSteps() {
		return 6;
	}
}
