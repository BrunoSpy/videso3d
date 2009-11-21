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
 * Contains CARA_GENER datas
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class CaraGener {
	/**
	 * Nom du fichier
	 */
	private String name = "";
	/**
	 * Date de génération
	 */
	private String date = "";
	/**
	 * Jeu de donnée
	 */
	private String jeu = "";
	/**
	 * Type de système radar
	 */
	private String radar = "";
	/**
	 * Version OASIS
	 */
	private Double oasis = 0.0;
	/**
	 * Version BOA
	 */
	private Integer boa = 0;
	/**
	 * Nom de fichier Videomap
	 */
	private String videomap = "";
	/**
	 * Nom de fichier Edimap
	 */
	private String edimap = "";
	/**
	 * Nom de fichier Satin
	 */
	private String satin = "";
	/**
	 * Configuration calculateur en cours
	 */
	private String calculateur = "";
	/**
	 * Contexte
	 */
	private String contexte = "";
	
	//Constructors
	public CaraGener(String line, Boolean formated) throws ParseException {
		this.setFromLine(line, formated);
	}
	
	/**
	 * Extrait les données de la ligne passée en paramètre
	 * @param line Ligne CARA_GENER
	 * @param formated Type de fichier
	 * @throws ParseException 
	 * @throws ParseException 
	 */
	public void setFromLine(String line, Boolean formated) throws ParseException {
		//on découpe la ligne en mots séparés d'un ou plusieurs espaces
		String word[] = line.split(formated ? "\\s+" : ",");
		int length = word.length;
		int i = formated ? 0 : 1;//les données sont décalées d'un cran en cas de fichier non formaté
		if (word[0].equals(formated ? "CARA_GENER" : "CARA.GENER")){
			if(1+i<length) this.setName(word[1+i]);
			if(2+i<length) this.setDate(word[2+i]);
			if(3+i<length) this.setJeu(word[3+i]);
			if(4+i<length) this.setRadar(word[4+i]);
			if(5+i<length) this.setOasis(word[5+i]);
			if(6+i<length) this.setBoa(word[6+i]);
			if(7+i<length) this.setVideomap(word[7+i]);
			if(8+i<length) this.setEdimap(word[8+i]);
			if(9+i<length) this.setSatin(word[9+i]);
			if(10+i<length) this.setCalculateur(word[10+i]);
			if(11+i<length) this.setContexte(word[11+i]);
		} else {
			throw new ParseException("CARA_GENER Parse Error at " + line, 0);
		}
	}
	
	/* Getters and setters */
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getJeu() {
		return jeu;
	}
	public void setJeu(String jeu) {
		this.jeu = jeu;
	}
	public String getRadar() {
		return radar;
	}
	public void setRadar(String radar) {
		this.radar = radar;
	}
	public Double getOasis() {
		return oasis;
	}
	public void setOasis(String oasis) {
		this.oasis = new Double(oasis);
	}
	public Integer getBoa() {
		return boa;
	}
	public void setBoa(String boa) {
		this.boa = new Integer(boa);
	}
	public void setBoa(Integer boa) {
		this.boa = boa;
	}
	public String getVideomap() {
		return videomap;
	}
	public void setVideomap(String videomap) {
		this.videomap = videomap;
	}
	public String getEdimap() {
		return edimap;
	}
	public void setEdimap(String edimap) {
		this.edimap = edimap;
	}
	public String getSatin() {
		return satin;
	}
	public void setSatin(String satin) {
		this.satin = satin;
	}
	public String getCalculateur() {
		return calculateur;
	}
	public void setCalculateur(String calculateur) {
		this.calculateur = calculateur;
	}
	public String getContexte() {
		return contexte;
	}
	public void setContexte(String contexte) {
		this.contexte = contexte;
	}
	
}
