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

import javax.swing.tree.DefaultMutableTreeNode;

import fr.crnan.videso3d.Couple;
/**
 * Affiche les éléments dont le nom correspond à l'expression régulière.<br />
 * Garde affiché les éléments cochés.
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class RegexViewFilter implements ViewFilter {

	private String regex;
	
	public RegexViewFilter(String regex) {
		this.regex = regex;
	}
	
	@Override
	public boolean isShown(DefaultMutableTreeNode node) {
		return ((Couple<String, Boolean>)node.getUserObject()).getFirst().matches(regex) || ((Couple<String, Boolean>)node.getUserObject()).getSecond();
	}

}
