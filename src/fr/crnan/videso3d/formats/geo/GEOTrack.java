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

import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.formats.VidesoTrack;
import gov.nasa.worldwind.tracks.Track;
import gov.nasa.worldwind.tracks.TrackPoint;
import gov.nasa.worldwind.tracks.TrackSegment;
/**
 * Track provenant d'un fichier Elvira GEO.
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class GEOTrack extends VidesoTrack {

	private String indicatif;
	private String depart = "";
	private String arrivee = "";
	private String type;
	private Integer numTraj;
	
	private List<GEOTrackPoint> trackPoints = new LinkedList<GEOTrackPoint>();
	
	public GEOTrack(String sentence){
		String[] words = sentence.split("\t");
		this.indicatif = words[12];
		this.type = words[14];
		if(words.length > 16 ) this.depart = words[16];
		if(words.length > 17 ) this.arrivee = words[17];
		this.numTraj = new Integer(words[1]);
		if(this.indicatif.isEmpty()) this.indicatif = "N/A "+this.numTraj;
	}
	
	@Override
	public String getName() {
		return this.indicatif;
	}

	@Override
	public int getNumPoints() {
		return trackPoints.size();
	}

	@Override
	public List<TrackSegment> getSegments() {
		return null;
	}
	
	public List<GEOTrackPoint> getTrackPoints() {
		return trackPoints;
	}

	public void setTrackPoints(List<GEOTrackPoint> trackPoints) {
		this.trackPoints = trackPoints;
	}

	public void addTrackPoint(GEOTrackPoint point){
		this.trackPoints.add(point);
	}
	
	public void addTrackPoint(String sentence){
		this.trackPoints.add(new GEOTrackPoint(sentence));
	}
	
	public String getIndicatif() {
		return indicatif;
	}

	public String getDepart() {
		return depart;
	}

	public String getArrivee() {
		return arrivee;
	}

	public String getType() {
		return type;
	}

	public Integer getNumTraj(){
		return numTraj;
	}

	@Override
	public LinkedList<TrackPoint> getTrackPointsList() {
		LinkedList<TrackPoint> trackPointsList = new LinkedList<TrackPoint>();
		trackPointsList.addAll(trackPoints);
		return trackPointsList;
	}
}
