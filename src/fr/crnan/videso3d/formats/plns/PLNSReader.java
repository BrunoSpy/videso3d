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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import fr.crnan.videso3d.ProgressSupport;
import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.ihm.ProgressMonitor;
import fr.crnan.videso3d.stip.PointNotFoundException;
import fr.crnan.videso3d.trajectography.PLNSTracksModel;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class PLNSReader extends TrackFilesReader {


	public PLNSReader(File[] plnsFiles, PLNSTracksModel model) throws PointNotFoundException {
		this.setModel(model);
		try {
			//Chargement du driver
			Class.forName("org.sqlite.JDBC");
			//Connexion
			Connection database = DriverManager.getConnection("jdbc:sqlite:"+model.getDatabase().getAbsolutePath());
			PLNSExtractor extractor = new PLNSExtractor(plnsFiles, database);
			final ProgressMonitor progress = new ProgressMonitor(null, "Extraction des fichiers plns", "", 0, 100);
			extractor.addPropertyChangeListener(new PropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if(evt.getPropertyName().equals(ProgressSupport.TASK_STARTS)){
						progress.setMaximum((Integer) evt.getNewValue());
					} else if(evt.getPropertyName().equals(ProgressSupport.TASK_PROGRESS)){
						progress.setProgress((Integer) evt.getNewValue());
					} else if(evt.getPropertyName().equals(ProgressSupport.TASK_INFO)){
						progress.setNote((String)evt.getNewValue());
					}
				}
			});
			extractor.doExtract();
			
		} catch (ClassNotFoundException e){
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Si les fichiers sont déjà dans une basse de données, il suffit de référencer le modèle correspondant.
	 * @param model
	 */
	public PLNSReader(PLNSTracksModel model){
		this.setModel(model);
	}
	
	/**
	 * Return true if it is a PLNS file
	 * @param f
	 * @return
	 */
	public static boolean isPLNSFile(File f){
		if(f.getName().matches(".*stpv.*") || f.getName().matches(".*STPV.*")){
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Return true if it is a SQLite PLNS database
	 * @param f
	 * @return
	 */
	public static boolean isSQLitePLNSFile(File f){
		//Chargement du driver
		try {
			Class.forName("org.sqlite.JDBC");
			//Connexion
			Connection database = DriverManager.getConnection("jdbc:sqlite:"+f.getAbsolutePath());
			Statement st = database.createStatement();
			st.executeQuery("select * from plns where 1");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	
	@Override
	protected void doReadStream(FileInputStream fis)
	throws PointNotFoundException {
		

	}







}