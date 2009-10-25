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
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfacePolyline;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * Construit une polyline à partir d'une entité Edimap
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class PolylineEdimap extends SurfacePolyline {
	
	private String name;
		
	private HashMap<String, LatLonCautra> pointsRef;
		
	private LinkedList<LatLon> polyligne = new LinkedList<LatLon>();
	
	public PolylineEdimap(Entity polyline,
			HashMap<String,	LatLonCautra> pointsRef,
			PaletteEdimap palette,
			HashMap<String, Entity> idAtc){
		this.name = polyline.getValue("name");
		this.pointsRef = pointsRef;
		List<Entity> points = (LinkedList<Entity>) polyline.getEntity("geometry").getValue();
		Iterator<Entity> iterator = points.iterator();
		while(iterator.hasNext()){
			this.addPoint(iterator.next());
		}
		this.setLocations(polyligne);
		//on applique l'id atc
		String idAtcName = polyline.getValue("id_atc");
		if(idAtcName != null) this.applyIdAtc(idAtc.get(idAtcName), palette);
		//paramètres spécifiques
		String priority = polyline.getValue("priority");
	//	if(priority != null) this.setZValue(new Double(priority));
		String foregroundColor = polyline.getValue("foreground_color");
		if(foregroundColor != null) {
			BasicShapeAttributes attrs = new BasicShapeAttributes(this.getAttributes());
			attrs.setInteriorMaterial(new Material(palette.getColor(foregroundColor)));
			this.setAttributes(attrs);
		}
		if(polyline.getValue("polygone") != null){
			this.setClosed(polyline.getValue("polygone").equalsIgnoreCase("1"));
		}
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
		String fill = idAtc.getValue("fill_visibility");
		if(foregroundColor != null && fill != null){
			if(fill.equalsIgnoreCase("1")) {
				BasicShapeAttributes attrs = new BasicShapeAttributes(this.getAttributes());
				attrs.setInteriorMaterial(new Material(palette.getColor(foregroundColor)));
				attrs.setDrawOutline(false);
				this.setAttributes(attrs);
			} else {
				BasicShapeAttributes attrs = new BasicShapeAttributes(this.getAttributes());
				attrs.setOutlineMaterial(new Material(palette.getColor(foregroundColor)));
				attrs.setDrawOutline(true);
				attrs.setDrawInterior(false);
				this.setAttributes(attrs);
			}
		}
//		String lineWidth = idAtc.getValue("line_width");
//		if(lineWidth != null) {
//			this.width = new Double(lineWidth);
//		}
	}
	
	public void addPoint(Entity point){
		if(point.getKeyword().equalsIgnoreCase("point")){
			//point par référence
			this.polyligne.add(pointsRef.get(((String)point.getValue()).replaceAll("\"", "")));
		} else {
			String[] points = ((String)point.getValue()).split("\\s+");
			this.polyligne.add(LatLonCautra.fromCautra(new Double(points[1])/64*LatLonCautra.NM,
					new Double(points[3])/64*LatLonCautra.NM));
		}
	}
	
	public String getName(){
		return this.name;
	}
}
