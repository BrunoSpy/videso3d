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
package fr.crnan.videso3d.globes;

import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.BasicElevationModelFactory;

/**
 * Réécriture de {@link EarthFlat} pour prendre en compte {@link FlatGlobeCautra}
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class EarthFlatCautra extends FlatGlobeCautra{

	public static final double WGS84_EQUATORIAL_RADIUS = 6378137.0; // ellipsoid equatorial getRadius, in meters
    public static final double WGS84_POLAR_RADIUS = 6356752.3; // ellipsoid polar getRadius, in meters
    public static final double WGS84_ES = 0.00669437999013; // eccentricity squared, semi-major axis

    public EarthFlatCautra() {
        super(WGS84_EQUATORIAL_RADIUS, WGS84_POLAR_RADIUS, WGS84_ES, makeElevationModel());
    }

    private static ElevationModel makeElevationModel()
    {
        BasicElevationModelFactory emf = new BasicElevationModelFactory();
        return emf.createFromConfigFile("config/Earth/LegacyEarthElevationModel.xml");
    }
	
}
