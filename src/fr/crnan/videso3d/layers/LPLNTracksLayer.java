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
package fr.crnan.videso3d.layers;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import fr.crnan.videso3d.formats.lpln.LPLNTrack;
import fr.crnan.videso3d.formats.lpln.LPLNTrackPoint;
import fr.crnan.videso3d.graphics.Profil3D;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.tracks.Track;
/**
 * Layer d'accueil pour des trajectoires issues d'un LPLN
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class LPLNTracksLayer extends TrajectoriesLayer {

	private ProfilLayer layer = new ProfilLayer("LPLN");
	
	private List<LPLNTrack> tracks = new LinkedList<LPLNTrack>();
	
	private Set<LPLNTrack> selectedTracks = null;
	
	public LPLNTracksLayer(){
		super();
		this.add(layer);
	}
	
	@Override
	public void addFilter(int field, String regexp) {
		Collection<LPLNTrack> tracks;
		if(!this.isFilterDisjunctive()){
			if(selectedTracks == null) {
				tracks = this.tracks;
			} else {
				tracks = new HashSet<LPLNTrack>(this.selectedTracks);
				this.selectedTracks.clear();
			}
		} else {
			tracks = this.tracks;
		}
		switch (field) {
		case FIELD_ADEST:
			for(LPLNTrack track : tracks){
				if(track.getArrivee().matches(regexp)){
					this.addSelectedTrack(track);
				}
			}
			break;
		case FIELD_IAF:
			//Field not supported
			//TODO Throw Exception ?
			break;
		case FIELD_ADEP:
			for(LPLNTrack track : tracks){
				if(track.getDepart().matches(regexp)){
					this.addSelectedTrack(track);
				}
			}
			break;	
		case FIELD_INDICATIF:
			for(LPLNTrack track : tracks){
				if(track.getIndicatif().matches(regexp)){
					this.addSelectedTrack(track);
				}
			}
			break;
		case FIELD_TYPE_AVION:
			for(LPLNTrack track : tracks){
				if(track.getType().matches(regexp)){
					this.addSelectedTrack(track);
				}
			}
			break;
		default:
			break;
		}
	}

	private void addTrack(LPLNTrack track){
		this.tracks.add(track);
		this.addSelectedTrack(track);
		this.showTrack(track);
	}
	
	private void showTrack(LPLNTrack track){
		LinkedList<Position> positions = new LinkedList<Position>();
		LinkedList<String> balises = new LinkedList<String>();
		for(LPLNTrackPoint point : track.getTrackPoints()){
			positions.add(point.getPosition());
			balises.add(point.getName());			
		}
		if(positions.size()>1){ //only add a line if there's enough points
			Profil3D profil = new Profil3D(balises, positions);
			this.layer.addProfil3D(profil);
		}
	}
	
	@Override
	public void addTrack(Track track) {
		if(track instanceof LPLNTrack){
			this.addTrack((LPLNTrack)track);
		}
	}

	@Override
	public Collection<LPLNTrack> getSelectedTracks() {
		return this.selectedTracks == null ? tracks : selectedTracks;
	}

	@Override
	public void removeFilter() {
		this.selectedTracks = null;
	}

	@Override
	public void update() {
		this.layer.removeAll();
		for(LPLNTrack track : (selectedTracks == null ? tracks : selectedTracks)){
			this.showTrack(track);
		}
		this.firePropertyChange(AVKey.LAYER, null, this);

	}

	private void addSelectedTrack(LPLNTrack track) {
		if(selectedTracks == null) this.selectedTracks = new HashSet<LPLNTrack>();
		this.selectedTracks.add(track);
	}

	@Override
	public void highlightTrack(Track track) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeHighlightedTracks() {
		// TODO Auto-generated method stub
		
	}
	
}