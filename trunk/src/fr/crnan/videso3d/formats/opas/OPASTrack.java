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

import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.trajectography.TracksModel;
import gov.nasa.worldwind.tracks.TrackSegment;

/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class OPASTrack implements VidesoTrack{

	private String indicatif;
	private String depart;
	private String arrivee;
	private String iaf;
	
	private List<OPASTrackPoint> points = new LinkedList<OPASTrackPoint>();
	
	public OPASTrack(String indicatif, String depart, String arrivee, String iaf){
		this.indicatif = indicatif;
		this.depart = depart;
		this.arrivee = arrivee;
		this.iaf = iaf;
	}

	public void addTrackPoint(OPASTrackPoint point){
		this.points.add(point);
	}
	
	@Override
	public String getName() {
		return indicatif;
	}

	@Override
	public int getNumPoints() {
		return points.size();
	}

	@Override
	public List<TrackSegment> getSegments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OPASTrackPoint> getTrackPoints(){
		return points;
	}

	/**
	 * @return the indicatif
	 */
	public String getIndicatif() {
		return indicatif;
	}

	/**
	 * @return the depart
	 */
	public String getDepart() {
		return depart;
	}

	/**
	 * @return the arrivee
	 */
	public String getArrivee() {
		return arrivee;
	}

	/**
	 * @return the sid
	 */
	public String getIaf() {
		return iaf;
	}
	
	@Override
	public String getType() {
		return null;
	}

	@Override
	public Integer getNumTraj() {
		return null;
	}
	
	@Override
	public Integer getModeA() {
		return null;
	}
		
	@Override
	public boolean isFieldAvailable(int field) {
		switch (field) {
		case TracksModel.FIELD_ADEP:
			return true;
		case TracksModel.FIELD_ADEST:
			return true;
		case TracksModel.FIELD_IAF:
			return true;
		case TracksModel.FIELD_INDICATIF:
			return true;
		case TracksModel.FIELD_TYPE_AVION:
			return false;
		default:
			return false;
		}
	}




}
