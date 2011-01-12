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
package fr.crnan.videso3d.formats.fpl;

import java.util.LinkedList;

import fr.crnan.videso3d.formats.lpln.LPLNTrack;

/**
 * 
 * @author Adrien Vidal
 *
 */
public class FPLTrack extends LPLNTrack {
	
	private LinkedList<String> segmentsIncertains = new LinkedList<String>();

	public FPLTrack(String name) {
		super(name);
	}
	
	/**
	 * Indique que la route affichée n'est pas forcément celle déposée dans le plan de vol entre la balise passée en paramètre et
	 * la balise précédente.
	 * @param balise
	 */
	public void setSegmentIncertain(String balise){
		segmentsIncertains.add(balise);
	}
	
	public LinkedList<String> getSegmentsIncertains(){
		return segmentsIncertains;
	}

	
}
