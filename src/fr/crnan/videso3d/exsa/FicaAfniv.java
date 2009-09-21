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
 * @version 0.1
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
	private Integer firstCode;
	/**
	 * Dernier code de la suite
	 */
	private Integer lastCode;


	public FicaAfniv(String line) throws ParseException{
		String[] word = line.split("\\s+");
		if (word[0].equals("FICA_AFNIV")){
			this.setAbonne(word[1]);
			this.setCarré(new Integer(word[2]));
			this.setPlancher(new Integer(word[3]));
			this.setPlafond(new Integer(word[4]));
			this.setElimine(word[5]);
			this.setFirstCode(word[6]);
			this.setLastCode(word[7]);
		} else {
			throw new ParseException("FICA_AFNIV Parse Error at " + line, 0);
		}
	}

//	public FicaAfniv(QSqlRecord record){
//		this.record = record;
//	}

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
		if(string.equalsIgnoreCase("####")){
			this.firstCode = 0;
		} else {
			this.firstCode = new Integer(string);
		}
	}

	public Integer getLastCode() {
		return lastCode;
	}

	public void setLastCode(String string) {
		if(string.equalsIgnoreCase("####")){
			this.lastCode = 0;
		} else {
			this.lastCode = new Integer(string);
		}
	}
	
	
}
