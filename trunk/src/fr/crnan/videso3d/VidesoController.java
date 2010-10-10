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

import gov.nasa.worldwind.layers.Layer;

/**
 * Controleur d'éléments 3D
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public interface VidesoController {

	/**
	 * Mets en valeur l'élément <code>name</code>
	 * @param name Nom de l'élément à mettre en valeur
	 */
	public void highlight(String name);
	
	/**
	 * Supprime le cas échéant la mise en valeur de l'élément <code>name</code>
	 * @param name Nom de l'élément à remettre dans son état normal
	 */
	public void unHighlight(String name);
	
	/**
	 * Ajoute un calque. Le calque est affiché en même temps.
	 * @param name Nom du calque
	 * @param layer Calque à ajouter
	 */
	public void addLayer(String name, Layer layer);
	
	/**
	 * Supprime un calque
	 * @param name Nom du calque
	 * @param layer Calque à supprimer
	 */
	public void removeLayer(String name, Layer layer);
	
	/**
	 * Supprime tous les calques
	 */
	public void removeAllLayers();
	
	/**
	 * Affiche ou non un calque
	 * @param layer Calque dont l'état est à changer
	 * @param state Nouvel état du calque
	 */
	public void toggleLayer(Layer layer, Boolean state);
	
	/**
	 * Affiche l'objet
	 * @param type Type de l'objet
	 * @param name Nom de l'objet
	 */
	public void showObject(int type, String name);
	
	/**
	 * Cache l'objet : l'objet n'est pas supprimé de la vue
	 * @param type Type de l'objet
	 * @param name Nom de l'objet
	 */
	public void hideObject(int type, String name);
	
	/**
	 * Transforme un string en int conformément aux types gérés par le controlleur
	 * @param type 
	 * @return
	 */
	public int string2type(String type);
	
	/**
	 * Affiche les objets en 2D
	 * @param flat 2D si vrai, 3D si faux
	 */
	public void set2D(Boolean flat);
	
	/**
	 * Remet à zéro la vue.
	 */
	public void reset();
}
