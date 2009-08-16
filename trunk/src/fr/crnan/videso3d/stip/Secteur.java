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
 * Représente une ligne du fichier SECT
 * @author Bruno Spyckerelle
 * @version 0.1
 *
 */
public class Secteur {
	/**
	 * Nom du secteur
	 */
	private String nom;
	
	/**
	 * Nom du centre auquel appartient le secteur
	 */
	private String centre;
	
	/**
	 * Espace du secteur : F ou U
	 */
	private String espace;
	
	/**
	 * Numéro du secteur
	 */
	private Integer numero;
	
	/**
	 * Limite inférieure de la tranche : premier niveau de vol du secteur
	 */
	private Integer flinf;
	
	/**
	 * Limite supérieure de la tranche : dernier niveau de vol du secteur
	 */
	private Integer flsup;
	
	/**
	 * Mode S
	 */
	private Boolean modeS;

	
	public Secteur(String line) {
		this.setNom(line.substring(0,3).trim());
		this.setCentre(line.substring(4,8).trim());
		this.setEspace(line.substring(11,12));
		this.setNumero(new Integer(line.substring(12,16)));
		this.setFlinf(new Integer(line.substring(40,44)));
		this.setFlsup(new Integer(line.substring(44,48)));
		this.setModeS(line.substring(49,50));
	}
	
//	public Secteur(QSqlRecord record) {
//		this.record = record;
//		this.setNumero(((Long) record.value("numero")).intValue()); 
//	}
	
	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getCentre() {
		return centre;
	}

	public void setCentre(String centre) {
		this.centre = centre;
	}

	public String getEspace() {
		return espace;
	}

	public void setEspace(String espace) {
		this.espace = espace;
	}

	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}

	public Integer getFlinf() {
		return flinf;
	}

	public void setFlinf(Integer flinf) {
		this.flinf = flinf;
	}

	public Integer getFlsup() {
		return flsup;
	}

	public void setFlsup(Integer flsup) {
		this.flsup = flsup;
	}

	public Boolean getModeS() {
		return modeS;
	}

	public void setModeS(Boolean modeS) {
		this.modeS = modeS;
	}

	public void setModeS(String modeS){
		this.modeS = modeS.equalsIgnoreCase("s");
	}
	
	
}
