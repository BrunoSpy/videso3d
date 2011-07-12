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
import fr.crnan.videso3d.geom.LatLonCautra;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.airspaces.CappedCylinder;
/**
 * Cylindre 3D
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class Cylinder extends CappedCylinder implements VidesoObject {

	private String name;
	
	private VidesoAnnotation annotation;
	
	private Type database;
	
	private int type;
	
	/**
	 * 
	 * @param name Nom
	 * @param database Base de données auquel l'objet est rattaché
	 * @param type Type de l'objet
	 * @param center Centre du cylindre
	 * @param flinf Niveau infèrieur
	 * @param flsup Niveau supèrieur
	 * @param rayon Rayon du cylindre en NM
	 */
	public Cylinder(String name, Type database, int type, LatLon center, int flinf, int flsup, double rayon){
		super(center, rayon*LatLonCautra.NM);
		this.setName(name);
		this.setDatabaseType(database);
		this.setType(type);
		this.setAltitudes(flinf*30.48, flsup*30.48);
	}
	
	@Override
	public void setAnnotation(String text){
		if(annotation == null) {
			annotation = new VidesoAnnotation(text);
		} else {
			annotation.setText(text);
		}
	}
	
	@Override
	public VidesoAnnotation getAnnotation(Position pos){
		annotation.setPosition(pos);
		return annotation;
	}

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
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

}
