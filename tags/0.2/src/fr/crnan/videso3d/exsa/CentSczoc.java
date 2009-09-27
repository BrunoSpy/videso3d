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
 * Ligne CENT_SCZOC
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class CentSczoc {
	/**
	 * Carré de la mosaïque
	 */
	private Integer carre;
	/**
	 * Sous-carré de la mosaïque
	 */
	private Integer sousCarre;
	/**
	 * Zone d'occultation
	 */
	private String zone;
	/**
	 * Plafond de la zone
	 */
	private Integer plafond;
	
	public CentSczoc(String line) throws ParseException{
		String[] word = line.split("\\s+");
		if (word[0].equals("CENT_SCZOC")){
			this.setCarre(new Integer(word[1]));
			this.setSousCarre(word[2]);
			this.setZone(word[3]);
			this.setPlafond(word[4]);
		} else {
			throw new ParseException("CENT_SCZOC Parse Error at " + line, 0);
		}
	}

	public Integer getCarre() {
		return carre;
	}

	public void setCarre(Integer carre) {
		this.carre = carre;
	}

	public Integer getSousCarre() {
		return sousCarre;
	}

	public void setSousCarre(String string) {
		if(string.compareTo("##") == 0) {
			this.sousCarre = 0;
		} else {
			this.sousCarre = new Integer(string);
		}
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public Integer getPlafond() {
		return plafond;
	}

	public void setPlafond(String string) {
		if(string.compareTo("###") == 0){
			this.plafond = 660;
		} else {
			this.plafond = new Integer(string);
		}
	}
	
	
}
