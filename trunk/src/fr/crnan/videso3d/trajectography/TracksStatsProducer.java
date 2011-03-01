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

package fr.crnan.videso3d.trajectography;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.stip.StipController;
import gov.nasa.worldwind.tracks.TrackPoint;
import gov.nasa.worldwind.util.Logging;

/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.0.1
 */
public class TracksStatsProducer{
	
	private PropertyChangeSupport support = new PropertyChangeSupport(this);

	public Collection<Secteur3D> computeContainingSectors(VidesoTrack track, Type type){
		if(DatasManager.getController(type) == null){
			Logging.logger().severe("Controller inexistant");
			return null;
		}
		List<Secteur3D> containingSecteurs = new ArrayList<Secteur3D>();
		Collection<Object> secteurs = DatasManager.getController(type).getObjects(StipController.SECTEUR);
		Secteur3D last = null;
		int i = 0;
		for(TrackPoint point : track.getTrackPoints()){
			this.support.firePropertyChange("progress", i-1, i);
			boolean contain = false;
			Iterator<Object> iterator = secteurs.iterator();
			while(iterator.hasNext() && !contain){
				Secteur3D secteur = (Secteur3D) iterator.next();
				if(secteur.contains(point.getPosition())){
					contain = true;
					System.out.println("producer "+secteur.getName());
					if(!secteur.equals(last)){
						last = secteur;
						containingSecteurs.add(secteur);
					}
				}
			}
			i++;
		}
		
		return containingSecteurs;
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener){
		this.support.addPropertyChangeListener(propertyName, listener);
	}
}
