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

import fr.crnan.videso3d.DatabaseManager.Type;
import gov.nasa.worldwind.geom.LatLon;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class DatabaseCylinder extends Cylinder implements DatabaseVidesoObject{

	public DatabaseCylinder(String name, Type database, int type,
			LatLon center, int flinf, int flsup, double rayon) {
		super(name, center, flinf, flsup, rayon);
		this.setDatabaseType(database);
		this.setType(type);
	}

	private Type database;
	private int type;

	@Override
	public Type getDatabaseType() {
		return this.database;
	}

	@Override
	public void setDatabaseType(Type type) {
		this.database = type;
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
		return Cylinder.class.getName();
	}
	
}
