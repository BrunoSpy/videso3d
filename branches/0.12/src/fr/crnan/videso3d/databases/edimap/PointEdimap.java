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
package fr.crnan.videso3d.databases.edimap;

import fr.crnan.videso3d.geom.LatLonCautra;

/**
 * Point Edimap : coordonnées dans le repère CAUTRA
 * Abscisse en 64e de NM
 * Ordonnée en 64e de NM
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class PointEdimap extends LatLonCautra {


	public PointEdimap(double latitude, double longitude) {
		super(latitude, longitude);
	}

	/**
	 * Conversion d'un point au format :
	 * (ref_point
     *    (name "C10A3")
     *    (comment "")
     *    (value
     *       (nautical_mile 64 5038 64 1397)
     *    )
     * )
     * ou simplement :
     *    (nautical_mile 64 5038 64 1397)
	 * @param point
	 * @return {@link LatLonCautra}
	 */
	public static LatLonCautra fromEntity(Entity point){
		String[] xY;
		if(point.getEntity("value") != null) {
			String nauticalMile = point.getEntity("value").getValue("nautical_mile");
			xY = nauticalMile.split("\\s+");
		} else {
			xY = ((String)point.getValue()).split("\\s+");
		}
		double[] latlon = toStereo(new Double(xY[1])/64.0*NM, new Double(xY[3])/64.0*NM);
		
		return LatLonCautra.fromRadians(latlon[0], latlon[1]);

	}

	
}
