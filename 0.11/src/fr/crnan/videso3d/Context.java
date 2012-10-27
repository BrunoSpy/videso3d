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
package fr.crnan.videso3d;

import java.util.List;

import org.jdesktop.swingx.JXTaskPane;

/**
 * Permet de fournir les infos contextuelles relatives aux objets d'une base de données
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public abstract class Context {

	/**
	 * Get  informations
	 * @param type
	 * @param name
	 * @return liste des {@link JXTaskPane}
	 */
	public abstract List<JXTaskPane> getTaskPanes(int type, String name);
	
}
