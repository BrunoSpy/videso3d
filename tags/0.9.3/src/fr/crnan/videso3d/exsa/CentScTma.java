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
 *
 */
public class CentScTma {

	private int carre;
	
	private int souscarre;
	
	private int v1;
	
	private int v2;
	
	private int v3;
	
	private String name;
	
	public CentScTma(String line, Boolean formated) throws ParseException{
		if(!formated){
			//suppression du ; en fin de ligne
			line = line.substring(0, line.length() - 1);
		}
		String[] word = line.split(formated ? "\\s+" : ",");
		int i = formated ? 0 : 1;
		if (word[0].equals(formated ? "CENT_SCTMA" : "CENT.SCTMA")){
			this.setCarre(new Integer(word[1+i]));
			this.setSouscarre(word[2+i]);
			this.setV1(word[3+i]);
			this.setV2(word[4+i]);
			this.setV3(word[5+i]);
			this.setName(word[6+i]);
		} else {
			throw new ParseException("CENT_SCTMA Parse Error at " + line, 0);
		}
	}

	/**
	 * @return the carre
	 */
	public int getCarre() {
		return carre;
	}

	/**
	 * @param carre the carre to set
	 */
	public void setCarre(int carre) {
		this.carre = carre;
	}

	/**
	 * @return the souscarré
	 */
	public int getSouscarre() {
		return souscarre;
	}

	/**
	 * @param souscarré the souscarré to set
	 */
	public void setSouscarre(String souscarre) {
		if(souscarre.isEmpty() || souscarre.equals("##")){
			this.souscarre = 0;
		} else {
			this.souscarre = new Integer(souscarre);
		}
	}

	/**
	 * @return the v1
	 */
	public int getV1() {
		return v1;
	}

	/**
	 * @param v1 the v1 to set
	 */
	public void setV1(String v1) {
		if(v1.isEmpty() || v1.equals("###")){
			this.v1 = 0;
		} else {
			this.v1 = new Integer(v1);
		}
	}

	/**
	 * @return the v2
	 */
	public int getV2() {
		return v2;
	}

	/**
	 * @param v2 the v2 to set
	 */
	public void setV2(String v2) {
		if(v2.isEmpty() || v2.equals("###")){
			this.v2 = 0;
		} else {
			this.v2 = new Integer(v2);
		}
	}

	/**
	 * @return the v3
	 */
	public int getV3() {
		return v3;
	}

	/**
	 * @param v3 the v3 to set
	 */
	public void setV3(String v3) {
		if(v3.isEmpty() || v3.equals("###")){
			this.v3 = 0;
		} else {
			this.v3 = new Integer(v3);
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
	
	
}
