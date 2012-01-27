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
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.SurfacePolygon;
/**
 * {@link PisteAerodrome} linked with a database
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class DatabasePisteAerodrome extends PisteAerodrome implements DatabaseVidesoObject{

	private Type base;
	private int type;
	
	public DatabasePisteAerodrome(int type, String name, String nomPiste,
			double lat1, double lon1, double lat2, double lon2, double largeur,
			Position ref, Type base) {
		super(name, nomPiste, lat1, lon1, lat2, lon2, largeur, ref);
		this.setDatabaseType(base);
		this.setType(type);
		
		((DatabaseVidesoObject) this.getInnerRectangle()).setDatabaseType(base);
		((DatabaseVidesoObject) this.getInnerRectangle()).setType(type);
		
		((DatabaseVidesoObject) this.getOuterRectangle()).setDatabaseType(base);
		((DatabaseVidesoObject) this.getOuterRectangle()).setType(type);
	}

	public DatabasePisteAerodrome() {
		super();
	}
	
	@Override
	public SurfacePolygon getInnerRectangle(){
		if(this.inner == null)
			this.setInnerRectangle(new DatabaseSurfacePolygonAnnotation());
		return inner;
	}
	
	@Override
	public SurfacePolygon getOuterRectangle(){
		if(this.outer == null)
			this.setOuterRectangle(new DatabaseSurfacePolygonAnnotation());
		return outer;
	}
	
	@Override
	public String getRestorableClassName() {
		return PisteAerodrome.class.getName();
	}

	@Override
	public Type getDatabaseType() {
		return this.base;
	}

	/**
	 * Non implémenté
	 */
	@Override
	public void setDatabaseType(Type base) {
		this.base = base;
	}

	/**
	 * Non implémenté
	 */
	@Override
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Non implémenté
	 */
	@Override
	public int getType() {
		return this.type;
	}
}
