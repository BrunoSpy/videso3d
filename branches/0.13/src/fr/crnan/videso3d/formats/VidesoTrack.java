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

import fr.crnan.videso3d.trajectography.TracksModel;
import gov.nasa.worldwind.tracks.Track;
import gov.nasa.worldwind.tracks.TrackPoint;

import java.util.List;

/**
 * 
 * @author Adrien Vidal
 * @author Bruno Spyckerelle
 * @version 0.5.0
 */
public interface VidesoTrack extends Track{

	public List<? extends TrackPoint> getTrackPoints();
	
	public String getIndicatif();

	public String getDepart();

	public String getArrivee();

	public String getType();

	public Integer getNumTraj();
	
	public String getIaf();
	
	public Integer getModeA();
	
	public String getFirstHour();
	
	/**
	 * Returns true if the field is supported by the track.<br />
	 * List of available fields is defined by {@link TracksModel}
	 * @param field
	 * @return
	 */
	public boolean isFieldAvailable(int field);
}
