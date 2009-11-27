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

package fr.crnan.videso3d.stip;

import java.util.List;
import java.util.LinkedList;

/**
 * Représentation d'un lieu : carte 1 suivie de ses cartes M, D et R
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class Lieux {
	/**
	 * Indicateur OACI
	 */
	private String oaci;
	/**
	 * Paramètre de distance
	 */
	private String distanceType;
	/**
	 * Numéro du centre
	 */
	private Character[] centre = {0, 0, 0, 0};
	/**
	 * Distance
	 */
	private Integer[] distance = {0, 0, 0, 0};
	/**
	 * Paramètre PP
	 */
	private String[] pp = {new String(), new String(), new String(), new String()};
	/**
	 * Paramètre NC
	 */
	private String[] nc = {new String(), new String(), new String(), new String()};;
	
	/**
	 * Consignes
	 */
	private List<Consigne> consignes = new LinkedList<Consigne>();
	
	
	/**
	 * Construit un Lieu à partir d'une carte 1
	 * @param line Carte 1
	 */
	public Lieux(String line){
		this.oaci = line.substring(2, 6);
		this.distanceType = line.substring(12, 14);
		for(int i=0; i < 4;i++){
			String tmp = line.substring(16 + i*13, 17+i*13).trim();
			if(!tmp.isEmpty()) this.centre[i] = tmp.charAt(0);
			tmp = line.substring(18 + i*13, 22+i*13).trim();
			if(!tmp.isEmpty()) this.distance[i] = new Integer(tmp);
			tmp = line.substring(23 + i*13, 25+i*13).trim();
			if(!tmp.isEmpty()) this.pp[i] = tmp;
			tmp = line.substring(26 + i*13, 28+i*13).trim();
			if(!tmp.isEmpty()) this.nc[i] = tmp;
		}
	}

	public void addCarte(String line) {
		consignes.add(new Consigne(line));
	}

	/**
	 * @return the oaci
	 */
	public String getOaci() {
		return oaci;
	}

	/**
	 * @return the distanceType
	 */
	public String getDistanceType() {
		return distanceType;
	}

	/**
	 * @return the centre
	 */
	public Character[] getCentre() {
		return centre;
	}

	/**
	 * @return the distance
	 */
	public Integer[] getDistance() {
		return distance;
	}

	/**
	 * @return the pp
	 */
	public String[] getPp() {
		return pp;
	}

	/**
	 * @return the nc
	 */
	public String[] getNc() {
		return nc;
	}

	/**
	 * @return the consignes
	 */
	public List<Consigne> getConsignes() {
		return consignes;
	}
	
	
}
