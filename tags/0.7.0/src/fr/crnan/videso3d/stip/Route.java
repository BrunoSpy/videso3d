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

import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.Couple;
/**
 * Représentation d'une route définie dans le fichier stip ROUTE
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class Route {
	/**
	 * Classe d'espace de la route
	 */
	private String espace;
	/**
	 * Liste ordonnée des balises composant la route
	 */
	private List<Couple<String, Boolean>> balises = new LinkedList<Couple<String, Boolean>>();
	/**
	 * Sens de parcours de la route entre deux balises
	 */
	private List<String> sens = new LinkedList<String>();
	/**
	 * Sens de parcours d'une route entre deux balises
	 * @author Bruno Spyckerelle
	 */
	public static enum Sens {Unique, Interdit, Fermé, DoubleSens };
	/**
	 * Nom de la route
	 */
	private String name;
	
	
	public Route(){
		super();
	}
	
	/**
	 * Représentation d'une route définie par le fichier ROUTE
	 * @param line
	 */
	public Route(String line){
		this.setName(line.substring(0,7).trim());
		this.setEspace(line.substring(11, 12));
		this.addBalises(line.substring(15,80));
	}
	
//	public Route(QSqlRecord record){
//		this.record = record;
//		this.setName(record.value("name").toString());
//	}
	
	public void addBalises(String line){
		String[] list = line.split("\\s+");
		Integer length = list.length;
		for(Integer i=0; i<length; i++ ){
			//i pair : balise, i impair : sens de parcours
			if(i==0 || i%2 == 0){
				if(list[i].endsWith("/")){
					//balise "travers"
					balises.add(new Couple<String, Boolean>(list[i].substring(0, list[i].length()-1), false));
				} else {
					balises.add(new Couple<String, Boolean>(list[i], true));
				}
			} else {
					sens.add(list[i]);
			}
		}
	}
	
	/**
	 * @return the espace
	 */
	public String getEspace() {
		return espace;
	}
	/**
	 * @param espace the espace to set
	 */
	public void setEspace(String espace) {
		this.espace = espace;
	}
	/**
	 * @return the balises
	 */
	public List<Couple<String, Boolean>> getBalises() {
		return balises;
	}
	/**
	 * @param balises the balises to set
	 */
	public void setBalises(List<Couple<String, Boolean>> balises) {
		this.balises = balises;
	}
	/**
	 * @return the sens
	 */
	public List<String> getSens() {
		return sens;
	}
	/**
	 * @param sens the sens to set
	 */
	public void setSens(List<String> sens) {
		this.sens = sens;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
}
