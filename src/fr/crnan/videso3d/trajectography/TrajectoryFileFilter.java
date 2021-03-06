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
package fr.crnan.videso3d.trajectography;

import fr.crnan.videso3d.graphics.PolygonAnnotation;

/**
 * Filter for a trajectory file
 * @author Bruno Spyckerelle
 * @version 0.2.0
 */
public class TrajectoryFileFilter {

	private int field;
	private String regexp;
	private PolygonAnnotation polygon;
	
	/**
	 * 
	 * @param field A value between {@link TracksModel#FIELD_ADEP}, {@link TracksModel#FIELD_ADEST}, {@link TracksModel#FIELD_INDICATIF}, ...
	 * @param regexp
	 */
	public TrajectoryFileFilter(int field, String regexp){
		this.field = field;
		this.regexp = regexp;
	}

	/**
	 * 
	 * @param field A value between {@link TracksModel#FIELD_ADEP}, {@link TracksModel#FIELD_ADEST}, {@link TracksModel#FIELD_INDICATIF}, ...
	 * @param regexp
	 */
	public TrajectoryFileFilter(int field, PolygonAnnotation polygon){
		if(field == TracksModel.FIELD_POLYGON){
			this.polygon = polygon;
		}
	}
	
	/**
	 * @return the field
	 */
	public int getField() {
		return field;
	}

	/**
	 * @return the regexp
	 */
	public String getRegexp() {
		return regexp;
	}
	
	public PolygonAnnotation getPolygon(){
		return this.polygon;
	}
	
}
