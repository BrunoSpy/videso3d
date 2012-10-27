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
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.Configuration;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.trajectography.TracksModel;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.util.Logging;
/**
 * Layer contenant des tracks OPAS et permettant un affichage sélectif
 * @author Bruno Spyckerelle
 * @version 0.3.0
 */
public class OPASTracksLayer extends GEOTracksLayer {
	
	public OPASTracksLayer(TracksModel model) {
		super(model);
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
			for(VidesoTrack track : getModel().getVisibleTracks()){
				if(track.getArrivee().matches(regexp)){
					this.highlightTrack(track, false);
					Path line = this.lines.get(track);
					if(line != null) line.setAttributes(attrs);
				}
			}
			break;
		case TracksModel.FIELD_IAF:
			for(VidesoTrack track : getModel().getVisibleTracks()){
				if(track.getIaf().matches(regexp)){
					this.highlightTrack(track, false);
					Path line = this.lines.get(track);
					if(line != null) line.setAttributes(attrs);
				}
			}
			break;
		case TracksModel.FIELD_ADEP:
			for(VidesoTrack track : getModel().getVisibleTracks()){
				if(track.getDepart().matches(regexp)){
					this.highlightTrack(track, false);
					Path line = this.lines.get(track);
					if(line != null) line.setAttributes(attrs);
				}
			}
			break;	
		case TracksModel.FIELD_INDICATIF:
			for(VidesoTrack track : getModel().getVisibleTracks()){
				if(track.getIndicatif().matches(regexp)){
					this.highlightTrack(track, false);
					Path line = this.lines.get(track);
					if(line != null) line.setAttributes(attrs);
				}
			}
			break;
		case TracksModel.FIELD_TYPE_AVION:
			break;
		default:
			break;
		}
	}
	
	@Override
	public void update() {
		for(VidesoTrack track : getModel().getVisibleTracks()){
			this.showTrack(track);
		}
		this.firePropertyChange(AVKey.LAYER, null, this);
	}
	
	@Override
	/**
	 * If number of track > Configuration.TRAJECTOGRAPHIE_SEUIL_PRECISION,<br />
	 * doesn't change the style to prevent the app from crashing
	 */
	public void setStyle(int style) {
		if(style != this.getStyle()) {
			if(style == TrajectoriesLayer.STYLE_SIMPLE || this.getModel().getVisibleTracks().size() < Integer.parseInt(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_SEUIL_PRECISION, "100"))) {
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
		if(this.getModel().getAllTracks().size() < Integer.parseInt(Configuration.getProperty(Configuration.TRAJECTOGRAPHIE_SEUIL_PRECISION, "100"))) styles.add(TrajectoriesLayer.STYLE_CURTAIN);
		styles.add(TrajectoriesLayer.STYLE_SIMPLE);
		return styles;
	}
	
}
