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

import fr.crnan.videso3d.geom.LatLonCautra;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.BasicElevationModelFactory;

/**
 * Réécriture de {@link EarthFlat} pour prendre en compte {@link FlatGlobeCautra}
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class EarthFlatCautra extends FlatGlobeCautra{


    public EarthFlatCautra() {
        super(LatLonCautra.WGS84_EQUATORIAL_RADIUS, LatLonCautra.WGS84_POLAR_RADIUS, LatLonCautra.WGS84_ES, makeElevationModel());
    }

    private static ElevationModel makeElevationModel()
    {
        BasicElevationModelFactory emf = new BasicElevationModelFactory();
        return emf.createFromConfigFile("config/Earth/LegacyEarthElevationModel.xml");
    }
	
}
