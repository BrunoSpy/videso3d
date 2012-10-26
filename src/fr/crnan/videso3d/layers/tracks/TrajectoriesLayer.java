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
import java.util.List;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.layers.LayerSet;
import fr.crnan.videso3d.trajectography.TracksModel;
import gov.nasa.worldwind.tracks.Track;
/**
 * Layer contenant des trajectoires et permettant un affichage sélectif.
 * @author Bruno Spyckerelle
 * @version 0.8.1
 */
public abstract class TrajectoriesLayer extends LayerSet {

	
	//Les différents styles disponibles pour la représentation des trajectoires
	/**
	 * Une polyligne simple
	 */
	public final static int STYLE_SIMPLE = 1;
	/**
	 * Une suite de polygones allant du sol à la trajectoire
	 */
	public final static int STYLE_CURTAIN = 2;
	/**
	 * Un style complexe, avec une ombre projectée au sol et la possibilité d'afficher des balises
	 */
	public final static int STYLE_PROFIL = 3;
	/**
	 * Une polyligne dont avec un gradient de couleur en fcontion de l'altitude
	 */
	public final static int STYLE_SHADED = 4;
	/**
	 * Une polyligne avec plusieurs couleurs en fonction de l'altitude
	 */
	public final static int STYLE_MULTI_COLOR = 5;
	/**
	 * Zone d'analyse
	 */
	public final static int STYLE_ANALYTICS = 6;
		
	public final static int PARAM_SPEED = 10;
	public final static int PARAM_ALTITUDE = 11;
	
	public TrajectoriesLayer(TracksModel model){
		super();
		this.setModel(model);
	}

	/**
	 * Set the corresponding color to the matching tracks
	 * @param field FIELD_ADEP, FIELD_ADEST, FIELD_IAF, FIELD_INDICATIF
	 * @param regexp
	 * @param color Color to be set
	 */
	public abstract void addFilterColor(int field, String regexp, Color color);
	
	/**
	 * Delete all color filters
	 */
	public abstract void resetFilterColor();

	/**
	 * Met en valeur un track
	 * @param track 
	 * @param b
	 */
	public abstract void highlightTrack(Track track, Boolean b);
	
	/**
	 * Met à jour les trajectoires affichées
	 */
	public abstract void update();
	
	public abstract TracksModel getModel();
		
	/**
	 * Don't forget to listen to changes of the model
	 * @param model
	 */
	public abstract void setModel(TracksModel model);
		
	/**
	 * 
	 * @param p
	 * @return The corresponding track
	 */
	public abstract VidesoTrack getTrack(Object p);
	
	/**
	 * 
	 * @param track
	 * @return The graphical object corresponding to the track
	 */
	public abstract Object getLine(VidesoTrack track);
	
	/**
	 * Possibilité de sélectionner les tracks (avec highlight) ?
	 * @return
	 */
	public abstract Boolean isTrackHighlightable();
	/**
	 * Possibilité de cacher des tracks ?
	 * @return
	 */
	public abstract Boolean isTrackHideable();
	
	/**
	 * Possibilité d'utiliser des filtres de couleur ?
	 * @return
	 */
	public abstract Boolean isTrackColorFiltrable();
	
	/**
	 * Liste des styles disponibles
	 * @return
	 */
	public abstract List<Integer> getStylesAvailable();
	
	/**
	 * Mettre à FALSE améliore généralement les performances
	 * @param b
	 */
	public abstract void setTracksHideable(Boolean b);
	
	/**
	 * Mettre à FALSE améliore généralement les performances
	 * @param b
	 */
	public abstract void setTracksHighlightable(Boolean b);
	
	/**
	 * Change le style parmi <code>TrajectoriesLayer.STYLE_SIMPLE</code>, <code>TrajectoriesLayer.STYLE_SHADED</code>, <code>TrajectoriesLayer.STYLE_MULTICOLOR</code>,
	 * <code>TrajectoriesLayer.STYLE_CURTAIN</code> et <code>TrajectoriesLayer.STYLE_PROFIL</code>
	 * @param style
	 */
	public abstract void setStyle(int style);
	
	public abstract int getStyle();
	
	/**
	 * Nom du calque. Apparait dans le gestionnaire de calques
	 */
	public abstract void setName(String name);
	/**
	 * @return Nom du calque
	 */
	public abstract String getName();
	
	public abstract Color getDefaultOutsideColor();
	
	public abstract void setDefaultOutsideColor(Color color);
	
	public abstract Color getDefaultInsideColor();
	
	public abstract void setDefaultInsideColor(Color color);
	
	/**
	 * 
	 * @param param Speed or altitude
	 * @param min 
	 * @param max
	 * @param firstColor Corresponding to min value
	 * @param secondColor Corresponding to max value
	 */
	public abstract void setShadedColors(int param, double min, double max, Color firstColor, Color secondColor);
	
	/**
	 * @return Value corresponding to {@link #getMinColor()}
	 */
	public abstract double getMinValue();
	
	/**
	 * @return Value corresponding to {@link #getMaxColor()}
	 */
	public abstract double getMaxValue();
	
	public abstract Color getMinColor();
	
	public abstract Color getMaxColor();
	
	/**
	 * @return Param used to colorized trajectories, speed or altitude
	 */
	public abstract int getParamColor();
	
	/**
	 * 
	 * @param param Speed or Altitude
	 * @param values Array of values
	 * @param colors Array of colors
	 */
	public abstract void setMultiColors(int param, Double[] values, Color[] colors);
	
	public abstract Couple<Double[], Color[]> getMultiColors();
	
	public abstract double getDefaultOpacity();
	
	public abstract void setDefaultOpacity(double opacity);
	
	public abstract double getDefaultWidth();
	
	public abstract void setDefaultWidth(double width);

	/* *********** Analytic Surface *********** */
	public abstract void setAnalyticWidth(int width);
	
	public abstract int getAnalyticWidth();
	
	public abstract void setAnalyticHeight(int height);
	
	public abstract int getAnalyticHeight();
	
	public abstract void setAnalyticScale(int scale);
	
	public abstract int getAnalyticScale();
}
