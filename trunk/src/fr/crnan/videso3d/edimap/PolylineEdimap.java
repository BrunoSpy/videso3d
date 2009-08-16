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
import java.util.LinkedList;
import java.util.List;

import com.trolltech.qt.core.QPointF;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QGraphicsItemInterface;
import com.trolltech.qt.gui.QGraphicsPolygonItem;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPen;
import com.trolltech.qt.gui.QPolygonF;
import com.trolltech.qt.gui.QStyleOptionGraphicsItem;
import com.trolltech.qt.gui.QWidget;

/**
 * Construit une polyline à partir d'une entité Edimap
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class PolylineEdimap extends QGraphicsPolygonItem {
	
	private String name;

	private Boolean polygon = false;
	
	private double width = 0.0;
	
	private QPen pen = new QPen();
	
	private HashMap<String, PointEdimap> pointsRef;
	
	QPolygonF polyligne = new QPolygonF();
	
	public PolylineEdimap(QGraphicsItemInterface parent, 
			Entity polyline, HashMap<String, PointEdimap> pointsRef,
			PaletteEdimap palette,
			HashMap<String, Entity> idAtc){
		this.setParentItem(parent);
		this.name = polyline.getValue("name");
		this.pointsRef = pointsRef;
		List<Entity> points = (LinkedList<Entity>) polyline.getEntity("geometry").getValue();
		Iterator<Entity> iterator = points.iterator();
		while(iterator.hasNext()){
			this.addPoint(iterator.next());
		}
		this.setPolygon(polyligne);
		//on applique l'id atc
		String idAtcName = polyline.getValue("id_atc");
		if(idAtcName != null) this.applyIdAtc(idAtc.get(idAtcName), palette);
		//paramètres spécifiques
		String priority = polyline.getValue("priority");
		if(priority != null) this.setZValue(new Double(priority));
		String foregroundColor = polyline.getValue("foreground_color");
		if(foregroundColor != null) this.setBrush(new QBrush(palette.getColor(foregroundColor)));
		if(polyline.getValue("polygone") != null){
			this.polygon = polyline.getValue("polygone").equalsIgnoreCase("1");
		}
	}
	
	/**
	 * Applique les paramètres contenus dans l'id atc
	 */
	private void applyIdAtc(Entity idAtc, PaletteEdimap palette) {
		String priority = idAtc.getValue("priority");
		if(priority != null) {
			this.setZValue(new Double(priority));
		}
		String foregroundColor = idAtc.getValue("foreground_color");
		String fill = idAtc.getValue("fill_visibility");
		if(foregroundColor != null && fill != null){
			if(fill.equalsIgnoreCase("1")) {
				this.setBrush(new QBrush(palette.getColor(foregroundColor)));
				this.pen.setStyle(Qt.PenStyle.NoPen);
			} else {
				this.pen.setColor(palette.getColor(foregroundColor));
			}
		}
		String lineWidth = idAtc.getValue("line_width");
		if(lineWidth != null) {
			this.width = new Double(lineWidth);
		}
	}
	
	public void addPoint(Entity point){
		if(point.getKeyword().equalsIgnoreCase("point")){
			//point par référence
			this.polyligne.add(pointsRef.get(((String)point.getValue()).replaceAll("\"", "")));
		} else {
			String[] points = ((String)point.getValue()).split("\\s+");
			this.polyligne.add(new QPointF(new Double(points[1]), new Double(points[3])*-1));
		}
	}

	/* (non-Javadoc)
	 * @see com.trolltech.qt.gui.QGraphicsPolygonItem#paint(com.trolltech.qt.gui.QPainter, com.trolltech.qt.gui.QStyleOptionGraphicsItem, com.trolltech.qt.gui.QWidget)
	 */
	@Override
	public void paint(QPainter painter, QStyleOptionGraphicsItem option,
			QWidget widget) {
		this.pen.setWidthF(width/option.levelOfDetail());	
		if(!polygon){
			painter.setPen(this.pen);
			painter.setBrush(this.brush());
			painter.drawPolyline(polyligne);
		}else{
			painter.setPen(this.pen);
			painter.setBrush(this.brush());
			painter.drawPolygon(polyligne);
		}
	}
	
}
