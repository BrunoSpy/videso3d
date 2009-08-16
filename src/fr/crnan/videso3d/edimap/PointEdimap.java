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
package fr.crnan.videso3d.edimap;

import com.trolltech.qt.core.QPointF;
/**
 * Point Edimap : coordonnées dans le repère CAUTRA
 * Abscisse en 64e de NM
 * Ordonnée en 64e de NM
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class PointEdimap extends QPointF{
	/**
	 * Nom du point
	 */
	private String name;
	/**
	 * Commentaire
	 */
	private String comment;
	
	public PointEdimap(){
		super();
	}
	
	public PointEdimap(Entity point){
		this.name = point.getValue("name");
		this.comment = point.getValue("comment");
		String nauticalMile = point.getEntity("value").getValue("nautical_mile");
		String[] xY = nauticalMile.split("\\s+");
		this.setX(new Integer(xY[1]));
		this.setY(new Integer(xY[3])*-1);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	
}
