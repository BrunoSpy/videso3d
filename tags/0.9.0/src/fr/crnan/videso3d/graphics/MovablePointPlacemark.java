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

package fr.crnan.videso3d.graphics;

import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.util.Logging;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class MovablePointPlacemark extends PointPlacemark implements Movable {

	public MovablePointPlacemark(Position position) {
		super(position);
	}

	@Override
	public Position getReferencePosition() {
		return super.getPosition();
	}

	@Override
	public void move(Position delta) {
		 if (delta == null)
	        {
	            String msg = Logging.getMessage("nullValue.PositionIsNull");
	            Logging.logger().severe(msg);
	            throw new IllegalArgumentException(msg);
	        }

	        this.moveTo(this.getReferencePosition().add(delta));
	}

	@Override
	public void moveTo(Position position) {
		LatLon oldRef = this.getReferencePosition();
		if (oldRef == null)
            return;
		
		this.setPosition(position);
	}

}
