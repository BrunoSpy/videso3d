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

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.formats.geo.GEOTrack;
import fr.crnan.videso3d.graphics.VPolyline;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.tracks.Track;
import gov.nasa.worldwind.tracks.TrackPoint;
/**
 * Layer contenant des tracks Elvira GEO et permettant un affichage sélectif.
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class GEOTracksLayer extends TrajectoriesLayer {

	private List<GEOTrack> tracks = new LinkedList<GEOTrack>();
	
	private Set<GEOTrack> selectedTracks = null;
	
	private HashMap<GEOTrack, VPolyline> lines = new HashMap<GEOTrack, VPolyline>();
	
	private RenderableLayer layer = new RenderableLayer();
	
	
	public GEOTracksLayer(){
		super();
		this.add(layer);
		this.setPickEnabled(true);
	}
	
	private void addTrack(GEOTrack track){
		this.tracks.add(track);
		this.addSelectedTrack(track);
		this.showTrack(track);
	}

	private void showTrack(GEOTrack track){
		LinkedList<Position> positions = new LinkedList<Position>();
		Position position = Position.ZERO;
		for(TrackPoint point : track.getTrackPoints()){
			if(!(point.getLatitude() == position.latitude.degrees  //only add a position if different from the previous position
					&& point.getLongitude() == position.longitude.degrees
					&& point.getElevation() == position.elevation)) {
				positions.add(point.getPosition());
				position = point.getPosition();
			}
		}
		if(positions.size()>1){ //only add a line if there's enough points
			VPolyline line = new VPolyline();
			line.setPlain(true);
			line.setColor(Pallet.makeBrighter(new Color(0.0f, 0.0f, 1.0f, 0.4f)));
			line.setAntiAliasHint(Polyline.ANTIALIAS_NICEST);
			line.setPositions(positions);
			lines.put(track, line);
			this.layer.addRenderable(line);
		}
	}

	@Override
	public void addTrack(Track track) {
		this.addTrack((GEOTrack)track);
	}

	@Override
	public void addFilter(int field, String regexp) {
		Collection<GEOTrack> tracks;
		if(!this.isFilterDisjunctive()){
			if(selectedTracks == null) {
				tracks = this.tracks;
			} else {
				tracks = new HashSet<GEOTrack>(this.selectedTracks);
				this.selectedTracks.clear();
			}
		} else {
			tracks = this.tracks;
		}
		switch (field) {
		case FIELD_ADEST:
			for(GEOTrack track : tracks){
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
			for(GEOTrack track : tracks){
				if(track.getDepart().matches(regexp)){
					this.addSelectedTrack(track);
				}
			}
			break;	
		case FIELD_INDICATIF:
			for(GEOTrack track : tracks){
				if(track.getIndicatif().matches(regexp)){
					this.addSelectedTrack(track);
				}
			}
			break;
		case FIELD_TYPE_AVION:
			for(GEOTrack track : tracks){
				if(track.getType().matches(regexp)){
					this.addSelectedTrack(track);
				}
			}
			break;
		default:
			break;
		}
	}

	private void addSelectedTrack(GEOTrack track) {
		if(selectedTracks == null) this.selectedTracks = new HashSet<GEOTrack>();
		this.selectedTracks.add(track);
	}

	@Override
	public void removeFilter() {
		this.selectedTracks = null;
	}

	@Override
	public void update() {
		this.layer.removeAllRenderables();
		for(GEOTrack track : (selectedTracks == null ? tracks : selectedTracks)){
			this.showTrack(track);
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	@Override
	public Collection<GEOTrack> getSelectedTracks(){
		return this.selectedTracks == null ? tracks : selectedTracks;
	}

	@Override
	public void highlightTrack(Track track, Boolean b){
		VPolyline line = this.lines.get((GEOTrack)track);
		if(line != null){
			if(b){
				this.lines.get((GEOTrack)track).setColor(Pallet.makeBrighter(new Color(1.0f, 1.0f, 0.0f, 0.4f)));
			} else {
				this.lines.get((GEOTrack)track).setColor(Pallet.makeBrighter(new Color(0.0f, 0.0f, 1.0f, 0.4f)));
			}
			this.firePropertyChange(AVKey.LAYER, null, this);
		}
	}

	@Override
	public Boolean isVisible(Track track) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVisible(Boolean b, Track track) {
		// TODO Auto-generated method stub
		
	}
	
}
