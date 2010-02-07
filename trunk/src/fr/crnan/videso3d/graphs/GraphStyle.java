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
package fr.crnan.videso3d.graphs;

/**
 * Style de cellules
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public final class GraphStyle {

	/**
	 * Rayon des balises
	 */
	public static int baliseSize = 35;
	
	/**
	 * Balise simple
	 */
	public static String baliseStyle = "defaultVertex;shape=ellipse;fontSize=8;fillColor=white;opacity=40";
	
	/**
	 * Groupe ouvert
	 */
	public static String groupStyle = "defaultVertex;shape=swimlane;fontSize=12;fontStyle=1;startSize=23;horizontal=false";

	/**
	 * Groupe fermé
	 */
	public static String groupStyleFolded = "defaultVertex;shape=swimlane;fontSize=12;fontStyle=1;startSize=23;horizontal=true;align=left;spacingLeft=14";

	/**
	 * Connecteurs
	 */
	public static String edgeStyle = "defaultEdge;rounded=true";
	
	/**
	 * Connecteur pour les trajets
	 */
	public static String edgeTrajet = "defaultEdge;rounded=true;dashed=true;";
}
