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
 * 
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class Connexion {

	private String terrain;

	private String type;

	private String perfo;

	private Integer flinf;

	private Integer flsup;

	private String vitesseCompar;

	private Integer vitesseValue;

	private List<Couple<String,Boolean>> balises = new LinkedList<Couple<String,Boolean>>();

	public Connexion(String line){
		this.setTerrain(line.substring(0, 4));
		this.setType(line.substring(7, 8));
		this.setPerfo(line.substring(9, 10));
		this.setFlinf(line.substring(11, 14).trim());
		this.setFlsup(line.substring(15, 18).trim());
		this.setVitesseCompar(line.substring(19, 20));
		this.setVitesseValue(new Integer(line.substring(20, 23)));
		this.addBalise(line.substring(29, 35).trim());
		this.addBalise(line.substring(36, 42).trim());
		for(int i = 1; i<=2;i++){
			if(line.length()>42+13*i){
				String bal = line.substring(29+14*i, 35+14*i).trim();
				if(!bal.isEmpty()){
					this.addBalise(bal);
					this.addBalise(line.substring(36+14*i, 42+14*i).trim());
				}
			}
		}
	}

	private void addBalise(String balise){
		if(balise.endsWith("/")){
			this.balises.add(new Couple<String, Boolean>(balise.substring(0, balise.length()-1), false));
		} else {
			this.balises.add(new Couple<String, Boolean>(balise, true));
		}
	}

	public String getTerrain() {
		return terrain;
	}

	public void setTerrain(String terrain) {
		this.terrain = terrain;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPerfo() {
		return perfo;
	}

	public void setPerfo(String perfo) {
		this.perfo = perfo;
	}

	public Integer getFlinf() {
		return flinf;
	}

	public void setFlinf(String flinf) {
		if(flinf.isEmpty()){
			this.setFlinf(0);
		} else {
			this.setFlinf(new Integer(flinf));
		}
	}
	
	public void setFlinf(Integer flinf) {
		this.flinf = flinf;
	}

	public Integer getFlsup() {
		return flsup;
	}

	public void setFlsup(String flsup) {
		if(flsup.isEmpty()){
			this.setFlsup(800);
		} else {
			this.setFlsup(new Integer(flsup));
		}
	}
	
	public void setFlsup(Integer flsup) {
		this.flsup = flsup;
	}

	public String getVitesseCompar() {
		return vitesseCompar;
	}

	public void setVitesseCompar(String vitesseCompar) {
		this.vitesseCompar = vitesseCompar;
	}

	public Integer getVitesseValue() {
		return vitesseValue;
	}

	public void setVitesseValue(Integer vitesseValue) {
		this.vitesseValue = vitesseValue;
	}

	public List<Couple<String, Boolean>> getBalises() {
		return balises;
	}

}
