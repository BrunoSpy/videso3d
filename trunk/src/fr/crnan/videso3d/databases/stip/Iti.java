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

import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.Couple;

/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class Iti {

	/**
	 * Point d'entrée de l'iti
	 */
	private String entree;
	/**
	 * Point de sortie de l'iti
	 */
	private String sortie;
	/**
	 * Niveau inférieur de validité
	 */
	private Integer flinf;
	/**
	 * Niveau supérieur de validité
	 */
	private Integer flsup;
	/**
	 * Liste ordonnée des balises composant l'iti
	 */
	private List<Couple<String, Boolean>> balises = new LinkedList<Couple<String, Boolean>>();
	
	/**
	 * Construit un iti à partir d'une ligne
	 * @param line
	 */
	public Iti(String line){
		this.setEntree(line.substring(7, 12).trim());
		this.setSortie(line.substring(15, 20).trim());
		this.setFlinf(new Integer(line.substring(23, 26)));
		if(line.length() > 33){
			this.setFlsup(new Integer(line.substring(31, 34)));
		} else {
			this.setFlsup(800);
		}
	}

	/**
	 * @return the entree
	 */
	public String getEntree() {
		return entree;
	}

	/**
	 * @param entree the entree to set
	 */
	public void setEntree(String entree) {
		this.entree = entree;
	}

	/**
	 * @return the sortie
	 */
	public String getSortie() {
		return sortie;
	}

	/**
	 * @param sortie the sortie to set
	 */
	public void setSortie(String sortie) {
		this.sortie = sortie;
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
	 * @return the balises
	 */
	public List<Couple<String, Boolean>> getBalises() {
		return balises;
	}

	public void addBalises(String line){
		String[] list = line.trim().split("\\s+");
		for(Integer i=0; i<list.length; i++ ){
			if(list[i].endsWith("/")){
				//balise "travers"
				balises.add(new Couple<String, Boolean>(list[i].substring(0, list[i].length()-1), false));
			} else {
				balises.add(new Couple<String, Boolean>(list[i], true));
			}
		}
	}
	
}
