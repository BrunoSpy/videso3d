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

package fr.crnan.videso3d.pays;

import fr.crnan.videso3d.geom.Latitude;
import fr.crnan.videso3d.geom.Longitude;
/**
 * Représente une ligne du fichier POINPAYS
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class PoinPays {

	/**
	 * Référence du point significatif
	 */
	private String reference;
	
	/**
	 * Latitude en degrés
	 */
	private Latitude latitude;
	
	/**
	 * Longitude en degrés
	 */
	private Longitude longitude;
	
	public PoinPays(String ligne){
		this.setReference(ligne.substring(4,10));
		this.setLatitude(new Latitude(new Integer(ligne.substring(12, 14)),
										new Integer(ligne.substring(15, 17)),
										new Integer(ligne.substring(18,20))));
		this.setLongitude(new Longitude(new Integer(ligne.substring(23, 26)),
										new Integer(ligne.substring(27,29)),
										new Integer(ligne.substring(30,32)),
										ligne.substring(33,34)));
	}



	/**
	 * @return the reference
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * @param reference the reference to set
	 */
	public void setReference(String reference) {
		this.reference = reference;
	}

	/**
	 * @return the latitude
	 */
	public Latitude getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(Latitude latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public Longitude getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(Longitude longitude) {
		this.longitude = longitude;
	}
	
}
