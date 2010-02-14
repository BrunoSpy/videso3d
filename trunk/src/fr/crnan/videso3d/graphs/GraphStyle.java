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
	public static String baliseStyle = "defaultVertex;shape=ellipse;fontSize=8;fillColor=white;strokeColor=blue;opacity=40";
	/**
	 * Balise en surbrillance
	 */
	public static String baliseHighlight = "defaultVertex;shape=ellipse;fontSize=8;fillColor=yellow;opacity=40";
	/**
	 * Balise travers
	 */
	public static String baliseTravers = "defaultVertex;shape=ellipse;fontSize=8;fillColor=white;opacity=30;dashed=true";
	/**
	 * Balise travers en surbrillance
	 */
	public static String baliseTraversHighlight = "defaultVertex;shape=ellipse;fontSize=8;fillColor=yellow;opacity=30;dashed=true";
	/**
	 * Balise trajet
	 */
	public static String baliseTrajet = "defaultVertex;shape=ellipse;fontSize=8;fillColor=white;opacity=40;dashed=true";
	/**
	 * Balise trajet en surbrillance
	 */
	public static String baliseTrajetHighlight = "defaultVertex;shape=ellipse;fontSize=8;fillColor=yellow;opacity=40;dashed=true";
	/**
	 * Balise ouvrante ou fermante
	 */
	public static String baliseDefault = "defaultVertex;";
	
	/**
	 * Groupe ouvert
	 */
	public static String groupStyle = "defaultVertex;shape=swimlane;fontSize=12;fontStyle=1;startSize=23;horizontal=false";
	/**
	 * Groupe ouvert horizontal
	 */
	public static String groupStyleHorizontal = "defaultVertex;shape=swimlane;fontSize=12;fontStyle=1;startSize=23;horizontal=true";
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
	/**
	 * Connecteur route en double sens (=)
	 */
	public static String edgeRoute = "defaultEdge;rounded=true;startArrow=classic;endArrow=classic";
	/**
	 * Connecteur route en sens unique (>)
	 */
	public static String edgeRouteSensUnique = "defaultEdge;rounded=true;startArrow=none;endArrow=classic";
	/**
	 * Connecteur route en sens unique inverse (<)
	 */
	public static String edgeRouteSensUniqueInverse = "defaultEdge;rounded=true;startArrow=classic;endArrow=none";
	/**
	 * Connecteur route en sens interdit (+)
	 */
	public static String edgeRouteSensInterdit = "defaultEdge;rounded=true;startArrow=none;endArrow=none;dashed=true";


}
