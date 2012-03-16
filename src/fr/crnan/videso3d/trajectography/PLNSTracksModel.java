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
package fr.crnan.videso3d.trajectography;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;

import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.formats.lpln.LPLNTrack;
import fr.crnan.videso3d.formats.lpln.LPLNTrackPoint;
import fr.crnan.videso3d.formats.plns.PLNSExtractor;

/**
 * TracksModel that uses a PLNS SQLite database to store tracks.<br />
 * Format of the database is described in {@link PLNSExtractor}
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class PLNSTracksModel extends TracksModel {

	private File database;
	
	public PLNSTracksModel(File databaseFile){
		this.database = databaseFile;
		Collection<VidesoTrack> tracks = new HashSet<VidesoTrack>();
		//Chargement du driver
		try {
			Class.forName("org.sqlite.JDBC");
			//Connexion
			Connection base = DriverManager.getConnection("jdbc:sqlite:"+databaseFile.getAbsolutePath());
			Statement st = base.createStatement();
			ResultSet rs = st.executeQuery("select * from plns where 1");
			while(rs.next()){
				LPLNTrack track = new LPLNTrack(rs.getString(1));
				track.setIndicatif(rs.getString(4));
				track.setDepart(rs.getString(7));
				track.setArrivee(rs.getString(8));
				track.setType(rs.getString(10));
				tracks.add(track);
			}
			for(VidesoTrack t : tracks){
				rs = st.executeQuery("select * from balises where idpln='"+t.getName()+"'");
				while(rs.next()){
					LPLNTrackPoint point = new LPLNTrackPoint();
					
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public File getDatabase(){
		return this.database;
	}
	
}
