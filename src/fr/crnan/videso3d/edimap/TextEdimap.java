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

import com.trolltech.qt.gui.QGraphicsItemInterface;
import com.trolltech.qt.gui.QGraphicsTextItem;
/**
 * Construit un objet graphique de type Text à partir d'une entité TextEntity
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class TextEdimap extends QGraphicsTextItem {
	
	double dx = 0.0;
	
	double dy = 0.0;
	/**
	 * Constructeur
	 * @param parent QGraphicsItemInterface Parent de l'objet texte
	 * @param text Entity TextEntity à afficher
	 * @param pointsRef HashMap<String, PointEdimap> Ensemble des points de référence de la carte
	 * @param palette PaletteEdimap Palette de couleur.
	 * @param idAtc HashMap<String, Entity> Ensemble des idAtc de la carte
	 */
	public TextEdimap(QGraphicsItemInterface parent,
					  Entity text,
					  HashMap<String, PointEdimap> pointsRef, 
					  PaletteEdimap palette,
					  HashMap<String, Entity> idAtc){
		this.setParentItem(parent);
		this.setPlainText(text.getValue("text_content"));
		//translation du texte
		Entity point = ((List<Entity>) text.getEntity("geometry").getValue()).get(0);
		if(point.getKeyword().equalsIgnoreCase("point")){
			PointEdimap pt = pointsRef.get(((String)point.getValue()).replaceAll("\"", ""));
			dx = pt.x();
			dy = pt.y();
		} else {
			String[] points = ((String)point.getValue()).split("\\s+");
			dx = new Double(points[1]);
			dy = new Double(points[3])*-1.0;
		}
		this.setPos(dx, dy);
		this.scale(10.0, 10.0);
		//idAtc
		String idAtcName = text.getValue("id_atc");
		if(idAtcName != null) this.applyIdAtc(idAtc.get(idAtcName), palette);
		//paramètres spécifiques
		String priority = text.getValue("priority");
		if(priority != null) this.setZValue(new Double(priority));
		String foregroundColor = text.getValue("foreground_color");
		if(foregroundColor != null) this.setDefaultTextColor(palette.getColor(foregroundColor));
	}
	
	private void applyIdAtc(Entity idAtc, PaletteEdimap palette){
		String priority = idAtc.getValue("priority");
		if(priority != null) this.setZValue(new Double(priority));
		String foregroundColor = idAtc.getValue("foreground_color");
		if(foregroundColor != null) this.setDefaultTextColor(palette.getColor(foregroundColor));

	}	
}
