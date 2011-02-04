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
import java.util.Map.Entry;
import java.util.Set;

import fr.crnan.videso3d.formats.geo.GEOTrack;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.tracks.Track;
import gov.nasa.worldwind.tracks.TrackPoint;
/**
 * Layer contenant des tracks Elvira GEO et permettant un affichage sélectif.
 * @author Bruno Spyckerelle
 * @version 0.4
 */
public class GEOTracksLayer extends TrajectoriesLayer {
	
	private List<GEOTrack> tracks = new LinkedList<GEOTrack>();
	
	private HashMap<Integer, String> filters = new HashMap<Integer, String>();
	
	/**
	 * Ensemble des tracks surlignés
	 */
	private HashMap<GEOTrack, Path> lines = new HashMap<GEOTrack, Path>();
	
	/**
	 * Couleurs des tracks
	 */
	private List<ShapeAttributes> colors = new LinkedList<ShapeAttributes>();
	
	private RenderableLayer layer = new RenderableLayer();
		
	private Boolean tracksHighlightable = true;
	
	private Boolean tracksHideable = false;
	
	private Boolean tracksColorFiltrable = true;
	
	private int style = TrajectoriesLayer.STYLE_CURTAIN;
	
	private String name = "Trajectoires GEO";
	
	private ShapeAttributes normal = new BasicShapeAttributes();;
	
	private ShapeAttributes highlight = new BasicShapeAttributes();;
	
	/**
	 * Drops point if the previous is less <code>precision</code> far from the previous point
	 */
	private double precision = 0.0;
	
	public GEOTracksLayer(){
		super();
		this.add(layer);
		this.setPickEnabled(true);
		this.setDefaultMaterial();
	}

	public GEOTracksLayer(Boolean tracksHideable, Boolean tracksHighlightable){
		super();
		this.add(layer);
		this.setTracksHighlightable(tracksHighlightable);
		this.setDefaultMaterial();
	}
	
	private void addTrack(GEOTrack track){
		this.tracks.add(track);
		this.showTrack(track);
	}

	private void showTrack(GEOTrack track){
		if(this.lines.containsKey(track)){
			Path line = this.lines.get(track);
			if(line != null){
				line.setExtrude(this.getStyle() == TrajectoriesLayer.STYLE_CURTAIN);
	//			line.setDrawVerticals(!(this.getStyle() == TrajectoriesLayer.STYLE_CURTAIN));
			}
		} else {
			LinkedList<Position> positions = new LinkedList<Position>();
			Position position = Position.ZERO;
			for(TrackPoint point : track.getTrackPoints()){
				if(!(point.getLatitude() == position.latitude.degrees  //only add a position if different from the previous position
						&& point.getLongitude() == position.longitude.degrees
						&& point.getElevation() == position.elevation)) {
					if(precision == 0.0 || Position.greatCircleDistance(position, point.getPosition()).degrees > this.getPrecision()) {
						positions.add(point.getPosition());
						position = point.getPosition();
					}
				}
			}
			if(positions.size()>1){ //only add a line if there's enough points
				final Path line = new Path();
				line.setAttributes(normal);
				line.setHighlightAttributes(highlight);
				line.setNumSubsegments(1); //améliore les performances
				line.setExtrude(style == TrajectoriesLayer.STYLE_CURTAIN);
				line.setAttributes(normal);
				line.setAltitudeMode(WorldWind.ABSOLUTE);
				line.setPositions(positions);
				lines.put(track, line);
				this.layer.addRenderable(line);
			}
		}
	}

	@Override
	public void addTrack(Track track) {
		this.addTrack((GEOTrack)track);
	}

	@Override
	public void addFilter(int field, String regexp) {
		this.filters.put(field, regexp);
	}

