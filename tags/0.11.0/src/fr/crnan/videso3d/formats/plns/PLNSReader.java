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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import fr.crnan.videso3d.ProgressSupport;
import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.ihm.components.ProgressInputStream;
import fr.crnan.videso3d.stip.PointNotFoundException;
import fr.crnan.videso3d.trajectography.PLNSTracksModel;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.4
 */
public class PLNSReader extends TrackFilesReader {


	public PLNSReader(File[] plnsFiles, File databaseFile, final PLNSTracksModel model, PropertyChangeListener listener) throws PointNotFoundException {
		this.setModel(model);
		this.addPropertyChangeListener(listener);
		this.setName(plnsFiles[0].getName().substring(0, 6)+"...");
		try {
			//Chargement du driver
			Class.forName("org.sqlite.JDBC");
			//Connexion
			Connection database = DriverManager.getConnection("jdbc:sqlite:"+databaseFile.getAbsolutePath());
			PLNSExtractor extractor = new PLNSExtractor(plnsFiles, database);
			extractor.addPropertyChangeListener(new PropertyChangeListener() {
				
				int max = 0;
				int withModel = (model == null ? 2 : 1);
				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if(evt.getPropertyName().equals(ProgressSupport.TASK_STARTS)){
						max = (Integer) evt.getNewValue();
						fireTaskStarts(100);
					} else if(evt.getPropertyName().equals(ProgressSupport.TASK_PROGRESS)){
						fireTaskProgress(((Integer)evt.getNewValue()*50*withModel)/max);
					} else if(evt.getPropertyName().equals(ProgressSupport.TASK_INFO)){
						fireTaskInfo((String) evt.getNewValue());
					}
				}
			});
			extractor.doExtract();
			if(model != null){
				//once the extraction is done, fullfill the model
				model.getProgressSupport().addPropertyChangeListener(new PropertyChangeListener() {

					int max = 0;

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if(evt.getPropertyName().equals(ProgressSupport.TASK_STARTS)){
							max = (Integer) evt.getNewValue();
						} else if(evt.getPropertyName().equals(ProgressSupport.TASK_PROGRESS)){
							fireTaskProgress(50+((Integer)evt.getNewValue()*50)/max);
						} else if(evt.getPropertyName().equals(ProgressSupport.TASK_INFO)){
							fireTaskInfo((String)evt.getNewValue());
						}
					}
				});
				model.setDatabase(databaseFile);
			}
		} catch (ClassNotFoundException e){
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for(File f : plnsFiles){
				this.getFiles().add(f);
		}
		fireTaskEnds();
	}

	/**
	 * Si les fichiers sont déjà dans une basse de données, il suffit de référencer le modèle correspondant.
	 * @param model
	 */
	public PLNSReader(File file, PLNSTracksModel model, PropertyChangeListener listener){
		this.setModel(model);
		this.getFiles().add(file);
		this.setName("PLNS "+file.getName());
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
			return false;
		}
		return true;
	}

	
	@Override
	protected void doReadStream(ProgressInputStream fis)
	throws PointNotFoundException {
		

	}

	@Override
	protected boolean isTrackValid(VidesoTrack track) {
		// TODO Auto-generated method stub
		return true;
	}







}
