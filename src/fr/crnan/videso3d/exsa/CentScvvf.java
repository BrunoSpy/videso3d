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
 * Représentation d'une ligne CENT_SCVVF
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class CentScvvf {
	/**
	 * Numéro de carré
	 */
	private Integer carre;
	/**
	 * Numéro de sous carré
	 */
	private Integer sousCarre;
	/**
	 * Liste des VVF
	 */
	private String vvfs;
	/**
	 * Liste des plafonds
	 */
	private String plafonds;
	/**
	 * Liste des plafonds
	 */
	private String planchers;

	private int numPlancher = 0;
	
	
	public CentScvvf(String line, Boolean formated) throws ParseException{
		if(!formated){
			//suppression du ; en fin de ligne
			line = line.substring(0, line.length() - 1);
		}
		int i = formated ? 0 : 1;
		String[] word = line.split(formated ? "\\s+" : ",");
		int length = word.length;
		if (word[0].equals(formated ? "CENT_SCVVF" : "CENT.SCVVF")){
			this.setCarre(new Integer(word[1+i]));
			this.setSousCarre(new Integer(word[2+i]));
			if(3+i < length) {
				this.setVvfs(word[3+i]);
				this.setPlafonds(word, i);
				this.setPlanchers(word, i);
			}
		} else {
			throw new ParseException("CENT_SCVVF Parse Error at " + line, 0);
		}
	}
//	public CentScvvf(QSqlRecord record){
//		this.record = record;
//	}
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
	 * @return the sousCarre
	 */
	public Integer getSousCarre() {
		return sousCarre;
	}
	/**
	 * @param sousCarre the sousCarre to set
	 */
	public void setSousCarre(Integer sousCarre) {
		this.sousCarre = sousCarre;
	}
	/**
	 * @return the vvfs
	 */
	public String getVvfs() {
		return vvfs;
	}
	/**
	 * @param vvfs the vvfs to set
	 */
	public void setVvfs(String vvfs) {
		this.vvfs = vvfs;
	}
	/**
	 * @return the plafonds
	 */
	public String getPlafonds() {
		return plafonds;
	}
	/**
	 * @param plafonds the plafonds to set
	 */
	public void setPlafonds(String[] plafonds, int formated) {
		String plafond = "";
		int numVvf = this.getVvfs().split("\\\\").length;
		int i = 0;
		if(numVvf == 1){ //un seul vvf, l'entrée ne comporte pas de "/"
			plafond = plafonds[4+formated];
			i++;
		} else {
			while(plafond.split("\\\\").length < numVvf){
				plafond += plafonds[4+formated+i];
				i++;
			}
		}
		this.numPlancher = 4+i;
		this.plafonds = plafond;
	}
	/**
	 * @return the planchers
	 */
	public String getPlanchers() {
		return planchers;
	}
	/**
	 * @param planchers the planchers to set
	 */
	public void setPlanchers(String[] planchers, int formated) {
		String plancher = "";
		int numVvf = this.getVvfs().split("\\\\").length;
		int i=0;
		if(numVvf == 1){
			plancher = planchers[this.numPlancher+formated+i];
		} else {
			while(plancher.split("\\\\").length < numVvf){
				plancher += planchers[this.numPlancher+formated+i];
				i++;
			}
		}
		this.planchers = plancher;
	}
	
	
}
