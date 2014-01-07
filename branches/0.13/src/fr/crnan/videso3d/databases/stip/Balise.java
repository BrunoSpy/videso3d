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

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.geom.Latitude;
import fr.crnan.videso3d.geom.Longitude;

/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.5
 */
public class Balise {
	/**
	 * Précision des coordonnées à la seconde
	 */
	private Boolean precision = false ;
	/**
	 * Indicatif de la balise
	 */
	private String indicatif;
	/**
	 * ETRG (balise étrangère - permet un traitement spécifique par le STIP.
	 * NB : toutes les balises étrangères ne sont pas nécessairement ETRG
	 */
	private Boolean etrg = false;
	/**
	 * Latitude
	 */
	private Latitude latitude;
	/**
	 * Longitude
	 */
	private Longitude longitude;
	/**
	 * Balise publiée
	 */
	private Boolean publication;
	/**
	 * Abrégé du centre
	 */
	private String centre;
	/**
	 * Définition de la balise
	 */
	private String definition;
	/**
	 * Nom SCCAG
	 */
	private String sccag;
	/**
	 *  Liste des secteurs et de leurs limites
	 */
	private LinkedList<Couple<String,Integer>> secteurs;
	
	public Balise(){
		super();
	}
	
	/**
	 * Construit une balise avec la précision souhaitée
	 * @param precision {@link Boolean} True si précision à la seconde
	 */
	public Balise(Boolean precision){
		this.setPrecision(precision);
	}
	
	/**
	 * Construit l'objet avec la carte 1
	 * @param line
	 */
	public Balise(String line){
		this.setLigne1(line);
	}
	
	/**
	 * Construit l'objet avec la carte 1
	 * @param line
	 * @param precision
	 */
	public Balise(String line, Boolean precision){
		this.setPrecision(precision);
		this.setLigne1(line);
	}
	
	/**
	 * Precision des coordonnées à la seconde
	 * @param b {@link Boolean} True si précision à la seconde. False par défaut.
	 */
	public void setPrecision(Boolean b){
		this.precision = b;
	}
	/**
	 * Ajoute les données de la carte 1
	 * @param line
	 */
	public void setLigne1(String line){
	    this.setIndicatif(line.substring(2, 7).trim());
	    this.setPublication(!line.substring(20, 23).equalsIgnoreCase("PNP"));
	    this.setEtrg(line.substring(32,36).equalsIgnoreCase("ETRG"));
	}
	/**
	 * Ajoute les données de la carte 2
	 * @param line Carte 2
	 */
	public void setLigne2(String line) {
		if(precision){
			this.setLatitude(new Latitude(new Integer(line.substring(8, 10)),
					new Integer(line.substring(10, 12)), 
					new Integer(line.substring(12, 14))));
			this.setLongitude(new Longitude(new Integer(line.substring(16, 18)),
					new Integer(line.substring(18, 20)),
					new Integer(line.substring(20, 22)),
					line.substring(22, 23)));
			this.setCentre(line.substring(24, 27));
			this.setDefinition(line.substring(28,53).trim());
			this.setSccag(line.substring(66,69));
		} else {
			this.setLatitude(new Latitude(new Integer(line.substring(8, 10)),
					new Integer(line.substring(10, 12))));
			this.setLongitude(new Longitude(new Integer(line.substring(12, 14)),
					new Integer(line.substring(14, 16)),
					line.substring(16, 17)));
			this.setCentre(line.substring(18, 21));
			this.setDefinition(line.substring(22,47).trim());
			this.setSccag(line.substring(60,63));
		}
	}
	/**
	 * Ajoute les données de la carte 3
	 * @param line
	 */
	public void setLigne3(String line) {
		//la dernière balise est caractérisée par un niveau égal à ***
		Integer niveau;
		LinkedList<Couple<String,Integer>> liste = new LinkedList<Couple<String,Integer>>();
		if(line.substring(8,11).equalsIgnoreCase("***")) {
			niveau = 660; //illimité
		} else {
			niveau = new Integer(line.substring(8,11));
		}
		//ajout du premier secteur
		liste.add(new Couple<String,Integer>(line.substring(12,15).trim(),niveau));
		if (niveau != 660) { //balise suivante
			if(line.substring(32,35).equalsIgnoreCase("***")) {
				niveau = 660; //illimité
			} else {
				niveau = new Integer(line.substring(32,35));
			}
			liste.add(new Couple<String, Integer>(line.substring(36,39).trim(),niveau));
			if (niveau != 660){//dernière balise de la carte
				if(line.substring(56,59).equalsIgnoreCase("***")) {
					niveau = 660; //illimité
				} else {
					niveau = new Integer(line.substring(56,59));
				}
				liste.add(new Couple<String, Integer>(line.substring(60,63).trim(),niveau));
			}
		}
		this.setSecteurs(liste);
	}
	/**
	 * Ajoute les données d'une carte 3 facultative
	 * @param line
	 */
	public void addLigne3(String line) {
		Integer niveau;
		if(line.substring(8,11).equalsIgnoreCase("***")) {
			niveau = 660; //illimité
		} else {
			niveau = new Integer(line.substring(8,11));
		}
		this.getSecteurs().add(new Couple<String,Integer>(line.substring(12,15).trim(),niveau));
														//trim supprime les espaces au début et à la fin de la chaîne
														//utile car les secteurs ne font que deux caractères
		if (niveau != 660) { //balise suivante
			if(line.substring(32,35).equalsIgnoreCase("***")) {
				niveau = 660; //illimité
			} else {
				niveau = new Integer(line.substring(32,35));
			}
			this.getSecteurs().add(new Couple<String, Integer>(line.substring(36,39).trim(),niveau));
			if (niveau != 660){//dernière balise de la carte
				if(line.substring(56,59).equalsIgnoreCase("***")) {
					niveau = 660; //illimité
				} else {
					niveau = new Integer(line.substring(56,59));
				}
				this.getSecteurs().add(new Couple<String, Integer>(line.substring(60,63).trim(),niveau));
			}
		}
	}
	
	//Setters and getters
	public String getIndicatif() {
//		if (record != null){
//			return record.value("name").toString();
//		} else {
			return indicatif;
//		}
	}
	public void setIndicatif(String indicatif) {
		this.indicatif = indicatif;
	}
	public Latitude getLatitude() {
//		if (record != null) {
//			return new Latitude(record.value("latitude").toString());
//		} else {
			return latitude;
//		}
	}
	public void setLatitude(Latitude latitude) {
		this.latitude = latitude;
	}
	public Longitude getLongitude() {
	//	if(record != null) {
	//		return new Longitude(record.value("longitude").toString());
	//	} else {
			return longitude;
	//	}
	}
	public void setLongitude(Longitude longitude) {
		this.longitude = longitude;
	}
	public Boolean getPublication() {
		return publication;
	}
	public void setPublication(Boolean publication) {
		this.publication = publication;
	}
	
	public Boolean getEtrg() {
		return etrg;
	}
	
	public void setEtrg(Boolean etrg) {
		this.etrg = etrg;
	}

	public String getCentre() {
		return centre;
	}

	public void setCentre(String centre) {
		this.centre = centre;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getSccag() {
		return sccag;
	}

	public void setSccag(String sccag) {
		this.sccag = sccag;
	}

	public LinkedList<Couple<String,Integer>> getSecteurs() {
		return secteurs;
	}

	public void setSecteurs(LinkedList<Couple<String,Integer>> secteurs) {
		this.secteurs = secteurs;
	}
	
}
