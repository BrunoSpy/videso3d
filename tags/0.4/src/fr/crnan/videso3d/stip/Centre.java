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

/**
 * Représente une ligne du fichier CENTRE
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class Centre {
	/**
	 * Nom abrégé du centre
	 */
	private String nom;
	
	/**
	 * Nom complet du centre
	 */
	private String identite;
	
	/**
	 * Numéro du centre
	 */
	private Integer numero;
	
	/**
	 * Type du centre
	 */
	private String type;
	
	public Centre(String line){
		this.setNom(line.substring(0, 4).trim());
		this.setIdentite(line.substring(9, 29).trim());
		this.setNumero(new Integer(line.substring(34, 36)));
		this.setType(line.substring(39, 43));
	}

	//Accesseurs
	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getIdentite() {
		return identite;
	}

	public void setIdentite(String identite) {
		this.identite = identite;
	}

	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
