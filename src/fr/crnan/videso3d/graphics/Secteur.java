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
/**
 * Représentation graphique d'un secteur
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public interface Secteur extends VidesoObject{

	/**
	 * Type de zone
	 */
//	public static enum Type {Secteur, TSA, SIV, CTR, TMA, R, D, FIR, UIR, LTA, UTA, CTA, CTL, Pje, Aer, Vol, Bal, TrPla};
	
	public String getName();
	
}
