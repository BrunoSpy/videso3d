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

import fr.crnan.videso3d.DatabaseManager;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Highlightable;
/**
 * Un objet graphique pour Videso comprend 5 éléments :
 * <ul><li>l'objet en lui-même (renderable, airspace, ...)</li>
 * <li>une annotation</li>
 * <li>la base de données dont il fait partie (stip, str, ...)</li>
 * <li>le type de données qu'il représente (géré par le controleur)</li>
 * <li>un nom</li></ul>
 * @author Bruno Spyckerelle
 * @version 2.1.0
 */
public interface VidesoObject extends Highlightable{
	
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
	
	/**
	 * Base de données que l'objet représente
	 * @return
	 */
	public DatabaseManager.Type getDatabaseType();
	
	/**
	 * 
	 * @param type Type de base de données
	 */
	public void setDatabaseType(DatabaseManager.Type type);

	/**
	 * 
	 * @param type Type de données représenté, utilisé par VidesoController.showObject(int, name)
	 */
	public void setType(int type);
	
	/**
	 * 
	 * @return Type de données représenté
	 */
	public int getType();
	
	public String getName();
	
	public void setName(String name);
	
	public Object getNormalAttributes();
	public Object getHighlightAttributes();
	
}