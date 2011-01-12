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
 * Le style par défaut est <code>TrajectoriesLayer.STYLE_CURTAIN</code>
 * @author Bruno Spyckerelle
 * @version 0.3
 */
public class GEOTracksLayer extends TrajectoriesLayer {

	private List<GEOTrack> tracks = new LinkedList<GEOTrack>();
	
	private Set<GEOTrack> selectedTracks = null;
	
	/**
	 * Ensemble des tracks surlignés
	 */
	private HashMap<GEOTrack, VPolyline> lines = new HashMap<GEOTrack, VPolyline>();
	
	/**
	 * Couleurs des tracks
	 */
	private HashMap<GEOTrack, Color> colors = new HashMap<GEOTrack, Color>();
	
	private RenderableLayer layer = new RenderableLayer();
		
	private Boolean tracksHighlightable = true;
	
	private Boolean tracksHideable = false;
	
	private int style = TrajectoriesLayer.STYLE_CURTAIN;
	
	private String name = "Trajectoires GEO";
	
	public GEOTracksLayer(){
		super();
		this.add(layer);
		this.setPickEnabled(true);
	}
	
	public GEOTracksLayer(Boolean tracksHideable, Boolean tracksHighlightable){
		super();
		this.add(layer);
		this.setTracksHighlightable(tracksHighlightable);
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
			line.setNumSubsegments(1); //améliore les performances
			if(style == TrajectoriesLayer.STYLE_CURTAIN){
				line.setPlain(true);
				if(colors.containsKey(track)){
					line.setColor(colors.get(track));
				} else {
					line.setColor(Pallet.makeBrighter(new Color(0.0f, 0.0f, 1.0f, 0.4f)));
				}
			} else {
				line.setPlain(false);
				if(colors.containsKey(track)){
					line.setShadedColors(false);
					line.setColor(colors.get(track));
				} else {
					line.setShadedColors(true);
				}
			}
			line.setAntiAliasHint(Polyline.ANTIALIAS_NICEST);
			line.setPositions(positions);
			if(this.isTrackHighlightable()) lines.put(track, line);
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

	public void addFilterColor(int field, String regexp, Color color){
		switch (field) {
		case FIELD_ADEST:
			for(GEOTrack track : tracks){
				if(track.getArrivee().matches(regexp)){
					this.colors.put(track, color);
					this.highlightTrack(track, false);
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
					this.colors.put(track, color);
					this.highlightTrack(track, false);
				}
			}
			break;	
		case FIELD_INDICATIF:
			for(GEOTrack track : tracks){
				if(track.getIndicatif().matches(regexp)){
					this.colors.put(track, color);
					this.highlightTrack(track, false);
				}
			}
			break;
		case FIELD_TYPE_AVION:
			for(GEOTrack track : tracks){
				if(track.getType().matches(regexp)){
					this.colors.put(track, color);
					this.highlightTrack(track, false);
				}
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	public void resetFilterColor(){
		this.colors.clear();
		this.update();
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
		if(this.isTrackHighlightable()){
			VPolyline line = this.lines.get((GEOTrack)track);
			if(line != null){
				if(b){
					if(style == TrajectoriesLayer.STYLE_CURTAIN){
						line.setColor(Pallet.makeBrighter(new Color(1.0f, 1.0f, 0.0f, 1.0f)));
					} else {			
						line.setColor(Pallet.makeBrighter(new Color(1.0f, 1.0f, 0.0f, 1.0f)));
						line.setShadedColors(false);
						line.setLineWidth(2.0);
					}	
				} else {
					if(style == TrajectoriesLayer.STYLE_CURTAIN){
						if(colors.containsKey(track)){
							line.setColor(colors.get(track));
						} else {
							line.setColor(Pallet.makeBrighter(new Color(0.0f, 0.0f, 1.0f, 0.4f)));
						}
					}else {line.setLineWidth(1.0);
						if(colors.containsKey(track)){
							line.setShadedColors(false);
							line.setColor(colors.get(track));
						} else {
							line.setShadedColors(true);
						}
					}
				}
				this.firePropertyChange(AVKey.LAYER, null, this);
			}
		}
	}


	@Override
	public void centerOnTrack(Track track) {
		if(this.isVisible(track)){
			
		}
	}

	@Override
	public Boolean isVisible(Track track) {
		return selectedTracks == null ? tracks.contains(track) : selectedTracks.contains(track);
	}

	@Override
	public void setVisible(Boolean b, Track track) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Boolean isTrackHideable() {
		return this.tracksHideable;
	}

	@Override
	public Boolean isTrackHighlightable() {
		return this.tracksHighlightable;
	}

	@Override
	/**
	 * Non implémenté par ce calque.
	 */
	public void setTracksHideable(Boolean b) {
		
	}

	@Override
	public void setTracksHighlightable(Boolean b) {
		this.tracksHighlightable = b;
		this.setPickEnabled(b);
		if(b){
			this.lines = new HashMap<GEOTrack, VPolyline>();
		} else {
			this.lines = null;
		}
	}

	@Override
	public void setStyle(int style) {
		this.style = style;
		this.update();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
}
