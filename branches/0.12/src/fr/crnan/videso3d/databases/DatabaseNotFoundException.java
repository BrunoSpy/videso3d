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
package fr.crnan.videso3d.databases;

import fr.crnan.videso3d.DatasManager;


/**
 * Exception thrown when a mandatory database is not found
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class DatabaseNotFoundException extends Exception {

	private DatasManager.Type type;
	
	public DatabaseNotFoundException(DatasManager.Type type){
		super();
		this.type = type;
	}
	
	public DatasManager.Type getDatabaseType(){
		return type;
	}
	
}