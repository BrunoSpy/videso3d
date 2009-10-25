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
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceQuad;

import java.util.HashMap;
import java.util.List;

public class RectangleEdimap extends SurfaceQuad {

	private String name;
	
	HashMap<String, LatLonCautra> pointsRef;
	
	public RectangleEdimap(Entity entity,
						   HashMap<String, LatLonCautra> pointsRef,
						   PaletteEdimap palette,
						   HashMap<String, Entity> idAtc){
		this.pointsRef = pointsRef;
		this.name = entity.getValue("name");
		List<Entity> points = (List<Entity>) entity.getEntity("geometry").getValue();
		Entity point1 = points.get(0);
		Entity point2 = points.get(1);
		LatLonCautra corner;
		LatLonCautra corner2;
		if(point1.getKeyword().equalsIgnoreCase("point")){
			corner = pointsRef.get(((String)point1.getValue()).replaceAll("\"", ""));
		} else {
			String[] coord1 = ((String)point1.getValue()).split("\\s+");
			corner = LatLonCautra.fromCautra(new Double(coord1[1])/64*LatLonCautra.NM,
					new Double(coord1[3])/64*LatLonCautra.NM);
		}
		if(point2.getKeyword().equalsIgnoreCase("point")){
			corner2 = pointsRef.get(((String)point2.getValue()).replaceAll("\"", ""));
			//size = new QSizeF(coord2.x()-corner.x(), coord2.y()-corner.y());
		} else {
			String[] coord2 = ((String)point2.getValue()).split("\\s+");
			corner2 = LatLonCautra.fromCautra(new Double(coord2[1])/64*LatLonCautra.NM,
					new Double(coord2[3])/64*LatLonCautra.NM);
		//	size = new QSizeF(new Double(coord2[1])-corner.x(), (new Double(coord2[3])*-1)-corner.y());
		}
		LatLonCautra center = LatLonCautra.fromCautra((corner2.getCautra()[0]-corner.getCautra()[0])/2,
				(corner2.getCautra()[1]-corner.getCautra()[1])/2);
		double size = Math.abs(corner2.getCautra()[0]-corner.getCautra()[0]);
		this.setCenter(center);
		this.setSize(size, size);
		this.setHeight(0);
		//on applique l'id atc
		String idAtcName = entity.getValue("id_atc");
		if(idAtcName != null) this.applyIdAtc(idAtc.get(idAtcName), palette);
		//si des paramètres supplémentaires sont présents, ils écrasent ceux présents dans l'id atc
		String priority = entity.getValue("priority");
	//	if(priority != null) this.setZValue(new Double(priority));
		String foregroundColor = entity.getValue("foreground_color");
		if(foregroundColor != null){
			ShapeAttributes attrs = this.getAttributes();
			attrs.setInteriorMaterial(new Material(palette.getColor(foregroundColor)));
			this.setAttributes(attrs);
		}
	}

	/**
	 * Applique les paramètres contenus dans l'id atc
	 */
	private void applyIdAtc(Entity idAtc, PaletteEdimap palette) {
		String priority = idAtc.getValue("priority");
//		if(priority != null) this.setZValue(new Double(priority));
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
	}

	
}
