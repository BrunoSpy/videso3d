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

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * Palette de couleurs
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class PaletteEdimap {

	private HashMap<String, Color> palette = new HashMap<String, Color>();
	/**
	 * Construit la palette à partir de l'entité palette
	 * @param palette Entity
	 */
	public PaletteEdimap(Entity palette){
		List<Entity> colors = palette.getValues("color");
		Iterator<Entity> iterator = colors.iterator();
		while(iterator.hasNext()){
			Entity color = iterator.next();
			this.palette.put(color.getValue("name"), new QColor("#"+color.getValue("value").substring(2, 8)));
		}
	}
	/**
	 * Construction d'une palette par défaut en cas d'absence de fichier palette
	 */
	public PaletteEdimap() {
		this.palette.put("Noir", new QColor("#000000"));
		this.palette.put("Lilas", new QColor("#766a6a"));
		this.palette.put("Vert salle de bain", new QColor("#535e4e"));
		this.palette.put("Lie de vin", new QColor("#514948"));
		this.palette.put("Jaune", new QColor("#cbcb65"));
		this.palette.put("Gris clair", new QColor("#5a5a5a"));
		this.palette.put("Gris", new QColor("#424242"));
		this.palette.put("Gris fonce", new QColor("#3d3d3d"));
		this.palette.put("Orange", new QColor("#9c5d00"));
		this.palette.put("Gris kaki", new QColor("#1f3022"));
	}

	public QColor getColor(String color){
		return palette.get(color);
	}
}
