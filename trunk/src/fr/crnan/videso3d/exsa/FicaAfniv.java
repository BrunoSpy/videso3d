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
 * Représentation d'une ligne FICA_AFNIV
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class FicaAfniv {

	/**
	 * Abonné
	 */
	private String abonne;
	
	/**
	 * Carré de la mosaïque
	 */
	private Integer carré;
	/**
	 * Plancher
	 */
	private Integer plancher;
	/**
	 * Plafond
	 */
	private Integer plafond;
	/**
	 * Booléen d'élimination des carrés
	 */
	private Boolean elimine;
	/**
	 * Premier code de la suite
	 */
	private Integer firstCode = 0;
	/**
	 * Dernier code de la suite
	 */
	private Integer lastCode = 0;


	public FicaAfniv(String line, Boolean formated) throws ParseException{
		if(!formated){
			//suppression du ; en fin de ligne
			line = line.substring(0, line.length() - 1);
		}
		String[] word = line.split(formated ? "\\s+" : ",");
		int length = word.length;
		int i = formated ? 0 : 1;
		if (word[0].equals(formated ? "FICA_AFNIV" : "FICA.AFNIV")){
			this.setAbonne(word[1+i]);
			this.setCarré(new Integer(word[2+i]));
			this.setPlancher(new Integer(word[3+i]));
			this.setPlafond(new Integer(word[4+i]));
			this.setElimine(word[5+i]);
			if(6+i<length) this.setFirstCode(word[6+i]);
			if(7+i<length) this.setLastCode(word[7+i]);
		} else {
			throw new ParseException("FICA_AFNIV Parse Error at " + line, 0);
		}
	}

	public String getAbonne() {
		return abonne;
	}

	public void setAbonne(String abonne) {
		this.abonne = abonne;
	}

	public Integer getCarré() {
		return carré;
	}

	public void setCarré(Integer carré) {
		this.carré = carré;
	}

	public Integer getPlancher() {
		return plancher;
	}

	public void setPlancher(Integer plancher) {
		this.plancher = plancher;
	}

	public Integer getPlafond() {
		return plafond;
	}

	public void setPlafond(Integer plafond) {
		this.plafond = plafond;
	}

	public Boolean getElimine() {
		return elimine;
	}

	public void setElimine(String string) {
		this.elimine = string.equalsIgnoreCase("OUI");
	}

	public Integer getFirstCode() {
		return firstCode;
	}

	public void setFirstCode(String string) {
		if(string.startsWith("###") || string.isEmpty()){
			this.firstCode = 0;
		} else {
			this.firstCode = new Integer(string);
		}
	}

	public Integer getLastCode() {
		return lastCode;
	}

	public void setLastCode(String string) {
		if(string.startsWith("###") || string.isEmpty()){
			this.lastCode = 0;
		} else {
			this.lastCode = new Integer(string);
		}
	}
	
	
}
