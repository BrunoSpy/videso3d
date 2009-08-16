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
package fr.crnan.videso3d.exsa;

import java.text.ParseException;

/**
 * Ligne CENT_Z_OCC
 * @author Bruno Spyckerelle
 * @version 0.1
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
	
	public CentZOcc(String line) throws ParseException{
		String[] word = line.split("\\s+");
		if (word[0].equals("CENT_Z_OCC")){
			this.setName(word[1]);
			this.setEspace(word[2]);
			this.setTerrains(word[3]);
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
