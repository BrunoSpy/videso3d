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
import fr.crnan.videso3d.formats.lpln.LPLNTrackPoint;

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
	
	/**
	 * Indique que la route affichée n'est pas forcément celle déposée dans le plan de  vol entre la balise passée en paramètre et 
	 * la balise suivante
	 * @param balise
	 */
	public void setNextSegmentIncertain(String balise){
		int baliseIndex=-1;
		for(int i = 0; i < super.trackPoints.size(); i++){
			if(trackPoints.get(i).getName().equals(balise))
				baliseIndex = i;
		}
		if(baliseIndex<super.trackPoints.size()-1 && baliseIndex>=0){
			LPLNTrackPoint nextBalise = super.trackPoints.get(baliseIndex+1);
			if(nextBalise!=null)
				segmentsIncertains.add(nextBalise.getName());
		}
	}
	
	public LinkedList<String> getSegmentsIncertains(){
		return segmentsIncertains;
	}

	
}
