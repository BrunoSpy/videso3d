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
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class DatabaseRoute3D extends Route3D implements DatabaseVidesoObject{

	private Type base;
	private int type;

	
	public DatabaseRoute3D(){
		super();
	}
	
	public DatabaseRoute3D(String name, Space s, DatabaseManager.Type base, int type){
		super(name, s);
		this.setDatabaseType(base);
		this.setType(type);
	}

	public DatabaseRoute3D(DatabaseManager.Type base, int type) {
		super();
		this.setDatabaseType(base);
		this.setType(type);
	}
	
	@Override
	public DatabaseManager.Type getDatabaseType() {
		return this.base;
	}

	@Override
	public void setDatabaseType(DatabaseManager.Type type) {
		this.base = type;
	}

	@Override
	public String getRestorableClassName() {
		return Route3D.class.getName();
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
