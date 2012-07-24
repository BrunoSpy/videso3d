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
package fr.crnan.videso3d.databases.stip;
/**
 * Couple de balise interdit
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class BalInt {

	private Boolean uir;
	private Boolean fir;
	/**
	 * Première balise du couple
	 */
	private String balise1;
	/**
	 * Deuxième balise du couple
	 */
	private String balise2;
	/**
	 * balise imposée
	 */
	private String balise;
	/**
	 * Faux si travers balise imposée
	 */
	private Boolean appartient;
	
	public BalInt(String line){
		this.setFir(line.substring(0, 3).equals("FIR"));
		this.setUir(line.substring(4, 7).equals("UIR"));
		this.setBalise1(line.substring(8, 13).trim());
		this.setBalise2(line.substring(16, 21).trim());
		this.setBalise(line.substring(24, 29).trim());
		this.setAppartient(line.substring(29, 30).equals("/"));
	}

	/**
	 * @return the uir
	 */
	public Boolean getUir() {
		return uir;
	}

	/**
	 * @param uir the uir to set
	 */
	public void setUir(Boolean uir) {
		this.uir = uir;
	}

	/**
	 * @return the fir
	 */
	public Boolean getFir() {
		return fir;
	}

	/**
	 * @param fir the fir to set
	 */
	public void setFir(Boolean fir) {
		this.fir = fir;
	}

	/**
	 * Première balise du couple
	 * @return the balise1
	 */
	public String getBalise1() {
		return balise1;
	}

	/**
	 * @param balise1 the balise1 to set
	 */
	public void setBalise1(String balise1) {
		this.balise1 = balise1;
	}

	/**
	 * Deuxième balise du couple
	 * @return the balise2
	 */
	public String getBalise2() {
		return balise2;
	}

	/**
	 * @param balise2 the balise2 to set
	 */
	public void setBalise2(String balise2) {
		this.balise2 = balise2;
	}

	/**
	 * Balise imposée du couple
	 * @return the balise
	 */
	public String getBalise() {
		return balise;
	}

	/**
	 * @param balise the balise to set
	 */
	public void setBalise(String balise) {
		this.balise = balise;
	}

	/**
	 * Faux si travers balise imposée
	 * @return the appartient
	 */
	public Boolean getAppartient() {
		return appartient;
	}

	/**
	 * @param appartient the appartient to set
	 */
	public void setAppartient(Boolean appartient) {
		this.appartient = appartient;
	}
	
	
}
