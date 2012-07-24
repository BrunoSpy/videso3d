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

import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.databases.DatabaseManager.Type;
import fr.crnan.videso3d.layers.TextLayer;
import gov.nasa.worldwind.geom.Position;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class DatabaseBalise2D extends Balise2D implements DatabaseVidesoObject {
	
	private Type dataBaseType;
	private int type;

	public DatabaseBalise2D(){
	}
	
	public DatabaseBalise2D(CharSequence name, Position position, String annotation, DatabaseManager.Type base, int type, TextLayer tl){
		super(name, position, annotation, tl);
		
		this.setDatabaseType(base);
		this.setType(type);
	}

	public DatabaseBalise2D(CharSequence name, Position position, DatabaseManager.Type base, int type, TextLayer tl){
		super(name, position, tl);
		this.setDatabaseType(base);
		this.setType(type);
	}
	
	@Override
	public String getRestorableClassName() {
		return Balise2D.class.getName();
	}


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
}
