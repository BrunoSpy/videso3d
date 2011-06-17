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

/**
 * triplet de données
 * @version 1.0
 * @param <T>
 * @param <U>
 * @param <V>
 */
public class Triplet<T,U,V> {

	private T first;
	private U second;
	private V third;
	
	public Triplet(){
		super();
	}
	
	public Triplet(T first, U second, V third){
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public T getFirst() {
		return first;
	}

	public void setFirst(T first) {
		this.first = first;
	}

	public U getSecond() {
		return second;
	}

	public void setSecond(U second) {
		this.second = second;
	}
	
	public V getThird() {
		return third;
	}

	public void setThird(V third) {
		this.third = third;
	}
	
	public String toSring(){
		return "("+this.first.toString()+", "+this.second.toString()+","+this.third.toString()+")";
	}
}
