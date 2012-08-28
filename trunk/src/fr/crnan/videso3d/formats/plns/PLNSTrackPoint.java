package fr.crnan.videso3d.formats.plns;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.stip.PointNotFoundException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.tracks.TrackPoint;
/**
 * Point balise d'un vol d'une base PLNS.<br />
 * Les balises n'ayant pas de coordonnées dans un PLNS, une connection à une base STIP est indispensable.
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class PLNSTrackPoint implements TrackPoint {

	private String name;
	private Position position;
	private String time;
	
	public PLNSTrackPoint(String name, int fl, String time) throws PointNotFoundException{
		this.position = new Position(LatLon.ZERO, 0);
		this.setName(name);
		this.setElevation(new Double(fl)*30.48);
		this.setTime(time);
	}
	
	@Override
	public double getLatitude() {
		return this.getPosition().getLatitude().degrees;
	}

	@Override
	public void setLatitude(double latitude) {
		this.position = new Position(Angle.fromDegrees(latitude), this.position.getLongitude(), this.position.getElevation());
	}

	@Override
	public double getLongitude() {
		return this.getPosition().getLongitude().degrees;
	}

	@Override
	public void setLongitude(double longitude) {
		this.position = new Position(this.position.getLatitude(), Angle.fromDegrees(longitude), this.position.getElevation());

	}

	@Override
	public double getElevation() {
		return this.position.elevation;
	}

	@Override
	public void setElevation(double elevation) {
		this.position = new Position(this.position.getLatitude(), this.position.getLongitude(), elevation);
	}

	@Override
	public String getTime() {
		return time;
	}

	@Override
	public void setTime(String time) {
		this.time = time;
	}

	@Override
	public Position getPosition() {
		return this.position;
	}

	@Override
	public void setPosition(Position position) {
		this.position = position;
	}

	public String getName(){
		return this.name;
	}
	
	public void setName(String name) throws PointNotFoundException{
		this.name = name;
		try {
			Statement st = DatabaseManager.getCurrentStip();
			ResultSet rs = st.executeQuery("select * from balises where name ='"+this.getName()+"'");
			//TODO ajouter une erreur si pas de base STIP
			if(rs.next()){
				this.setPosition(new Position(Angle.fromDegrees(rs.getDouble("latitude")),
										  Angle.fromDegrees(rs.getDouble("longitude")),
										  this.getPosition().elevation));
			} else {
				throw new PointNotFoundException(this.getName(), "Point "+this.getName()+" non trouvé");
			}
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
