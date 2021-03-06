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
package fr.crnan.videso3d.databases.stip;
/**
 * Exception throwed when a point is not found in the database
 * @author Bruno Spyckerelle
 * @version 0.0.1
 */
public class PointNotFoundException extends Exception {

	private String name;
	/**
	 * 
	 * @param name Point's name
	 * @param message
	 */
	public PointNotFoundException(String name, String message){
		super(message);
		this.setName(name);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
