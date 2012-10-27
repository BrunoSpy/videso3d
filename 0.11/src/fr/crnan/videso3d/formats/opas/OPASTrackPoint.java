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
package fr.crnan.videso3d.formats.opas;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.tracks.TrackPoint;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class OPASTrackPoint implements TrackPoint {

	private Position position;
	private String time;
	
	public OPASTrackPoint(String line){
		String[] words = line.split("\\s+");
		this.position = new Position(Angle.fromDegrees(new Double(words[1])),
				Angle.fromDegrees(new Double(words[2])),
				new Double(words[3])*0.3048006);
		this.time =	words[0];
	}
	/**
	 * 
	 * @param lat Degrees
	 * @param lon Degrees
	 * @param elevation Meters
	 * @param time
	 */
	public OPASTrackPoint(Angle lat, Angle lon, double elevation, String time){
		this.position = new Position(lat, lon, elevation);
		this.time = time;
	}
	
	@Override
	public double getElevation() {
		return position.elevation;
	}

	@Override
	public double getLatitude() {
		return position.latitude.degrees;
	}

	@Override
	public double getLongitude() {
		return position.longitude.degrees;
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public String getTime() {
		return time;
	}

	@Override
	public void setElevation(double elevation) {
		this.position = new Position(this.position.getLatitude(), this.position.getLongitude(), elevation);
	}

	@Override
	public void setLatitude(double latitude) {
		this.position = new Position(Angle.fromDegrees(latitude), this.position.getLongitude(), this.position.getElevation());
	}

	@Override
	public void setLongitude(double longitude) {
		this.position = new Position(this.position.getLatitude(), Angle.fromDegrees(longitude), this.position.getElevation());

	}

	@Override
	public void setPosition(Position position) {
		this.position = position;
	}

	@Override
	public void setTime(String time) {
		this.time = time;
	}

}
