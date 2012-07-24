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

import java.util.List;

import fr.crnan.videso3d.databases.DatabaseManager.Type;
import fr.crnan.videso3d.geom.LatLonCautra;
/**
 * Videso Polygon with annotation and link with a database
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class DatabasePolygonAnnotation extends PolygonAnnotation implements DatabaseVidesoObject {

	private Type base;
	private int type;

	public DatabasePolygonAnnotation(){
		super();
	}
	
	public DatabasePolygonAnnotation(List<LatLonCautra> locations) {
		super(locations);
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
		return PolygonAnnotation.class.getName();
	}
	
}
