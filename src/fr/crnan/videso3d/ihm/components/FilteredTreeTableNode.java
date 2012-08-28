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
package fr.crnan.videso3d.ihm.components;
/**
 * 
 * @author Bruno Spyckerelle
 *
 */
public class FilteredTreeTableNode {

	private String name;
	private boolean visible;
	
	public FilteredTreeTableNode(String name, boolean visible){
		this.name = name;
		this.visible = visible;
	}

	public String getName() {
		return name;
	}

	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible){
		this.visible = visible;
	}
}
