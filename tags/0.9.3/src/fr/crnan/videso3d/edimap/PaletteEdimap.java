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

import fr.crnan.videso3d.Pallet;


/**
 * Palette de couleurs
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class PaletteEdimap extends Pallet{

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
			this.palette.put(color.getValue("name"), Color.decode(color.getValue("value")));
		}
	}
	/**
	 * Construction d'une palette par défaut en cas d'absence de fichier palette
	 */
	public PaletteEdimap() {
		this.palette.put("Noir", Color.decode("#000000"));
		this.palette.put("Lilas", Color.decode("#766a6a"));
		this.palette.put("Vert salle de bain", Color.decode("#535e4e"));
		this.palette.put("Lie de vin", Color.decode("#514948"));
		this.palette.put("Jaune", Color.decode("#cbcb65"));
		this.palette.put("Gris clair", Color.decode("#5a5a5a"));
		this.palette.put("Gris", Color.decode("#424242"));
		this.palette.put("Gris fonce", Color.decode("#3d3d3d"));
		this.palette.put("Orange", Color.decode("#9c5d00"));
		this.palette.put("Gris kaki", Color.decode("#1f3022"));
	}

	public Color getColor(String color){
		return palette.get(color);
	}
}
