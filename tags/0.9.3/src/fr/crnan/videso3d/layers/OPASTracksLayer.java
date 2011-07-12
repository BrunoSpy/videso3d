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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import fr.crnan.videso3d.Configuration;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.formats.opas.OPASTrack;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.util.Logging;
/**
 * Layer contenant des tracks OPAS et permettant un affichage sélectif
 * @author Bruno Spyckerelle
 * @version 0.2.2
 */
public class OPASTracksLayer extends GEOTracksLayer {

	private List<OPASTrack> tracks = new LinkedList<OPASTrack>();
	
	@Override
	public void addTrack(final VidesoTrack track) {
		this.tracks.add((OPASTrack) track);
		this.showTrack(track);
	}
	
	@Override
	protected void applyFilters() {
		if(filters.size() == 0)
			return;
		for(Path p : this.lines.values()){
			p.setVisible(!this.isFilterDisjunctive());
		}
		
		for(Entry<Integer, String> filter : filters.entrySet()) {
		switch (filter.getKey()) {
		case FIELD_ADEST:
			for(OPASTrack track : tracks){
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
			for(OPASTrack track : tracks){
				if(track.getIaf().matches(filter.getValue())){
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
		case FIELD_ADEP:
			for(OPASTrack track : tracks){
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
			for(OPASTrack track : tracks){
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
			//TODO Throw EXception ?
			break;
		default:
			break;
		}
		}
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
			for(OPASTrack track : tracks){
				if(track.getArrivee().matches(regexp)){
					this.highlightTrack(track, false);
					Path line = this.lines.get(track);
					if(line != null) line.setAttributes(attrs);
				}
			}
			break;
		case FIELD_IAF:
			for(OPASTrack track : tracks){
				if(track.getIaf().matches(regexp)){
					this.highlightTrack(track, false);
					Path line = this.lines.get(track);
					if(line != null) line.setAttributes(attrs);
				}
			}
			break;
		case FIELD_ADEP:
			for(OPASTrack track : tracks){
				if(track.getDepart().matches(regexp)){
					this.highlightTrack(track, false);
					Path line = this.lines.get(track);
					if(line != null) line.setAttributes(attrs);
				}
			}
			break;	
		case FIELD_INDICATIF:
			for(OPASTrack track : tracks){
				if(track.getIndicatif().matches(regexp)){
					this.highlightTrack(track, false);
					Path line = this.lines.get(track);
					if(line != null) line.setAttributes(attrs);
				}
			}
			break;
		case FIELD_TYPE_AVION:
			break;
		default:
			break;
		}
	}
	
	@Override
	public void update() {
		for(OPASTrack track : tracks){
			this.showTrack(track);
		}
		this.applyFilters();
		this.firePropertyChange(AVKey.LAYER, null, this);
	}
	
	@Override
	public Collection<VidesoTrack> getSelectedTracks(){
		Set<VidesoTrack> selectedTracks = new HashSet<VidesoTrack>();
		for(OPASTrack track : tracks){
			Path line = lines.get(track);
			if(line != null) {
				if(line.isVisible()) selectedTracks.add(track);
			}
		}
		return selectedTracks;
	}
	
	@Override
	/**
	 * If number of track > Configuration.TRAJECTOGRAPHIE_SEUIL_PRECISION,<br />
	 * doesn't change the style to prevent the app from crashing
	 */
	public void setStyle(int style) {
		if(style != this.getStyle()) {
			if(style == TrajectoriesLayer.STYLE_SIMPLE || this.tracks.size() < Integer.parseInt(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_SEUIL_PRECISION, "100"))) {
				this.style = style;
				{
					//display bug when changing extrude -> delete and redraw lines
					this.lines.clear();
					this.layer.removeAllRenderables();
				}
				this.update();
			} else {
				Logging.logger().warning("Style inchangé car nombre de tracks trop important.");
			}
		}
	}
	
	@Override
	public List<Integer> getStylesAvailable() {
		List<Integer> styles = new LinkedList<Integer>();
		if(this.tracks.size() < Integer.parseInt(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_SEUIL_PRECISION, "100"))) styles.add(TrajectoriesLayer.STYLE_CURTAIN);
		styles.add(TrajectoriesLayer.STYLE_SIMPLE);
		return styles;
	}
	
	@Override
	public List<? extends VidesoTrack> getTracks() {
		return this.tracks;
	}
	
}
