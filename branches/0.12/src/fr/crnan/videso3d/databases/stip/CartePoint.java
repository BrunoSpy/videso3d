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
 * Représentation d'une ligne PT* du fichier ROUTSECT
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class CartePoint {

	private Integer sectnum;
	
	private Integer flsup;
	
	private String pointRef;
	
	public CartePoint(Integer sectnum, Integer flsup, String line){
		this.setSectnum(sectnum);
		this.setFlsup(flsup);
		this.setPointRef(line.substring(4, 10));
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
	 * @return the pointRef
	 */
	public String getPointRef() {
		return pointRef;
	}

	/**
	 * @param pointRef the pointRef to set
	 */
	public void setPointRef(String pointRef) {
		this.pointRef = pointRef;
	}
	
	
}
