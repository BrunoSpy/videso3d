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
 * Représentation d'une ligne CENT_STACK
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class CentStack {
	
	private String name;
	
	private Latitude latitude;
	
	private Longitude longitude;
	
	private double x;
	
	private double y;
	
	private int rayonInt;
	
	private int rayonExt;
	
	private int flInf;
	
	private int flSup;
	
	private String type;
	
	public CentStack(String line, Boolean formated) throws ParseException {
		if(!formated){
			//suppression du ; en fin de ligne
			line = line.substring(0, line.length() - 1);
		}
		String[] word = line.split(formated ? "\\s+" : ",");
		int i = formated ? 0 : 1;
		if (word[0].equals(formated ? "CENT_STACK" : "CENT.STACK")){
			this.setName(word[1+i]);
			this.setLatitude(new Latitude(new Integer(word[2+i]), new Integer(word[3+i]), new Integer(word[4+i])));
			this.setLongitude(new Longitude(new Integer(word[5+i]), new Integer(word[6+i]), new Integer(word[7+i]), word[8+i]));
			this.setX(new Double(word[9+i]));
			this.setY(new Double(word[10+i]));
			this.setRayonInt(new Integer(word[11+i]));
			this.setRayonExt(new Integer(word[12+i]));
			this.setFlInf(new Integer(word[13+i]));
			this.setFlSup(new Integer(word[14+i]));
			this.setType(word[15+i]);
		} else {
			throw new ParseException("CENT_STACK Parse Error at " + line, 0);
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
	public int getRayonInt() {
		return rayonInt;
	}

	/**
	 * @param rayonInt the rayonInt to set
	 */
	public void setRayonInt(int rayonInt) {
		this.rayonInt = rayonInt;
	}

	/**
	 * @return the rayonExt
	 */
	public int getRayonExt() {
		return rayonExt;
	}

	/**
	 * @param rayonExt the rayonExt to set
	 */
	public void setRayonExt(int rayonExt) {
		this.rayonExt = rayonExt;
	}

	/**
	 * @return the flInf
	 */
	public int getFlInf() {
		return flInf;
	}

	/**
	 * @param flInf the flInf to set
	 */
	public void setFlInf(int flInf) {
		this.flInf = flInf;
	}

	/**
	 * @return the flSup
	 */
	public int getFlSup() {
		return flSup;
	}

	/**
	 * @param flSup the flSup to set
	 */
	public void setFlSup(int flSup) {
		this.flSup = flSup;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	
}
