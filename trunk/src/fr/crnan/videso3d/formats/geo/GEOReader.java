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

import gov.nasa.worldwind.util.Logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.ProgressMonitorInputStream;

/**
 * Lecteur de fichiers Elvira GEO.<br />
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class GEOReader {

private List<GEOTrack> tracks = new LinkedList<GEOTrack>();
	
	private String name;

	public GEOReader(){
		super();
	}
	
	public GEOReader(File selectedFile) {
		try {
			this.readFile(selectedFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public static Boolean isGeoFile(File file){
		Boolean geo = false;
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(file)));
			int count = 0; //nombre de lignes lues
			while(in.ready() && !geo && count < 10){//on ne lit que les 10 premières lignes pour détecter le type de fichier
				if(in.readLine().startsWith("!	Version")){
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
	
	/**
     * @param path
     * @throws IllegalArgumentException if <code>path</code> is null
     * @throws java.io.IOException
     */
    public void readFile(String path) throws IOException
    {
        if (path == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.setName(path);

        java.io.File file = new java.io.File(path);
        if (!file.exists())
        {
            String msg = Logging.getMessage("generic.FileNotFound", path);
            Logging.logger().severe(msg);
            throw new FileNotFoundException(path);
        }

        FileInputStream fis = new FileInputStream(file);
        this.doReadStream(fis);
    }
	
    private void doReadStream(InputStream stream)
    {
        String sentence;

        BufferedReader in = new BufferedReader(
        						new InputStreamReader(
        						new ProgressMonitorInputStream(null, 
        								"Extraction du fichier GEO ...",
        								stream)));
        
        try
        {
        	GEOTrack track = null;
        	while(in.ready()){
        		sentence = in.readLine();
        		if (sentence != null)
        		{
        			if(!sentence.startsWith("!")  && !sentence.startsWith("Voie")){
        				if(track == null || track.getNumTraj().compareTo(new Integer(sentence.split("\t")[1]))!=0){
        					if(track != null) tracks.add(track);
        					track = new GEOTrack(sentence);
        				} else {
        					track.addTrackPoint(sentence);
        				}
        			}
        		} 
        	}
        	if(track != null) tracks.add(track);
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

	private void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
    
	public List<GEOTrack> getTracks(){
		return tracks;
	}
	
}
