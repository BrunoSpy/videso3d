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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Annotation;
/**
 * Objet WorldWind (Renderable, Airspace, ...) avec annotation intégrée.<br />
 * @author Bruno Spyckerelle
 * @version 1.0
 */
public interface ObjectAnnotation{
	
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
	public Annotation getAnnotation(Position pos);

}