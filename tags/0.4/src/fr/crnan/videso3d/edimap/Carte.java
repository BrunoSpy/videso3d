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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Carte Edimap
 * @author Bruno Spyckerelle
 *
 */
public class Carte extends QGraphicsItem {
	/**
	 * Ensemble des points de référence de la carte
	 */
	private HashMap<String,PointEdimap> pointsRef;

	/**
	 * Ensemble des id ATC de la carte
	 */
	private HashMap<String, Entity> idAtc;
	
	
	private String name;
	
	public Carte(Entity carte, PaletteEdimap palette){
		Entity map = carte.getEntity("map");
		this.name = map.getValue("name");
		//récupération des points de référence
		pointsRef = new HashMap<String,PointEdimap>();
		Iterator<Entity> iterator = map.getValues("ref_point").iterator();
		while(iterator.hasNext()){
			Entity point = iterator.next();
			pointsRef.put(point.getValue("name"), new PointEdimap(point));
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
				new PolylineEdimap(this, entity, pointsRef, palette, idAtc);
			} else if(type.equalsIgnoreCase("LineEntity")) {
				new PolylineEdimap(this, entity, pointsRef, palette, idAtc);
			} else if(type.equalsIgnoreCase("RectangleEntity")){
				new RectangleEdimap(this, entity, pointsRef, palette, idAtc);
			} else if(type.equalsIgnoreCase("TextEntity")){
				new TextEdimap(this, entity, pointsRef, palette, idAtc);
			} else if(type.equalsIgnoreCase("EllipseEntity")){
				new EllipseEdimap(this, entity, pointsRef, palette, idAtc);
			}
		}
//		Iterator<Entry<String, PointEdimap>> ite = pointsRef.entrySet().iterator();
//		while(ite.hasNext()){
//			Entry<String, PointEdimap> entry = ite.next();
//			System.out.println(entry.getKey() + " x : "+entry.getValue().x() +"; y : "+entry.getValue().y());
//		}
	}
	
	@Override
	public QRectF boundingRect() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void paint(QPainter painter, QStyleOptionGraphicsItem option,
			QWidget widget) {
		// TODO Auto-generated method stub
		
	}
	
}
