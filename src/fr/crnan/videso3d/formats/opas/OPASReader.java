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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.ProgressMonitorInputStream;

import gov.nasa.worldwind.util.Logging;

/**
 * Lecteur de fichier OPAS
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class OPASReader {

	private List<OPASTrack> tracks = new LinkedList<OPASTrack>();
	
	private String name;

	public OPASReader(){
		super();
	}
	
	public OPASReader(File selectedFile) {
		try {
			this.readFile(selectedFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
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
        				if(track != null) this.tracks.add(track);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	private void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
    
	public List<OPASTrack> getTracks(){
		return tracks;
	}
}
