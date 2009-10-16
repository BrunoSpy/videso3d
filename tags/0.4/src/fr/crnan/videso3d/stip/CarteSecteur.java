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

import fr.crnan.videso3d.geom.Latitude;
import fr.crnan.videso3d.geom.Longitude;

/**
 * Représentation des données d'une carte secteur
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class CarteSecteur {

	/**
	 * Numéro du secteur
	 */
	private Integer sectnum;
	/**
	 * FL Inf de la carte secteur
	 */
	private Integer flinf;
	/**
	 * FL Sup de la carte secteur
	 */
	private Integer flsup;
	/**
	 * Latitude de l'étiquette
	 */
	private Latitude lateti;
	/**
	 * Longitude de l'étiquette
	 */
	private Longitude longeti;
	/**
	 * Construit une représentation à partir du numéro du secteur et la ligne NIV
	 * @param sectnum Numéro du secteur
	 * @param line Ligne NIV
	 */
	public CarteSecteur(Integer sectnum, String line){
		this.setSectnum(sectnum);
		this.setFlinf(new Integer(line.substring(4, 7)));
		this.setFlsup(new Integer(line.substring(8, 11)));
	}
	/**
	 * Ajoute les données contenues par la ligne ETI
	 * @param line Ligne ETI du fichier ROUTSECT
	 */
	public void addEtiquette(String line){
		this.setLateti(new Latitude(new Integer(line.substring(4,6)),
									new Integer(line.substring(7,9)),
									new Integer(line.substring(10,12))));
		this.setLongeti(new Longitude(new Integer(line.substring(15,18)),
									  new Integer(line.substring(19,21)),
									  new Integer(line.substring(22, 24)),
									  line.substring(25,26)));
	}
	/**
	 * @return the sectnum
	 */
	public Integer getSectnum() {
		return sectnum;
	}
	/**
	 * @param sectnum the sectnum to set
	 */
	public void setSectnum(Integer sectnum) {
		this.sectnum = sectnum;
	}
	/**
	 * @return the flinf
	 */
	public Integer getFlinf() {
		return flinf;
	}
	/**
	 * @param flinf the flinf to set
	 */
	public void setFlinf(Integer flinf) {
		this.flinf = flinf;
	}
	/**
	 * @return the flsup
	 */
	public Integer getFlsup() {
		return flsup;
	}
	/**
	 * @param flsup the flsup to set
	 */
	public void setFlsup(Integer flsup) {
		this.flsup = flsup;
	}
	/**
	 * @return the lateti
	 */
	public Latitude getLateti() {
		return lateti;
	}
	/**
	 * @param lateti the lateti to set
	 */
	public void setLateti(Latitude lateti) {
		this.lateti = lateti;
	}
	/**
	 * @return the longeti
	 */
	public Longitude getLongeti() {
		return longeti;
	}
	/**
	 * @param longeti the longeti to set
	 */
	public void setLongeti(Longitude longeti) {
		this.longeti = longeti;
	}
	
	
}
