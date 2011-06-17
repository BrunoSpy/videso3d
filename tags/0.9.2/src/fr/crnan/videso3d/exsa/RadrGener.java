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

import fr.crnan.videso3d.geom.Latitude;
import fr.crnan.videso3d.geom.Longitude;

/**
 * Ligne RADR_GENER
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class RadrGener {

	private String nom;
	/**
	 * Numéro d'extraction
	 */
	private Integer numero;
	/**
	 * Type
	 */
	private String type;
	/**
	 * Nom dans la mosaique
	 */
	private String nomMosaique;
	/**
	 * Latitude
	 */
	private Latitude latitude;
	/**
	 * Longitude
	 */
	private Longitude longitude;
	/**
	 * Abscisse Cautra
	 */
	private Double x;
	/**
	 * Ordonnée Cautra
	 */
	private Double y;
	/**
	 * Ecart-nord
	 */
	private Double ecartNord;
	/**
	 * Radar en relation
	 */
	private String radarRelation;
	/**
	 * Type de relation
	 */
	private String typeRelation;
	/**
	 * Type de plots
	 */
	private String typePlots;
	/**
	 * Type du radar
	 */
	private String typeRadar;
	/**
	 * Code européen du pays
	 */
	private Integer codePays;
	/**
	 * Code européen du radar dans son pays
	 */
	private Integer codeRadar;
	/**
	 * Radar militaire
	 */
	private Boolean militaire;
	
	public RadrGener(String line, Boolean formated) throws ParseException{
		if(!formated){
			//suppression du ; en fin de ligne
			line = line.substring(0, line.length() - 1);
		}
		String[] word = line.split(formated ? "\\s+" : ",");
		int i = formated ? 0 : 1;
		if (word[0].equals(formated ? "RADR_GENER" : "RADR.GENER")){
			this.setNom(word[1+i]);
			this.setNumero(new Integer(word[2+i]));
			this.setType(word[3+i]);
			this.setNomMosaique(word[4+i]);
			this.setLatitude(new Latitude(new Integer(word[5+i]), new Integer(word[6+i]), new Integer(word[7+i])));
			this.setLongitude(new Longitude(new Integer(word[8+i]), new Integer(word[9+i]), new Integer(word[10+i]), word[11+i]));
			this.setX(new Double(word[12+i]));
			this.setY(new Double(word[13+i]));
			this.setEcartNord(new Double(word[14+i]));
			this.setRadarRelation(word[15+i]);
			this.setTypeRelation(word[16+i]);
			this.setTypePlots(word[17+i]);
			this.setTypeRadar(word[18+i]);
			this.setCodePays(new Integer(word[19+i]));
			this.setCodeRadar(new Integer(word[20+i]));
			this.setMilitaire(word[21+i]);
		} else {
			throw new ParseException("RADR_GENER Parse Error at " + line, 0);
		}
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
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

	public String getNomMosaique() {
		return nomMosaique;
	}

	public void setNomMosaique(String nomMosaique) {
		this.nomMosaique = nomMosaique;
	}

	public Latitude getLatitude() {
		return latitude;
	}

	public void setLatitude(Latitude latitude) {
		this.latitude = latitude;
	}

	public Longitude getLongitude() {
		return longitude;
	}

	public void setLongitude(Longitude longitude) {
		this.longitude = longitude;
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	public Double getEcartNord() {
		return ecartNord;
	}

	public void setEcartNord(Double ecartNord) {
		this.ecartNord = ecartNord;
	}

	public String getRadarRelation() {
		return radarRelation;
	}

	public void setRadarRelation(String radarRelation) {
		this.radarRelation = radarRelation;
	}

	public String getTypeRelation() {
		return typeRelation;
	}

	public void setTypeRelation(String typeRelation) {
		this.typeRelation = typeRelation;
	}

	public String getTypePlots() {
		return typePlots;
	}

	public void setTypePlots(String typePlots) {
		this.typePlots = typePlots;
	}

	public String getTypeRadar() {
		return typeRadar;
	}

	public void setTypeRadar(String typeRadar) {
		this.typeRadar = typeRadar;
	}

	public Integer getCodePays() {
		return codePays;
	}

	public void setCodePays(Integer codePays) {
		this.codePays = codePays;
	}

	public Integer getCodeRadar() {
		return codeRadar;
	}

	public void setCodeRadar(Integer codeRadar) {
		this.codeRadar = codeRadar;
	}

	public Boolean getMilitaire() {
		return militaire;
	}

	public void setMilitaire(Boolean militaire) {
		this.militaire = militaire;
	}
	
	public void setMilitaire(String militaire) {
		this.militaire = militaire.compareTo("NON")==0;
	}
}
