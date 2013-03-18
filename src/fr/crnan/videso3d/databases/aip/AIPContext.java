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
package fr.crnan.videso3d.databases.aip;

import java.awt.event.ActionEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXTaskPane;
import org.jdom2.Element;

import fr.crnan.videso3d.Context;
import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.aip.AIP.Altitude;
import fr.crnan.videso3d.graphics.Route;
import fr.crnan.videso3d.graphics.Route.Space;
import fr.crnan.videso3d.graphics.Route2D;
import fr.crnan.videso3d.ihm.ContextPanel;
/**
 * 
 * @author Bruno Spyckerelle	
 * @version 0.1.1
 */
public class AIPContext extends Context {

	
	private AIPController getController(){
		return (AIPController) DatasManager.getController(DatasManager.Type.AIP);
	}


	@Override
	public List<JXTaskPane> getTaskPanes(int type, String name) {
		if(type < AIP.AWY){
			return showZoneInfos(type, name);
		}else if(type >= AIP.AWY && type <AIP.DMEATT){
			return showRouteInfos(name);
		}else if(type>=AIP.DMEATT && type<AIP.AERODROME){
			return showNavFixInfos(type, name);
		}else if(type>=AIP.AERODROME){
			return showAirportInfos(type, name);
		}
		return null;
	}

	
	public List<JXTaskPane> showZoneInfos(int type, String name) {
		String zoneID = AIP.getID(type, name);
		
		JXTaskPane infos = new JXTaskPane();
		boolean hasElements = false;
		infos.setTitle("Éléments AIP");
		String classe = getController().getAIP().getZoneAttributeValue(zoneID, "Classe");
		String hor = getController().getAIP().getZoneAttributeValue(zoneID, "HorTxt");
		String act = getController().getAIP().getZoneAttributeValue(zoneID, "Activite");
		String rmq = getController().getAIP().getZoneAttributeValue(zoneID, "Remarque");
		if(classe != null){
			infos.add(new JLabel("<html><b>Classe</b> : " + classe+"</html>"));
			hasElements = true;
		}
		if(type == AIP.R){
			if(getController().getAIP().getZoneAttributeValue(zoneID, "Rtba")!=null) {
				infos.add(new JLabel("<html><b>RTBA</b></html>"));
				hasElements = true;
			}
		}
		if(hor != null){
			infos.add(new JLabel("<html><b>Horaires</b> : " + hor.replaceAll("#", "<br/>")+"</html>"));
			hasElements = true;
		}
		if(act != null){
			infos.add(new JLabel("<html><b>Activité</b> : " + act.replaceAll("#", "<br/>")+"</html>"));
			hasElements = true;
		}
		if(rmq != null){
			infos.add(new JLabel("<html><b>Remarques</b> : " + rmq.replaceAll("#", "<br/>")+"</html>"));
			hasElements = true;
		}
		
		if(!hasElements){
			infos.add(new JLabel("<html><i>Pas d'éléments AIP</i></html>"));
		}
		
		LinkedList<JXTaskPane> taskPanesList = new LinkedList<JXTaskPane>();
		taskPanesList.add(infos);
		return taskPanesList;
	}
	
	
	public List<JXTaskPane> showRouteInfos(String name) {
		LinkedList<JXTaskPane> taskPanesList = new LinkedList<JXTaskPane>();
		
		Route segment = getController().getRoutes2DLayer().getRoute(name);
		AIP aip = getController().getAIP();
		String routeName = "";
		fr.crnan.videso3d.graphics.Route.Space type = null;
		String sequence = "";
		
		if(segment == null){
			//pas de segment, on recherche la route AIP correspondante
			try {
				Statement st = DatabaseManager.getCurrentAIP();
				ResultSet rs = st.executeQuery("select nom, type from routes");
				String routeAIPName = null;
				while(rs.next() && routeAIPName == null){
					if(name.equalsIgnoreCase(rs.getString(1).replaceAll(" ", ""))){
						routeAIPName = rs.getString(1);
						if(rs.getString(2).equals("AWY")) {
							type = Space.FIR;
						} else {
							type = Space.UIR;
						}
					}
				}
				if(routeAIPName != null) {
					routeName = routeAIPName;
				} else {
					//aucune route AIP trouvée
					return null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		} else {
			String[] splittedSegmentName = segment.getName().split("-");
			
			if(splittedSegmentName.length >2){
				routeName = (splittedSegmentName[0]+"-"+splittedSegmentName[1]).trim();
			}else{
				routeName = splittedSegmentName[0].trim();
			}
			sequence = splittedSegmentName[splittedSegmentName.length-1].trim();
			type = segment.getSpace();
		}

		String pkRoute = null;
		String typeRoute = aip.RouteType2AIPType(routeName, type);

		try {
			pkRoute = getController().getRouteIDFromSegmentName(routeName, typeRoute);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Element maRoute = aip.findElement(aip.getDocumentRoot().getChild("RouteS"), pkRoute);
		taskPanesList.add(routeInfos(maRoute, typeRoute, pkRoute));

		if(segment != null){
			//éléments du segment AIP
			List<Element> segmentsXML = aip.findElementsByChildId(aip.getDocumentRoot().getChild("SegmentS"), "Route", pkRoute);
			Element monSegmentXML = null;
			for(Element segmentXML : segmentsXML){
				if(segmentXML.getChildText("Sequence").equals(sequence)){
					monSegmentXML = segmentXML;
				}
			}		
			
			
			
			taskPanesList.add(segmentRouteInfos(segment, monSegmentXML, routeName, sequence, typeRoute));
		}
				
		
		return taskPanesList;
	}


	private JXTaskPane routeInfos(Element maRoute, String typeRoute, String pkRoute){
		StringBuilder ACCTraverses = new StringBuilder();
		try {	
			PreparedStatement st = DatabaseManager.prepareStatement(DatasManager.Type.AIP, "select nomACC from ACCTraverses where routes_pk = ?");
			st.setString(1, pkRoute);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				ACCTraverses.append(rs.getString(1)+" ");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		JXTaskPane infosRoute = new JXTaskPane();
		infosRoute.setTitle("Eléments AIP sur la route");
		String CRType = maRoute.getChildText("TypeCompteRendu");
		String rmqRoute = maRoute.getChildText("Remarque");
		infosRoute.add(new JLabel("<html><b>Type de la route</b> : " + typeRoute+"</html>"));
		if(CRType != null){
			infosRoute.add(new JLabel("<html><b>Compte-rendu</b> : " + CRType+"</html>"));
		}
		infosRoute.add(new JLabel("<html><b>ACC traversés</b> : " + ACCTraverses+"</html>"));
		if(rmqRoute != null){
			infosRoute.add(new JLabel("<html><b>Remarque</b> : " + rmqRoute+"</html>"));
		}
		final String pkRouteFinal = pkRoute;
		infosRoute.add(new AbstractAction("Afficher les balises"){
			boolean afficher = true;
			@Override
			public void actionPerformed(ActionEvent e) {
				if(afficher){
					getController().displayRouteNavFixs(pkRouteFinal, true, false);
					putValue(Action.NAME, "Cacher les balises");
					afficher = false;
				}else{
					getController().displayRouteNavFixs(pkRouteFinal, false, false);
					putValue(Action.NAME, "Afficher les balises");
					afficher = true;
				}
				
			}
		});
		return infosRoute;
	}
	
	private JXTaskPane segmentRouteInfos(Route segment, Element monSegmentXML, String routeName, String sequence, String typeRoute){
		AIP aip = getController().getAIP();
		final JXTaskPane infosSegment = new JXTaskPane();
		infosSegment.setTitle("Éléments sur le segment");
		String CR = aip.getChildText(monSegmentXML, "CompteRendu");
		String circul = aip.getChildText(monSegmentXML, "Circulation");
		String rnp = aip.getChildText(monSegmentXML, "CodeRnp");
		String dist = aip.getChildText(monSegmentXML, "Distance");
		String routeM = aip.getChildText(monSegmentXML, "RouteMag");
		String ACC = aip.getChildText(monSegmentXML, "Acc");
		String rmq = aip.getChildText(monSegmentXML, "Remarque");
		Couple<Altitude, Altitude> levels = aip.getLevels(monSegmentXML);
		
		if(CR != null){
			infosSegment.add(new JLabel("<html><b>Compte-rendu</b> : " + CR+"</html>"));
		}
		if(circul != null){
			infosSegment.add(new JLabel("<html><b>Circulation</b> : " + circul+"</html>"));
		}
		if(rnp != null){
			infosSegment.add(new JLabel("<html><b>Code Rnp</b> : " + rnp+"</html>"));
		}
		infosSegment.add(new JLabel("<html><b>Plafond</b> : " + levels.getSecond().getFullText()+"</html>"));
		infosSegment.add(new JLabel("<html><b>Plancher</b> : " + levels.getFirst().getFullText()+"</html>"));
		if(dist != null){
			infosSegment.add(new JLabel("<html><b>Longueur du segment</b> : " + dist+"nm</html>"));
		}
		if(routeM != null){
			infosSegment.add(new JLabel("<html><b>Route magnétique</b> : " + routeM+"</html>"));
		}
		if(ACC != null){
			infosSegment.add(new JLabel("<html><b>ACC</b> : " + ACC+"</html>"));
		}
		if(rmq != null){
			infosSegment.add(new JLabel("<html><b>Remarque</b> : " + rmq+"</html>"));
		}
		boolean route3D = true;
		if(segment instanceof Route2D){
			route3D = false;
		}
		final Route segmentPrecedent = getController().getPrevious(routeName, sequence, typeRoute, route3D); 
		final Route segmentSuivant = getController().getNext(routeName, sequence, typeRoute, route3D);
		
		if(segmentPrecedent != null){
			AbstractAction previous = new AbstractAction("<html><font color=\"blue\">&lt;&lt; Segment précédent</font></html>"){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					getController().displayAnnotationAndGoTo(segmentPrecedent);
					((ContextPanel)infosSegment.getParent().getParent().getParent().getParent()).showInfo(DatasManager.Type.AIP, AIP.AWY, segmentPrecedent.getName());
				}
			};
			infosSegment.add(previous);
		}
		if(segmentSuivant != null){
			AbstractAction next = new AbstractAction("<html><font color=\"blue\">Segment suivant &gt;&gt;</font></html>"){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					getController().displayAnnotationAndGoTo(segmentSuivant);
					((ContextPanel)infosSegment.getParent().getParent().getParent().getParent()).showInfo(DatasManager.Type.AIP, AIP.AWY, segmentSuivant.getName());
				}
			};
			infosSegment.add(next);
		}
		return infosSegment;
	}
	
	private List<JXTaskPane> showNavFixInfos(int type, String name){
		LinkedList<JXTaskPane> taskPanesList = new LinkedList<JXTaskPane>();
		final JXTaskPane infosNavFix = new JXTaskPane("Eléments AIP");
		AIP aip = getController().getAIP();

		float latitude = 0, longitude = 0;
		try {
			PreparedStatement ps = DatabaseManager.prepareStatement(DatasManager.Type.AIP, "select lat, lon from NavFix where nom=? and type = ?");
			ps.setString(1, name);
			ps.setString(2, AIP.type2String(type));
			ResultSet rs = ps.executeQuery();
			latitude = rs.getFloat(1);
			longitude = rs.getFloat(2);
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		String nordSud = "";
		String estOuest = "";
		if(latitude>=0){
			nordSud = "N";
		}else{
			nordSud = "S";
		}
		if(longitude>=0){
			estOuest = "E";
		}else{
			estOuest = "W";
		}
		infosNavFix.add(new JLabel("<html><b>Position</b> :"+latitude+"°"+nordSud+", "+longitude+"°"+estOuest));
	//	if(type != AIP.WPT && type != AIP.VFR && type != AIP.PNP){

			Element navFixXML = aip.findNavFixInfosByName(name);
			if(navFixXML != null){
				String nomPhraseo = navFixXML.getChildText("NomPhraseo");
				String freq = navFixXML.getChildText("Frequence");
				String station = navFixXML.getChildText("Station");
				String LatDme = navFixXML.getChildText("LatDme");
				String LonDme = navFixXML.getChildText("LongDme");
				String alti = navFixXML.getChildText("AltitudeFt");
				String situation = navFixXML.getChildText("Situation");
				String pkAd = navFixXML.getChild("Ad").getAttributeValue("pk");
				Element adXML = aip.findElement(aip.getDocumentRoot().getChild("AdS"), pkAd);
				String finCode = adXML.getChildText("AdCode");
				String territoire = adXML.getChild("Territoire").getAttributeValue("lk").substring(1, 3);
				final String adCode = territoire+finCode;
				String adName = adXML.getChildText("AdNomComplet");
				String horCode = navFixXML.getChildText("HorCode");
				String usage = navFixXML.getChildText("Usage");
				String portee = navFixXML.getChildText("Portee");
				String flPortee = navFixXML.getChildText("FlPorteeVert");
				String couverture = navFixXML.getChildText("Couverture");
				if(nomPhraseo != null){
					infosNavFix.add(new JLabel("<html><b>Nom phraseo</b> : " + nomPhraseo+"</html>"));
					infosNavFix.setTitle(nomPhraseo);
				}
				if(freq != null){
					infosNavFix.add(new JLabel("<html><b>Fréquence</b> : " + freq+"</html>"));
				}
				if(station != null){
					infosNavFix.add(new JLabel("<html><b>Station</b> : " + station+"</html>"));
				}
				if(LatDme != null){
					infosNavFix.add(new JLabel("<html><b>Lat. Dme</b> : " + LatDme+"</html>"));
				}
				if(LonDme != null){
					infosNavFix.add(new JLabel("<html><b>Lon. Dme</b> : " + LonDme+"</html>"));
				}
				if(alti != null){
					infosNavFix.add(new JLabel("<html><b>Altitude</b> : " + alti+" ft</html>"));
				}
				if(situation != null){
					infosNavFix.add(new JLabel("<html><b>Situation</b> : " + situation+"</html>"));
				}
				if(adCode != null && adName != null){
					AbstractAction adLink = new AbstractAction("<html><b>Aérodrome</b> :<font color=\"blue\">"+adCode+" -- "+adName+"</font></html>"){
						@Override
						public void actionPerformed(ActionEvent arg0) {
							getController().highlight(AIP.AERODROME, adCode);
							((ContextPanel)infosNavFix.getParent().getParent().getParent().getParent()).showInfo(DatasManager.Type.AIP, AIP.AERODROME, adCode);
						}
					};
					infosNavFix.add(adLink);
				}
				if(horCode != null){
					infosNavFix.add(new JLabel("<html><b>Code horaire</b> : " + horCode+"</html>"));
				}
				if(usage != null){
					infosNavFix.add(new JLabel("<html><b>Usage</b> : " + usage+"</html>"));
				}
				if(portee != null){
					infosNavFix.add(new JLabel("<html><b>Portée</b> : " + portee+"</html>"));
				}
				if(flPortee != null){
					infosNavFix.add(new JLabel("<html><b>Portée verticale</b> : FL" + flPortee+"</html>"));
				}
				if(couverture != null){
					infosNavFix.add(new JLabel("<html><b>Couverture</b> : " + couverture+"</html>"));
				}
		//	}
		}
		taskPanesList.add(infosNavFix);
		return taskPanesList;
	}

	
	private List<JXTaskPane> showAirportInfos(int type, String name){
		LinkedList<JXTaskPane> taskPanesList = new LinkedList<JXTaskPane>();
		final JXTaskPane infosGen = new JXTaskPane("Infos générales");
		JXTaskPane infosHor = new JXTaskPane("Horaires");
		JXTaskPane infosSvc = new JXTaskPane("Services");
		JXTaskPane infosMet = new JXTaskPane("Météo");
		JXTaskPane divers = new JXTaskPane("Divers");
		AIP aip = getController().getAIP();

		int pkAd = -1;
		List<Integer> pkPistes = new LinkedList<Integer>();
		try {
			PreparedStatement ps=null;
			if(type == AIP.AERODROME){
				ps = DatabaseManager.prepareStatement(DatasManager.Type.AIP, "select pk from aerodromes where upper(code)=?");
				ps.setString(1, name.split("--")[0].trim());
				ResultSet rs = ps.executeQuery();
				pkAd = rs.getInt(1);
			}else{
				ps = DatabaseManager.prepareStatement(DatasManager.Type.AIP, "select pk from aerodromes where nom = ?");
				ps.setString(1, name);
				ResultSet rs = ps.executeQuery();
				pkAd = rs.getInt(1);
			}
			ps = DatabaseManager.prepareStatement(DatasManager.Type.AIP, "select pk from runways where pk_ad = ?");
			ps.setInt(1, pkAd);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				pkPistes.add(rs.getInt(1));
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}	
		Element airportXML = aip.findElement(aip.getDocumentRoot().getChild("AdS"), ""+pkAd);
		if(airportXML!=null){
			for(Element e : (List<Element>)airportXML.getChildren()){
				String nom = espacer(e.getName());
				String texte = e.getText().replaceAll("#", "<br/>");
				if(  nom.startsWith("Ad") 	&& ! ( nom.equals("Ad Code") || nom.equals("Ad Ad2") || nom.equals("Ad Geo Und") )   ){
					infosGen.add(new JLabel("<html><b>"+nom.substring(3)+"</b> : "+texte+"</html>"));
				}else if( nom.startsWith("Arp")){
					infosGen.add(new JLabel("<html><b>"+nom.substring(4)+"</b> : "+texte+"</html>"));
				}else if(nom.startsWith("Tfc")){
					infosGen.add(new JLabel("<html><b>"+nom+"</b> : "+texte+"</html>"));					
				}else if(nom.equals("Ctr")){
					String pkCTR = e.getAttributeValue("pk");
					Element espaceCTR = aip.findElement(aip.getDocumentRoot().getChild("EspaceS"), pkCTR);
					final String nomCTR = espaceCTR.getChildText("Nom");
					
					AbstractAction ctrAction = new AbstractAction("<html><b>CTR</b> :<font color=\"blue\">"+nomCTR+"</font></html>"){
						@Override
						public void actionPerformed(ActionEvent arg0) {
							try {
								Statement st = DatabaseManager.getCurrentAIP();
								ResultSet rs = st.executeQuery("select nom from volumes where type ='CTR' and (nom LIKE '"+nomCTR+" %' OR nom ='"+nomCTR+"')");
								String lastCTR = null;
								while(rs.next()){
									lastCTR = rs.getString(1);
									getController().showObject(AIP.CTR, lastCTR);
								}
								getController().highlight(AIP.CTR, lastCTR);
							} catch (SQLException e1) {
								e1.printStackTrace();
							}
						}
					};
					infosGen.add(ctrAction);
				}else if(nom.startsWith("Hor")){
					infosHor.add(new JLabel("<html><b>"+nom.substring(4).replaceAll("Txt", "")+"</b> : "+texte+"</html>"));
				}else if(nom.startsWith("Svc")){ 
					infosSvc.add(new JLabel("<html><b>"+nom.substring(4)+"</b> : "+texte+"</html>"));
				}else if( nom.startsWith("Sslia") || nom.startsWith("Neige")){
					infosSvc.add(new JLabel("<html><b>"+nom+"</b> : "+texte+"</html>"));
				}else if(nom.startsWith("Met")){
					infosMet.add(new JLabel("<html><b>"+nom.substring(4)+"</b> : "+texte+"</html>"));
				}else if(!(nom.startsWith("Ad") || nom.equals("Wgs84") || nom.equals("Geometrie") || nom.equals("Territoire"))){
					divers.add(new JLabel("<html><b>"+nom+"</b> : "+texte+"</html>"));
				}
			}
		}
		taskPanesList.add(infosGen);
		taskPanesList.add(infosHor);
		taskPanesList.add(infosSvc);
		taskPanesList.add(infosMet);
		taskPanesList.add(divers);

		for(JXTaskPane infos : taskPanesList){
			if(infos.getContentPane().getComponentCount()==0){
				JLabel noInfo = new JLabel("<html><i>Aucune info.</i></html>");
				infos.add(noInfo);
			}
			infos.setCollapsed(true);
		}
		infosGen.setCollapsed(false);
		if(pkPistes.size()>0){
			for (int pkPiste : pkPistes){
				Element pisteXML = aip.findElement(aip.getDocumentRoot().getChild("RwyS"), ""+pkPiste);
				String orientation = pisteXML.getChildText("Rwy");
				JXTaskPane piste = new JXTaskPane("Piste "+orientation);
				piste.add(new JLabel("<html><b><i>Généralités</i></b><html>"));
				piste.add(new JLabel("\n"));
				piste.add(new JLabel("<html><b>Orientation :</b> "+orientation));

				List<JLabel> gen = new LinkedList<JLabel>();
				List<JLabel> dist = new LinkedList<JLabel>();
				List<JLabel> thr = new LinkedList<JLabel>();

				for(Element e : (List<Element>)pisteXML.getChildren()){
					String nom = espacer(e.getName());
					String texte = e.getText().replaceAll("#", "<br/>");
					if(nom.contains("Thr")){
						thr.add(new JLabel("<html><b>"+nom+"</b> : "+texte));
					}else if(nom.contains("Cwy") || nom.contains("Swy") || nom.contains("Lda") || nom.contains("Dist")){
						dist.add(new JLabel("<html><b>"+nom+"</b> : "+texte));
					}else if(!(nom.equals("Ad") || nom.equals("Rwy") || nom.equals("Geometrie") || nom.equals("Extension"))){
						gen.add(new JLabel("<html><b>"+nom+"</b> : "+texte));
					}
				}
				for(JLabel jl : gen){
					piste.add(jl);
				}
				piste.add(new JLabel("\n"));
				piste.add(new JLabel("<html><b><i>Distances</i></b><html>"));
				piste.add(new JLabel("\n"));
				for(JLabel jl : dist){
					piste.add(jl);
				}
				piste.add(new JLabel("\n"));
				piste.add(new JLabel("<html><b><i>Coordonnées</i></b><html>"));
				piste.add(new JLabel("\n"));
				for(JLabel jl : thr){
					piste.add(jl);
				}
				piste.setCollapsed(true);
				taskPanesList.add(piste);
			}
		}
		return taskPanesList;
	}
	
	/**
	 * @param txt une chaîne de caractères de longueur non nulle.
	 * @return Le même texte avec un espace avant chaque majuscule.
	 */
	private String espacer(String txt){
		String resultat = "";
		int debutMot = 0;
		for(int i=1; i<txt.length();i++){
			if(txt.charAt(i)>='A' && txt.charAt(i)<='Z' ){
					resultat = resultat.concat(txt.substring(debutMot, i)+" ");
					debutMot=i;				
			}
		}
		resultat = resultat.concat(txt.substring(debutMot));
		return resultat;
	}

}
