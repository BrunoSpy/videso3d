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

/**
 * Représentation 3D d'un secteur de contrôle
 * @author Bruno Spyckerelle
 * @version 0.3
 */
public class Secteur3D extends PolygonAnnotation{

	/*
	 * Nom du secteur
	 */
	private String name;
	
	/**
	 * Crée un secteur 3D
	 * @param name Nom du secteur
	 * @param plancher Plancher en niveaux
	 * @param plafond Plafond en niveaux
	 */
	public Secteur3D(String name, Integer plancher, Integer plafond){
		this.setName(name);
		this.setNiveaux(plancher, plafond);
		
		this.setAnnotation("<p><b>Secteur "+name+"</b></p>"
											+"<p>Plafond : FL"+plafond
											+"<br />Plancher : FL"+plancher+"</p>");
	}

	public void setNiveaux(Integer plancher, Integer plafond){
		this.setAltitudes(plancher * 30.48,	plafond * 30.48);
	}
	
	private void setName(String name) {
		this.name = name;
		this.setValue("description", "Secteur "+name);
	}
	
	public String getName(){
		return this.name;
	}
	 
}

