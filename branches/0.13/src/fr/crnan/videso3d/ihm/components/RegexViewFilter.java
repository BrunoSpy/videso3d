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

import java.util.regex.Pattern;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Affiche les éléments dont le nom correspond à l'expression régulière.<br />
 * Garde affiché les éléments cochés.
 * @author Bruno Spyckerelle
 * @version 0.2.1
 */
public class RegexViewFilter implements ViewFilter {
	
	private Pattern pattern;
	
	public RegexViewFilter(String regex) {
		//si la regex ne contient que des lettres et des chiffres, on l'augmente un peu
		if(regex.split("\\s+").length <= 1 && regex.trim().matches("[a-zA-Z0-9]*")){
			pattern = Pattern.compile(".*"+regex.trim()+".*", Pattern.CASE_INSENSITIVE);
		} else if(regex.split("\\s+").length > 1 && regex.matches("[a-zA-Z0-9\\s+]*")){
			//si la regex contient que des lettres et des chiffres ainsi que des espaces
			String[] splittedRegex = regex.split("\\s+");
			String regex2 = "";
			for(int i=0;i<splittedRegex.length;i++){
				regex2 += ".*"+splittedRegex[i];
			}
			regex2 += ".*";
			pattern = Pattern.compile(regex2, Pattern.CASE_INSENSITIVE);
		} else {
			pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		}
	}
	
	@Override
	public boolean isShown(DefaultMutableTreeNode node) {
		return pattern.matcher(((FilteredTreeTableNode)node.getUserObject()).getName()).matches() /*|| ((Couple<String, Boolean>)node.getUserObject()).getSecond()*/;
	}

}
