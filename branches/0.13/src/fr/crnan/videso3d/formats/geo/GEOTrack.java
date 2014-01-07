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

import java.util.ArrayList;
import java.util.List;

import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.trajectography.TracksModel;
import gov.nasa.worldwind.tracks.TrackSegment;
/**
 * Track provenant d'un fichier Elvira GEO.
 * @author Bruno Spyckerelle
 * @version 0.1.3.1
 */
public class GEOTrack implements VidesoTrack {

	private String indicatif;
	private String depart = "";
	private String arrivee = "";
	private String type;
	private Integer numTraj;
	private Integer modeA = 0;
	
	private List<GEOTrackPoint> trackPoints = new ArrayList<GEOTrackPoint>();
	
	public GEOTrack(String sentence){
		String[] words = sentence.split("\t");
		this.indicatif = words[12].trim();
		if(!words[11].trim().isEmpty()) this.modeA = new Integer(words[11].trim());
		this.type = words[14].trim();
		if(words.length > 16 ) this.depart = words[16].trim();
		if(words.length > 17 ) this.arrivee = words[17].trim();
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
	
	@Override
	public String getIndicatif() {
		return indicatif;
	}

	@Override
	public String getDepart() {
		return depart;
	}

	@Override
	public String getArrivee() {
		return arrivee;
	}

	@Override
	public String getType() {
		return type;
	}
	
	@Override
	public Integer getNumTraj(){
		return numTraj;
	}
	
	@Override
	public String getIaf() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see fr.crnan.videso3d.formats.VidesoTrack#getModeA()
	 */
	@Override
	public Integer getModeA() {
		return this.modeA;
	}

	@Override
	public boolean isFieldAvailable(int field) {
		switch (field) {
		case TracksModel.FIELD_ADEP:
			return true;
		case TracksModel.FIELD_ADEST:
			return true;
		case TracksModel.FIELD_IAF:
			return false;
		case TracksModel.FIELD_INDICATIF:
			return true;
		case TracksModel.FIELD_TYPE_AVION:
			return true;
		case TracksModel.FIELD_MODE_A:
			return true;
		default:
			return false;
		}
	}


	
}
