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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.ProgressMonitorInputStream;

import fr.crnan.videso3d.formats.TrackFilesReader;

/**
 * Lecteur de fichier OPAS
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class OPASReader extends TrackFilesReader{
		
	public OPASReader(Vector<File> files){
		super(files);
	}
	
	public OPASReader(File selectedFile) {
		super(selectedFile);
	}

	/**
	 * Détection du type de fichier
	 * @return True si le fichier est de type OPAS
	 */
	public static Boolean isOpasFile(File file){
		Boolean opas = false;
		try {
			BufferedReader in = new BufferedReader(
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
		}
		return opas;
	}
	
    protected void doReadStream(FileInputStream stream)
    {
        String sentence;

        BufferedReader in = new BufferedReader(
        						new InputStreamReader(
        						new ProgressMonitorInputStream(null, 
        								"Extraction du fichier OPAS ...",
        								stream)));
        
        try
        {
        	OPASTrack track = null;
        	while(in.ready()){
        		sentence = in.readLine();

        		if (sentence != null)
        		{
        			if(sentence.startsWith("Simulation de")){
        				if(track != null) this.getTracks().add(track);
        				String[] words = sentence.split("\\s+");
        				track = new OPASTrack(words[2], (words[6].split(":"))[1], (words[7].split(":"))[1], (words[8].split(":"))[1]);
        			} else {
        				if(!sentence.isEmpty() && track != null) track.addPoint(new OPASTrackPoint(sentence)); 
        			}
        		} 
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
}
