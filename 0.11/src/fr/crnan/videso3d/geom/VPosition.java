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

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
/**
 * {@link Position} with speed
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class VPosition extends Position {

	
	public static final VPosition ZERO = new VPosition(Angle.ZERO, Angle.ZERO, 0d, 0);
	
	/**
	 * Speed in knots
	 */
	private int speed = 0;
	
	public VPosition(Angle latitude, Angle longitude, double elevation, int speed)
    {
        super(latitude, longitude, elevation);
        this.speed = speed;
    }

	public VPosition(Position position, int speed) {
		super(position, position.elevation);
		this.speed = speed;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
}
