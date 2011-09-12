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
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.formats.lpln.LPLNTrack;
import fr.crnan.videso3d.formats.lpln.LPLNTrackPoint;
import fr.crnan.videso3d.graphics.Profil3D;
import fr.crnan.videso3d.trajectography.PolygonsSetFilter;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.tracks.Track;
/**
 * Layer d'accueil pour des trajectoires issues d'un LPLN
 * @author Bruno Spyckerelle
 * @version 0.3.2
 */
public class LPLNTracksLayer extends TrajectoriesLayer {

	private ProfilLayer layer = new ProfilLayer("LPLN");
	
	private HashMap<LPLNTrack, Boolean> tracks = new HashMap<LPLNTrack, Boolean>();
	
	private HashMap<LPLNTrack, Profil3D> profils = new HashMap<LPLNTrack, Profil3D>();
	
	private Set<LPLNTrack> selectedTracks = null;
	
	private String name = "LPLN";
	
	private int style = TrajectoriesLayer.STYLE_PROFIL;
	
	protected Color defaultInsideColor = Pallet.makeBrighter(Color.BLUE);
	
	protected Color defaultOutsideColor = Color.BLUE;
	
	protected double defaultWidth = 1.0;

	protected double defaultOpacity = 0.3;
	
	public LPLNTracksLayer(){
		super();
		this.add(layer);
		this.setDefaultInsideColor(defaultInsideColor);
		this.setDefaultOutsideColor(this.defaultOutsideColor);
	}
	
	@Override
	public void addFilter(int field, String regexp) {
		Collection<LPLNTrack> tracks;
		if(!this.isFilterDisjunctive()){
			if(selectedTracks == null) {
				this.selectedTracks = new HashSet<LPLNTrack>();
				tracks = this.tracks.keySet();
			} else {
				tracks = new HashSet<LPLNTrack>(this.selectedTracks);
				this.selectedTracks.clear();
			}
		} else {
			this.selectedTracks = new HashSet<LPLNTrack>();
			tracks = this.tracks.keySet();
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
		this.tracks.put(track, true);
		this.showTrack(track);
	}
	

	@Override
	public void removeTracks(List<Track> selectedTracks) {
		for(Track track : selectedTracks){
			this.tracks.remove(track);
			this.profils.remove(track);
			this.selectedTracks.remove(track);
		}
		this.update();
	}
	
	protected void showTrack(LPLNTrack track){
		if(profils.containsKey(track)){
			this.layer.addProfil3D(profils.get(track));
		} else {
			LinkedList<Position> positions = new LinkedList<Position>();
			LinkedList<String> balises = new LinkedList<String>();
			for(LPLNTrackPoint point : track.getTrackPoints()){
				positions.add(point.getPosition());
				balises.add(point.getName());
			}
			if(positions.size()>1){ //only add a line if there's enough points
				Profil3D profil = new Profil3D(balises, positions);
				this.profils.put(track, profil);
				this.layer.addProfil3D(profil);
			}
		}
	}
	
	@Override
	public void addTrack(VidesoTrack track) {
		if(track instanceof LPLNTrack){
			this.addTrack((LPLNTrack)track);
		}
	}

	@Override
	public Collection<LPLNTrack> getSelectedTracks() {
		return this.selectedTracks == null ? tracks.keySet() : selectedTracks;
	}

	@Override
	public void removeFilter() {
		this.selectedTracks = null;
		this.update();
	}

	@Override
	public void update() {
		this.layer.removeAllRenderables();
		for(LPLNTrack track : (selectedTracks == null ? tracks.keySet() : selectedTracks)){
			if(this.isVisible(track)) this.showTrack(track);
		}
		this.firePropertyChange(AVKey.LAYER, null, this);

	}

	private void addSelectedTrack(LPLNTrack track) {
		if(selectedTracks == null) this.selectedTracks = new HashSet<LPLNTrack>();
		this.selectedTracks.add(track);
	}

	@Override
	public void highlightTrack(Track track, Boolean b) {
		Profil3D profil = this.profils.get(track);
		if(profil != null){
			profil.highlight(b);
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	@Override
	public Boolean isVisible(Track track) {
		return tracks.get(track);
	}

	@Override
	public void setVisible(Boolean b, Track track) {
		tracks.put((LPLNTrack) track, b);
		this.update();
	}
	
	
	@Override
	public Boolean isTrackHideable() {
		return true;
	}

	@Override
	public Boolean isTrackHighlightable() {
		return true;
	}

	@Override

	public void setTracksHideable(Boolean b) {}

	@Override
	public void setTracksHighlightable(Boolean b) {}

	@Override
	/**
	 * Not implemented by this layer
	 */
	public void setStyle(int style) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void addFilterColor(int field, String regexp, Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetFilterColor() {
		// TODO Auto-generated method stub
		
	}
	
	protected HashMap<LPLNTrack, Profil3D> getProfils(){
		return profils;
	}
	protected ProfilLayer getLayer(){
		return layer;
	}

	@Override
	public Color getDefaultOutsideColor() {
		return this.defaultOutsideColor;
	}

	@Override
	public void setDefaultOutsideColor(Color color) {
		for(Profil3D p : profils.values()){
			p.setOutsideColor(color);
		}
		this.defaultOutsideColor = color;
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	@Override
	public Color getDefaultInsideColor() {
		return this.defaultInsideColor;
	}

	@Override
	public void setDefaultInsideColor(Color color) {
		for(Profil3D p : profils.values()){
			p.setInsideColor(color);
		}
		this.defaultInsideColor = color;
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	@Override
	public double getDefaultOpacity() {
		return this.defaultOpacity;
	}

	@Override
	public void setDefaultOpacity(double opacity) {
		this.defaultOpacity  = opacity;
		Color c = new Color(defaultInsideColor.getRed(), defaultInsideColor.getGreen(), defaultInsideColor.getBlue(), (int)(opacity*255));
		for(Profil3D p : profils.values()){
			p.getCurtain().setColor(c);
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	@Override
	public double getDefaultWidth() {
		return this.defaultWidth;
	}

	@Override
	public void setDefaultWidth(double width) {
		for(Profil3D p : profils.values()){
			p.getProfil().setLineWidth(width);
		}
		this.defaultWidth = width;
		this.firePropertyChange(AVKey.LAYER, null, this);

	}
	
	@Override
	public int getStyle() {
		return this.style;
	}

	@Override
	public Boolean isTrackColorFiltrable() {
		return false;
	}

	@Override
	public List<Integer> getStylesAvailable() {
		List<Integer> styles = new LinkedList<Integer>();
		styles.add(TrajectoriesLayer.STYLE_PROFIL);
		return styles;
	}

	@Override
	public List<? extends VidesoTrack> getTracks() {
		List<LPLNTrack> tracksList = new LinkedList<LPLNTrack>();
		tracksList.addAll(this.tracks.keySet());
		return tracksList;
	}

	@Override
	public boolean isPolygonFilterable() {
		return false;
	}

	@Override
	public void addPolygonFilter(PolygonsSetFilter polygon) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disablePolygonFilter(PolygonsSetFilter polygon) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enablePolygonFilter(PolygonsSetFilter polygon) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isPolygonFilterActive(PolygonsSetFilter polygon) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getNumberTrajectories(PolygonsSetFilter polygon) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<PolygonsSetFilter> getPolygonFilters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removePolygonFilter(PolygonsSetFilter polygons) {
		// TODO Auto-generated method stub
		
	}


}
