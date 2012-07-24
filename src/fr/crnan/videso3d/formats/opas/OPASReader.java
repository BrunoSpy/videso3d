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

import fr.crnan.videso3d.formats.TrackFilesReader;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.ihm.components.ProgressInputStream;
import fr.crnan.videso3d.stip.PointNotFoundException;
import fr.crnan.videso3d.trajectography.TracksModel;
import fr.crnan.videso3d.trajectography.TrajectoryFileFilter;

/**
 * Lecteur de fichier OPAS
 * TODO Ajouter les filtres 
 * @author Bruno Spyckerelle
 * @version 0.3.2
 */
public class OPASReader extends TrackFilesReader{

	public OPASReader(Vector<File> files, TracksModel model, PropertyChangeListener listener) throws PointNotFoundException{
		super(files, model, listener);
	}
	
	public OPASReader(Vector<File> files, TracksModel model) throws PointNotFoundException{
		super(files, model);
	}
	
	public OPASReader(File selectedFile, TracksModel model) throws PointNotFoundException {
		super(selectedFile, model);
	}
	
	public OPASReader(Vector<File> files) throws PointNotFoundException{
		super(files);
		this.setModel(new TracksModel());
	}
	
	public OPASReader(File selectedFile) throws PointNotFoundException {
		super(selectedFile);
		this.setModel(new TracksModel());
	}

	public OPASReader(Vector<File> files, TracksModel model,
			PropertyChangeListener readerListener,
			List<TrajectoryFileFilter> filters, boolean disjunctive) throws PointNotFoundException {
		super(files, model, readerListener, filters, disjunctive);
	}

	/**
	 * Détection du type de fichier
	 * @return True si le fichier est de type OPAS
	 */
	public static Boolean isOpasFile(File file){
		Boolean opas = false;
		BufferedReader in = null;
		try {
			in = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(file)));
			int count = 0; //nombre de lignes lues
			while(in.ready() && !opas && count < 10){//on ne lit que les 10 premières lignes pour détecter le type de fichier
				if(in.readLine().startsWith("Simulation de ")){
					opas = true;
				}
				count++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return opas;
	}
	
    protected void doReadStream(ProgressInputStream stream)
    {
        String sentence;

        BufferedReader in = new BufferedReader(
        						new InputStreamReader(stream));
        
        try
        {
        	OPASTrack track = null;
        	while(in.ready()){
        		sentence = in.readLine();

        		if (sentence != null)
        		{
        			if(sentence.startsWith("Simulation de")){ //nouveau track
        				if(track != null && track.getNumPoints()>1) {//on enregistre le précédent
        					this.getModel().addTrack(track);
        				}
        				String[] words = sentence.split("\\s+");
        				track = new OPASTrack(words[2], (words[6].split(":"))[1], (words[7].split(":"))[1], (words[8].split(":"))[1]);
        			} else {
        				if(!sentence.isEmpty() && track != null) track.addTrackPoint(new OPASTrackPoint(sentence)); 
        			}
        		} 
        	}
        	//last Track
        	if(track != null && track.getNumPoints()>1) {
        		this.getModel().addTrack(track);
        	}
        }
        catch (NoSuchElementException e)
        {
        	//noinspection UnnecessaryReturnStatement
        	return;
        } catch (IOException e) {
			e.printStackTrace();
		}
    }

	@Override
	protected boolean isTrackValid(VidesoTrack track) {
		// TODO Auto-generated method stub
		return true;
	}
}
