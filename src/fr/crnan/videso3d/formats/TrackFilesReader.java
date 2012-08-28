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

import fr.crnan.videso3d.ProgressSupport;
import fr.crnan.videso3d.databases.stip.PointNotFoundException;
import fr.crnan.videso3d.ihm.components.ProgressInputStream;
import fr.crnan.videso3d.trajectography.TracksModel;
import fr.crnan.videso3d.trajectography.TrajectoryFileFilter;
import gov.nasa.worldwind.util.Logging;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Lecteur de fichiers trace radar<br/>
 * Envoie la progression de la lecture des fichiers avec une valeur comprise entre 0 et 100.
 * @author Bruno Spyckerelle
 * @version 0.6.0
 */
public abstract class TrackFilesReader extends ProgressSupport{

	private String name;
		
	private List<File> files = new ArrayList<File>();
	
	protected int numberFiles = 0;
	
	private TracksModel model;
	
	public TrackFilesReader(){}

	protected List<TrajectoryFileFilter> filters;
	protected boolean disjunctive;
	/**
	 * 
	 * @param files
	 * @param model
	 * @param listener
	 * @throws PointNotFoundException
	 */
	public TrackFilesReader(Vector<File> files, TracksModel model, PropertyChangeListener listener) throws PointNotFoundException {
		this(files, model, listener, null, true);
	}
	
	public TrackFilesReader(Vector<File> files, TracksModel model) throws PointNotFoundException {
		this(files, model, null);
		
	}
	
	public TrackFilesReader(File selectedFile, TracksModel model) throws PointNotFoundException {
		this.setModel(model);
		try {
			this.files.add(selectedFile);
			this.readFile(selectedFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public TrackFilesReader(Vector<File> files) throws PointNotFoundException {
		this(files, new TracksModel());
	}
	
	public TrackFilesReader(File selectedFile) throws PointNotFoundException {
		this(selectedFile, new TracksModel());
	}
	
	public TrackFilesReader(Vector<File> files, TracksModel model,
			PropertyChangeListener listener,
			List<TrajectoryFileFilter> filters, boolean disjunctive) throws PointNotFoundException {
		this.setModel(model);
		this.numberFiles = files.size();
		if(listener !=  null) this.addPropertyChangeListener(listener);
		this.filters = filters;
		this.disjunctive = disjunctive;
		this.fireTaskStarts(100);
		for(File f : files){
			try {
				this.files.add(f);
				this.readFile(f.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
		
		this.fireTaskInfo(file.getName());
		
		this.setName(file.getName());
		ProgressInputStream fis = new ProgressInputStream(new FileInputStream(file));
		fis.addPropertyChangeListener(ProgressInputStream.UPDATE, new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				fireTaskProgress(((files.size()-1)*100)/numberFiles+((Integer)evt.getNewValue()/numberFiles));
			}
		});
		this.doReadStream(fis);
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	protected abstract void doReadStream(ProgressInputStream fis) throws PointNotFoundException;
	
	protected abstract boolean isTrackValid(VidesoTrack track);
	
	public TracksModel getModel(){
		return this.model;
	}
	
	public void setModel(TracksModel model){
		this.model = model;
	}
	
	public List<File> getFiles(){
		return files;
	}
}
