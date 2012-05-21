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
 * @version 0.1.1
 */
public class DatabaseSimpleStack3D extends SimpleStack3D implements DatabaseVidesoObject {

	private Type base;
	private int type;
	
	public DatabaseSimpleStack3D(){
		super();
	}
	
	public DatabaseSimpleStack3D(String name, LatLon center, double rayonInt,
			double rayonExt, int flInf, int flSup, Type base, int type) {
		super(name, center, rayonInt, rayonExt, flInf, flSup);
		this.setDatabaseType(base);
		this.setType(type);
	}

	
	@Override
	public Type getDatabaseType() {
		return this.base;
	}

	@Override
	public void setDatabaseType(Type type) {
		this.base = type;
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
		return SimpleStack3D.class.getName();
	}
	
}
