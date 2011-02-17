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
import java.util.List;

import fr.crnan.videso3d.formats.VidesoTrack;
import fr.crnan.videso3d.graphics.VPolygon;
import gov.nasa.worldwind.tracks.Track;
/**
 * Layer contenant des trajectoires et permettant un affichage sélectif.
 * @author Bruno Spyckerelle
 * @version 0.5.0
 */
public abstract class TrajectoriesLayer extends LayerSet {

	/**
	 * Les différents champs accessibles
	 */
	public static final int FIELD_ADEP = 1;
	public static final int FIELD_ADEST = 2;
	public static final int FIELD_IAF = 3;
	public static final int FIELD_INDICATIF = 4;
	public static final int FIELD_TYPE_AVION = 5;
	
	
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
	 * Une polyligne dont la couleur change en fonction de l'altitude
	 */
	public final static int STYLE_SHADED = 4;
	
	
	private boolean disjunctive = true; //"or" by default
	
	public TrajectoriesLayer(){
		super();
	}
	/**
	 * Ajoute un Track
	 * @param track {@link Track}
	 */
	public abstract void addTrack(VidesoTrack track);

	/**
	 * Filter tracks whose field matches regexp
	 * @param field FIELD_ADEP, FIELD_ADEST, FIELD_IAF, FIELD_INDICATIF
	 * @param regexp
	 */
	public abstract void addFilter(int field, String regexp);

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
	 * Sets filter type.<br />
	 * Does not apply to the current filters.
	 * @param b If true, filters are conjonctives (= and), otherwise disjunctives (=or)
	 */
	public void setFilterDisjunctive(Boolean b){
		if(isFilterDisjunctive() != b ){
			this.disjunctive = b;
		}
	}
	
	public Boolean isFilterDisjunctive(){
		return this.disjunctive;
	}
	
	/**
	 * Supprime les filtres.
	 */
	public abstract void removeFilter();

	/**
	 * Met en valeur un track
	 * @param track 
	 * @param b
	 */
	public abstract void highlightTrack(Track track, Boolean b);
	
	/**
	 * Centers the globe on the specified track
	 * @param track
	 */
	public abstract void centerOnTrack(Track track);
	
	/**
	 * Met à jour les trajectoires affichées
	 */
	public abstract void update();
	
	public abstract Collection<? extends VidesoTrack> getSelectedTracks();
	
	public abstract List<? extends VidesoTrack> getTracks();
	
	/**
	 * Returns true or false wether the track is displayed on the globe or not
	 * @param track
	 * @return
	 */
	public abstract Boolean isVisible(Track track);
	
	/**
	 * Rend visible ou non le track concerné
	 * @param b
	 * @param track
	 */
	public abstract void setVisible(Boolean b, Track track);
	
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
	 * Change le style parmi <code>TrajectoriesLayer.STYLE_SIMPLE</code>, <code>TrajectoriesLayer.STYLE_SHADED</code>, <code>TrajectoriesLayer.STYLE_CURTAIN</code> et <code>TrajectoriesLayer.STYLE_PROFIL</code>
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
	
	public abstract double getDefaultOpacity();
	
	public abstract void setDefaultOpacity(double opacity);
	
	public abstract double getDefaultWidth();
	
	public abstract void setDefaultWidth(double width);
	
	/********* Polygon filtering ***********/
	
	/**
	 * Adds a filter
	 */
	public abstract void addPolygonFilter(VPolygon polygon);
	
	/**
	 * Disable a filter without removing it
	 * @param polygon
	 */
	public abstract void disablePolygonFilter(VPolygon polygon);
	
	/**
	 * Enable a previously added filter
	 * @param polygon
	 */
	public abstract void enablePolygonFilter(VPolygon polygon);
	
	/**
	 * Returns if a filter is enabled
	 * @param polygon
	 * @return
	 */
	public abstract boolean isPolygonFilterActive(VPolygon polygon);
	
	/**
	 * Returns the number of trajectories contained by a filter
	 * @param polygon
	 * @return
	 */
	public abstract int getNumberTrajectories(VPolygon polygon);
	
	/**
	 * True if the layer allows polygon filtering
	 * @return
	 */
	public abstract boolean isPolygonFilterable();
	
	/**
	 * Returns all filters
	 * @return
	 */
	public abstract Collection<VPolygon> getPolygonFilters();
	
	/**
	 * Disables and removes a filter
	 * @param polygon
	 */
	public abstract void removePolygonFilter(VPolygon polygon);
	
	/****************************************/
	
	public static int string2type(String type){
		if("Départ".equals(type)){
			return FIELD_ADEP;
		} else if("Arrivée".equals(type)){
			return FIELD_ADEST;
		} else if("IAF".equals(type)){
			return FIELD_IAF;
		} else if("Indicatif".equals(type)){
			return FIELD_INDICATIF;
		} else if("Type avion".equals(type)){
			return FIELD_TYPE_AVION;
		}
		return 0;
	}
	
	public static String type2string(int type){
		if(type == FIELD_ADEP){
			return "Départ";
		} else if (type == FIELD_ADEST){
			return "Arrivée";
		} else if (type == FIELD_IAF){
			return "IAF";
		} else if (type == FIELD_INDICATIF){
			return "Indicatif";
		} else if (type == FIELD_TYPE_AVION){
			return "Type avion";
		}
		return null;
	}
}
