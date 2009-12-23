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

import fr.crnan.videso3d.formats.opas.OPASTrack;
import fr.crnan.videso3d.graphics.VPolyline;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.tracks.Track;
import gov.nasa.worldwind.tracks.TrackPoint;
/**
 * Layer contenant des tracks OPAS et permettant un affichage sélectif
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class OPASTracksLayer extends TrajectoriesLayer {

	private List<OPASTrack> tracks = new LinkedList<OPASTrack>();
	
	private Set<OPASTrack> selectedTrack = null;
	
	private RenderableLayer layer = new RenderableLayer();
	
	public OPASTracksLayer() {
		super();
		this.add(layer);
	}

	public void addTrack(OPASTrack track) {
		this.tracks.add(track);
		if( selectedTrack == null) selectedTrack = new HashSet<OPASTrack>(); 
		this.selectedTrack.add(track);
		this.showTrack(track);
	}

	/**
	 * Met à jour les trajectoires.<br />
	 * Warning : très consommateur de ressources car recalcule toutes les trajectoires
	 */
	public void update(){
		this.layer.removeAllRenderables();
		for(OPASTrack track : (selectedTrack == null ? tracks : selectedTrack)){
			this.showTrack(track);
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}
	
	private void showTrack(OPASTrack track){
		LinkedList<Position> positions = new LinkedList<Position>();
		for(TrackPoint point : track.getTrackPoints()){
			positions.add(point.getPosition());
		}
		VPolyline line = new VPolyline();
		line.setShadedColors(true);
		line.setMaxElevation(400*30.48);
		line.setMinElevation(50*30.48);
		line.setAntiAliasHint(Polyline.ANTIALIAS_NICEST);
		line.setPositions(positions);
		this.layer.addRenderable(line);
	}
	
	@Override
	public void addTrack(Track track) {
		if(!(track instanceof OPASTrack) ){
			throw new ClassFormatError("OPASTrack class required");
		} else {
			this.addTrack((OPASTrack)track);
		}
	}

	
	
	@Override
	public void addFilter(int field, String regexp) {
		Collection<OPASTrack> tracks;
		if(!this.isFilterDisjunctive()){
			if(selectedTrack == null) {
				tracks = this.tracks;
			} else {
				tracks = new HashSet<OPASTrack>(this.selectedTrack);
				this.selectedTrack.clear();
			}
		} else {
			tracks = this.tracks;
		}
		switch (field) {
		case FIELD_ADEST:
			for(OPASTrack track : tracks){
				if(track.getArrivee().matches(regexp)){
					this.addSelectedTrack(track);
				}
			}
			break;
		case FIELD_IAF:
			for(OPASTrack track : tracks){
				if(track.getIaf().matches(regexp)){
					this.addSelectedTrack(track);
				}
			}
			break;
		case FIELD_ADEP:
			for(OPASTrack track : tracks){
				if(track.getDepart().matches(regexp)){
					this.addSelectedTrack(track);
				}
			}
			break;	
		case FIELD_INDICATIF:
			for(OPASTrack track : tracks){
				if(track.getIndicatif().matches(regexp)){
					this.addSelectedTrack(track);
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

	private void addSelectedTrack(OPASTrack track) {
		if(selectedTrack == null) selectedTrack = new HashSet<OPASTrack>();
		this.selectedTrack.add(track);
	}

	@Override
	public void removeFilter() {
		this.selectedTrack = null;
	}

	@Override
	public Object[] getSelectedTracks(){
		return this.selectedTrack == null ? tracks.toArray() : selectedTrack.toArray();
	}
}
