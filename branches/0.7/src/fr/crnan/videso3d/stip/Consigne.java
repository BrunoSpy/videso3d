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
 * Carte M, D ou R du fichier LIEUX
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class Consigne {

	/**
	 * Type de la consigne
	 */
	private Character type;
	/**
	 * Code OACI
	 */
	private String oaci;
	/**
	 * Balise
	 */
	private String balise;
	/**
	 * Niveau sur la consigne
	 */
	private Integer niveau;
	/**
	 * Ecart sur la consigne
	 */
	private Integer ecart;
	/**
	 * Action Eveil
	 */
	private Boolean eveil;
	/**
	 * Action act
	 */
	private Boolean act;
	/**
	 * Action mod
	 */
	private Boolean mod;
	/**
	 * Niveau de base de la consigne
	 */
	private Integer base;
	
	/**
	 * Construit une consigne à partir d'une carte M, D ou R<br />
	 * Ne prend pas en compte les catégories de performances
	 * @param line
	 */
	public Consigne(String line) {
		this.type = line.charAt(0);
		this.oaci = line.substring(2, 6);
		this.balise = line.substring(7, 12);
		this.niveau = new Integer(line.substring(13, 16));
		this.ecart = new Integer(line.substring(17, 20));
		this.eveil = line.substring(21, 24).equals("EVE");
		this.act = line.substring(25, 28).equals("ACT");
		this.mod = line.substring(29, 32).equals("MOD");
		this.base = new Integer(line.substring(33, 34));
	}

	/**
	 * @return the type
	 */
	public Character getType() {
		return type;
	}

	/**
	 * @return the oaci
	 */
	public String getOaci() {
		return oaci;
	}

	/**
	 * @return the balise
	 */
	public String getBalise() {
		return balise;
	}

	/**
	 * @return the niveau
	 */
	public Integer getNiveau() {
		return niveau;
	}

	/**
	 * @return the ecart
	 */
	public Integer getEcart() {
		return ecart;
	}

	/**
	 * @return the eveil
	 */
	public Boolean getEveil() {
		return eveil;
	}

	/**
	 * @return the act
	 */
	public Boolean getAct() {
		return act;
	}

	/**
	 * @return the mod
	 */
	public Boolean getMod() {
		return mod;
	}

	/**
	 * @return the base
	 */
	public Integer getBase() {
		return base;
	}

	
	
}
