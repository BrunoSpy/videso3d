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
import java.util.List;

import com.trolltech.qt.core.QPointF;
import com.trolltech.qt.core.QRectF;
import com.trolltech.qt.core.QSizeF;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QGraphicsItemInterface;
import com.trolltech.qt.gui.QGraphicsRectItem;
import com.trolltech.qt.gui.QGraphicsSceneHoverEvent;
import com.trolltech.qt.gui.QPen;

public class RectangleEdimap extends QGraphicsRectItem {

	private String name;
	
	HashMap<String, PointEdimap> pointsRef;
	
	public RectangleEdimap(QGraphicsItemInterface parent, 
						   Entity entity,
						   HashMap<String, PointEdimap> pointsRef,
						   PaletteEdimap palette,
						   HashMap<String, Entity> idAtc){
		this.setParentItem(parent);
		this.pointsRef = pointsRef;
		this.name = entity.getValue("name");
		List<Entity> points = (List<Entity>) entity.getEntity("geometry").getValue();
		Entity point1 = points.get(0);
		Entity point2 = points.get(1);
		QPointF corner;
		QSizeF size;
		if(point1.getKeyword().equalsIgnoreCase("point")){
			corner = pointsRef.get(((String)point1.getValue()).replaceAll("\"", ""));
		} else {
			String[] coord1 = ((String)point1.getValue()).split("\\s+");
			corner = new QPointF(new Double(coord1[1]), new Double(coord1[3])*-1);
		}
		if(point2.getKeyword().equalsIgnoreCase("point")){
			QPointF coord2 = pointsRef.get(((String)point2.getValue()).replaceAll("\"", ""));
			size = new QSizeF(coord2.x()-corner.x(), coord2.y()-corner.y());
		} else {
			String[] coord2 = ((String)point2.getValue()).split("\\s+");
			size = new QSizeF(new Double(coord2[1])-corner.x(), (new Double(coord2[3])*-1)-corner.y());
		}
		this.setRect(new QRectF(corner, size));
		//on applique l'id atc
		String idAtcName = entity.getValue("id_atc");
		if(idAtcName != null) this.applyIdAtc(idAtc.get(idAtcName), palette);
		//si des paramètres supplémentaires sont présents, ils écrasent ceux présents dans l'id atc
		String priority = entity.getValue("priority");
		if(priority != null) this.setZValue(new Double(priority));
		String foregroundColor = entity.getValue("foreground_color");
		if(foregroundColor != null) this.setBrush(new QBrush(palette.getColor(foregroundColor)));
	}

	/**
	 * Applique les paramètres contenus dans l'id atc
	 */
	private void applyIdAtc(Entity idAtc, PaletteEdimap palette) {
		String priority = idAtc.getValue("priority");
		if(priority != null) this.setZValue(new Double(priority));
		String foregroundColor = idAtc.getValue("foreground_color");
		String fill = idAtc.getValue("fill_visibility");
		if(foregroundColor != null && fill != null){
			if(fill.equalsIgnoreCase("1")) {
				this.setBrush(new QBrush(palette.getColor(foregroundColor)));
				this.setPen(new QPen(Qt.PenStyle.NoPen));
			} else {
				this.setPen(new QPen(palette.getColor(foregroundColor)));
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.trolltech.qt.gui.QAbstractGraphicsShapeItem#hoverEnterEvent(com.trolltech.qt.gui.QGraphicsSceneHoverEvent)
	 */
	@Override
	public void hoverEnterEvent(QGraphicsSceneHoverEvent event) {
		super.hoverEnterEvent(event);
		System.out.println(name);
	}
	
}
