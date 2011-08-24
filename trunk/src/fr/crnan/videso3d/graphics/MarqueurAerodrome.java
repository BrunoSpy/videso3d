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
/**
 * Représentation d'un aérodrome dont on ne connaît pas les pistes.
 * @author Adrien Vidal
 * @version 0.1.2
 */
public class MarqueurAerodrome extends DatabaseBalise2D implements Aerodrome {

	private String nomPiste = "";
	
	public MarqueurAerodrome(){
		super();
	}
	
	public MarqueurAerodrome(int type, String name, Position position,String nomPiste, Type base) {
		super(name.split("--")[0].trim(), position, "<b>"+name+"</b><br/>Piste "+ nomPiste, base, type);
		this.nomPiste = nomPiste;
		this.setDatabaseType(base);
		this.setType(type);
	}

	@Override
	public Position getRefPosition() {
		return this.getPosition();
	}

	@Override
	public String getAnnotationText() {
		return this.getAnnotation(null).getText();
	}
	
	@Override
	public String getNomPiste(){
		return nomPiste;
	}

	@Override
	public void setVisible(boolean visible) {
		this.getUserFacingText().setVisible(visible);
	}

	@Override
	public boolean isVisible() {
		return this.getUserFacingText().isVisible();
	}

	
	


	
	

}
