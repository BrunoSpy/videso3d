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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import fr.crnan.videso3d.ProgressSupport;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.formats.plns.PLNSExtractor;
import fr.crnan.videso3d.formats.plns.PLNSTrack;
import fr.crnan.videso3d.formats.plns.PLNSTrackPoint;
import fr.crnan.videso3d.stip.PointNotFoundException;

/**
 * TracksModel that uses a PLNS SQLite database to store tracks.<br />
 * Format of the database is described in {@link PLNSExtractor}<br />
 * Gives a {@link ProgressSupport} in oreder to follow the progression of the creation of the tracks
 * @author Bruno Spyckerelle
 * @version 0.2.0
 */
public class PLNSTracksModel extends TracksModel {

	private File database;
	
	private ProgressSupport progressSupport;
	
	public PLNSTracksModel(){
		super();
		this.progressSupport = new ProgressSupport();
	}
	
	/**
	 * TracksModel with a non empty PLNS database
	 * @param databaseFile
	 * @throws PointNotFoundException
	 */
	public PLNSTracksModel(File databaseFile) throws PointNotFoundException {
		this();
		this.setDatabase(databaseFile);		
	}
	
	public void setDatabase(File databaseFile) throws PointNotFoundException{
		this.database = databaseFile;
		Collection<VidesoTrack> tracks = new HashSet<VidesoTrack>();
		//Chargement du driver
		try {
			Class.forName("org.sqlite.JDBC");
			//Connexion
			Connection base = DriverManager.getConnection("jdbc:sqlite:"+databaseFile.getAbsolutePath());
			Statement st = base.createStatement();
			ResultSet rs =  st.executeQuery("select count(*) from plns where 1");
			this.progressSupport.fireTaskStarts(rs.getInt(1)*2);
			this.progressSupport.fireTaskInfo("Création des tracks");
			int i = 0;
			rs = st.executeQuery("select * from plns where 1");
			while(rs.next()){
				PLNSTrack track = new PLNSTrack(rs.getString(1));
				track.setIndicatif(rs.getString(4));
				track.setDepart(rs.getString(7));
				track.setArrivee(rs.getString(8));
				track.setType(rs.getString(10));
				tracks.add(track);
				this.progressSupport.fireTaskProgress(++i);
			}
			for(VidesoTrack t : tracks){
				rs = st.executeQuery("select * from balises where idpln='"+t.getName()+"'");
				while(rs.next()){
					PLNSTrackPoint point = new PLNSTrackPoint(rs.getString(3), rs.getInt(4), rs.getString(5));
					((PLNSTrack)t).addPoint(point);
				}
				this.addTrack(t);
				this.progressSupport.fireTaskProgress(++i);
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
	
	public ProgressSupport getProgressSupport(){
		return this.progressSupport;
	}
	
}
