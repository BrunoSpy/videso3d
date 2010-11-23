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

import java.awt.event.ActionEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXTaskPane;
import org.jdom.Element;

import fr.crnan.videso3d.Context;
import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.aip.AIP.Altitude;
import fr.crnan.videso3d.graphics.Route;
import fr.crnan.videso3d.graphics.Route2D;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.graphics.VidesoObject;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Annotation;
/**
 * 
 * @author Bruno Spyckerelle	
 * @version 0.1
 */
public class AIPContext extends Context {

	private Annotation lastSegmentAnnotation;
	
	private AIPController getController(){
		return (AIPController) DatasManager.getController(Type.AIP);
	}


	@Override
	public List<JXTaskPane> getTaskPanes(int type, String name) {
		JXTaskPane taskPane = new JXTaskPane();
		taskPane.setTitle("Eléments AIP");
		if(type<20){
			return showAIPZoneInfos(type, name);
		}else if(type>=20 && type <30){
			return showAIPRoute(getController().getRoutes2DLayer().getRoute(name));
		}else if(type>=30 && type<40){
			//showNavFix
		}
		return null;
	}

	//TODO voir si on peut pas se contenter du nom de la zone (on a besoin que de l'ID et du type).
	public List<JXTaskPane> showAIPZoneInfos(int type, String name) {
		String zoneID = AIP.getID(type, name);
		
		JXTaskPane infos = new JXTaskPane();
		infos.setTitle("Informations diverses");
		String classe = getController().getAIP().getZoneAttributeValue(zoneID, "Classe");
		String hor = getController().getAIP().getZoneAttributeValue(zoneID, "HorTxt");
		String act = getController().getAIP().getZoneAttributeValue(zoneID, "Activite");
		String rmq = getController().getAIP().getZoneAttributeValue(zoneID, "Remarque");
		
		if(classe != null){
			infos.add(new JLabel("<html><b>Classe</b> : " + classe+"</html>"));
		}
		if(type == AIP.R){
			if(getController().getAIP().getZoneAttributeValue(zoneID, "Rtba")!=null)
				infos.add(new JLabel("<html><b>RTBA</b></html>"));
		}
		if(hor != null){
			infos.add(new JLabel("<html><b>Horaires</b> : " + hor.replaceAll("#", "<br/>")+"</html>"));
		}
		if(act != null){
			infos.add(new JLabel("<html><b>Activité</b> : " + act.replaceAll("#", "<br/>")+"</html>"));
		}
		if(rmq != null){
			infos.add(new JLabel("<html><b>Remarques</b> : " + rmq.replaceAll("#", "<br/>")+"</html>"));
		}
		
		LinkedList<JXTaskPane> taskPanesList = new LinkedList<JXTaskPane>();
		taskPanesList.add(infos);
		return taskPanesList;
	}
	
	public List<JXTaskPane> showAIPRoute(Route segment) {
		AIP aip = getController().getAIP();
		String route = segment.getName().split("-")[0].trim();
		String[] splittedSegmentName = segment.getName().split("-");
		String sequence = splittedSegmentName[splittedSegmentName.length-1].trim();
		fr.crnan.videso3d.graphics.Route.Space type = segment.getSpace();
		String pkRoute = null;
		String typeRoute = aip.RouteType2AIPType(route, type);
		StringBuilder ACCTraverses = new StringBuilder();
		try {
			pkRoute = getController().getRouteIDFromSegmentName(route, typeRoute);
			PreparedStatement st = DatabaseManager.prepareStatement(DatabaseManager.Type.AIP, "select nomACC from ACCTraverses where routes_pk = ?");
			st.setString(1, pkRoute);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				ACCTraverses.append(rs.getString(1)+" ");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		List<Element> segmentsXML = aip.findElementsByChildId(aip.getDocumentRoot().getChild("SegmentS"), "Route", pkRoute);
		Element monSegmentXML = null;
		for(Element segmentXML : segmentsXML){
			if(segmentXML.getChildText("Sequence").equals(sequence)){
				monSegmentXML = segmentXML;
			}
		}		
		
		Element maRoute = aip.findElement(aip.getDocumentRoot().getChild("RouteS"), pkRoute);
		
		JXTaskPane infosRoute = new JXTaskPane();
		infosRoute.setTitle("Informations sur la route");
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
					getController().displayRouteNavFixs(pkRouteFinal, true);
					putValue(Action.NAME, "Cacher les balises");
					afficher = false;
				}else{
					getController().displayRouteNavFixs(pkRouteFinal, false);
					putValue(Action.NAME, "Afficher les balises");
					afficher = true;
				}
				
			}
		});
		
		JXTaskPane infosSegment = new JXTaskPane();
		infosSegment.setTitle("Informations sur le segment");
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
		final Route segmentPrecedent = getController().getPrevious(route, sequence, typeRoute, route3D); 
		final Route segmentSuivant = getController().getNext(route, sequence, typeRoute, route3D);
		
		if(segmentPrecedent != null){
			AbstractAction previous = new AbstractAction("<html><font color=\"blue\">&lt;&lt; Segment précédent</font></html>"){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					getController().displayAnnotationAndGoTo(segmentPrecedent);
					showAIPRoute(segmentPrecedent);
				}
			};
			infosSegment.add(previous);
		}
		if(segmentSuivant != null){
			AbstractAction next = new AbstractAction("<html><font color=\"blue\">Segment suivant &gt;&gt;</font></html>"){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					getController().displayAnnotationAndGoTo(segmentSuivant);
					showAIPRoute(segmentSuivant);
				}
			};
			infosSegment.add(next);
		}
		LinkedList<JXTaskPane> taskPanesList = new LinkedList<JXTaskPane>();
		taskPanesList.add(infosRoute);
		taskPanesList.add(infosSegment);
		return taskPanesList;
	}
	
	
	

}
