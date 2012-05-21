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
package fr.crnan.videso3d.layers.tracks;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TableModelEvent;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.DatasManager;
import fr.crnan.videso3d.Pallet;
import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.formats.plns.PLNSTrack;
import fr.crnan.videso3d.formats.plns.PLNSTrackPoint;
import fr.crnan.videso3d.graphics.Profil3D;
import fr.crnan.videso3d.layers.ProfilLayer;
import fr.crnan.videso3d.stip.StipController;
import fr.crnan.videso3d.trajectography.TracksModel;
import fr.crnan.videso3d.trajectography.TracksModelListener;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.tracks.Track;
/**
 * 
 * @author Bruno SPyckerelle
 * @version 0.1.0
 */
public class PLNSTracksLayer extends TrajectoriesLayer {

	private ProfilLayer layer = new ProfilLayer("PLNS");

	private HashMap<PLNSTrack, Profil3D> profils = new HashMap<PLNSTrack, Profil3D>();

	private String name = "LPLN";

	private int style = TrajectoriesLayer.STYLE_PROFIL;

	protected Color defaultInsideColor = Pallet.makeBrighter(Color.BLUE);

	protected Color defaultOutsideColor = Color.BLUE;

	protected double defaultWidth = 1.0;

	protected double defaultOpacity = 0.3;

	private TracksModel model;
	
	private Double[] altitudes = {0.0, 50.0*30.47, 195*30.47, 300*30.47, 600*30.47};
	private Color[] multicolors = {Color.WHITE, Color.GREEN, Color.ORANGE, Color.RED};	
	
	public PLNSTracksLayer(TracksModel model){
		super(model);
		this.add(layer);
		this.setDefaultInsideColor(defaultInsideColor);
		this.setDefaultOutsideColor(this.defaultOutsideColor);
	}

	@Override
	public TracksModel getModel() {
		return this.model;
	}

	@Override
	public void setModel(TracksModel model) {
		this.model = model;
		this.model.addTableModelListener(new TracksModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {}

			@Override
			public void trackAdded(VidesoTrack track) {
				showTrack((PLNSTrack) track);
			}

			@Override
			public void trackVisibilityChanged(VidesoTrack track,
					boolean visible) {
				if(visible){
					showTrack((PLNSTrack) track);
				} else {
					hideTrack((PLNSTrack) track);
				}
			}

			@Override
			public void trackSelectionChanged(VidesoTrack track,
					boolean selected) {
				highlightTrack(track, selected);				
			}

			@Override
			public void trackRemoved(VidesoTrack track) {
				removeTrack((PLNSTrack) track);
			}

			@Override
			public void trackVisibilityChanged(Collection<VidesoTrack> track,
					boolean visible) {
				for(VidesoTrack t : track){
					trackVisibilityChanged(t, visible);
				}
			}

			@Override
			public void trackSelectionChanged(Collection<VidesoTrack> track,
					boolean selected) {
				for(VidesoTrack t : track){
					trackSelectionChanged(t, selected);
				}
			}

			@Override
			public void trackAdded(Collection<VidesoTrack> track) {
				for(VidesoTrack t : track){
					trackAdded(t);
				}				
			}

			@Override
			public void trackRemoved(Collection<VidesoTrack> track) {
				for(VidesoTrack t : track){
					trackRemoved(t);
				}
			}
		});
	}

	protected void showTrack(PLNSTrack track){
		if(profils.containsKey(track)){
			this.layer.addProfil3D(profils.get(track));
		} else {
			LinkedList<Position> positions = new LinkedList<Position>();
		//	LinkedList<String> balises = new LinkedList<String>();
			for(PLNSTrackPoint point : track.getTrackPoints()){
				positions.add(point.getPosition());
				DatasManager.getController(Type.STIP).showObject(StipController.BALISES, point.getName());
			}
			if(positions.size()>1){ //only add a line if there's enough points
				Profil3D profil = new Profil3D(positions);
				this.profils.put(track, profil);
				this.layer.addProfil3D(profil);
			}
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	protected void removeTrack(PLNSTrack track){
		Profil3D profil = this.profils.get(track);
		if(profil != null){
			this.layer.removeProfil3D(profil);
			this.firePropertyChange(AVKey.LAYER, null, this);
			this.profils.remove(track);
		}
	}
	
	protected void hideTrack(PLNSTrack track){
		Profil3D profil = this.profils.get(track);
		if(profil != null){
			this.layer.removeProfil3D(profil);
			this.firePropertyChange(AVKey.LAYER, null, this);
		}
	}
	
	@Override
	public void update() {
		this.layer.removeAllRenderables();
		for(VidesoTrack track : this.getModel().getVisibleTracks()){
			this.showTrack((PLNSTrack) track);
		}
		this.firePropertyChange(AVKey.LAYER, null, this);

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

	protected HashMap<PLNSTrack, Profil3D> getProfils(){
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
	public void setShadedColors(double minAltitude, double maxAltitude,
			Color firstColor, Color secondColor) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getMinAltitude() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMaxAltitude() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Color getMinAltitudeColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getMaxAltitudeColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMultiColors(Double[] altitudes, Color[] colors) {
		this.altitudes = altitudes;
		this.multicolors = colors;
	}

	@Override
	public Couple<Double[], Color[]> getMultiColors() {
		return new Couple<Double[], Color[]>(this.altitudes, this.multicolors);
	}

}
