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


/**
 * Représentation d'une longitude
 * @author Bruno Spyckerelle
 * @version 0.2.1
 */
public class Longitude extends Coordonnee {

	/**
	 * Signe longitude : "O" ou "E"
	 */
	private String sens;
	
	public Longitude(Integer degres, Integer minutes, Integer secondes, String sens){
		super(degres, minutes, secondes);
		this.sens = sens;
	}

	public Longitude(Integer degres, Integer minutes, String sens){
		super(degres, minutes);
		this.sens = sens;
	}
	
	public Longitude(String longitude){
		String[] words = longitude.split(":");
		this.degres = new Integer(words[0]);
		this.minutes = new Integer(words[1]);
		Integer length = words[2].length();
		this.secondes = new Integer(words[2].substring(0,length -1));
		this.sens = words[2].substring(length-1, length);
	}

	public Longitude(Double y) {
		if(y.compareTo(0.0) < 0){
			y *= -1;
			this.sens = "O";
		} else {
			this.sens = "E";
		}
		this.degres = y.intValue();
		this.minutes = new Double((y - this.degres)*60).intValue();
		this.secondes = new Double(((y - this.degres)*60 - this.minutes)*60).intValue();
	}

	public Double toDecimal(){
		if (sens.equalsIgnoreCase("E")){
			return super.toDecimal();
		} else {
			return super.toDecimal() * -1;
		}
	}
	
	public String toString(){
		return super.toString() + getSens();
	}
	
	public String getSens() {
		return sens;
	}

	public void setSens(String sens) {
		this.sens = sens;
	}
	
}
