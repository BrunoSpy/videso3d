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
 * Représentation d'une ligne CENT_FLVVF
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class CentFlvvf {
	/**
	 * Nom du VVF
	 */
	private String name;

	
	public CentFlvvf(String line) throws ParseException{
		String[] word = line.split("\\s+");
		if (word[0].equals("CENT_FLVVF")){
			this.setName(word[1]);
		} else {
			throw new ParseException("CENT_FLVVF Parse Error at " + line, 0);
		}
	}
	
//	public CentFlvvf(QSqlRecord record){
//		this.record = record;
//	}

	/**
	 * @return the name
	 */
	public String getName() {
//		if(record != null){
//			return this.record.value("name").toString();
//		} else {
			return name;
//		}
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
			this.name = name;
	}
	
}
