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
package fr.crnan.videso3d.geom;

import java.text.DecimalFormat;

/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.3.2
 */
public class Coordonnee {

	protected Integer degres;
	protected Integer minutes;
	protected Integer secondes;

	public Coordonnee(){
		super();
	}
	
	public Coordonnee(Integer degres, Integer minutes, Integer secondes){
		this.degres = degres;
		this.minutes = minutes;
		this.secondes = secondes;
	}
	
	public Coordonnee(Integer degres, Integer minutes){
		this.degres = degres;
		this.minutes = minutes;
		this.secondes = 0;
	}
	
	/**
	 * Construit une coordonnee à partir d'une chaîne de la forme dd:mm:ss
	 * @param coordonnee
	 */
	public Coordonnee(String coordonnee){
		String[] words = coordonnee.split(":");
		this.degres = new Integer(words[0]);
		this.minutes = new Integer(words[1]);
		this.secondes = new Integer(words[2]);
	}
	
	public Coordonnee(Double x) {
		this.degres = x.intValue();
		this.minutes = new Double((x - this.degres)*60).intValue();
		this.secondes = new Double(((x - this.degres)*60 - this.minutes)*60).intValue();
	}

	/**
	 * Renvoit la longitude sous la forme dd:mm:ssS
	 */
	public String toString() {
		DecimalFormat format = new DecimalFormat("00");
		return format.format(this.degres) + ":" + format.format(this.minutes) + ":" + format.format(this.secondes);
	}

	/**
	 * Convertit la latitude en système décimal
	 * @return Latitude en décimal
	 */
	public Double toDecimal() {
		return new Double(this.degres) + new Double(this.minutes) / 60 + new Double(this.secondes) / 3600;
	}

	
	//Accesseurs
	public Integer getDegres() {
		return degres;
	}

	public void setDegres(Integer degres) {
		this.degres = degres;
	}

	public Integer getMinutes() {
		return minutes;
	}

	public void setMinutes(Integer minutes) {
		this.minutes = minutes;
	}

	public Integer getSecondes() {
		return secondes;
	}

	public void setSecondes(Integer secondes) {
		this.secondes = secondes;
	}

}