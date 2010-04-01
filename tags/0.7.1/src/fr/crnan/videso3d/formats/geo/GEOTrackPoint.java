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
package fr.crnan.videso3d.formats.geo;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.tracks.TrackPoint;
/**
 * Point d'une trajectoire Elvira GEO.<br />
 * Voir la description du format.
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class GEOTrackPoint implements TrackPoint {

	private Position position;
	private String time;
	private Integer vitesse;
	
	public GEOTrackPoint(String sentence){
		String[] words = sentence.split("\t");
		String alt = words[7];
		String modeC = words[6];
		Double elev = 1000.0;
		if(alt.equals("inv")) {
			if(!modeC.equals("inv")) elev = new Double(modeC)*30.48;
		} else {
			elev = new Double(alt)*0.3048;
		}
		this.position = new Position(Angle.fromDegrees(new Double(words[4])),
									Angle.fromDegrees(new Double(words[5])),
									elev);
		Integer time = new Double(words[3]).intValue();
		int heure = time / 3600;
		time -= heure * 3600;
		int minutes = time / 60;
		int secondes = time - minutes * 60;
		this.time = heure+"h"+minutes+"min"+secondes;
		this.vitesse = new Integer(words[8]);
	}
	
	@Override
	public double getElevation() {
		return this.getPosition().getElevation();
	}

	@Override
	public double getLatitude() {
		return this.getPosition().getLatitude().degrees;
	}

	@Override
	public double getLongitude() {
		return this.getPosition().getLongitude().degrees;
	}

	@Override
	public Position getPosition() {
		return this.position;
	}

	@Override
	public String getTime() {
		return this.time;
	}

	@Override
	public void setElevation(double elevation) {
		this.position = new Position(Angle.fromDegrees(this.getLatitude()), Angle.fromDegrees(this.getLongitude()), elevation);
	}

	@Override
	public void setLatitude(double latitude) {
		this.position = new Position(Angle.fromDegrees(latitude), Angle.fromDegrees(this.getLongitude()), this.getElevation());
	}

	@Override
	public void setLongitude(double longitude) {
		this.position = new Position(Angle.fromDegrees(this.getLatitude()), Angle.fromDegrees(longitude), this.getElevation());
	}

	@Override
	public void setPosition(Position position) {
		this.position = position;
	}

	@Override
	public void setTime(String time) {
		this.time = time;
	}

	public Integer getVitesse() {
		return vitesse;
	}
	
}
