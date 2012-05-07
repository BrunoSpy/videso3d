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

import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.ihm.components.ProgressInputStream;
import fr.crnan.videso3d.stip.PointNotFoundException;
import fr.crnan.videso3d.trajectography.TracksModel;
import fr.crnan.videso3d.trajectography.TrajectoryFileFilter;

import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * Lecteur de fichiers Elvira GEO.<br />
 * @author Bruno Spyckerelle
 * @version 0.2.5
 */
public class GEOReader extends TrackFilesReader{
		
	public GEOReader(Vector<File> files) throws PointNotFoundException {
		super(files);
		this.setModel(new TracksModel());
	}
	
	public GEOReader(File selectedFile) throws PointNotFoundException {
		super(selectedFile);
		this.setModel(new TracksModel());
	}

	public GEOReader(File selectedFile, TracksModel model) throws PointNotFoundException {
		super(selectedFile, model);
	}
	
	public GEOReader(Vector<File> geoFile, TracksModel model) throws PointNotFoundException {
		super(geoFile, model);
	}

	public GEOReader(Vector<File> geoFile, TracksModel model, PropertyChangeListener listener) throws PointNotFoundException {
		super(geoFile, model, listener);
	}
	
	public GEOReader(Vector<File> files, TracksModel model,
			PropertyChangeListener listener,
			List<TrajectoryFileFilter> filters, boolean disjunctive) throws PointNotFoundException {
		super(files, model, listener, filters, disjunctive);
	}

	public static Boolean isGeoFile(File file){
		Boolean geo = false;
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(file)));
			int count = 0; //nombre de lignes lues
			while(in.ready() && !geo && count < 10){//on ne lit que les 10 premières lignes pour détecter le type de fichier
				String line = in.readLine();
				if(line.startsWith("!	Version") || line.startsWith("!	voie")){
					geo = true;
				}
				count++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return geo;
	}
	
    @Override
    protected void doReadStream(ProgressInputStream stream)
    {
        String sentence;

        BufferedReader in = new BufferedReader(
        						new InputStreamReader(stream));
        
        try
        {
        	GEOTrack track = null;
        	boolean trackValid = true;
        	while(in.ready()){
        		sentence = in.readLine();
        		if (sentence != null)
        		{
        			if(!sentence.startsWith("!")  && !sentence.startsWith("Voie")){
        				if(track == null || track.getNumTraj().compareTo(new Integer(sentence.split("\t")[1]))!=0){
        					if(track != null) {
        						if(trackValid) this.getModel().addTrack(track);
        					}
        					track = new GEOTrack(sentence);
        					trackValid = this.isTrackValid(track);
        				} else {
        					if(trackValid) track.addTrackPoint(sentence);
        				}
        			}
        		} 
        	}
        	//last Track
        	if(track != null){
        		//if layer is set, create immediately the track instead of memorizing it
        		if(trackValid) this.getModel().addTrack(track);
        	}
        }
        catch (NoSuchElementException e)
        {
        	//noinspection UnnecessaryReturnStatement
        	return;
        } catch (IOException e) {
        	e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
    }

	@Override
	protected boolean isTrackValid(VidesoTrack track) {
		boolean result = !this.disjunctive;
		if(filters == null)
			return result;
		
		for(TrajectoryFileFilter f : filters){
			switch (f.getField()) {
			case TracksModel.FIELD_ADEP:
				result = this.disjunctive
						 ? result || track.getDepart().matches(f.getRegexp())
						 : result && track.getDepart().matches(f.getRegexp());
				break;
			case TracksModel.FIELD_ADEST:
				result = this.disjunctive
							? result || track.getArrivee().matches(f.getRegexp())
							: result && track.getArrivee().matches(f.getRegexp());
				break;
			case TracksModel.FIELD_TYPE_MODE_A:
				result = this.disjunctive
				? result || track.getModeA().toString().matches(f.getRegexp())
				: result && track.getModeA().toString().matches(f.getRegexp());
				break;
			default:
				break;
			}
		}
		return result;
	}
	
}
