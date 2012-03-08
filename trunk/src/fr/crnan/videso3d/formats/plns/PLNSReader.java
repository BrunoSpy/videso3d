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
package fr.crnan.videso3d.formats.plns;

import java.io.File;
import java.io.FileInputStream;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.stip.PointNotFoundException;
import fr.crnan.videso3d.trajectography.TracksModel;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class PLNSReader extends TrackFilesReader {

	
	public PLNSReader(File[] plnsFiles, TracksModel model) throws PointNotFoundException {
		this.setModel(model);
		try {
			//Chargement du driver
			Class.forName("org.sqlite.JDBC");
			//Connexion
			Connection database = DriverManager.getConnection("jdbc:sqlite:plns_test");
		PLNSExtractor extractor = new PLNSExtractor(plnsFiles, database);
		
		} catch (ClassNotFoundException e){
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doReadStream(FileInputStream fis)
			throws PointNotFoundException {
		

	}

	

	
	
	
	
}
