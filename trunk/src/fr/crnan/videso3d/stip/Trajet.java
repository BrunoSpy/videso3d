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

public class Trajet {

	private String eclatement;
	private String raccordement;
	private String type;
	private Integer fl;
	
	private List<Couple<String, String>> conditions = new LinkedList<Couple<String,String>>();
	
	private List<Couple<String, Boolean>> balises = new LinkedList<Couple<String,Boolean>>();
	
	public Trajet(String line){
		this.setEclatement(line.substring(7, 12));
		this.setRaccordement(line.substring(15, 20));
		this.setType(line.substring(23, 25));
		if(!line.substring(31, 34).trim().isEmpty()) {
			this.setFl(new Integer(line.substring(31, 34).trim()));
		} else {
			this.setFl(800);
		}
		this.getConditions().add(new Couple<String, String>(line.substring(39, 46), line.substring(46, 47)));
		if(line.length() > 56 && !line.substring(49, 57).trim().isEmpty())
			this.getConditions().add(new Couple<String, String>(line.substring(49, 56), line.substring(56, 57)));
		if(line.length() > 66 && !line.substring(59, 67).trim().isEmpty())
			this.getConditions().add(new Couple<String, String>(line.substring(59, 66), line.substring(66, 77)));
		if(line.length() > 76 && !line.substring(69, 77).trim().isEmpty())
			this.getConditions().add(new Couple<String, String>(line.substring(69, 76), line.substring(76, 77)));
		
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

	public String getEclatement() {
		return eclatement;
	}

	public void setEclatement(String eclatement) {
		this.eclatement = eclatement;
	}

	public String getRaccordement() {
		return raccordement;
	}

	public void setRaccordement(String raccordement) {
		this.raccordement = raccordement;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getFl() {
		return fl;
	}

	public void setFl(Integer fl) {
		this.fl = fl;
	}

	public List<Couple<String, String>> getConditions() {
		return conditions;
	}

	public void setConditions(List<Couple<String, String>> conditions) {
		this.conditions = conditions;
	}

	public List<Couple<String, Boolean>> getBalises() {
		return balises;
	}

	public void setBalises(List<Couple<String, Boolean>> balises) {
		this.balises = balises;
	}
	
	
	
}
