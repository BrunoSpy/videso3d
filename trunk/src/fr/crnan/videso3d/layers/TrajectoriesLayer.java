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

import gov.nasa.worldwind.tracks.Track;
/**
 * Layer contenant des trajectoires et permettant un affichage sélectif.
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public abstract class TrajectoriesLayer extends LayerSet {

	public static final int FIELD_ADEP = 1;
	public static final int FIELD_ADEST = 2;
	public static final int FIELD_IAF = 3;
	public static final int FIELD_INDICATIF = 4;
	public static final int FIELD_TYPE_AVION = 5;
	
	private boolean disjunctive = true; //"or" by default
	
	public TrajectoriesLayer(){
		super();
	}
	/**
	 * Ajoute un Track
	 * @param track {@link Track}
	 */
	public abstract void addTrack(Track track);

	/**
	 * Filter tracks whose field matches regexp
	 * @param field FIELD_ADEP, FIELD_ADEST, FIELD_IAF, FIELD_INDICATIF
	 * @param regexp
	 */
	public abstract void addFilter(int field, String regexp);

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
	 */
	public abstract void highlightTrack(Track track);
	/**
	 * Supprime le highlight des tracks
	 */
	public abstract void removeHighlightedTracks();
	
	/**
	 * Met à jour les trajectoires affichées
	 */
	public abstract void update();
	
	public abstract Collection<? extends Track> getSelectedTracks();
}
