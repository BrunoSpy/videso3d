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

package fr.crnan.videso3d.layers;
/**
 * Layer capable of filtering its objects depending on a maximum and a minimum altitude.
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public interface AltitudeFilterableLayer {

	/**
	 * Objects higher than <code>altitude</code> will not be displayed.<br />
	 * @param altitude in meters
	 */
	public void setMaximumViewableAltitude(double altitude);
	
	/**
	 * Objects lower than <code>altitude</code> will not be displayed.<br />
	 * @param altitude in meters
	 */
	public void setMinimumViewableAltitude(double altitude);
	
}
