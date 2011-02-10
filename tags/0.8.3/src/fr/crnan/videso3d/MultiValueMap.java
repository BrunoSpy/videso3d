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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import fr.crnan.videso3d.Couple;

/**
 * Une structure permettant de stocker plusieurs valeurs associées à une clef, basée sur une HashMap et stockant les valeurs dans une ArrayList.
 * @author Adrien Vidal
 *
 */
public class MultiValueMap<T, U> {

	private HashMap<T, Couple<Integer, Integer>> map = new HashMap<T, Couple<Integer,Integer>>();
	private ArrayList<U> valuesArray = new ArrayList<U>();
	
	public void put(T key, Collection<U> values){
		int debut = valuesArray.size();
		map.put(key, new Couple<Integer, Integer>(debut, debut+values.size()));
		valuesArray.addAll(values);
	}
	
	//TODO à vérifier
	public void put(T key, U value){
		Couple<Integer, Integer> debutFin = map.get(key);
		if(debutFin == null){
			int debut = valuesArray.size();
			map.put(key, new Couple<Integer, Integer>(debut, debut+1));
			valuesArray.add(value);
		}else{
			map.put(key, new Couple<Integer, Integer>(debutFin.getFirst(), debutFin.getSecond()+1));
			valuesArray.add(debutFin.getSecond(), value);
		}
	}
	
	public List<U> get(T key){
		Couple<Integer, Integer> debutFin = map.get(key);
		return valuesArray.subList(debutFin.getFirst(), debutFin.getSecond());
	}
	
	public boolean containsKey(T key){
		return map.containsKey(key);
	}
	
	
}
