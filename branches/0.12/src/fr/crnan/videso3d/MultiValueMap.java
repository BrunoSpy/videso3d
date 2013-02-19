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
package fr.crnan.videso3d;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Une structure permettant de stocker plusieurs valeurs associées à une clef, basée sur une HashMap et stockant les valeurs dans une ArrayList.
 * @author Adrien Vidal
 *
 */
public class MultiValueMap<T, U> {

	private HashMap<T, List<U>> map = new HashMap<T, List<U>>();
	
	public void put(T key, Collection<U> values){
		List<U> liste = map.get(key);
		if(liste == null){
			liste = new LinkedList<U>();
			map.put(key, liste);
		}
		liste.addAll(values);
	}


	public void put(T key, U value){
		List<U> liste = map.get(key);
		if(liste == null){
			liste = new LinkedList<U>();
			map.put(key, liste);
		}
		liste.add(value);
	}
	
	public List<U> get(T key){
		return map.get(key);
	}
	
	public boolean containsKey(T key){
		return map.containsKey(key);
	}
	
	public List<U> values(){
		LinkedList<U> values = new LinkedList<U>();
		for(T key : map.keySet()){
			values.addAll(map.get(key));
		}
		return values;
	}
	
	public void remove(T key){
		map.remove(key);
	}
}
