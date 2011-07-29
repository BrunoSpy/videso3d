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

package fr.crnan.videso3d.aip;

import java.awt.Color;
import java.awt.Point;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jdom.Element;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.MultiValueMap;
import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.ProgressSupport;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.aip.AIP.Altitude;
import fr.crnan.videso3d.aip.RoutesSegments.Segment;
import fr.crnan.videso3d.graphics.Aerodrome;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.Balise3D;
import fr.crnan.videso3d.graphics.Route;
import fr.crnan.videso3d.graphics.Route.Sens;
import fr.crnan.videso3d.graphics.MarqueurAerodrome;
import fr.crnan.videso3d.graphics.PisteAerodrome;
import fr.crnan.videso3d.graphics.Route2D;
import fr.crnan.videso3d.graphics.Route3D;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.graphics.VPolygon;
import fr.crnan.videso3d.graphics.VidesoAnnotation;
import fr.crnan.videso3d.graphics.VidesoObject;
import fr.crnan.videso3d.layers.AirportLayer;
import fr.crnan.videso3d.layers.Balise2DLayer;
import fr.crnan.videso3d.layers.Balise3DLayer;
import fr.crnan.videso3d.layers.FilterableAirspaceLayer;
import fr.crnan.videso3d.layers.Routes2DLayer;
import fr.crnan.videso3d.layers.Routes3DLayer;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;

/**
 * Contrôle l'affichage et la construction des éléments AIP
 * @author A. Vidal
 * @author Bruno Spyckerelle
 * @version 0.4.2
 */
public class AIPController extends ProgressSupport implements VidesoController {

	private VidesoGLCanvas wwd;
	private AIP aip = new AIP();
	
	private FilterableAirspaceLayer zonesLayer = new FilterableAirspaceLayer();
	{zonesLayer.setName("Zones");
	zonesLayer.setEnableAntialiasing(true);}	
	
	/**
	 * Layer contenant les routes
	 */
	private Routes2DLayer routes2D;
	private Routes3DLayer routes3D;
	
	private Balise2DLayer navFixLayer;
	private Balise3DLayer navFixLayer3D;

	private AirportLayer arptLayer;
	
	private HashMap<String, Secteur3D> zones;
	
	private MultiValueMap<String, String> volumesSecteursCTL = new MultiValueMap<String, String>();
	
	private HashSet<String> balises;
	
	private HashMap<String, GlobeAnnotation> routesAnnotations;
	
	private RoutesSegments routesSegments = new RoutesSegments();
	
	private Annotation lastSegmentAnnotation;
	
	private Balise2D lastHighlighted;
	private Annotation lastNavFixAnnotation;
	
	private String routeEnCreation = "";
	
