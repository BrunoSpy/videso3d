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
import fr.crnan.videso3d.layers.FilterableAirspaceLayer;
import fr.crnan.videso3d.layers.LayerSet;
import gov.nasa.worldwind.layers.RenderableLayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Carte Edimap
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class Carte extends LayerSet {
	/**
	 * Ensemble des points de référence de la carte
	 */
	private HashMap<String,LatLonCautra> pointsRef;

	/**
	 * Ensemble des id ATC de la carte
	 */
	private HashMap<String, Entity> idAtc;
	
	
	private String name;
	
	/**
	 * Layers
	 */
	private RenderableLayer surfaceLayer = new RenderableLayer();
	private FilterableAirspaceLayer airspaceLayer = new FilterableAirspaceLayer();
	
	public Carte(Entity carte, PaletteEdimap palette){
		this.add(surfaceLayer);
		this.add(airspaceLayer);
		
		Entity map = carte.getEntity("map");
		this.name = map.getValue("name");
		//récupération des points de référence
		pointsRef = new HashMap<String,LatLonCautra>();
		Iterator<Entity> iterator = map.getValues("ref_point").iterator();
		while(iterator.hasNext()){
			Entity point = iterator.next();
			pointsRef.put(point.getValue("name"), PointEdimap.fromEntity((point)));
		}
		//récupération des id atc
		idAtc = new HashMap<String, Entity>();
		iterator = carte.getValues("id_atc").iterator();
		while(iterator.hasNext()){
			Entity id = iterator.next();
			idAtc.put(id.getValue("name"),id);
		}
		List<Entity> entities = map.getEntity("submap").getValues("entity");
		iterator = entities.iterator();
		while(iterator.hasNext()){
			Entity entity = iterator.next();
			String type = entity.getValue("shape");
			if(type.equalsIgnoreCase("PolylineEntity")){
				String fill = (idAtc.get(entity.getValue("id_atc"))).getValue("fill_visibility");
				if(fill != null && fill.equals("1")){ //polygone
					this.surfaceLayer.addRenderable(new PolygonEdimap(entity, pointsRef, palette, idAtc));
				} else { //polyligne
					this.surfaceLayer.addRenderable(new PolylineEdimap(entity, pointsRef, palette, idAtc));
				}
				
			} else if(type.equalsIgnoreCase("LineEntity")) {
				this.surfaceLayer.addRenderable(new PolylineEdimap(entity, pointsRef, palette, idAtc));
			} else if(type.equalsIgnoreCase("RectangleEntity")){
				this.surfaceLayer.addRenderable(new RectangleEdimap(entity, pointsRef, palette, idAtc));
			} else if(type.equalsIgnoreCase("TextEntity")){
	//			this.addRenderable(new TextEdimap(entity, pointsRef, palette, idAtc);
			} else if(type.equalsIgnoreCase("EllipseEntity")){
				this.surfaceLayer.addRenderable(new EllipseEdimap(entity, pointsRef, palette, idAtc));
			} else if(type.equalsIgnoreCase("MosaiqueEntity")) {
				this.airspaceLayer.addAirspaces(new MosaiqueEntity(entity, pointsRef));
			}
		}
//		Iterator<Entry<String, PointEdimap>> ite = pointsRef.entrySet().iterator();
//		while(ite.hasNext()){
//			Entry<String, PointEdimap> entry = ite.next();
//			System.out.println(entry.getKey() + " x : "+entry.getValue().x() +"; y : "+entry.getValue().y());
//		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}	
	
}
