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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;


import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.VidesoController;
import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.aip.AIP.Altitude;
import fr.crnan.videso3d.graphics.Route;
import fr.crnan.videso3d.graphics.Route2D;
import fr.crnan.videso3d.graphics.Route3D;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.graphics.Secteur3D.Type;
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

	
	private HashMap<String, Secteur3D> zones;
	
	private HashMap<String, GlobeAnnotation> routesAnnotations;
	
	
	
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
		
		//Layers pour les routes
		if(routes3D != null) {
			routes3D.removeAllAirspaces();
		} else {
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
		this.buildRoutes();
		this.wwd.redraw();
	}


	
	
	@Override
	public void unHighlight(String name) {
	}
	@Override
	public void addLayer(String name, Layer layer) {
	}
	@Override
	public void removeLayer(String name, Layer layer) {
	}
	@Override
	public void removeAllLayers() {
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

	
	private void buildRoutes(){
		List<Couple<String,String>> routeNamesAndTypes = aip.getRouteNamesFromDB();
		for(Couple<String,String> nameAndType : routeNamesAndTypes){
			String typeString = nameAndType.getSecond();
			int type = AIP.AWY;
			if(typeString.equals("PDR"))
				type = AIP.PDR;
			if(typeString.equals("TAC"))
				type = AIP.TAC;
			addRouteToLayer(nameAndType.getFirst(), type);
		}
	}
	
	
	
	@Override
	public void showObject(int type, String name) {
		switch(type){
		case AIP.AWY :
		case AIP.PDR :
		case AIP.TAC :
			this.showRoute(name,type);
			break;
		case AIP.CTL :
			// si c'est de type CTL, il se peut qu'il y ait plusieurs volumes correspondant à un seul secteur
			// donc on va chercher les différents morceaux avec getCTLSecteurs et on les ajoute tous.
			for(String nomPartieSecteur : getCTLSecteurs(name)){
				if(!zones.containsKey(nomPartieSecteur))
					this.addZone(type, nomPartieSecteur);
			}
			break;
		default :
			if(!zones.containsKey(name))
				this.addZone(type,name);
		}
	}

	@Override
	public void hideObject(int type, String name) {
		switch(type){
		case AIP.AWY :
		case AIP.PDR :
		case AIP.TAC :
			this.removeRoute(name, type);
			break;
			// si c'est de type CTL, il se peut qu'il y ait plusieurs volumes correspondant à un seul secteur
			// donc on va chercher les différents morceaux avec getCTLSecteurs et on les enlève tous.
		case AIP.CTL:
			for(String nomPartieSecteur : getCTLSecteurs(name)){
				this.removeZone(type,nomPartieSecteur);
			}
			break;
		default :
			this.removeZone(type,name);
		}
	}

	
	public int string2type(String type){
		return AIP.string2type(type);
	}

	/**
	 * Affiche tous les objets correspondant aux checkBox de la liste.
	 * @param type Le type des objets à afficher
	 */
	public void displayAll(int type){
		Iterator<Couple<Integer,String>> it = aip.getZones(type).iterator();
		while(it.hasNext()){
			showObject(type, it.next().getSecond());
		}
	}
	/**
	 * Enlève tous les objets correspondant aux checkBox de la liste.
	 * @param type
	 */
	public void hideAll(int type){
		Iterator<Couple<Integer,String>> it = aip.getZones(type).iterator();
		while(it.hasNext()){
			hideObject(type, it.next().getSecond());
		}
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
		String routeID = AIP.getID(type, routeName);
		try {
			Statement st = DatabaseManager.getCurrentAIP();
			ResultSet segments = st.executeQuery("select pk from segments where pkRoute = '"+routeID+"' ORDER BY sequence");
			while(segments.next()){
				Element segment = aip.findElement(aip.getDocumentRoot().getChild("SegmentS"), segments.getString(1));
				if(!segment.getChildText("Circulation").equals("(XxX)")){
					String segmentName = buildSegmentName(routeName, segment.getChildText("Sequence"));
					routes2D.displayRoute(segmentName);
					routes3D.displayRoute(segmentName);
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		
	}

	/**
	 * Ajoute une route identifiée par son nom et son type à la vue 3D. Si les points d'une route ne sont pas définis dans le fichier SIA,
	 * on représente la route seulement par un point situé à 48°N, 0°E .
	 * @param routeName Le nom de la route à afficher.
	 * @param type 
	 */
	public void addRouteToLayer(String routeName, int type){
		String routeID = AIP.getID(type, routeName);
		fr.crnan.videso3d.graphics.Route.Type RouteType;
		if(type == AIP.PDR){
			RouteType = fr.crnan.videso3d.graphics.Route.Type.UIR;
		}else{
			RouteType = fr.crnan.videso3d.graphics.Route.Type.FIR;
		}
		try {
			Statement st = DatabaseManager.getCurrentAIP();
			ResultSet segments = st.executeQuery("select pk from segments where pkRoute = '"+routeID+"' ORDER BY sequence");
			while(segments.next()){
				Element segment = aip.findElement(aip.getDocumentRoot().getChild("SegmentS"), segments.getString(1));
				String segmentName = buildSegmentName(routeName, segment.getChildText("Sequence"));
				if(routes2D.getRoute(segmentName)==null){
					if(!segment.getChildText("Circulation").equals("(XxX)")){
						Couple<Altitude,Altitude> altis = aip.getLevels(segment);
						LinkedList<LatLon> loc = new LinkedList<LatLon>();
						Geometrie geometrieSegment = new Geometrie(segment);
						loc.addAll(geometrieSegment.getLocations());
						if(loc.get(0)==null){
							loc.clear();
							loc.add(LatLon.fromDegrees(48, 0));
						}
						
						Route2D segment2D = new Route2D(segmentName, RouteType);
						segment2D.setLocations(loc);
						segment2D.setAnnotation("<html>Route "+segmentName+"<br/><b>Plancher :</b>"+altis.getFirst().getFullText()
								+"<br/><b>Plafond :</b>"+altis.getSecond().getFullText()+"</html>");
						routes2D.addRoute(segment2D, segmentName);

						//TODO prendre en compte le sens de circulation... 
						Route3D segment3D = new Route3D(segmentName, RouteType);
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
					}
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	
	
	
	private void removeRoute(String routeName, int type){
		//TODO problème avec les routes qui ont le même nom (J 22 et J 22 Polynésie...) : quand on met l'annotation dans le hashmap 
		//on ne connaît pas le territoire, donc quand on affiche les deux J 22 en même temps, on ne garde qu'une seule des deux annotations
		//dans le hashmap. Du coup on ne peut plus enlever l'autre.
		String routeID = AIP.getID(type, routeName);
		if(routesAnnotations.containsKey(routeName.split("-")[0])){
			routesAnnotations.get(routeName.split("-")[0]).getAttributes().setVisible(false);
		}
		try {
			Statement st = DatabaseManager.getCurrentAIP();
			ResultSet rs = st.executeQuery("select sequence from segments where pkRoute = '"+routeID+"' ORDER BY sequence");
			while(rs.next()){
				routes2D.hideRoute(buildSegmentName(routeName, rs.getString(1)));
				routes3D.hideRoute(buildSegmentName(routeName, rs.getString(1)));
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	
	

	@Override
	public void set2D(Boolean flat) {		
	}

	@Override
	public void reset() {
		this.zones.clear();
		this.zonesLayer.removeAllAirspaces();
		this.routes2D.removeAllRenderables();
		this.routes3D.removeAllAirspaces();
		this.wwd.getAnnotationLayer().removeAllAnnotations();
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
	public void highlight(String name) {
		if(name.startsWith(AIP.CTL+" ")){
			//on passe en paramètre name.substring(3) car le nom qu'on a récupéré est précédé du chiffre correspondant au type de zone
			highlightCTL(getCTLSecteurs(name.substring(3)));
			//Si le nom commence par un 2, c'est une route
		}else if(name.startsWith("2") && name.charAt(1)!=' '){
			highlightRoute(getSegments(name.substring(3)));
		}else{
			Secteur3D zone = zones.get(name);
			this.centerView(zone);
		}
	}
	

	
	
	/**
	 * Centre la vue sur une route ou un secteur3D, avec le niveau de zoom approprié, et affiche l'annotation associée.
	 * @param zone
	 * @return La position sur laquelle la vue est centrée.
	 */
	public Position centerView(Object object){
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
		if(obj instanceof Secteur3D){
			this.wwd.getAnnotationLayer().addAnnotation(((Secteur3D)obj).getAnnotation(pos));
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
	private void highlightCTL(ArrayList<String> names){
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
	
	
	/**
	 * Centre la vue sur la route définie par les segments passés en paramètre.
	 * @param segmentsNames Les noms des segments de la route
	 */
	private void highlightRoute(ArrayList<String> segmentsNames){
		ArrayList<Route2D> segments2D = new ArrayList<Route2D>();
		ArrayList<Route3D> segments3D = new ArrayList<Route3D>();
		for(String s : segmentsNames){
			segments2D.add((Route2D) routes2D.getRoute(s));
			segments3D.add((Route3D) routes3D.getRoute(s));
		}
		//On peut appeler centerView indiféremment sur une route2D ou une route3D puisqu'elles sont au même endroit.
		centerView(segments2D);
	}
	
	
	/**
	 * 
	 * @param routeName 
	 * @return
	 */
	private ArrayList<String> getSegments(String routeName){
		ArrayList<String> segmentsNames = new ArrayList<String>();
		String routeID = AIP.getID(AIP.AWY, routeName);
		try{
			Statement st = DatabaseManager.getCurrentAIP();
			ResultSet rs = st.executeQuery("select sequence from segments where pkRoute = '"+routeID+"' ORDER BY sequence");
			while(rs.next()){				
				segmentsNames.add(buildSegmentName(routeName, rs.getString(1)));
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return segmentsNames;
	}

	private String buildSegmentName(String routeName, String sequence){
		return routeName.split("-")[0].trim().concat(" - ").concat(sequence);
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
	
}
