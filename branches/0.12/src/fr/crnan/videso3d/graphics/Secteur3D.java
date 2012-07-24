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

import java.util.ArrayList;

import fr.crnan.videso3d.databases.DatabaseManager;
import fr.crnan.videso3d.geom.LatLonUtils;
import fr.crnan.videso3d.layers.TextLayer;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.UserFacingText;

/**
 * Représentation 3D d'un secteur de contrôle
 * @author Bruno Spyckerelle
 * @version 0.4.2
 */
public class Secteur3D extends DatabasePolygonAnnotation {
	
	private boolean locationsVisible = false;
	private TextLayer textLayer;
	private ArrayList<UserFacingText> locationTexts = new ArrayList<UserFacingText>();
	
	public Secteur3D(){
		super();
	}
	
	/**
	 * Crée un secteur 3D
	 * @param name Nom du secteur
	 * @param plancher Plancher en niveaux
	 * @param plafond Plafond en niveaux
	 * @param t Type de secteur
	 * @param base de données origine
	 */
	public Secteur3D(String name, Integer plancher, Integer plafond, int t, DatabaseManager.Type base, TextLayer tl){
		super();
		this.setName(name);
		this.setType(t);
		this.setDatabaseType(base);
		this.setNiveaux(plancher, plafond);
		this.textLayer = tl;
		this.setAnnotation("<p><b>Secteur "+name+"</b></p>"
											+"<p>Plafond : FL"+plafond
											+"<br />Plancher : FL"+plancher+"</p>");
	}

	

	public void setNiveaux(Integer plancher, Integer plafond){
		this.setAltitudes(plancher * 30.48,	plafond * 30.48);
	}
	
	@Override
	public void setName(String name) {
		super.setName(name);
		this.setValue("description", "Secteur "+name);
	}

	public boolean areLocationsVisible() {
		return locationsVisible;
	}

	public void setLocationsVisible(boolean visible) {
		if(!visible && locationsVisible){
			locationsVisible = false;
			for(UserFacingText location : locationTexts){
				this.textLayer.removeGeographicText(location);
			}
		}else if(visible && !locationsVisible){
			locationsVisible = true;
			for(LatLon l : this.getLocations()){
				String latLonString = LatLonUtils.toLatLonToString(l);
				UserFacingText location = new UserFacingText(latLonString, new Position(l,0));
				locationTexts.add(location);
				this.textLayer.addGeographicText(location);
			}
		}
	}

}

