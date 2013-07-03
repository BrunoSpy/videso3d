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

/**
 * 
 * @author Bruno Spyckerelle
 *
 */
public class CentCRVSM {

	private int carre;
	
	private int souscarre;
	
	/**
	 * 0: non RVSM<br />
	 * 1: RVSM sauf pour les avions non corrélés
	 * 2: RVSM même pour les avions non corrélés
	 */
	private int rvsm;
	
	/**
	 * 
	 * @param line
	 * @param formated
	 * @throws ParseException
	 */
	public CentCRVSM(String line, Boolean formated) throws ParseException{
		if(!formated){
			//suppression du ; en fin de ligne
			line = line.substring(0, line.length() - 1);
		}
		String[] word = line.split(formated ? "\\s+" : ",");
		int i = formated ? 0 : 1;
		if (word[0].equals(formated ? "CENT_CRVSM" : "CENT.CRVSM")){
			this.setCarre(new Integer(word[1+i]));
		    if(word.length > 2+i)	this.setSouscarre(word[2+i]);
		    if(word.length > 4+i)   this.setRVSM(word[3+i], word[4+i]);
		} else {
			throw new ParseException("CENT_CRVSM Parse Error at " + line, 0);
		}
	}

	public void setRVSM(String carre, String souscarre){
		if(this.souscarre != 0){
			if(souscarre.equals("NON")) {
				rvsm = 0;
			} else if(souscarre.equals("OUI")){
				rvsm = 1;
			} else if(souscarre.equals("OUI_W")){
				rvsm = 2;
			}
		} else {
			if(carre.equals("NON")) {
				rvsm = 0;
			} else if(carre.equals("OUI")){
				rvsm = 1;
			} else if(carre.equals("OUI_W")){
				rvsm = 2;
			}
		}
	}
	
	public int getRVSM(){
		return rvsm;
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
	public int getSousCarre() {
		return souscarre;
	}

	/**
	 * @param souscarré the souscarré to set
	 */
	public void setSouscarre(String souscarre) {
		if(souscarre.isEmpty() || souscarre.startsWith("##")){
			this.souscarre = 0;
		} else {
			this.souscarre = new Integer(souscarre);
		}
	}


	
	
}
