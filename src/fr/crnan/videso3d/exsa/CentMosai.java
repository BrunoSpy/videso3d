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
 * Représentation d'une ligne CENT_MOSAI
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class CentMosai {
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
	private double x;
	/**
	 * Ordonnée Cautra
	 */
	private double y;
	/**
	 * Nombre de lignes
	 */
	private int lignes;
	/**
	 * Nombre de colonnes
	 */
	private int colonnes;
	/**
	 * Type de la mosaïque
	 */
	private String type;

	
	public CentMosai(String line) throws ParseException{
		String[] word = line.split("\\s+");
		if (word[0].equals("CENT_MOSAI")){
			this.setLatitude(new Latitude(new Integer(word[1]), new Integer(word[2]), new Integer(word[3])));
			this.setLongitude(new Longitude(new Integer(word[4]), new Integer(word[5]), new Integer(word[6]), word[7]));
			this.setX(new Double(word[8]));
			this.setY(new Double(word[9]));
			this.setLignes(new Integer(word[10]));
			this.setColonnes(new Integer(word[11]));
			this.setType(word[12]);
		} else {
			throw new ParseException("CENT_MOSAI Parse Error at " + line, 0);
		}
	}

//	public CentMosai(QSqlRecord record){
//		this.record = record;
//	}
	
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

	public double getX() {
//		if(record != null){
//			return new Double(record.value("xcautra").toString());
//		} else {
			return x;
//		}
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
//		if(record != null){
//			return new Double(record.value("ycautra").toString());
//		} else {
			return y;
//		}
	}

	public void setY(double y) {
		this.y = y;
	}

	public int getLignes() {
//		if(record != null){
//			return ((Long) record.value("lignes")).intValue();
//		} else {
			return lignes;
//		}
	}

	public void setLignes(int lignes) {
		this.lignes = lignes;
	}

	public int getColonnes() {
//		if(record != null){
//			return ((Long) record.value("colonnes")).intValue();
//		} else {
			return colonnes;
//		}
	}

	public void setColonnes(int colonnes) {
		this.colonnes = colonnes;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
}
