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
package fr.crnan.videso3d.edimap;

import fr.crnan.videso3d.geom.LatLonCautra;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfacePolyline;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Construit une ellipse à partir de l'entité EllipseEntity.
 * Voir le DDI Edimap pour plus de détails sur la construction d'un arc de cercle.
 * Le  type de construction est ignoré ici.
 * @author Bruno Spyckerelle
 * @author Adrien Vidal
 * @version 0.2.1
 */
public class EllipseEdimap extends SurfacePolyline {

	//Nombre de points utilisés pour dessiner l'arc de cercle, par quart de cercle.
	//Si on a un arc plus petit qu'un quart de cercle, on utilise <i>precision<i/> points, si on a un arc entre un quart de cercle 
	//et un demi-cercle, on utilise 2*<i>precision<i/> points, etc.
	private int precision = 7;
	
	private LinkedList<LatLon> polyligne = new LinkedList<LatLon>();
	
	public EllipseEdimap(Entity ellipse,
			  HashMap<String, LatLonCautra> pointsRef, 
			  PaletteEdimap palette,
			  HashMap<String, Entity> idAtc){
	
		super(new BasicShapeAttributes());
		
		Entity geometry = ellipse.getEntity("geometry");
		List<Entity> axes = geometry.getValues("distance");
		List<Entity> angles = geometry.getValues("angle");
		
		LatLonCautra centre = null;
		double rayon;
		Angle angle1;
		Angle angle2;
		
		int typePoint=0;
		String point = geometry.getValue("point");
		if(point==null){
			point = geometry.getValue("nautical_mile");
			typePoint=1;
		}
		if(point==null){
			point = geometry.getValue("lat_long");
			typePoint=2;
		}
		
		//Récupération des coordonnées du centre.
		switch(typePoint){
		//Cas où le centre du cercle est donné par un point de référence
		case 0: centre = pointsRef.get(point);
				break;
		//Cas où le centre du cercle est donné par ses coordonnées Cautra.
		case 1: String[] cautraCoords = point.split("\\s+");
				centre = LatLonCautra.fromCautra(Double.parseDouble(cautraCoords[1])/64., Double.parseDouble(cautraCoords[3])/64.);
				break;
		//Cas où le centre du cercle est donné par sa latitude et longitude.
		case 2: String[] degreesCoords = point.split(" ");
				double latitude = Double.parseDouble(degreesCoords[0].substring(1))
									+ Double.parseDouble(degreesCoords[1])/60.0
									+ Double.parseDouble(degreesCoords[2])/3600.0;
				
				if(degreesCoords[3].substring(0, 1).equals("S")){
					latitude=-latitude;
				}
				double longitude = Double.parseDouble(degreesCoords[4].substring(1))
				+ Double.parseDouble(degreesCoords[5])/60.0
				+ Double.parseDouble(degreesCoords[6])/3600.0;
				
				if(degreesCoords[7].substring(0, 1).equals("E")){
					longitude=-longitude;
				}
				centre = LatLonCautra.fromDegrees(latitude, longitude);
		}
		
		rayon = Double.parseDouble(((String)axes.get(0).getValue()).split("\\s+")[1])/64;
		angle1 = Angle.fromDegrees(Double.parseDouble(((String)angles.get(0).getValue()).split("\\s+")[1])
									/Integer.parseInt(((String)angles.get(0).getValue()).split("\\s+")[0])+90);
		angle2 = Angle.fromDegrees(Double.parseDouble(((String)angles.get(1).getValue()).split("\\s+")[1])
				/Integer.parseInt(((String)angles.get(1).getValue()).split("\\s+")[0])+90);
		
		double diffAngles = angle2.subtract(angle1).degrees;
		double ouvertureAngulaire = diffAngles>0 ? diffAngles  :  360+diffAngles;
		
		//Nombre de points à utiliser pour dessiner l'arc de cercle.
		int nbPoints = (int) ((ouvertureAngulaire/90+1)*precision);
		double pas = ouvertureAngulaire/(nbPoints-1);
		
		for (int i=0; i<nbPoints; i++){
			double x = centre.getCautra()[0]+rayon*angle1.addDegrees(pas*i).cos();
			double y = centre.getCautra()[1]+rayon*angle1.addDegrees(pas*i).sin();
			this.polyligne.add(LatLonCautra.fromCautra(x, y));
		}
		this.setLocations(polyligne);
		
		//on applique l'id atc
		String idAtcName = ellipse.getValue("id_atc");
		if(idAtcName != null) this.applyIdAtc(idAtc.get(idAtcName), palette);
	}
	
	
	/**
	 * Applique les paramètres contenus dans l'id atc
	 */
	private void applyIdAtc(Entity idAtc, PaletteEdimap palette) {
//		String priority = idAtc.getValue("priority");
//		if(priority != null) {
//			this.setZValue(new Double(priority));
//		}
		String foregroundColor = idAtc.getValue("foreground_color");
		
		BasicShapeAttributes attrs = new BasicShapeAttributes(this.getAttributes());
		attrs.setDrawInterior(false);
        attrs.setDrawOutline(true);
        attrs.setOutlineOpacity(1);
		attrs.setOutlineMaterial(new Material(palette.getColor(foregroundColor)));
		
		String lineWidth = idAtc.getValue("line_width");
		if(lineWidth != null) {
			attrs.setOutlineWidth(new Double(lineWidth));
		}
		
		this.setAttributes(attrs);
	}
}
