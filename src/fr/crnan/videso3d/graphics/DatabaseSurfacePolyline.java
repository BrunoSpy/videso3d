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

import java.util.LinkedList;

import fr.crnan.videso3d.DatabaseManager.Type;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.BasicShapeAttributes;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class DatabaseSurfacePolyline extends VSurfacePolyline implements DatabaseVidesoObject{

	public DatabaseSurfacePolyline(LinkedList<LatLon> line) {
		super(line);
	}

	public DatabaseSurfacePolyline(BasicShapeAttributes basicShapeAttributes) {
		super(basicShapeAttributes);
	}

	private Type dataBaseType;
	private int type;

	@Override
	public Type getDatabaseType() {
		return this.dataBaseType;
	}

	@Override
	public void setDatabaseType(Type type) {
		this.dataBaseType = type;
	}

	@Override
	public void setType(int type) {
		this.type = type;	
	}

	@Override
	public int getType() {
		return this.type;
	}

	@Override
	public String getRestorableClassName() {
		return VSurfacePolyline.class.getName();
	}
	
}
