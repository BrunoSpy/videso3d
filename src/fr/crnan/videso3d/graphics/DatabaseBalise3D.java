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

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import gov.nasa.worldwind.geom.Position;

/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class DatabaseBalise3D extends Balise3D implements DatabaseVidesoObject {

	private Type base;
	private int type;

	public DatabaseBalise3D(){
		super();
	}
	
	public DatabaseBalise3D(CharSequence name, Position position, String annotation, DatabaseManager.Type base, int type){
		super(name, position, annotation);
		this.setType(type);
		this.setDatabaseType(base);
	}
	
	public DatabaseBalise3D(String balise, Position position, Type base, int type) {
		super(balise,  position, null);
		this.setType(type);
		this.setDatabaseType(base);
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
		return Balise3D.class.getName();
	}

}
