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

package fr.crnan.videso3d.graphs;

import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.stip.Stip;
import fr.crnan.videso3d.databases.stpv.Stpv;

/**
 * Contenu d'une cellule
 * @author Bruno Spyckerelle
 * @version 0.2.0
 */
public class CellContent {
	
	private int type;
	
	private DatabaseManager.Type base;
	
	private int id = 0;
	
	private String name;

	/**
	 * 
	 * @param type
	 * @param id
	 */
	public CellContent(DatabaseManager.Type base, int type, int id, String name){
		this.type = type;
		this.base = base;
		this.id = id;
		this.name = name;
	}


	public int getType() {
		return type;
	}


	public int getId() {
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public DatabaseManager.Type getBase() {
		return base;
	}

	@Override
	public String toString(){
		return name;
	}
	
	/**
	 * Returns a String to be used in a clipboard for example
	 * @return 
	 */
	public String toFormattedString(){
		switch (base) {
		case STIP:
			return Stip.getString(type, id);
		case STPV:
			return Stpv.getString(type, id);
		default:
			return null;
		}
	}



}

