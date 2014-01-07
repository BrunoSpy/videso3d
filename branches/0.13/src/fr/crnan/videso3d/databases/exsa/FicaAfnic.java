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
 * Représentation d'une ligne FICA_AFNIC
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class FicaAfnic {
	/**
	 * Abonné
	 */
	private String abonne;
	/**
	 * Carré ed la mosaïque
	 */
	private Integer carre;
	/**
	 * Plancher
	 */
	private Integer plancher;
	/**
	 * Plafond
	 */
	private Integer plafond;
	/**
	 * Premier code de la suite
	 */
	private Integer firstCode;
	/**
	 * Dernier code de la suite
	 */
	private Integer lastCode;


	public FicaAfnic(String line, Boolean formated) throws ParseException{
		if(!formated){
			//suppression du ; en fin de ligne
			line = line.substring(0, line.length() - 1);
		}
		String[] word = line.split(formated ? "\\s+" : ",");
		int i = formated ? 0 : 1;
		if (word[0].equals(formated ? "FICA_AFNIC" : "FICA.AFNIC")){
			this.setAbonne(word[1+i]);
			this.setCarre(new Integer(word[2+i]));
			this.setPlancher(new Integer(word[3+i]));
			this.setPlafond(new Integer(word[4+i]));
			this.setFirstCode(new Integer(word[5+i]));
			this.setLastCode(new Integer(word[6+i]));
		} else {
			throw new ParseException("FICA_AFNIC Parse Error at " + line, 0);
		}
	}

	/**
	 * @return the abonne
	 */
	public String getAbonne() {
		return abonne;
	}

	/**
	 * @param abonne the abonne to set
	 */
	public void setAbonne(String abonne) {
		this.abonne = abonne;
	}

	/**
	 * @return the carre
	 */
	public Integer getCarre() {
		return carre;
	}

	/**
	 * @param carre the carre to set
	 */
	public void setCarre(Integer carre) {
		this.carre = carre;
	}

	/**
	 * @return the plancher
	 */
	public Integer getPlancher() {
		return plancher;
	}

	/**
	 * @param plancher the plancher to set
	 */
	public void setPlancher(Integer plancher) {
		this.plancher = plancher;
	}

	/**
	 * @return the plafond
	 */
	public Integer getPlafond() {
		return plafond;
	}

	/**
	 * @param plafond the plafond to set
	 */
	public void setPlafond(Integer plafond) {
		this.plafond = plafond;
	}

	/**
	 * @return the firstCode
	 */
	public Integer getFirstCode() {
		return firstCode;
	}

	/**
	 * @param firstCode the firstCode to set
	 */
	public void setFirstCode(Integer firstCode) {
		this.firstCode = firstCode;
	}

	/**
	 * @return the lastCode
	 */
	public Integer getLastCode() {
		return lastCode;
	}

	/**
	 * @param lastCode the lastCode to set
	 */
	public void setLastCode(Integer lastCode) {
		this.lastCode = lastCode;
	}
	
	
}
