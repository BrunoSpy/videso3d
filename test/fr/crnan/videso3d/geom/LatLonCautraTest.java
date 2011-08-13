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

package fr.crnan.videso3d.geom;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 * @author Bruno Spyckerelle
 *
 */
public class LatLonCautraTest {

	private final static double THRESHOLD = 1e-3;
	
	@Test
	public void testTrivialProjection(){
		LatLonCautra center = LatLonCautra.fromDegrees(47, 0);
		assertEquals("X Cautra Center", 0, center.getCautra()[0], THRESHOLD);
		assertEquals("X Cautra Center", 0, center.getCautra()[1], THRESHOLD);
	}
	
	
	@Test
	public void testTrivialRetroProjection(){
		LatLonCautra center = LatLonCautra.fromCautra(0, 0);
		assertEquals("Latitude center", 47, center.latitude.degrees, THRESHOLD);
		assertEquals("Longitude center", 0, center.longitude.degrees, THRESHOLD);
	}
}
