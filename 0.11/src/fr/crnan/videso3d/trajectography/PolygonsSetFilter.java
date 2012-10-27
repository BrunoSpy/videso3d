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
package fr.crnan.videso3d.trajectography;

import java.util.Collection;

import fr.crnan.videso3d.graphics.VPolygon;

/***
 * Ensemble de polygones formant un unique filtre
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class PolygonsSetFilter {

	private String name;
	
	private Collection<VPolygon> polygons;
	
	private int containedTrajectories;
	
	private boolean active;
	
	public PolygonsSetFilter(String name, Collection<VPolygon> polygons){
		this.setName(name);
		this.polygons = polygons;
		this.setActive(true);
	}
	
	public Collection<VPolygon> getPolygons(){
		return polygons;
	}

	private void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setContainedTrajectories(int containedTrajectories) {
		this.containedTrajectories = containedTrajectories;
	}

	public int getContainedTrajectories() {
		return containedTrajectories;
	}

	public void setActive(boolean active) {
		this.active = active;
		this.setContainedTrajectories(0);
	}

	public boolean isActive() {
		return active;
	}
	
}
