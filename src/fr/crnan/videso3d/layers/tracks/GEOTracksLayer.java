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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.event.TableModelEvent;

import fr.crnan.videso3d.Configuration;
import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.formats.geo.GEOTrack;
import fr.crnan.videso3d.graphics.AltitudeFilterablePath;
import fr.crnan.videso3d.layers.AltitudeFilterableLayer;
import fr.crnan.videso3d.trajectography.TracksModel;
import fr.crnan.videso3d.trajectography.TracksModelListener;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Path.PositionColors;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.tracks.Track;
import gov.nasa.worldwind.tracks.TrackPoint;
import gov.nasa.worldwind.util.Logging;
/**
 * Layer contenant des tracks Elvira GEO et permettant un affichage sélectif.
 * @author Bruno Spyckerelle
 * @version 0.4.4
 */
public class GEOTracksLayer extends TrajectoriesLayer implements AltitudeFilterableLayer{
		
	private TracksModel model;
		
	protected HashMap<VidesoTrack, AltitudeFilterablePath> lines = new HashMap<VidesoTrack, AltitudeFilterablePath>();
	
	/**
	 * Couleurs des tracks
	 */
	protected List<ShapeAttributes> colors = new ArrayList<ShapeAttributes>();
	
	protected RenderableLayer layer = new RenderableLayer();
		
	private Boolean tracksHighlightable = true;
	
	private Boolean tracksHideable = false;
	
	private Boolean tracksColorFiltrable = true;
	
	protected int style = TrajectoriesLayer.STYLE_SIMPLE;
	
	private String name = "Trajectoires GEO";
	
	private ShapeAttributes normal = new BasicShapeAttributes();
	
	private ShapeAttributes highlight = new BasicShapeAttributes();

	
	/**
	 * Drops point if the previous is less <code>precision</code> far from the previous point
	 */
	private double precision = 0.0;
	
	/**
	 * Shaded color support
	 */
	private double minAltitude = 0.0;
	private double maxAltitude = 400.0*30.47;
	private Color minAltitudeColor = Color.GREEN;
	private Color maxAltitudeColor = Color.RED;
	/**
	 * Multicolor
	 */
	private Double[] altitudes = {0.0, 50.0*30.47, 195*30.47, 300*30.47, 600*30.47};
	private Color[] multicolors = {Color.WHITE, Color.GREEN, Color.ORANGE, Color.RED};	
	
	public GEOTracksLayer(TracksModel model){
		super(model);
		this.add(layer);
		this.setPickEnabled(true);
		this.setDefaultMaterial();
	}

	public GEOTracksLayer(Boolean tracksHideable, Boolean tracksHighlightable){
		this(new TracksModel());
		this.setTracksHighlightable(tracksHighlightable);
		this.setTracksHideable(tracksHideable);
	}
	
