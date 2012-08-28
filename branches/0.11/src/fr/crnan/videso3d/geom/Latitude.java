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

package fr.crnan.videso3d.geom;


/**
 * Représentation d'une latitude
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class Latitude extends Coordonnee{

	public Latitude(Integer degres, Integer minutes) {
		super(degres, minutes);
	}
	
	public Latitude(Integer degres, Integer minutes, Integer secondes){
		super(degres, minutes, secondes);
	}

	public Latitude(String latitude){
		super(latitude);
	}
	
	public Latitude(double x) {
		super(x);
	}
}
