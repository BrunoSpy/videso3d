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
import gov.nasa.worldwind.render.SurfaceEllipse;

import java.util.HashMap;
import java.util.List;

/**
 * Construit une ellipse à partir de l'entité EllipseEntity.
 * Ne prend pas en compte l'entité "construction", ni l'azimuth.
 * Voir le DDI Edimap pour plus de détails sur la construction d'un arc de cercle
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class EllipseEdimap extends SurfaceEllipse {

	private double dx = 0.0;
	private double dy = 0.0;
	
	public EllipseEdimap(Entity ellipse,
			  HashMap<String, LatLonCautra> pointsRef, 
			  PaletteEdimap palette,
			  HashMap<String, Entity> idAtc){
		
		
		Entity geometry = ellipse.getEntity("geometry");
		List<Entity> axes = geometry.getValues("distance");
		List<Entity> angles = geometry.getValues("angle");
		String[] length = ((String) axes.get(0).getValue()).split("\\s+");
		double a = new Double(length[1]); //demi grand axe
		length = ((String) axes.get(1).getValue()).split("\\s+");
		double b = new Double(length[1]); //demi petit axe
		String[] angle = ((String) angles.get(0).getValue()).split("\\s+");
		int angle1 =  (new Integer(angle[1]) * 16) / new Integer(angle[0]);
		angle = ((String) angles.get(1).getValue()).split("\\s+");
		int angle2 = (new Integer(angle[1]) * 16) / new Integer(angle[0]);
		
		this.setRect(-a, -b, 2*a, 2*b);
		this.setStartAngle(angle1);
		this.setSpanAngle(angle2-angle1);
		
		Entity point = ( (List<Entity>) geometry.getValue()).get(0);
		if(point.getKeyword().equalsIgnoreCase("point")){
			PointEdimap pt = pointsRef.get(((String)point.getValue()).replaceAll("\"", ""));
			dx = pt.x();
			dy = pt.y();
		} else {
			String[] points = ((String)point.getValue()).split("\\s+");
			dx = new Double(points[1]);
			dy = new Double(points[3])*-1.0;
		}
		this.translate(dx, dy);
	}
}
