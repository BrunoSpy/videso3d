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

package fr.crnan.videso3d.layers;

import java.util.List;

import fr.crnan.videso3d.graphics.Balise;
import gov.nasa.worldwind.layers.Layer;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.2.3
 */
public interface BaliseLayer extends Layer {

	public void addBalise(Balise balise);
	
	/**
	 * Ajoute plusieurs balises.<br />
	 * Utiliser <code>showAll</code> ou <b>showBalise</b> pour rendre visible la balise.
	 * @param balise Balise à ajouter
	 */
	public void addBalises(Iterable<? extends Balise> balises);
	
	/**
	 * Affiche toutes les balises
	 */
	public void showAll();
	
	/**
	 * Affiche une balise.<br />
	 * Cette balise doit d'abord être ajoutée grâce à <code>addBalise(Balise2D balise)</code>
	 * @param name
	 * @param type
	 */
	public void showBalise(String name, int type);
	
	/**
	 * Affiche une balise.<br />
	 * Cett balise doit d'abord être ajoutée grâce à <code>addBalise(Balise2D balise)</code>
	 * @param b Balise à afficher
	 */
	public void showBalise(Balise b);
	
	/**
	 * Affiche une liste de balises
	 * @param balises
	 */
	public void showBalises(List<String> balises, int type);
	
	/**
	 * Enlève une liste de balises de la vue
	 * @param balises
	 */
	public void hideBalises(List<String> balises, int type);
	
	/**
	 * Enlève une balise de la vue.<br />
	 * Cette balise est toujours accessible pour être à nouveau affichée plus tard.
	 * @param name
	 * @param type
	 */
	public void hideBalise(String name, int type);
	
	/**
	 * Enlève une balise de la vue.<br />
	 * Cette balise est toujours accessible pour être à nouveau affichée plus tard.
	 * @param name
	 */
	public void hideBalise(Balise b);

	/**
	 * Cache toutes les balises, ne les supprime pas du calque.
	 */
	public void removeAllBalises();
	
	/**
	 * Supprime toutes les balises
	 */
	public void eraseAllBalises();

	/**
	 * 
	 * @return Names of visible balises
	 */
	public List<String> getVisibleBalisesNames();
	
	/**
	 * 
	 * @return Visible balises
	 */
	public List<? extends Balise> getVisibleBalises();
	/**
	 * Supprime une balise du calque
	 * Si affichée, enlève la balise de la vue
	 * @param balise
	 */
	public void removeBalise(Balise balise);
	
}