	@Override
	public void setModel(TracksModel model){
		this.model = model;
		this.model.addTableModelListener(new TracksModelListener() {
			
			@Override
			public void tableChanged(TableModelEvent e) {}
			
			@Override
			public void trackAdded(VidesoTrack track) {
				showTrack(track);
			}

			@Override
			public void trackVisibilityChanged(VidesoTrack track,
					boolean visible) {
				Path line = lines.get(track);
				if(line != null) {
					line.setVisible(visible);
					layer.firePropertyChange(AVKey.LAYER, null, layer);
				}
			}

			@Override
			public void trackSelectionChanged(VidesoTrack track,
					boolean selected) {
				Path line = lines.get(track);
				if(line != null){
					line.setHighlighted(selected);		
					layer.firePropertyChange(AVKey.LAYER, null, layer);
				}
			}

			@Override
			public void trackRemoved(VidesoTrack track) {
				removeTrack(track);
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
	
	@Override
	public TracksModel getModel(){
		return this.model;
	}
	
	protected void removeTrack(VidesoTrack track){
		Path line = this.lines.get(track);
		if(line != null){
			this.lines.remove(track);
			this.layer.removeRenderable(line);
			this.layer.firePropertyChange(AVKey.LAYER, null, this.layer);
		}
	}
	
	protected void showTrack(final VidesoTrack track){
		if(this.lines.containsKey(track)){
			Path line = this.lines.get(track);
			if(line != null){
				line.setExtrude(this.getStyle() == TrajectoriesLayer.STYLE_CURTAIN);
				if(this.getStyle() == TrajectoriesLayer.STYLE_MULTI_COLOR){
					line.setPositionColors(new PositionColors() {
						
						@Override
						public Color getColor(Position position, int ordinal) {
							double altitude = position.getElevation();
							if(altitude < altitudes[0])
								return multicolors[0];
							for(int i = 0; i< multicolors.length;i++){
								if(altitude > altitudes[i] && altitude <= altitudes[i+1])
									return multicolors[i];
							}
							return multicolors[multicolors.length-1];
						}
					});
				} else if(this.getStyle() == TrajectoriesLayer.STYLE_SHADED){
					line.setPositionColors(new PositionColors() {

						@Override
						public Color getColor(Position position, int ordinal) {
							double altitude = position.getElevation();
							if(altitude<=minAltitude)
								return minAltitudeColor;
							if(altitude>=maxAltitude)
								return maxAltitudeColor;
							return new Color (  (float)(((altitude-minAltitude)*(maxAltitudeColor.getRed()/255.0)+(maxAltitude-altitude)*(minAltitudeColor.getRed()/255.0))/(maxAltitude-minAltitude)),
									(float)(((altitude-minAltitude)*(maxAltitudeColor.getGreen()/255.0)+(maxAltitude-altitude)*(minAltitudeColor.getGreen()/255.0))/(maxAltitude-minAltitude)),
									(float)(((altitude-minAltitude)*(maxAltitudeColor.getBlue()/255.0)+(maxAltitude-altitude)*(minAltitudeColor.getBlue()/255.0))/(maxAltitude-minAltitude)));
						}
					});
				}
				//	line.setDrawVerticals(!(this.getStyle() == TrajectoriesLayer.STYLE_CURTAIN));
			}
		} else {
			List<Position> positions = new ArrayList<Position>();
			Position position = Position.ZERO;
			for(TrackPoint point : track.getTrackPoints()){
				if(!(point.getLatitude() == position.latitude.degrees  //only add a position if different from the previous position
						&& point.getLongitude() == position.longitude.degrees
						&& point.getElevation() == position.elevation)) {
					if(this.precision == 0.0 || Position.greatCircleDistance(position, point.getPosition()).degrees > this.getPrecision()) {
						positions.add(point.getPosition());
						position = point.getPosition();
					}
				}
			}
			if(positions.size()>1){ //only add a line if there's enough points
				AltitudeFilterablePath line = new AltitudeFilterablePath();
				line.setOutlinePickWidth(20);
				line.setAttributes(normal);
				line.setHighlightAttributes(highlight);
				line.setNumSubsegments(1); //améliore les performances
				line.setExtrude(this.style == TrajectoriesLayer.STYLE_CURTAIN);
		//		line.setDrawVerticals(!(this.getStyle() == TrajectoriesLayer.STYLE_CURTAIN));
				line.setAltitudeMode(WorldWind.ABSOLUTE);
				line.setPositions(positions);
				lines.put(track, line);
				//track highlight
				line.addPropertyChangeListener(new PropertyChangeListener() {
					
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if(evt.getPropertyName().equals("HIGHLIGHT")){
							getModel().setSelected((Boolean) evt.getNewValue(), track);
						} else if(evt.getPropertyName().equals("VISIBLE")){
							getModel().setVisible((Boolean) evt.getNewValue(), track);
						}
					}
				});
				this.layer.addRenderable(line);
				this.layer.firePropertyChange(AVKey.LAYER, null, this.layer);
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
		case TracksModel.FIELD_ADEST:
			for(VidesoTrack track : this.getModel().getVisibleTracks()){
				if(((GEOTrack) track).getArrivee().matches(regexp)){
					this.highlightTrack(track, false);
					Path line = this.lines.get(track);
					if(line != null) line.setAttributes(attrs);
				}
			}
			break;
		case TracksModel.FIELD_IAF:
			//Field not supported
			//TODO Throw Exception ?
			break;
		case TracksModel.FIELD_ADEP:
			for(VidesoTrack track : this.getModel().getVisibleTracks()){
				if(((GEOTrack) track).getDepart().matches(regexp)){
					this.highlightTrack(track, false);
					Path line = this.lines.get(track);
					if(line != null) line.setAttributes(attrs);
				}
			}
			break;	
		case TracksModel.FIELD_INDICATIF:
			for(VidesoTrack track : this.getModel().getVisibleTracks()){
				if(((GEOTrack) track).getIndicatif().matches(regexp)){
					this.highlightTrack(track, false);
					Path line = this.lines.get(track);
					if(line != null) line.setAttributes(attrs);
				}
			}
			break;
		case TracksModel.FIELD_TYPE_AVION:
			for(VidesoTrack track : this.getModel().getVisibleTracks()){
				if(((GEOTrack) track).getType().matches(regexp)){
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
	public void update() {
		for(VidesoTrack track : this.getModel().getVisibleTracks()){
			this.showTrack(track);
		}
		//mettre à jour les filtres volumiques
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	@Override
	public void highlightTrack(Track track, Boolean b){
		if(this.isTrackHighlightable()){
			Path line = this.lines.get((VidesoTrack)track);
			if(line != null){
				line.setHighlighted(b);
				this.firePropertyChange(AVKey.LAYER, null, this);
			}
		}
	}

//	@Override
//	public Boolean isVisible(Track track) {
//		Path line = this.lines.get(track);
//		if(line != null)
//			return line.isVisible();
//		return false;
//	}
//
//	@Override
//	public void setVisible(Boolean b, Track track) {
//		Path line = this.lines.get(track);
//		if(line != null) line.setVisible(b);
//	}

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
	/**
	 * If number of track > Configuration.TRAJECTOGRAPHIE_SEUIL_PRECISION,<br />
	 * doesn't change the style to prevent the app from crashing
	 */
	public void setStyle(int style) {
		if(style != this.style) {
			if(style == TrajectoriesLayer.STYLE_SIMPLE || 
					style == TrajectoriesLayer.STYLE_SHADED || 
					style == TrajectoriesLayer.STYLE_MULTI_COLOR ||
					this.getModel().getVisibleTracks().size() < Integer.parseInt(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_SEUIL_PRECISION, "100"))) {
				this.style = style;
				{
					//display bug when changing extrude -> delete and redraw lines
					this.lines.clear();
					this.layer.removeAllRenderables();
				}
				this.update();
			}  else {
				Logging.logger().warning("Style inchangé car nombre de tracks trop important.");
			}
		}
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
		this.firePropertyChange(AVKey.LAYER, null, this);
	}

	@Override
	public Color getDefaultInsideColor() {
		return this.normal.getInteriorMaterial().getDiffuse();
	}

	@Override
	public void setDefaultInsideColor(Color color) {
		this.normal.setInteriorMaterial(new Material(color));
		this.firePropertyChange(AVKey.LAYER, null, this);
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
		this.firePropertyChange(AVKey.LAYER, null, this);
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
		this.firePropertyChange(AVKey.LAYER, null, this);
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
		List<Integer> styles = new ArrayList<Integer>();
		if(this.getModel().getVisibleTracks().size() < Integer.parseInt(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_SEUIL_PRECISION, "100"))) styles.add(TrajectoriesLayer.STYLE_CURTAIN);
		styles.add(TrajectoriesLayer.STYLE_SIMPLE);
		styles.add(TrajectoriesLayer.STYLE_SHADED);
		styles.add(TrajectoriesLayer.STYLE_MULTI_COLOR);
		return styles;
	}

	

	@Override
	public void setMaximumViewableAltitude(double altitude) {
		for(AltitudeFilterablePath p : lines.values()){
			p.setMaximumViewableAltitude(altitude);
		}
		this.update();
	}

	@Override
	public void setMinimumViewableAltitude(double altitude) {
		for(AltitudeFilterablePath p : lines.values()){
			p.setMinimumViewableAltitude(altitude);
		}
		this.update();
	}

	@Override
	public void setShadedColors(double minAltitude, double maxAltitude,
			Color minAltitudeColor, Color maxAltitudeColor) {
		this.maxAltitude = maxAltitude;
		this.minAltitude = minAltitude;
		this.minAltitudeColor = minAltitudeColor;
		this.maxAltitudeColor = maxAltitudeColor;
	}

	@Override
	public double getMinAltitude() {
		return this.minAltitude;
	}

	@Override
	public double getMaxAltitude() {
		return this.maxAltitude;
	}

	@Override
	public Color getMinAltitudeColor() {
		return this.minAltitudeColor;
	}

	@Override
	public Color getMaxAltitudeColor() {
		return this.maxAltitudeColor;
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
