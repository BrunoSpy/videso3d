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
 * 
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class RadrTechn {

	/**
	 * Nom du radar
	 */
	private String nom;
	/**
	 * Vitesse de l'aérien
	 */
	private Double vitesse;
	/**
	 * Hauteur de l'aérien
	 */
	private Double hauteur;
	/**
	 * Portée Radar en NM
	 */
	private Integer portee; 
	/**
	 * Déport analogique
	 */
	private Boolean deport;
	
	/**
	 * Ligne RADR_TECHN
	 * @param line Ligne RADR_TECHN
	 * @throws ParseException
	 */
	public RadrTechn(String line) throws ParseException{
		String[] word = line.split("\\s+");
		if (word[0].equals("RADR_TECHN")){
			this.setNom(word[1]);
			this.setVitesse(new Double(word[2]));
			this.setHauteur(new Double(word[3]));
			this.setPortee(new Integer(word[4]));
			this.setDeport(word[5].equals("OUI"));
		} else {
			throw new ParseException("RADR_TECHN Parse Error at " + line, 0);
		}
	}


	/**
	 * @return the nom
	 */
	public String getNom() {
		return nom;
	}


	/**
	 * @param nom the nom to set
	 */
	public void setNom(String nom) {
		this.nom = nom;
	}


	/**
	 * @return the vitesse
	 */
	public Double getVitesse() {
		return vitesse;
	}


	/**
	 * @param vitesse the vitesse to set
	 */
	public void setVitesse(Double vitesse) {
		this.vitesse = vitesse;
	}


	/**
	 * @return the hauteur
	 */
	public Double getHauteur() {
		return hauteur;
	}


	/**
	 * @param hauteur the hauteur to set
	 */
	public void setHauteur(Double hauteur) {
		this.hauteur = hauteur;
	}


	/**
	 * @return the portee
	 */
	public Integer getPortee() {
		return portee;
	}


	/**
	 * @param portee the portee to set
	 */
	public void setPortee(Integer portee) {
		this.portee = portee;
	}


	/**
	 * @return the deport
	 */
	public Boolean getDeport() {
		return deport;
	}


	/**
	 * @param deport the deport to set
	 */
	public void setDeport(Boolean deport) {
		this.deport = deport;
	}
	
}
