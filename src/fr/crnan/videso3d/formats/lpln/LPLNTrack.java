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
package fr.crnan.videso3d.formats.lpln;

import java.util.LinkedList;
import java.util.List;

import gov.nasa.worldwind.tracks.Track;
import gov.nasa.worldwind.tracks.TrackSegment;
/**
 * Track provenant d'un LPLN
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class LPLNTrack implements Track{

	private String name;
	
	protected List<LPLNTrackPoint> trackPoints = new LinkedList<LPLNTrackPoint>();

	/**
	 * Indicatif du vol
	 */
	private String indicatif;
	private String depart = "";
	private String arrivee = "";
	private String type = "";
	
	/**
	 * Nouveau LPLN
	 * @param string Numéro cautra
	 */
	public LPLNTrack(String string){
		this.name = string;
	}
	
	public void addPoint(LPLNTrackPoint lplnTrackPoint) {
		this.trackPoints.add(lplnTrackPoint);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getNumPoints() {
		return trackPoints.size();
	}

	public List<LPLNTrackPoint> getTrackPoints(){
		return trackPoints;
	}
	
	@Override
	public List<TrackSegment> getSegments() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setType(String type){
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}

	public String getIndicatif() {
		return this.indicatif;
	}

	public void setArrivee(String arrivee){
		this.arrivee = arrivee;
	}
	
	public String getArrivee() {
		return this.arrivee;
	}

	public void setDepart(String depart){
		this.depart = depart;
	}
	
	public String getDepart() {
		return this.depart;
	}

	public void setIndicatif(String indicatif) {
		this.indicatif = indicatif;
	}

}
