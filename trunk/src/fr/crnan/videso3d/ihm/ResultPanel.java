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

package fr.crnan.videso3d.ihm;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;

import javax.swing.JPanel;

/**
 * Panel de résultats.
 * Doit permettre la liaison avec le panel contextuel
 * @author Bruno Spyckerelle
 * @version 0.2.3
 */
public abstract class ResultPanel extends JPanel {
	/**
	 * Property fired when the number of results is known
	 */
	public static String PROPERTY_RESULT = "number_results_changed";
	
	/**
	 * Property fired when the tile of the tab is changed
	 */
	public static String TITLE_TAB_NAME = "title_tab_name";
	
	public abstract void setContext(ContextPanel context);
		
	public void fireNumberResults(int count){
		firePropertyChange(PROPERTY_RESULT, -1, count);
	}

	protected String forgeSql(String balise){
		int length = balise.length();
		char end =  balise.charAt(length-1);
		if(end == '*') {
			return "LIKE '"+balise.substring(0, length-1)+"%'";
		}else if(end == '/'){
				return "= '"+balise.substring(0, length-1)+"' AND id = '0'";
		}else if(end == '\\'){
			return "= '"+balise.substring(0, length-1)+"'";
		}else{
			return "= '"+balise+"'";
		}
	}
	
	protected String forgeSqlTravers(String balise){
		int length = balise.length();
		if(balise.charAt(length-1) == '*') {
			return "LIKE '"+balise.substring(0, length-1)+"%'";
		} else if (balise.charAt(length-1)=='/'){
			return "= '"+balise.substring(0, length-1)+"' AND appartient = '0'";
		}else if (balise.charAt(length-1)=='\\'){
			return "= '"+balise.substring(0, length-1)+"' AND appartient = '1'";
		}else{
			return "= '"+balise+"'";
		}
	}
		
	protected boolean nameMatch(String balise, String name){
		int length = balise.length();
		if(!balise.isEmpty()){
			char end = balise.charAt(length-1);
			if(end == '*'){
				return name.startsWith(balise.substring(0, length-1));
			}else if(end == '/' || end == '\\'){
				return name.equals(balise.substring(0, length-1));
			}else{
				return name.equals(balise);
			}
		}
		return false;

	}

	public abstract String getTitleTab();
	
}
