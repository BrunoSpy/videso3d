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
 * Représentation d'une latitude
 * @author Bruno Spyckerelle
 * @author David Granado
 * @version 0.3
 */
public class Latitude extends Coordonnee{
	
	/**
	 * Signe latitude : "S" ou "N" - utile pour les terrains OACI, sinon "N" par défaut - DG.
	 */
	private String sens;

	public Latitude(Integer degres, Integer minutes) {
		super(degres, minutes);
		this.sens = "N";
	}
	
	public Latitude(Integer degres, Integer minutes, String sens) {
		super(degres, minutes);
		this.sens = sens;
	}
	
	public Latitude(Integer degres, Integer minutes, Integer secondes){
		super(degres, minutes, secondes);
		this.sens = "N";
	}

	/**
	 * Le signe de la latitude est utile pour les terrains OACI. DG
	 */
	public Latitude(Integer degres, Integer minutes, Integer secondes, String sens){
		super(degres, minutes, secondes);
		this.sens = sens;
	}
	
	
	/**
	 * Constructeur à partir d'une Latitude de la forme "dd:mm:ss S" (possibilité espace(s) à la place des ":") - par défaut le sens est N. DG
	 * @param latitude
	 */
	public Latitude(String latitude){
		String[] words = latitude.trim().split("[(( *):( *))( +)]");
		this.degres = new Integer(words[0]);
		this.minutes = new Integer(words[1]);
		this.secondes = new Integer(words[2]);
		if (words.length<4) { //pas de sens renseigné
			this.sens = "N"; //N par défaut
		} else {
			this.sens = words[3];
		}
	}
	
	public Latitude(Double x) {
		if(x.compareTo(0.0) < 0){
			x *= -1;
			this.sens = "S";
		} else {
			this.sens = "N";
		}
		this.degres = x.intValue();
		this.minutes = new Double((x - this.degres)*60).intValue();
		this.secondes = new Double(((x - this.degres)*60 - this.minutes)*60).intValue();
	}

	public Double toDecimal(){
		if (sens.equalsIgnoreCase("N")){
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
