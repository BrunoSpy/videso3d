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
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.UserFacingText;

import java.util.HashMap;
import java.util.List;

/**
 * Construit un objet graphique de type Text à partir d'une entité TextEntity
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class TextEdimap extends UserFacingText {
	
	private LatLon latlon;
	
	/**
	 * Constructeur
	 * @param text Entity TextEntity à afficher
	 * @param pointsRef HashMap<String, PointEdimap> Ensemble des points de référence de la carte
	 * @param palette PaletteEdimap Palette de couleur.
	 * @param idAtc HashMap<String, Entity> Ensemble des idAtc de la carte
	 */
	public TextEdimap(Entity text,
					  HashMap<String, LatLonCautra> pointsRef, 
					  PaletteEdimap palette,
					  HashMap<String, Entity> idAtc){
		super(text.getValue("text_content"), Position.ZERO);
		//translation du texte
		Entity point = ((List<Entity>) text.getEntity("geometry").getValue()).get(0);
		if(point.getKeyword().equalsIgnoreCase("point")){
			latlon = pointsRef.get(((String)point.getValue()).replaceAll("\"", ""));
		} else {
			String[] points = ((String)point.getValue()).split("\\s+");
			latlon = LatLonCautra.fromCautra(new Double(points[1])/64, new Double(points[3])/64);
		}
		this.setPosition(new Position(latlon, 100.0));
		//this.scale(10.0, 10.0);
		//idAtc
		String idAtcName = text.getValue("id_atc");
		if(idAtcName != null) this.applyIdAtc(idAtc.get(idAtcName), palette);
		//paramètres spécifiques
		String priority = text.getValue("priority");
//		if(priority != null) this.setZValue(new Double(priority));
		String foregroundColor = text.getValue("foreground_color");
		if(foregroundColor != null) this.setColor(palette.getColor(foregroundColor));
	}
	
	private void applyIdAtc(Entity idAtc, PaletteEdimap palette){
		String priority = idAtc.getValue("priority");
	//	if(priority != null) this.setZValue(new Double(priority));
		String foregroundColor = idAtc.getValue("foreground_color");
		if(foregroundColor != null) this.setColor(palette.getColor(foregroundColor));

	}	
}
