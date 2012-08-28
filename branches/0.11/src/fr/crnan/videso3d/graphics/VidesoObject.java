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
package fr.crnan.videso3d.graphics;

import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Highlightable;
/**
 * Un objet graphique pour Videso comprend 3 éléments :
 * <ul><li>l'objet en lui-même (renderable, airspace, ...)</li>
 * <li>une annotation</li>
 * <li>un nom</li></ul>
 * Tous les objets pour Videso doivent implémenter {@link Highlightable} et {@link Restorable}<br/>
 * Un objet pour Videso lié à une base de données doit implémenter {@link DatabaseVidesoObject}
 * @author Bruno Spyckerelle
 * @version 3.0
 */
public interface VidesoObject extends Highlightable, Restorable{
	
	/**
	 * Enregistre le texte de l'annotation
	 * @param text
	 */
	public void setAnnotation(String text);

	/**
	 * Retourne l'annotation à la position <code>pos</code>
	 * @param pos Position de l'annotation par rapport au Globe
	 * @return
	 */
	public VidesoAnnotation getAnnotation(Position pos);
	
	public String getName();
	
	public void setName(String name);
	
	public Object getNormalAttributes();
	public Object getHighlightAttributes();
	
}