	private void applyFilters(){
		if(filters.size() == 0)
			return;
		for(Path p : this.lines.values()){
			p.setVisible(!this.isFilterDisjunctive());
		}
		for(Entry<Integer, String> filter : filters.entrySet()) {
			switch (filter.getKey()) {
			case FIELD_ADEST:
				for(GEOTrack track : tracks){				
					if(track.getArrivee().matches(filter.getValue())){
						if(this.isFilterDisjunctive()){
							Path line = this.lines.get(track);
							if(line != null)
								line.setVisible(true);
						}
					} else {
						if(!this.isFilterDisjunctive()){
							Path line = this.lines.get(track);
							if(line != null)
								line.setVisible(false);
						}
					}
				}
				break;
			case FIELD_IAF:
				//Field not supported
				//TODO Throw Exception ?
				break;
			case FIELD_ADEP:
				for(GEOTrack track : tracks){
					if(track.getDepart().matches(filter.getValue())){
						if(this.isFilterDisjunctive()){
							Path line = this.lines.get(track);
							if(line != null)
								line.setVisible(true);
						}
					} else {
						if(!this.isFilterDisjunctive()){
							Path line = this.lines.get(track);
							if(line != null)
								line.setVisible(false);
						}

					}
				}
				break;	
			case FIELD_INDICATIF:
				for(GEOTrack track : tracks){
					if(track.getIndicatif().matches(filter.getValue())){
						if(this.isFilterDisjunctive()){
							Path line = this.lines.get(track);
							if(line != null)
								line.setVisible(true);
						}
					} else {
						if(!this.isFilterDisjunctive()){
							Path line = this.lines.get(track);
							if(line != null)
								line.setVisible(false);
						}

					}
				}
				break;
			case FIELD_TYPE_AVION:
				for(GEOTrack track : tracks){
					if(track.getType().matches(filter.getValue())){
						if(this.isFilterDisjunctive()){
							Path line = this.lines.get(track);
							if(line != null)
								line.setVisible(true);
						}
					} else {
						if(!this.isFilterDisjunctive()){
							Path line = this.lines.get(track);
							if(line != null)
								line.setVisible(false);
						}

					}
				}
				break;
			default:
				break;
			}
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}
	
	@Override
	public void addFilterColor(int field, String regexp, Color color){
		BasicShapeAttributes attrs = new BasicShapeAttributes();
		attrs.setOutlineWidth(this.getDefaultWidth());
		attrs.setOutlineOpacity(this.getDefaultOpacity());
		attrs.setInteriorOpacity(this.getDefaultOpacity());
		attrs.setInteriorMaterial(new Material(color));
		attrs.setOutlineMaterial(new Material(color));
		this.colors.add(attrs);
		switch (field) {
		case FIELD_ADEST:
			for(GEOTrack track : tracks){
				if(track.getArrivee().matches(regexp)){
					this.highlightTrack(track, false);
					Path line = this.lines.get(track);
					if(line != null) line.setAttributes(attrs);
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
					this.highlightTrack(track, false);
					Path line = this.lines.get(track);
					if(line != null) line.setAttributes(attrs);
				}
			}
			break;	
		case FIELD_INDICATIF:
			for(GEOTrack track : tracks){
				if(track.getIndicatif().matches(regexp)){
					this.highlightTrack(track, false);
					Path line = this.lines.get(track);
					if(line != null) line.setAttributes(attrs);
				}
			}
			break;
		case FIELD_TYPE_AVION:
			for(GEOTrack track : tracks){
				if(track.getType().matches(regexp)){
					this.highlightTrack(track, false);
					Path line = this.lines.get(track);
					if(line != null) line.setAttributes(attrs);
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
		for(Path p : lines.values()){
			p.setAttributes(normal);
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	@Override
	public void removeFilter() {
		this.filters.clear();
		for(Path p : lines.values()){
			p.setVisible(true);
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	@Override
	public void update() {
		for(GEOTrack track : tracks){
			this.showTrack(track);
		}
		this.applyFilters();
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	@Override
	public Collection<GEOTrack> getSelectedTracks(){
		Set<GEOTrack> selectedTracks = new HashSet<GEOTrack>();
		for(GEOTrack track : tracks){
			Path line = lines.get(track);
			if(line != null) {
				if(line.isVisible()) selectedTracks.add(track);
			}
		}
		return selectedTracks;
	}

	@Override
	public void highlightTrack(Track track, Boolean b){
		if(this.isTrackHighlightable()){
			Path line = this.lines.get((GEOTrack)track);
			if(line != null){
				line.setHighlighted(b);
				this.firePropertyChange(AVKey.LAYER, null, this);
			}
		}
	}


	@Override
	public void centerOnTrack(Track track) {
		if(this.isVisible(track)){
			//TODO
		}
	}

	@Override
	public Boolean isVisible(Track track) {
		Path line = this.lines.get(track);
		if(line != null)
			return line.isVisible();
		return false;
	}

	@Override
	public void setVisible(Boolean b, Track track) {
		Path line = this.lines.get(track);
		if(line != null) line.setVisible(b);
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
	 * Always true
	 */
	public void setTracksHideable(Boolean b) {}

	@Override
	/**
	 * Always true
	 */
	public void setTracksHighlightable(Boolean b) {}

	@Override
	public void setStyle(int style) {
		this.style = style;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * In degrees
	 * @param precision the precision to set
	 */
	public void setPrecision(double precision) {
		this.precision = precision;
	}

	/**
	 * @return the precision (in degrees)
	 */
	public double getPrecision() {
		return precision;
	}

	private void setDefaultMaterial() {
		this.normal.setInteriorMaterial(Material.RED);
		this.normal.setInteriorOpacity(0.4);
		this.normal.setOutlineMaterial(Material.RED);
		this.normal.setOutlineOpacity(0.4);
		
		this.highlight = new BasicShapeAttributes(normal);
		this.highlight.setOutlineWidth(2.0);
		this.highlight.setOutlineOpacity(1.0);
		this.highlight.setOutlineMaterial(Material.YELLOW);
		
	}
	
	@Override
	public Color getDefaultOutsideColor() {
		return this.normal.getOutlineMaterial().getDiffuse();
	}

	@Override
	public void setDefaultOutsideColor(Color color) {
		this.normal.setOutlineMaterial(new Material(color));
	}

	@Override
	public Color getDefaultInsideColor() {
		return this.normal.getInteriorMaterial().getDiffuse();
	}

	@Override
	public void setDefaultInsideColor(Color color) {
		this.normal.setInteriorMaterial(new Material(color));
	}

	@Override
	public double getDefaultOpacity() {
		return this.normal.getInteriorOpacity();
	}

	@Override
	public void setDefaultOpacity(double opacity) {
		for(ShapeAttributes attrs : colors){
			attrs.setInteriorOpacity(opacity);
			attrs.setOutlineOpacity(opacity);
		}
		this.normal.setInteriorOpacity(opacity);
		this.normal.setOutlineOpacity(opacity);
	}

	@Override
	public double getDefaultWidth() {
		return this.normal.getOutlineWidth();
	}

	@Override
	public void setDefaultWidth(double width) {
		for(ShapeAttributes attrs : colors){
			attrs.setOutlineWidth(width);
		}
		this.normal.setOutlineWidth(width);
	}

	@Override
	public int getStyle() {
		return this.style;
	}

	@Override
	public Boolean isTrackColorFiltrable() {
		return this.tracksColorFiltrable;
	}

	@Override
	public List<Integer> getStylesAvailable() {
		List<Integer> styles = new LinkedList<Integer>();
		styles.add(TrajectoriesLayer.STYLE_CURTAIN);
		styles.add(TrajectoriesLayer.STYLE_SIMPLE);
		return styles;
	}
	
}
