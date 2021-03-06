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

package fr.crnan.videso3d.databases.edimap;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatasManager.Type;
import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.graphics.DatabaseVidesoObject;
import fr.crnan.videso3d.graphics.VidesoSurfaceQuad;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;

import java.util.HashMap;
import java.util.List;
/**
 * Rectangle
 * @author Bruno Spyckerelle
 * @version 0.2.3
 */
public class RectangleEdimap extends VidesoSurfaceQuad implements DatabaseVidesoObject {

	private int typeCarte=-1;
	
	HashMap<String, LatLonCautra> pointsRef;
	
	@SuppressWarnings("unchecked")
	public RectangleEdimap(Entity entity,
						   HashMap<String, LatLonCautra> pointsRef,
						   PaletteEdimap palette,
						   HashMap<String, Entity> idAtc){
		super(new BasicShapeAttributes());
		this.pointsRef = pointsRef;
		this.setName(entity.getValue("name"));
		List<Entity> points = (List<Entity>) entity.getEntity("geometry").getValue();
		Entity point1 = points.get(0);
		Entity point2 = points.get(1);
		LatLonCautra corner;
		LatLonCautra corner2;
		if(point1.getKeyword().equalsIgnoreCase("point")){
			corner = pointsRef.get(((String)point1.getValue()).replaceAll("\"", ""));
		} else {
			corner = PointEdimap.fromEntity(point1);
		}
		if(point2.getKeyword().equalsIgnoreCase("point")){
			corner2 = pointsRef.get(((String)point2.getValue()).replaceAll("\"", ""));
		} else {
			corner2 = PointEdimap.fromEntity(point2);
		}
		LatLonCautra center = LatLonCautra.fromCautra((corner2.getCautra()[0]+corner.getCautra()[0])/2.0,
				(corner2.getCautra()[1]+corner.getCautra()[1])/2.0);
		double width = Math.abs(corner2.getCautra()[0]-corner.getCautra()[0])*LatLonCautra.NM;
		double height = Math.abs(corner2.getCautra()[1]-corner.getCautra()[1])*LatLonCautra.NM;
		this.setCenter(center);
		this.setSize(width, height);
		//on applique l'id atc
		String idAtcName = entity.getValue("id_atc");
		if(idAtcName != null) this.applyIdAtc(idAtc.get(idAtcName), palette);
		//si des paramètres supplémentaires sont présents, ils écrasent ceux présents dans l'id atc
		String priority = entity.getValue("priority");
		if(priority != null) this.setPriority(new Integer(priority));
		String foregroundColor = entity.getValue("foreground_color");
		if(foregroundColor != null){
			ShapeAttributes attrs = this.getAttributes();
			attrs.setInteriorMaterial(new Material(palette.getColor(foregroundColor)));
			this.setAttributes(attrs);
		}
		
		BasicShapeAttributes attrsH = new BasicShapeAttributes(this.getAttributes());
		attrsH.setInteriorMaterial(new Material(Pallet.makeBrighter(attrsH.getInteriorMaterial().getDiffuse())));
		this.setHighlightAttributes(attrsH);
	}

	/**
	 * Applique les paramètres contenus dans l'id atc
	 */
	private void applyIdAtc(Entity idAtc, PaletteEdimap palette) {
		String priority = idAtc.getValue("priority");
		if(priority != null) this.setPriority(new Integer(priority));
		String foregroundColor = idAtc.getValue("foreground_color");
		BasicShapeAttributes attrs = new BasicShapeAttributes(this.getAttributes());
		String fill = idAtc.getValue("fill_visibility");
		if(foregroundColor != null && fill != null && palette.getColor(foregroundColor) != null){
			if(fill.equalsIgnoreCase("1")) {
				attrs.setInteriorMaterial(new Material(palette.getColor(foregroundColor)));
				attrs.setDrawInterior(true);
				attrs.setDrawOutline(false);
				attrs.setInteriorOpacity(1.0);
			} else {
				attrs.setOutlineMaterial(new Material(palette.getColor(foregroundColor)));
				attrs.setDrawOutline(true);
				attrs.setDrawInterior(false);
			}
		}
		this.setAttributes(attrs);
	}

	@Override
	public DatasManager.Type getDatabaseType() {
		return DatasManager.Type.Edimap;
	}

	@Override
	public void setDatabaseType(DatasManager.Type type) {
		//Ne rien faire, le type sera toujours Edimap
	}

	@Override
	public void setType(int type) {
		this.typeCarte = type;
	}

	@Override
	public int getType() {
		return this.typeCarte;
	}

	@Override
	public String getRestorableClassName() {
		return VidesoSurfaceQuad.class.getName();
	}

	
	
}
