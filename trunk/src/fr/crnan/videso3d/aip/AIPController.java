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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.jdom.Element;


import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.aip.AIP.Altitude;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.ObjectAnnotation;
import fr.crnan.videso3d.graphics.Route;
import fr.crnan.videso3d.graphics.Route.Sens;
import fr.crnan.videso3d.graphics.Route2D;
import fr.crnan.videso3d.graphics.Route3D;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.graphics.Secteur.Type;
import fr.crnan.videso3d.layers.BaliseLayer;
import fr.crnan.videso3d.layers.Routes2DLayer;
import fr.crnan.videso3d.layers.Routes3DLayer;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.util.Logging;

/**
 * Contrôle l'affichage et la construction des éléments AIP
 * @author A. Vidal
 *
 */
public class AIPController implements VidesoController {

	private VidesoGLCanvas wwd;
	private AIP aip = new AIP();
	
	private AirspaceLayer zonesLayer = new AirspaceLayer();
	{zonesLayer.setName("Zones");
	zonesLayer.setEnableAntialiasing(true);}	
	
	/**
	 * Layer contenant les routes
	 */
	private Routes2DLayer routes2D;
	private Routes3DLayer routes3D;
	
	private BaliseLayer navFixLayer;

	
	private HashMap<String, Secteur3D> zones;
	
	private HashMap<String, GlobeAnnotation> routesAnnotations;
	
	private RoutesSegments routesSegments = new RoutesSegments();
	
