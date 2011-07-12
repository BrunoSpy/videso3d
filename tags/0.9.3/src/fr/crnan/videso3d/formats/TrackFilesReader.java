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

package fr.crnan.videso3d.formats;

import fr.crnan.videso3d.layers.TrajectoriesLayer;
import fr.crnan.videso3d.stip.PointNotFoundException;
import gov.nasa.worldwind.util.Logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Lecteur de fichiers trace radar
 * @author Bruno Spyckerelle
 * @version 0.3
 */
public abstract class TrackFilesReader {

	private String name;
	
	private TrajectoriesLayer layer = null;
	
	private List<File> files = new ArrayList<File>();
	
	private List<VidesoTrack> tracks = new ArrayList<VidesoTrack>();
	
	public TrackFilesReader(){}
	
	public TrackFilesReader(Vector<File> files, TrajectoriesLayer layer) throws PointNotFoundException {
		this.setLayer(layer);
		for(File f : files){
			try {
				this.files.add(f);
				this.readFile(f.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public TrackFilesReader(File selectedFile, TrajectoriesLayer layer) throws PointNotFoundException {
		this.setLayer(layer);
		try {
			this.files.add(selectedFile);
			this.readFile(selectedFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public TrackFilesReader(Vector<File> files) throws PointNotFoundException {
		this(files, null);
	}
	
	public TrackFilesReader(File selectedFile) throws PointNotFoundException {
		this(selectedFile, null);
	}
	
	/**
	 * @param path
	 * @throws IllegalArgumentException if <code>path</code> is null
	 * @throws java.io.IOException
	 * @throws PointNotFoundException 
	 */
	public void readFile(String path) throws IOException, PointNotFoundException
	{
		if (path == null)
		{
			String msg = Logging.getMessage("nullValue.PathIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		

		java.io.File file = new java.io.File(path);
		if (!file.exists())
		{
			String msg = Logging.getMessage("generic.FileNotFound", path);
			Logging.logger().severe(msg);
			throw new FileNotFoundException(path);
		}
		
		this.setName(file.getName());
		FileInputStream fis = new FileInputStream(file);
		this.doReadStream(fis);
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void setLayer(TrajectoriesLayer layer) {
		this.layer = layer;
	}

	public TrajectoriesLayer getLayer() {
		return this.layer;
	}
	
	protected abstract void doReadStream(FileInputStream fis) throws PointNotFoundException;
	
	public List<VidesoTrack> getTracks(){
		if(this.tracks.isEmpty()){
			if(this.layer != null){
				return (List<VidesoTrack>) this.layer.getTracks();
			}
		}
		return this.tracks;
	}
	
	public List<File> getFiles(){
		return files;
	}
}
