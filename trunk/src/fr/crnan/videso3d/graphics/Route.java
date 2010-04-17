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

import java.util.LinkedList;
import java.util.List;

/**
 * Représentation graphique d'une route
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public interface Route {

	/**
	 * Type de la route
	 */
	public static enum Type {FIR, UIR};
	
	public void setType(Type type);
	
	public Type getType();
	
	public String getName();
	
	public void setBalises(List<String> balises);
	
	public void addBalise(String balise);
	
	public List<String> getBalises();
	
	public void highlight(boolean highlight);

}
