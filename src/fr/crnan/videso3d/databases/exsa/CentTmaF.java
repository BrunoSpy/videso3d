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

package fr.crnan.videso3d.databases.exsa;

import java.text.ParseException;

import fr.crnan.videso3d.geom.Latitude;
import fr.crnan.videso3d.geom.Longitude;

/**
 * Représentation d'une ligne CENT_TMA_F
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class CentTmaF {
	
	private String name;
	
	private Latitude latitude;
	
	private Longitude longitude;
	
	private double x;
	
	private double y;
		
	private int rayon;
		
	private int fl;
	
	private String nomSecteur;
	
	public CentTmaF(String line, Boolean formated) throws ParseException {
		if(!formated){
			//suppression du ; en fin de ligne
			line = line.substring(0, line.length() - 1);
		}
		String[] word = line.split(formated ? "\\s+" : ",");
		int i = formated ? 0 : 1;
		if (word[0].equals(formated ? "CENT_TMA_F" : "CENT.TMA_F")){
			this.setName(word[1+i]);
			this.setLatitude(new Latitude(new Integer(word[2+i]), new Integer(word[3+i]), new Integer(word[4+i])));
			this.setLongitude(new Longitude(new Integer(word[5+i]), new Integer(word[6+i]), new Integer(word[7+i]), word[8+i]));
			this.setX(new Double(word[9+i]));
			this.setY(new Double(word[10+i]));
			this.setRayon(new Integer(word[11+i]));
			this.setFl(new Integer(word[12+i]));
			this.setNomSecteur(word[13+i]);
		} else {
			throw new ParseException("CENT_TMA_F Parse Error at " + line, 0);
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @return the rayonInt
	 */
	public int getRayon() {
		return rayon;
	}

	/**
	 * @param rayonInt the rayonInt to set
	 */
	public void setRayon(int rayon) {
		this.rayon = rayon;
	}


	/**
	 * @return the flInf
	 */
	public int getFl() {
		return fl;
	}

	/**
	 * @param flInf the flInf to set
	 */
	public void setFl(int fl) {
		this.fl = fl;
	}

	/**
	 * @return the type
	 */
	public String getNomSecteur() {
		return nomSecteur;
	}

	/**
	 * @param type the type to set
	 */
	public void setNomSecteur(String nomSecteur) {
		this.nomSecteur = nomSecteur;
	}
}
