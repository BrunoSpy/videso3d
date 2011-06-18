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

import fr.crnan.videso3d.stip.Stip;

/**
 * Contenu d'une cellule
 * @author Bruno Spyckerelle
 * @version 0.1.2
 */
public class CellContent {

	public static String TYPE_BALISE = "balise";
	
	public static String TYPE_ITI = "iti";
	
	public static String TYPE_ROUTE = "route";
	
	public static String TYPE_TRAJET = "trajet";
	
	public static String TYPE_TRAJET_GROUPE = "trajet_groupe";
	
	public static String TYPE_CONNEXION = "connexion";
	
	public static String TYPE_STAR = "star";
	
	private String type = null;
	
	private int id = 0;
	
	private String name;

	/**
	 * 
	 * @param type
	 * @param id
	 */
	public CellContent(String type, int id, String name){
		this.type = type;
		this.id = id;
		this.name = name;
	}


	public String getType() {
		return type;
	}


	public int getId() {
		return id;
	}
	
	public String getName(){
		return name;
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
		String selection = new String();
		if(this.getType() == TYPE_ITI){
			selection = Stip.itiToString(getId());
		}
		return selection;
	}
}

