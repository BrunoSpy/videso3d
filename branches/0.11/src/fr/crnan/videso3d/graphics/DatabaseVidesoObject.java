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

/**
 * Extension d'un objet pour Videso contenant le lien à sa base de données
 * <ul> <li>la base de données dont il fait partie (stip, str, ...)</li>
 * <li>le type de données qu'il représente (géré par le controleur)</li></ul>
 * Cet objet fourni aussi la classe parente permettant une restauration autonome.
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public interface DatabaseVidesoObject extends VidesoObject {
	/**
	 * Base de données que l'objet représente<br />
	 * Not null.
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
	
	/**
	 * name of the class for a standalone restauration
	 * @return
	 */
	public String getRestorableClassName();
}
