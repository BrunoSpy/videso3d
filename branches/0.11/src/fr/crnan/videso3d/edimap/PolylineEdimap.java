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

import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.graphics.DatabaseSurfacePolyline;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * Construit une polyline à partir d'une entité Edimap
 * @author Bruno Spyckerelle
 * @version 0.2.2
 */
public class PolylineEdimap extends DatabaseSurfacePolyline {

	private int typeCarte= -1;
		
	private HashMap<String, LatLonCautra> pointsRef;
		
	private LinkedList<LatLon> polyligne = new LinkedList<LatLon>();
	
	@SuppressWarnings("unchecked")
	public PolylineEdimap(Entity polyline,
			HashMap<String,	LatLonCautra> pointsRef,
			PaletteEdimap palette,
			HashMap<String, Entity> idAtc){
		super(new BasicShapeAttributes());
		this.setName(polyline.getValue("name"));
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
		if(priority != null) this.setPriority(new Integer(priority));
		String foregroundColor = polyline.getValue("foreground_color");
		if(foregroundColor != null) {
			BasicShapeAttributes attrs = new BasicShapeAttributes(this.getAttributes());
			attrs.setOutlineMaterial(new Material(palette.getColor(foregroundColor)));
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
		String priority = idAtc.getValue("priority");
		if(priority != null) {
			this.setPriority(new Integer(priority));
		}
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
	
	public void addPoint(Entity point){
		if(point.getKeyword().equalsIgnoreCase("point")){
			//point par référence
			this.polyligne.add(pointsRef.get(((String)point.getValue()).replaceAll("\"", "")));
		} else {
			this.polyligne.add(PointEdimap.fromEntity(point));
		}
	}
	
	@Override
	public Type getDatabaseType() {
		return Type.Edimap;
	}

	@Override
	public void setType(int type) {
		this.typeCarte = type;
	}

	@Override
	public int getType() {
		return this.typeCarte;
	}

}
