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
package fr.crnan.videso3d.databases.exsa;

import java.text.ParseException;

/**
 * Représentation d'une ligne CENT_SCODF
 * @author Adrien Vidal
 *
 */
public class CentSCodf {

	private String name = "";
	private String debut = "";
	private String fin = "";
	/**
	 * les espaces de visualisation
	 */
	private String espaces = "";
	
	public CentSCodf(String line, boolean formated)throws ParseException{
		if(!formated){
			//suppression du ; en fin de ligne
			line = line.substring(0, line.length() - 1);
		}
		String[] word = line.split(formated ? "\\s+" : ",");
		if(word.length<7 || !word[0].equals(formated ? "CENT_SCODF" : "CENT.SCODF")){
			throw new ParseException(line, 0);
		}
		debut = word[1];
		fin = word[2];
		name = word[3];
		espaces = word[6];
	}
	
	public String getDebut(){
		return debut;
	}
	
	public String getFin(){
		return fin;
	}
	
	public String getEspaces(){
		return espaces;
	}
	
	public String getName(){
		return name;
	}
	
}
