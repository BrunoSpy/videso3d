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
 * Ligne CENT_Z_OCC
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class CentZOcc {

	/**
	 * Nom de la zone
	 */
	private String name;
	/**
	 * Nom de la touche d'espace
	 */
	private String espace;
	/**
	 * Terrains d'arrivée associés
	 */
	private String terrains;
	
	public CentZOcc(String line, Boolean formated) throws ParseException{
		if(!formated){
			//suppression du ; en fin de ligne
			line = line.substring(0, line.length() - 1);
		}
		int i = formated ? 0 : 1;
		String[] word = line.split(formated ? "\\s+" : ",");
		int length = word.length;
		if (word[0].equals(formated ? "CENT_Z_OCC" : "CENT.Z_OCC")){
			this.setName(word[1+i]);
			if(2+i<length) this.setEspace(word[2+i]);
			if(3+i<length) this.setTerrains(word[3+i]);
		} else {
			throw new ParseException("CENT_Z_OCC Parse Error at " + line, 0);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEspace() {
		return espace;
	}

	public void setEspace(String espace) {
		this.espace = espace;
	}

	public String getTerrains() {
		return terrains;
	}

	public void setTerrains(String terrains) {
		this.terrains = terrains;
	}
	
	
}
