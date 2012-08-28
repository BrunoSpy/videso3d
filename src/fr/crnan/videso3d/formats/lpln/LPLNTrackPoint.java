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
package fr.crnan.videso3d.formats.lpln;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.stip.PointNotFoundException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.tracks.TrackPoint;
/**
 * Point balise d'un LPLN.<br />
 * Les balises n'ayant pas de coordonnées dans un LPLN, une connection à une base STIP est indispensable.
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class LPLNTrackPoint implements TrackPoint{

	private String time;
	private String name;
	private Position position;
	
	public LPLNTrackPoint(String sentence) throws PointNotFoundException {
		this.setName(sentence.substring(17,27).trim());
		this.setTime(sentence.substring(47, 52));	
		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from balises where name ='"+this.getName()+"'");
			//TODO ajouter une erreur si pas de base STIP
			if(rs.next()){
				this.setPosition(new Position(Angle.fromDegrees(rs.getDouble("latitude")),
										  Angle.fromDegrees(rs.getDouble("longitude")),
										  (new Double(sentence.substring(55, 58)))*30.48));
			} else {
				throw new PointNotFoundException(this.getName(), "Point "+this.getName()+" non trouvé");
			}
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public LPLNTrackPoint() {
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

	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		this.name = name;
	}
}