	public AIPController(VidesoGLCanvas wwd) {
		this.wwd = wwd;
		this.buildAIP();
	}

	
	private void buildAIP() {
		this.wwd.firePropertyChange("step", "", "Création des volumes AIP");
		if(zonesLayer != null) {
			zonesLayer.removeAllAirspaces();
			this.toggleLayer(zonesLayer, true);
		} else {
			zonesLayer = new FilterableAirspaceLayer();
			zonesLayer.setName("Zones");
			zonesLayer.setEnableAntialiasing(true);
			this.toggleLayer(zonesLayer, true);
		}
		try {
			if(DatabaseManager.getCurrentAIP() != null) {
				//création des nouveaux objets
				zones = new HashMap<String, Secteur3D>();	
				balises = new HashSet<String>();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		this.wwd.firePropertyChange("step", "", "Création des routes AIP");
		//Layers pour les routes
		if(routes3D != null) {
			routes3D.removeAllAirspaces();
		}else{
			routes3D = new Routes3DLayer("Routes AIP 3D");
			this.toggleLayer(routes3D, false); //affichage en 2D par défaut
		}
		if(routes2D != null){
			routes2D.removeAllRenderables();
		} else {
			routes2D = new Routes2DLayer("Routes AIP 2D");
			this.toggleLayer(routes2D, true);
		}
		routesAnnotations = new HashMap<String, GlobeAnnotation>();
		this.wwd.firePropertyChange("step", "", "Création des balises AIP");
		if(navFixLayer != null){
			navFixLayer.removeAllBalises();
		}else{
			navFixLayer = new Balise2DLayer("NavFix AIP");	
			this.toggleLayer(navFixLayer, true);
		}
		if(navFixLayer3D != null){
			navFixLayer3D.removeAllBalises();
		}else{
			navFixLayer3D = new Balise3DLayer("NavFix AIP");	
			this.toggleLayer(navFixLayer3D, false);
		}
		if(arptLayer != null){
			arptLayer.removeAllAirports();
			this.toggleLayer(arptLayer, true);
		}else{
			arptLayer = new AirportLayer("Aérodromes AIP");
			this.toggleLayer(arptLayer, true);
		}
		this.wwd.redraw();
	}


	
	
	@Override
	public void unHighlight(int type, String name) {
	}
	@Override
	public void addLayer(String name, Layer layer) {
	}
	@Override
	public void removeLayer(String name, Layer layer) {
	}
	@Override
	public void removeAllLayers() {
		this.wwd.removeLayer(routes2D);
		this.wwd.removeLayer(routes3D);
		this.wwd.removeLayer(navFixLayer);
		this.wwd.removeLayer(navFixLayer3D);
		this.wwd.removeLayer(zonesLayer);
		this.wwd.removeLayer(arptLayer);
	}
	
	

	public FilterableAirspaceLayer getZonesLayer(){
		return zonesLayer;
	}
	
	public Routes2DLayer getRoutes2DLayer(){
		return routes2D;
	}
	
	public Routes3DLayer getRoutes3DLayer(){
		return routes3D;
	}
	
	public RoutesSegments.Route getSegmentsOfRoute(int pkRoute){
		return routesSegments.getSegmentOfRoute(pkRoute);
	}
	
	@Override
	public void toggleLayer(Layer layer, Boolean state) {
		this.wwd.toggleLayer(layer, state);
	}
	
	@Override
	public void showObject(int type, String name) {
		if(type>=AIP.AWY && type<AIP.DMEATT){
			this.showRoute(name,type);
		}else if(type == AIP.CTL){
			// si c'est de type CTL, il se peut qu'il y ait plusieurs volumes correspondant à un seul secteur
			// donc on va chercher les différents morceaux avec getCTLSecteurs et on les ajoute tous.
			name = name.split(" ")[0].trim();
			List<String> volumesSecteur = volumesSecteursCTL.get(name);
			if(volumesSecteur==null){
				ArrayList<String> volumesSect = getCTLSecteurs(name);
				volumesSecteursCTL.put(name, volumesSect);
				for(String nomVolumeSecteur : volumesSect){
					this.addZone(type, nomVolumeSecteur);
				}
			} else {
				for(String nomVolumeSecteur : volumesSecteur){
					this.addZone(type, nomVolumeSecteur);
				}
			}
		}else if(type>=AIP.DMEATT && type<AIP.AERODROME){
			this.showNavFix(type, name);
		}else if(type>=AIP.AERODROME){
			this.showAerodrome(type, name);
		}else{
			this.addZone(type,name);
		}
		//synchroniser la vue si l'appel n'a pas été fait par la vue
		DatasManager.getView(Type.AIP).showObject(type, name);
	}

	@Override
	public void hideObject(int type, String name) {
		this.wwd.getView().stopMovement();
		if(type>=AIP.AWY && type<AIP.DMEATT){
			this.removeRoute(type, name);
		}else if(type>=AIP.DMEATT && type<AIP.AERODROME){
			this.removeNavFix(type, name);
		}else if(type>=AIP.AERODROME){
			this.removeAerodrome(type, name);
		}else if(type == AIP.CTL){
			// si c'est de type CTL, il se peut qu'il y ait plusieurs volumes correspondant à un seul secteur
			// donc on va chercher les différents morceaux avec getCTLSecteurs et on les enlève tous.
			name = name.split(" ")[0].trim();
			List<String> volumesSecteur = volumesSecteursCTL.get(name);
			if(volumesSecteur!=null){
				for(String nomVolumeSecteur : volumesSecteur){
					this.hideZone(type, nomVolumeSecteur);
				}
			}
		}else{
			this.hideZone(type,name);
		}
		//synchroniser la vue si l'appel n'a pas été fait par la vue
		DatasManager.getView(Type.AIP).hideObject(type, name);
	}

	
	public int string2type(String type){
		return AIP.string2type(type);
	}


	private void addZone(int type, String name) {
		Secteur3D zone = this.zones.get(type+" "+name);
		if(zone==null){
			this.createZone(type, name);
			zone = this.zones.get(type+" "+name);
		}
		if(zone!=null){
			zone.setVisible(true);
			this.zonesLayer.firePropertyChange(AVKey.LAYER, null, this.zonesLayer);	
		}
	}



	private void createZone(int type, String name){
		if(!this.zones.containsKey(type+" "+name)){
			Color couleurZone=null;
			switch(type){
			case AIP.TSA:
				couleurZone=Color.orange;
				break;
			case AIP.SIV:
				couleurZone=Pallet.SIVColor;
				break;
			case AIP.CTR:
				couleurZone=Pallet.CTRColor;
				break;
			case AIP.TMA:
				couleurZone=Pallet.TMAColor;
				break;
			case AIP.P:
				couleurZone=Color.red;
				break;
			case AIP.R:
				couleurZone=Color.red;
				break;
			case AIP.D:
				couleurZone=Color.red;
				break;
			case AIP.FIR:
				couleurZone=Pallet.FIRColor;
				break;
			case AIP.UIR:
				couleurZone=Pallet.UIRColor;
				break;
			case AIP.LTA:
				couleurZone=Pallet.LTAColor;
				break;
			case AIP.UTA:
				couleurZone=Pallet.UTAColor;
				break;
			case AIP.CTA:
				couleurZone=Pallet.CTAColor;
				break;
			case AIP.CTL:
				couleurZone=Pallet.CTLColor;
				break;
			case AIP.Pje:
				couleurZone=Pallet.defaultColor;
				break;
			case AIP.Aer:
				couleurZone=Pallet.defaultColor;
				break;
			case AIP.Vol:
				couleurZone=Pallet.defaultColor;
				break;
			case AIP.Bal:
				couleurZone=Pallet.defaultColor;
				break;
			case AIP.TrPla:
				couleurZone=Pallet.defaultColor;
				break;
			case AIP.OCA:
				couleurZone=Pallet.OCAColor;
				break;
			case AIP.PRN:
				couleurZone=Pallet.PRNColor;
				break;
			default: 
				break;
			}

			Element maZone = aip.findElementByName(type, name);
			Couple<Altitude,Altitude> niveaux = aip.getLevels(maZone);
			Secteur3D zone = new Secteur3D(name, niveaux.getFirst().getFL(), niveaux.getSecond().getFL(),type, DatabaseManager.Type.AIP);

			BasicAirspaceAttributes attrs = new BasicAirspaceAttributes();
			attrs.setDrawOutline(true);
			attrs.setMaterial(new Material(couleurZone));
			attrs.setOutlineMaterial(new Material(Pallet.makeBrighter(couleurZone)));
			attrs.setOpacity(0.2);
			attrs.setOutlineOpacity(0.9);
			attrs.setOutlineWidth(1.5);
			zone.setAttributes(attrs);

			zone.setAnnotation("<p><b>"+name+"</b></p>"
					+"<p>Plafond : "+niveaux.getSecond().getFullText()
					+"<br />Plancher : "+niveaux.getFirst().getFullText()+"</p>");
			Geometrie contour = new Geometrie(aip.findElement(aip.getDocumentRoot().getChild("PartieS"),maZone.getChild("Partie").getAttributeValue("pk")));
			if(contour.getLocations().isEmpty())
				return;
			zone.setLocations(contour.getLocations());
			String upperAltitudeRef, lowerAltitudeRef = null;

			if(niveaux.getFirst().getRef()==Altitude.asfc){
				lowerAltitudeRef = AVKey.ABOVE_GROUND_LEVEL;
			}else{
				//Si le plancher est SFC, on met comme référence AMSL pour éviter que 
				//les zones au-dessus de la mer ne descendent sous la surface de l'eau.
				lowerAltitudeRef = AVKey.ABOVE_MEAN_SEA_LEVEL;
			}
			if(niveaux.getSecond().getRef()==Altitude.asfc||niveaux.getSecond().getRef()==Altitude.refSFC){
				upperAltitudeRef = AVKey.ABOVE_GROUND_LEVEL;
			}else{
				upperAltitudeRef = AVKey.ABOVE_MEAN_SEA_LEVEL;
			}
			zone.setAltitudeDatum(lowerAltitudeRef, upperAltitudeRef);
			zone.setVisible(false);
			this.zonesLayer.addAirspace(zone);
			zones.put(type+" "+name, zone);
		}
	}

	private void hideZone(int type, String name) {
		Secteur3D zone = zones.get(type+" "+name);
		if(zone!=null){
			zone.setVisible(false);
			zone.getAnnotation(Position.ZERO).getAttributes().setVisible(false);
			this.zonesLayer.firePropertyChange(AVKey.LAYER, null, this.zonesLayer);
		}
	}
	
	public void showRoute(String routeName, int type){
		if(!routeEnCreation.equals(routeName)){
			routeEnCreation = routeName;
			RoutesSegments.Route route = routesSegments.getSegmentOfRoute(routeName);
			if(route==null){
				addRouteToLayer(routeName, type, true);
			}else{
				if(!route.isVisible()){
					route.setVisible(true);
					for(Segment s : route){
						routes2D.displayRoute(s.getName());
						routes3D.displayRoute(s.getName());
					}
				}
			}
			routeEnCreation = "";
		}
	}


	/**
	 * Ajoute une route identifiée par son nom et son type à la vue 3D. Si les points d'une route ne sont pas définis dans le fichier SIA,
	 * on représente la route seulement par un point situé à 48°N, 0°E .
	 * @param routeName Le nom de la route à afficher.
	 * @param type 
	 */
	public void addRouteToLayer(String routeName, int type, boolean display){
		String routeID = AIP.getID(type, routeName);
		Route.Space routeType;
		if(type == AIP.PDR){
			routeType = Route.Space.UIR;
		}else{
			routeType = Route.Space.FIR;
		}
		try {
			String previousNavFix = null;
			PreparedStatement st = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP, "select NavFix.nom from NavFix, routes where routes.pk = ? AND routes.navFixExtremite = NavFix.pk");
			st.setString(1, routeID);
			ResultSet rs = st.executeQuery();
			if(rs.next()){
				previousNavFix = rs.getString(1);
			}
			st = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP,"select segments.pk, NavFix.nom from segments, NavFix where pkRoute = ? AND NavFix.pk = segments.navFixExtremite ORDER BY sequence");
			st.setString(1, routeID);
			ResultSet segments = st.executeQuery();
			while(segments.next()){
				Element segment = aip.findElement(aip.getDocumentRoot().getChild("SegmentS"), segments.getString(1));
				String navFixExtremite = segments.getString(2);
				String segmentName = buildSegmentName(routeName, segment.getChildText("Sequence"));
				if(routes2D.getRoute(segmentName)==null){
					if(!segment.getChildText("Circulation").equals("(XxX)")){
						routesSegments.addSegment(segmentName, previousNavFix, navFixExtremite, routeName, Integer.parseInt(routeID), display);
						previousNavFix = navFixExtremite;
						Couple<Altitude,Altitude> altis = aip.getLevels(segment);
						ArrayList<LatLon> loc = new ArrayList<LatLon>();
						Geometrie geometrieSegment = new Geometrie(segment);
						loc.addAll(geometrieSegment.getLocations());
						if(loc.get(0)==null){
							loc.clear();
							loc.add(LatLon.fromDegrees(48, 0));
						}
						
						Route2D segment2D = new Route2D(segmentName, routeType, Type.AIP, AIP.AWY);
						if(routeType == Route.Space.UIR){
							Sens sens = null;
							String sensString = segment.getChildText("Circulation");
							if(sensString.equals("(2=1)") || sensString.equals("(1=2)") || sensString.equals("(0=0)")){
								sens = Sens.RED;
							}else if(sensString.equals("(X-1)") || sensString.equals("(1-X)")){
								sens = Sens.GREEN;
							}else if(sensString.equals("(X-2)") || sensString.equals("(2-X)") || sensString.equals("(X-0)")){
								sens = Sens.BLUE;
							}
							segment2D.setSens(sens);
						}
						segment2D.setLocations(loc);
						segment2D.setAnnotation("<html>Route "+segmentName+"<br/><b>Plancher :</b>"+altis.getFirst().getFullText()
								+"<br/><b>Plafond :</b>"+altis.getSecond().getFullText()+"</html>");
						routes2D.addRoute(segment2D, segmentName);

						//TODO prendre en compte le sens de circulation pour les routes 3D... 
						Route3D segment3D = new Route3D(segmentName, routeType, Type.AIP, AIP.AWY);
						segment3D.setLocations(loc);
						boolean lowerTerrainConformant = false, upperTerrainConformant = false;
						if(altis.getFirst().isTerrainConforming()){
							lowerTerrainConformant = true;
						}
						if(altis.getSecond().isTerrainConforming()){
							upperTerrainConformant=true;
						}
						segment3D.setTerrainConforming(lowerTerrainConformant, upperTerrainConformant);
						segment3D.setAltitudes(altis.getFirst().getMeters(), altis.getSecond().getMeters());	
						segment3D.setAnnotation("<html>Route "+segmentName+"<br/><b>Plancher :</b>"+altis.getFirst().getFullText()
								+"<br/><b>Plafond :</b>"+altis.getSecond().getFullText()+"</html>");
						routes3D.addRoute(segment3D, segmentName);
						
						if(display){
							routes3D.displayRoute(segmentName);
							routes2D.displayRoute(segmentName);
						}
					}
				}
			}
		
//			if(segmentsEmpty){
//				new JOptionPane("<html><b>Le tracé de la route <font color=\"red\">"+routeName+"</font> est inconnu !</b></html>", 
//						JOptionPane.INFORMATION_MESSAGE).createDialog("Route "+routeName).setVisible(true);
//			}
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	



	private void removeRoute(int type, String routeName){
		//TODO problème avec les routes qui ont le même nom (J 22 et J 22 Polynésie...) : quand on met l'annotation dans le hashmap 
		//on ne connaît pas le territoire, donc quand on affiche les deux J 22 en même temps, on ne garde qu'une seule des deux annotations
		//dans le hashmap. Du coup on ne peut plus enlever l'autre.
		String routeID = AIP.getID(type, routeName);
		GlobeAnnotation routeAnnotation = routesAnnotations.get(routeName.split("-")[0].trim());
		if(routeAnnotation != null)
			routeAnnotation.getAttributes().setVisible(false);
		RoutesSegments.Route route = routesSegments.getSegmentOfRoute(routeName);
		if(route!=null){
			if(route.isVisible()){
				route.setVisible(false);
				for(Segment s : route){
					routes2D.hideRoute(s.getName());
					routes3D.hideRoute(s.getName());
				}
			}
			if(route.areNavFixsVisible()){
				displayRouteNavFixs(routeID, false);
			}
		}
	}

	
	public void displayRouteNavFixs(String pkRoute, boolean display){
		List<Couple<String,String>> navFixExtremites = new ArrayList<Couple<String,String>>();
		try {
			PreparedStatement st = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP, "select nom, type from NavFix, segments where segments.pkRoute = ? AND segments.navFixExtremite = NavFix.pk");
			st.setString(1, pkRoute);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				navFixExtremites.add(new Couple<String,String>(rs.getString(1), rs.getString(2)));
			}
			st = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP, "select NavFix.nom, NavFix.type from NavFix, routes where routes.pk = ? AND routes.navFixExtremite = NavFix.pk");
			st.setString(1, pkRoute);
			rs = st.executeQuery();
			if(rs.next()){
				navFixExtremites.add(new Couple<String,String>(rs.getString(1), rs.getString(2)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(display){
			for(Couple<String,String> navFix : navFixExtremites){			
				showObject(AIP.string2type(navFix.getSecond()), navFix.getFirst());
			}
			routesSegments.getSegmentOfRoute(Integer.parseInt(pkRoute)).setNavFixsVisible(true);
		}else{
			for(Couple<String,String> navFix : navFixExtremites){			
				hideObject(AIP.string2type(navFix.getSecond()), navFix.getFirst());
			}
			routesSegments.getSegmentOfRoute(Integer.parseInt(pkRoute)).setNavFixsVisible(false);
		}
	}
	
	
	private void showNavFix(int type, String name){
		if(!balises.contains(type+" "+name)){
			double latitude = 0;
			double longitude = 0;
			String typeString = "";
			double freq = 0;
			PreparedStatement ps;
			try {
				ps = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP, "select lat, lon, type, frequence from NavFix where nom = ? and type = ?");
				ps.setString(1, name);
				ps.setString(2, AIP.type2String(type));
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					latitude = rs.getDouble(1);
					longitude = rs.getDouble(2);
					typeString = rs.getString(3);
					freq = rs.getDouble(4);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			Balise2D navFix = new Balise2D(name, Position.fromDegrees(latitude, longitude), Type.AIP, type);
			String annotation = "<html><b>"+name+"</b><br/><i>Type : </i>"+typeString;
			if(freq != 0){
				annotation += "<br/><i>Fréq. : </i>"+freq;
			}
			annotation += "</html>";
			navFix.setAnnotation(annotation);
						
			navFixLayer.addBalise(navFix);
			navFixLayer.showBalise(navFix);
			
			Balise3D navFix3D = new Balise3D(name, Position.fromDegrees(latitude, longitude, 665*30.47), Type.AIP, type);
			navFix3D.setAnnotation(annotation);
			navFixLayer3D.addBalise(navFix3D);
			navFixLayer3D.showBalise(navFix3D);
			
			balises.add(type+" "+name);
		} else {
			navFixLayer.showBalise(name, type);
			navFixLayer3D.showBalise(name, type);
		}
	}
	
	private void removeNavFix(int type, String name){
		if(balises.contains(type+" "+name)){
			Balise2D navFix = navFixLayer.getBalise(name, type);
			navFixLayer.hideBalise(navFix);
			
			navFixLayer3D.hideBalise(name, type);
			
			this.wwd.getAnnotationLayer().removeAnnotation(navFix.getAnnotation(null));
		}
	}
	
	

	private void showAerodrome(int type, String nom){
		if( arptLayer.containsAirport(nom)){	
			ArrayList<Aerodrome> aerodromes = arptLayer.getAirport(nom);	
			for(Aerodrome ad : aerodromes){
				arptLayer.addAirport(ad);
			}
		}else{
			int pk = -1;
			double latRef =0, lonRef=0;

			ResultSet rs;
			PreparedStatement ps;
			try{
				if(type == AIP.AERODROME){
					ps = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP, "select pk, latRef, lonRef from aerodromes where upper(code) = ?");
					ps.setString(1, nom.split("--")[0].trim());
					rs = ps.executeQuery();

				}else{
					ps = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP, "select pk, latRef, lonRef from aerodromes where nom = ?");
					ps.setString(1, nom);
					rs = ps.executeQuery();
				}
				if(rs.next()){
					pk = rs.getInt(1);
					latRef = rs.getDouble(2);
					lonRef = rs.getDouble(3);
				}

				ps = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP, "select * from runways where pk_ad=?");
				ps.setInt(1, pk);
				ResultSet rs2 = ps.executeQuery();
				boolean runwayExists = false;
				while(rs2.next()){
					runwayExists = true;
					String nomPiste = rs2.getString("nom");
					if(rs2.getInt("largeur")>0 && rs2.getDouble("lat1")!=0){
						double lat1 = rs2.getDouble("lat1");
						double lon1 = rs2.getDouble("lon1");
						double lat2 = rs2.getDouble("lat2");
						double lon2 = rs2.getDouble("lon2");
						double largeur = rs2.getDouble("largeur");
						PisteAerodrome piste = new PisteAerodrome(type, nom, nomPiste, lat1, lon1, lat2, lon2, largeur, Position.fromDegrees(latRef, lonRef), Type.AIP);
						arptLayer.addAirport(piste);
					}else{
						MarqueurAerodrome airportBalise = new MarqueurAerodrome(type, nom, Position.fromDegrees(latRef, lonRef), nomPiste, DatabaseManager.Type.AIP);
						arptLayer.addAirport(airportBalise);
					}
				}
				if(!runwayExists){
					MarqueurAerodrome airportBalise = new MarqueurAerodrome(type, nom, Position.fromDegrees(latRef, lonRef), "Pistes inconnues", DatabaseManager.Type.AIP);
					arptLayer.addAirport(airportBalise);
				}
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
	}
	
	
	private void removeAerodrome(int type, String nom){
		arptLayer.hideAirport(nom);
	}
	
	
	
	

	@Override
	public void set2D(Boolean flat) {		
	}

	@Override
	public void reset() {
		for(Secteur3D s : zones.values()){
			s.setVisible(false);
		}
		this.routes2D.hideAllRoutes();
		this.routes3D.hideAllRoutes();
		this.navFixLayer.removeAllBalises();
		this.navFixLayer3D.removeAllBalises();
		this.arptLayer.removeAllAirports();
	}

	
	public AIP getAIP(){
		return aip;
	}

	
	/**
	 * Centre la vue sur la zone identifiée par name et affiche l'annotation associée 
	 * (ou les annotations si c'est un secteur en plusieurs morceaux)
	 * @param name
	 */
	@Override
	public void highlight(int type, String name) {
		this.showObject(type, name);
	 	if(type == AIP.CTL){
			highlightCTL(name);
			//Si le type est supérieur à 20 et inférieur à 30, c'est une route
		}else if(type>=20 && type <30){
			highlightRoute(type, name);
			//Si le type est supérieur ou égal à 30 et inférieur à 40, c'est une balise
		}else if(type>=30 && type <40){
			highlightNavFix(type, name);
			//Si le type est supérieur ou égal à 40, c'est un aérodrome
		}else if(type>=40){
			highlightAirport(type,name);
			//Sinon c'est un volume
		}else{
			this.addZone(type, name);
			Secteur3D zone = zones.get(type+" "+name);
			this.centerView(zone);
		}
	}
	

	
	
	/**
	 * Centre la vue sur une route ou un secteur3D, avec le niveau de zoom approprié, et affiche l'annotation associée.
	 * @param zone
	 * @return La position sur laquelle la vue est centrée.
	 */
	public Position centerView(Object object){
		Position centerPosition = this.wwd.centerView(object);		
		if(centerPosition!=null)
			showAnnotation(object, centerPosition);
		return centerPosition;
	}
	
	/**
	 * Affiche une annotation pour l'objet obj à la position pos 
	 * (pour l'instant, seuls les secteurs3D et les routes2D sont pris en charge).
	 * @param obj
	 * @param pos
	 */
	@SuppressWarnings("unchecked")
	public void showAnnotation(Object obj, Position pos){
		if(obj instanceof VidesoObject){
			this.wwd.getAnnotationLayer().addAnnotation(((VidesoObject)obj).getAnnotation(pos));
		}else if(obj instanceof List){
			if(((List<?>)obj).get(0) instanceof Route){
				String routeName = ((List<? extends Route>)obj).get(0).getName().split("-")[0].trim();
				GlobeAnnotation routeAnnotation = new VidesoAnnotation(routeName, pos);
				routeAnnotation.getAttributes().setLeaderGapWidth(10);
				routeAnnotation.getAttributes().setDrawOffset(new Point(20,20));
				routesAnnotations.put(routeName, routeAnnotation);
				this.wwd.getAnnotationLayer().addAnnotation(routeAnnotation);
			}
		}
	}
	
	
	/**
	 * Centre la vue sur le premier morceau du secteur de contrôle, et affiche les annotations de tous les morceaux.
	 * @param names les noms des différents morceaux correspondant à un secteur.
	 */
	private void highlightCTL(String name){
		String nomSecteur = name.split(" ")[0].trim();
		List<String> names = volumesSecteursCTL.get(nomSecteur);
		//on construit le secteur s'il n'existe pas encore
//		for(String n : names){
//			this.addZone(AIP.CTL, n);
//		}
		//puis on le centre dans la vue
		Position center = centerView(zones.get(AIP.CTL+" "+names.get(0)));
		if(names.size()>1){
			for(int i = 1; i<names.size(); i++){
				Position otherPosition = new Position(center.latitude.addDegrees(i*0.2), center.longitude.addDegrees(-i*0.2), center.elevation);
				this.wwd.getAnnotationLayer().addAnnotation(zones.get(AIP.CTL+" "+names.get(i)).getAnnotation(otherPosition));
			}
		}
	}

	
	/**
	 * Renvoie les noms des morceaux de secteur correspondant au secteur name.
	 * @param name
	 * @return
	 */
	private ArrayList<String> getCTLSecteurs(String name){
		ArrayList<String> names = new ArrayList<String>();
			try{
				Statement st = DatabaseManager.getCurrentAIP();
				ResultSet rs = st.executeQuery("select nom from volumes where type ='CTL' and (nom LIKE '"+name+" %' OR nom ='"+name+"')");
				while(rs.next()){
					names.add(rs.getString(1));
				}
				st.close();
			}catch(SQLException e){
				e.printStackTrace();
			}
		return names;
	}
	
	
	private void highlightAirport(int type, String name){
		centerView(arptLayer.getAirport(name).get(0));
	}
	
	private void highlightNavFix(int type, String name){
		Balise2D navFix = navFixLayer.getBalise(name, type);
		navFix.highlight(true);
		if(lastHighlighted!=null){
			lastHighlighted.highlight(false);
		}
		if(lastNavFixAnnotation!=null){
			lastNavFixAnnotation.getAttributes().setVisible(false);
		}
		lastHighlighted = navFix;
		lastNavFixAnnotation = navFix.getAnnotation(null);
		centerView(navFix);
	}
	
	
	
	
	/**
	 * Centre la vue sur la route définie par les segments passés en paramètre.
	 * @param segmentsNames Les noms des segments de la route
	 */
	private void highlightRoute(int type, String name){
		RoutesSegments.Route route = routesSegments.getSegmentOfRoute(name);
		if(route != null){
			ArrayList<Route2D> segments2D = new ArrayList<Route2D>();
			ArrayList<Route3D> segments3D = new ArrayList<Route3D>();
			for(Segment s : route){
				segments2D.add((Route2D) routes2D.getRoute(s.getName()));
				segments3D.add((Route3D) routes3D.getRoute(s.getName()));
			}
			centerView(segments2D);
		}
	}



	private String buildSegmentName(String routeName, String sequence){
		return routeName.concat(" - ").concat(sequence);
	}
	
	
	public String getRouteIDFromSegmentName(String segmentName, String typeRoute) throws SQLException{
		String route = segmentName.split("-")[0].trim();
		
		String pkRoute = null;
		
		PreparedStatement st = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP, "select pk from routes where nom = ? AND type = ?");
		st.setString(1, route);
		st.setString(2, typeRoute);
		ResultSet rs = st.executeQuery();
		if(rs.next()){
			pkRoute=rs.getString(1);
		}else{
			st = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP, "select pk from routes where nom LIKE ? AND type = ?");
			st.setString(1, route+" -%");
			st.setString(2, typeRoute);
			rs = st.executeQuery();
			if(rs.next())
				pkRoute=rs.getString(1);
		}
		return pkRoute;
	}
	
	
	/**
	 * Renvoie le segment précédent (par le numéro de séquence) ou null s'il n'y en a pas.
	 * @param routeName Le nom de la route (et pas le nom du segment)
	 * @param segmentSequenceString le numéro de séquence  du segment
	 * @param type le type de route (AWY, PDR ou TAC)
	 * @return null si le segment 
	 */
	public Route getPrevious(String routeName, String segmentSequenceString, String type, boolean route3D){
		Route previousSegment = null;
		String pkRoute = null;
		int segmentSequence = Integer.parseInt(segmentSequenceString);
		try{
			pkRoute = getRouteIDFromSegmentName(routeName, type);
			int previousSeq = getNearSequence(pkRoute, segmentSequence, -1);
			if(previousSeq != 0){
				if(route3D){
					previousSegment = (Route3D) routes3D.getRoute(routeName.concat(" - "+previousSeq));
				}else{
					previousSegment = (Route2D) routes2D.getRoute(routeName.concat(" - "+previousSeq));
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return previousSegment;
	}
	
	/**
	 * Renvoie le segment suivant (par le numéro de séquence) ou null s'il n'y en a pas.
	 * @param routeName Le nom de la route (et pas le nom du segment)
	 * @param segmentSequenceString le numéro de séquence  du segment
	 * @param type le type de route (AWY, PDR ou TAC)
	 * @return
	 */
	public Route getNext(String routeName, String segmentSequenceString, String type, boolean route3D){
		Route nextSegment = null;
		String pkRoute = null;
		int segmentSequence = Integer.parseInt(segmentSequenceString);
		try{
			pkRoute = getRouteIDFromSegmentName(routeName, type);
			int nextSeq = getNearSequence(pkRoute, segmentSequence, 1);
			if(nextSeq != 0){
				if(route3D){
					nextSegment = (Route3D) routes3D.getRoute(routeName.concat(" - "+nextSeq));
				}else{
					nextSegment = (Route2D) routes2D.getRoute(routeName.concat(" - "+nextSeq));
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return nextSegment;
	}
	
	
	/**
	 * Renvoie le numéro de séquence précédent ou suivant (selon nextOrPrevious) pour la même route, 
	 * ou 0 s'il n'y a pas de segment précédent ou suivant.
	 * @param pkRoute l'identifiant de la route
	 * @param originalSequence le numéro de séquence de départ
	 * @param nextOrPrevious doit être >0 si on cherche la séquence suivante, <=0 si on cherche la précédente.
	 * @return Le numéro de séquence recherché ou 0.
	 * @throws SQLException
	 */
	private int getNearSequence(String pkRoute, int originalSequence, int nextOrPrevious) throws SQLException{
		PreparedStatement ps = null;
		if(nextOrPrevious>0){
			ps = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP, "select sequence from segments where pkRoute = ? ORDER BY sequence DESC");
		}else{
			ps = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP, "select sequence from segments where pkRoute = ? ORDER BY sequence ASC");
		}
		ps.setString(1, pkRoute);
		ResultSet rs = ps.executeQuery();
		int nearSeq = 0;
		if(nextOrPrevious>0){
			while(rs.next()){
				int seq = rs.getInt(1);
				if(seq > originalSequence){
					nearSeq = seq;
				}else{
					break;
				}
			}
		}else{
			while(rs.next()){
				int seq = rs.getInt(1);
				if(seq < originalSequence){
					nearSeq = seq;
				}else{
					break;
				}
			}
		}
		return nearSeq;
	}
	
	public void displayAnnotationAndGoTo(Route segment){
		Position annotationPosition = new Position(segment.getLocations().iterator().next(), 0);
		Annotation annotation = ((VidesoObject)segment).getAnnotation(annotationPosition);
		if(lastSegmentAnnotation != null)
			lastSegmentAnnotation.getAttributes().setVisible(false);
		lastSegmentAnnotation = annotation;
		annotation.getAttributes().setVisible(true);
		wwd.getAnnotationLayer().addAnnotation(annotation);
		BasicOrbitView bov = (BasicOrbitView) wwd.getView();
		bov.addPanToAnimator(annotationPosition, bov.getHeading(), bov.getPitch(), bov.getEyePosition().elevation, 2000, true);
		bov.firePropertyChange(AVKey.VIEW, null, bov);
		wwd.redraw();
	}
	
	
	public void setBalisesLayer3D(boolean threeD){
		this.toggleLayer(this.navFixLayer, !threeD);
		this.toggleLayer(this.navFixLayer3D, threeD);
	}
	
	@Override
	public String type2string(int type) {
		return AIP.type2String(type);
	}
	
	public String toString(){
		return "AIP";
	}


	public static int getNumberInitSteps() {
		return 3;
	}


	/**
	 * 
	 * @param name
	 * @return Tous les {@link VPolygon} qui forment une zone ou un secteur
	 */
	public List<VPolygon> getPolygons(int type, String name) {
		List<VPolygon> polygons = new ArrayList<VPolygon>();
		if(type == AIP.CTL){
			for(String n : this.getCTLSecteurs(name.split(" ")[0])){
				polygons.add(this.zones.get(type+" "+n));
			}
		} else {
			polygons.add(this.zones.get(type +" "+name));
		}
		return polygons;
	}


	@Override
	public Collection<Object> getObjects(int type) {
		if(type == AIP.CTL){
			try{
				Statement st = DatabaseManager.getCurrentAIP();
				ResultSet rs = st.executeQuery("select count(*) from volumes where type = 'CTL'");
				this.fireTaskStarts(rs.getInt(1));
				rs = st.executeQuery("select nom from volumes where type = 'CTL' order by nom");
				int i = 1;
				while(rs.next()){
					this.createZone(AIP.CTL, rs.getString(1));
					this.fireTaskProgress(i++);
				}
				st.close();
			} catch(SQLException e){
				e.printStackTrace();
			}
			Collection<Object> secteurs = new HashSet<Object>();
			for(Secteur3D s : this.zones.values()){
				if(s.getType() == AIP.CTL)
					secteurs.add(s);
			}
			return secteurs;
		} else if(type == AIP.TMA){
			try{
				Statement st = DatabaseManager.getCurrentAIP();
				ResultSet rs = st.executeQuery("select count(*) from volumes where type = 'TMA'");
				this.fireTaskStarts(rs.getInt(1));
				rs = st.executeQuery("select nom from volumes where type = 'TMA' order by nom");
				int i = 1;
				while(rs.next()){
					this.createZone(AIP.TMA, rs.getString(1));
					this.fireTaskProgress(i++);
				}
				st.close();
			} catch(SQLException e){
				e.printStackTrace();
			}
			Collection<Object> secteurs = new HashSet<Object>();
			for(Secteur3D s : this.zones.values()){
				if(s.getType() == AIP.TMA)
					secteurs.add(s);
			}
			return secteurs;
		} else if(type == AIP.CTR){
			try{
				Statement st = DatabaseManager.getCurrentAIP();
				ResultSet rs = st.executeQuery("select count(*) from volumes where type = 'CTR'");
				this.fireTaskStarts(rs.getInt(1));
				rs = st.executeQuery("select nom from volumes where type = 'CTR' order by nom");
				int i = 1;
				while(rs.next()){
					this.createZone(AIP.CTR, rs.getString(1));
					this.fireTaskProgress(i++);
				}
				st.close();
			} catch(SQLException e){
				e.printStackTrace();
			}
			Collection<Object> secteurs = new HashSet<Object>();
			for(Secteur3D s : this.zones.values()){
				if(s.getType() == AIP.CTR)
					secteurs.add(s);
			}
			return secteurs;
		}
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
	public HashMap<Integer, List<String>> getSelectedObjects() {
		// TODO Auto-generated method stub
		return null;
	}
}