	public AIPController(VidesoGLCanvas wwd) {
		this.wwd = wwd;
		this.buildAIP();
	}

	
	private void buildAIP() {
		this.wwd.firePropertyChange("step", "", "Création des volumes");
		if(zonesLayer != null) {
			zonesLayer.removeAllAirspaces();
			this.toggleLayer(zonesLayer, true);
		} else {
			zonesLayer = new AirspaceLayer();
			zonesLayer.setName("Zones");
			zonesLayer.setEnableAntialiasing(true);
			this.toggleLayer(zonesLayer, true);
		}
		try {
			if(DatabaseManager.getCurrentAIP() != null) {
				//création des nouveaux objets
				zones = new HashMap<String, Secteur3D>();				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		this.wwd.firePropertyChange("step", "", "Création des routes");
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
		this.wwd.firePropertyChange("step", "", "Création des balises");
		if(navFixLayer != null){
			navFixLayer.removeAllBalises();
		}else{
			navFixLayer = new BaliseLayer("NavFix AIP");	
			this.toggleLayer(navFixLayer, true);
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
		this.wwd.removeLayer(zonesLayer);
	}
	
	

	public AirspaceLayer getZonesLayer(){
		return zonesLayer;
	}
	
	public Routes2DLayer getRoutes2DLayer(){
		return routes2D;
	}
	
	public Routes3DLayer getRoutes3DLayer(){
		return routes3D;
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
			// donc on va chercher les différents morceaux avec getCTLSecteurs et on les affiche tous.
			for(String nomPartieSecteur : getCTLSecteurs(name)){
				if(!zones.containsKey(nomPartieSecteur))
					this.addZone(type, nomPartieSecteur);
			}
		}else if(type>=AIP.DMEATT){
			this.showNavFix(name);
		}else{
			if(!zones.containsKey(name))
				this.addZone(type,name);
		}
	}

	@Override
	public void hideObject(int type, String name) {
		this.wwd.getView().stopMovement();
		if(type>=AIP.AWY && type<AIP.DMEATT){
			this.removeRoute(type, name);
		}else if(type>=AIP.DMEATT){
			this.removeNavFix(type, name);
		}else if(type == AIP.CTL){
			// si c'est de type CTL, il se peut qu'il y ait plusieurs volumes correspondant à un seul secteur
			// donc on va chercher les différents morceaux avec getCTLSecteurs et on les enlève tous.
			for(String nomPartieSecteur : getCTLSecteurs(name)){
				this.removeZone(type,nomPartieSecteur);
			}
		}else{
			this.removeZone(type,name);
		}
	}

	
	public int string2type(String type){
		return AIP.string2type(type);
	}


	private void addZone(int type, String name) {
		Type secteur3DType=null;
		Color couleurZone=null;
		switch(type){
		case AIP.TSA:
			secteur3DType=Type.TSA;
			couleurZone=Color.orange;
			break;
		case AIP.SIV:
			secteur3DType=Type.SIV;
			couleurZone=Pallet.SIVColor;
			break;
		case AIP.CTR:
			secteur3DType=Type.CTR;
			couleurZone=Pallet.CTRColor;
			break;
		case AIP.TMA:
			secteur3DType=Type.TMA;
			couleurZone=Pallet.TMAColor;
			break;
		case AIP.R:
			secteur3DType=Type.R;
			couleurZone=Color.red;
			break;
		case AIP.D:
			secteur3DType=Type.D;
			couleurZone=Color.red;
			break;
		case AIP.FIR:
			secteur3DType=Type.FIR;
			couleurZone=Pallet.FIRColor;
			break;
		case AIP.UIR:
			secteur3DType=Type.UIR;
			couleurZone=Pallet.UIRColor;
			break;
		case AIP.LTA:
			secteur3DType=Type.LTA;
			couleurZone=Pallet.LTAColor;
			break;
		case AIP.UTA:
			secteur3DType=Type.UTA;
			couleurZone=Pallet.UTAColor;
			break;
		case AIP.CTA:
			secteur3DType=Type.CTA;
			couleurZone=Pallet.CTAColor;
			break;
		case AIP.CTL:
			secteur3DType=Type.CTL;
			couleurZone=Pallet.CTLColor;
			break;
		case AIP.Pje:
			secteur3DType=Type.Pje;
			couleurZone=Pallet.defaultColor;
			break;
		case AIP.Aer:
			secteur3DType=Type.Aer;
			couleurZone=Pallet.defaultColor;
			break;
		case AIP.Vol:
			secteur3DType=Type.Vol;
			couleurZone=Pallet.defaultColor;
			break;
		case AIP.Bal:
			secteur3DType=Type.Bal;
			couleurZone=Pallet.defaultColor;
			break;
		case AIP.TrPla:
			secteur3DType=Type.TrPla;
			couleurZone=Pallet.defaultColor;
			break;
		default: 
			break;
		}
		Element maZone = aip.findElementByName(type, name);
		Couple<Altitude,Altitude> niveaux = aip.getLevels(maZone);
		Secteur3D zone = new Secteur3D(name, niveaux.getFirst().getFL(), niveaux.getSecond().getFL(),secteur3DType);

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
		zones.put(type+" "+name, zone);
		this.addToZonesLayer(zone);
	}





	private void removeZone(int type, String name) {
		this.removeFromZonesLayer(zones.get(type+" "+name));
		zones.remove(name);
	}


	private void addToZonesLayer(Secteur3D zone){
		this.zonesLayer.addAirspace(zone);
		this.wwd.redraw();
	}
	
	private void removeFromZonesLayer(Secteur3D zone){
		try{
			this.wwd.getAnnotationLayer().removeAnnotation(zone.getAnnotation(null));
			this.zonesLayer.removeAirspace(zone);
		}catch(java.lang.IllegalArgumentException e){
			e.printStackTrace();
			Logging.logger().log(java.util.logging.Level.WARNING, "La zone à supprimer n'existe pas.");
		}
		this.wwd.redraw();
	}
	
	public void showRoute(String routeName, int type){
		LinkedList<String> segments = routesSegments.getSegmentsOfRoute(routeName);
		if(segments==null){
			addRouteToLayer(routeName, type, true);
		}else{
			if(segments.getFirst().equals("false")){
				routesSegments.setRouteVisible(routeName, true);
				segments.removeFirst();
				for(String s : segments){
					routes2D.displayRoute(s);
					routes3D.displayRoute(s);
				}
			}
		}
	}

	/**
	 * Ajoute une route identifiée par son nom et son type à la vue 3D. Si les points d'une route ne sont pas définis dans le fichier SIA,
	 * on représente la route seulement par un point situé à 48°N, 0°E .
	 * @param routeName Le nom de la route à afficher.
	 * @param type 
	 * @param display true pour afficher la route immédiatement, false sinon
	 */
	public void addRouteToLayer(String routeName, int type, boolean display){
		String routeID = AIP.getID(type, routeName);
		fr.crnan.videso3d.graphics.Route.Type routeType;
		if(type == AIP.PDR){
			routeType = fr.crnan.videso3d.graphics.Route.Type.UIR;
		}else{
			routeType = fr.crnan.videso3d.graphics.Route.Type.FIR;
		}
		try {
			Statement st = DatabaseManager.getCurrentAIP();
			ResultSet segments = st.executeQuery("select pk from segments where pkRoute = '"+routeID+"' ORDER BY sequence");
			boolean segmentsEmpty = true;
			while(segments.next()){
				segmentsEmpty = false;
				Element segment = aip.findElement(aip.getDocumentRoot().getChild("SegmentS"), segments.getString(1));
				String segmentName = buildSegmentName(routeName, segment.getChildText("Sequence"));
				if(routes2D.getRoute(segmentName)==null){
					if(!segment.getChildText("Circulation").equals("(XxX)")){
						routesSegments.addSegment(segmentName, routeName, display);
						Couple<Altitude,Altitude> altis = aip.getLevels(segment);
						LinkedList<LatLon> loc = new LinkedList<LatLon>();
						Geometrie geometrieSegment = new Geometrie(segment);
						loc.addAll(geometrieSegment.getLocations());
						if(loc.get(0)==null){
							loc.clear();
							loc.add(LatLon.fromDegrees(48, 0));
						}
						
						Route2D segment2D = new Route2D(segmentName, routeType);
						if(routeType == fr.crnan.videso3d.graphics.Route.Type.UIR){
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
						Route3D segment3D = new Route3D(segmentName, routeType);
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
							routes2D.displayRoute(segmentName);
							routes3D.displayRoute(segmentName);
						}
					}
				}
			}
			if(segmentsEmpty){
				new JOptionPane("<html><b>Les points de la route <font color=\"red\">"+routeName+"</font> sont inconnus !</b></html>", 
						JOptionPane.WARNING_MESSAGE).createDialog("Route "+routeName).setVisible(true);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	
	
	
	private void removeRoute(int type, String routeName){
		//TODO problème avec les routes qui ont le même nom (J 22 et J 22 Polynésie...) : quand on met l'annotation dans le hashmap 
		//on ne connaît pas le territoire, donc quand on affiche les deux J 22 en même temps, on ne garde qu'une seule des deux annotations
		//dans le hashmap. Du coup on ne peut plus enlever l'autre.
		if(routesAnnotations.containsKey(routeName.split("-")[0].trim())){
			routesAnnotations.get(routeName.split("-")[0].trim()).getAttributes().setVisible(false);
		}
		LinkedList<String> segments = routesSegments.getSegmentsOfRoute(routeName);
		if(segments!=null){
			if(segments.getFirst().equals("true")){
				routesSegments.setRouteVisible(routeName, false);
				segments.removeFirst();
				for(String s : segments){
					routes2D.hideRoute(s);
					routes3D.hideRoute(s);
				}
			}
		}
	}
	
	
	private void showNavFix(String name){
		double latitude = 0;
		double longitude = 0;
		String type = "";
		double freq = 0;
		PreparedStatement ps;
		try {
			ps = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP, "select lat, lon, type, frequence from NavFix where nom = ?");

			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				latitude = rs.getDouble(1);
				longitude = rs.getDouble(2);
				type = rs.getString(3);
				freq = rs.getDouble(4);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Balise2D navFix = new Balise2D(name, Position.fromDegrees(latitude, longitude));
		String annotation = "<html><b>"+name+"</b><br/><i>Type : </i>"+type;
		if(freq != 0){
			annotation += "<br/><i>Fréq. : </i>"+freq;
		}
		annotation += "</html>";
		navFix.setAnnotation(annotation);
		navFixLayer.addBalise(navFix);
		navFixLayer.showBalise(navFix);
	}
	
	private void removeNavFix(int type, String name){
		Balise2D navFix = navFixLayer.getBalise(name);
		navFixLayer.hideBalise(navFix);
		this.wwd.getAnnotationLayer().removeAnnotation(navFix.getAnnotation(null));
		
	}
	
	

	@Override
	public void set2D(Boolean flat) {		
	}

	@Override
	public void reset() {
		this.zones.clear();
		this.zonesLayer.removeAllAirspaces();
		this.routes2D.hideAllRoutes();
		this.routes3D.hideAllRoutes();
		this.navFixLayer.eraseAllBalises();
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
	 	if(type == AIP.CTL){
			highlightCTL(name);
			//Si le type est supérieur à 20 et inférieur à 30, c'est une route
		}else if(type>=20 && type <30){
			highlightRoute(type, name);
			//Si le type est supérieur ou égal à 30, c'est une balise
		}else if(type>=30){
			highlightNavFix(type, name);
			//Sinon c'est un volume
		}else{
			if(!zones.containsKey(type+" "+name)){
				this.addZone(type, name);
			}
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
		Logging.logger("center");
		wwd.getView().setValue(AVKey.ELEVATION, 1e11);
		double[] eyePosition = this.wwd.computeBestEyePosition(object);
		Position centerPosition = Position.fromDegrees(eyePosition[0], eyePosition[1]);
		this.wwd.getView().setHeading(Angle.ZERO);
		this.wwd.getView().setPitch(Angle.ZERO);
		this.wwd.getView().goTo(centerPosition, eyePosition[2]);
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
		if(obj instanceof ObjectAnnotation){
			this.wwd.getAnnotationLayer().addAnnotation(((ObjectAnnotation)obj).getAnnotation(pos));
		}else if(obj instanceof List){
			if(((List<?>)obj).get(0) instanceof Route){
				String routeName = ((List<? extends Route>)obj).get(0).getName().split("-")[0].trim();
				GlobeAnnotation routeAnnotation = new GlobeAnnotation(routeName, pos);
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
		ArrayList<String> names = getCTLSecteurs(name);
		//on construit le secteur s'il n'existe pas encore
		if(!zones.containsKey(AIP.CTL+" "+names.get(0))){
			addZone(AIP.CTL, name);
		}
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
		}catch(SQLException e){
			e.printStackTrace();
		}
		return names;
	}
	
	
	
	
	private void highlightNavFix(int type, String name){
		if(!navFixLayer.contains(name)){
			showNavFix(name);
		}
		Balise2D navFix = navFixLayer.getBalise(name);
		centerView(navFix);
	}
	
	
	

	/**
	 * Centre la vue sur la route
	 * @param type Le type de la route
	 * @param name Le nom de la route
	 */
	private void highlightRoute(int type, String name){
		showRoute(name, type);
		ArrayList<Route2D> segments2D = new ArrayList<Route2D>();
		LinkedList<String> segmentsNames = routesSegments.getSegmentsOfRoute(name);
		if(segmentsNames != null){
			segmentsNames.removeFirst();
			for(String s : segmentsNames){
				segments2D.add((Route2D) routes2D.getRoute(s));
			}
			//On peut appeler centerView indiféremment sur une route2D ou une route3D puisqu'elles sont au même endroit.
			centerView(segments2D);
		}else{
			//TODO
			
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
	 * @return
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
					previousSegment = (Route3D) routes3D.getRoute(routeName.split("-")[0].concat(" - "+previousSeq));
				}else{
					previousSegment = (Route2D) routes2D.getRoute(routeName.split("-")[0].concat(" - "+previousSeq));
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
					nextSegment = (Route3D) routes3D.getRoute(routeName.split("-")[0].concat(" - "+nextSeq));
				}else{
					nextSegment = (Route2D) routes2D.getRoute(routeName.split("-")[0].concat(" - "+nextSeq));
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


	@Override
	public String type2string(int type) {
		return AIP.getTypeString(type);
	}
	
	public String toString(){
		return "AIP";
	}
}
