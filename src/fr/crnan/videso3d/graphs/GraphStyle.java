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
 * @version 0.2.1
 */
public final class GraphStyle {

	/**
	 * Rayon des balises
	 */
	public static int baliseSize = 35;
	/**
	 * Balise ouvrante ou fermante
	 */
	public static String baliseDefault = "defaultVertex;fontColor=black;verticalAlign=middle;";
	/**
	 * Balise simple
	 */
	public static String baliseStyle = baliseDefault+"shape=ellipse;fillColor=white;strokeColor=blue;opacity=40;fontSize=8;";
	/**
	 * Balise en surbrillance
	 */
	public static String baliseHighlight = baliseDefault+"shape=ellipse;fillColor=yellow;opacity=40;fontSize=8;";
	/**
	 * Balise travers
	 */
	public static String baliseTravers = baliseDefault+"shape=cloud;fillColor=white;opacity=30;fontSize=8;";
	/**
	 * Balise travers en surbrillance
	 */
	public static String baliseTraversHighlight = baliseDefault+"shape=cloud;fillColor=yellow;opacity=30;fontSize=8;";
	/**
	 * Balise trajet
	 */
	public static String baliseTrajet = baliseDefault+"shape=ellipse;fillColor=white;opacity=40;dashed=true;fontSize=8;";
	/**
	 * Balise trajet en surbrillance
	 */
	public static String baliseTrajetHighlight = baliseDefault+"shape=ellipse;fillColor=yellow;opacity=40;dashed=true;fontSize=8;";
	
	/**
	 * Balise travers dans un trajet (vue iti)
	 */
	public static String baliseTrajetTravers = baliseDefault+"shape=cloud;fillColor=white;opacity=40;dashed=true;fontSize=8;";
	/**
	 * Balise travers en surbrillance dans un trajet (vue iti)
	 */
	public static String baliseTrajetHighlightTravers = baliseDefault+"shape=cloud;fillColor=yellow;opacity=40;dashed=true;fontSize=8;";
	
	
	/**
	 * Groupe ouvert
	 */
	public static String groupStyle = "defaultVertex;shape=swimlane;fontSize=12;fontStyle=1;startSize=23;horizontal=false;";
	/**
	 * Groupe ouvert horizontal
	 */
	public static String groupStyleHorizontal = groupStyle+"horizontal=true";
	/**
	 * Groupe fermé
	 */
	public static String groupStyleFolded = groupStyle+"horizontal=true;align=left;spacingLeft=14";

	/**
	 * Connecteurs
	 */
	public static String edgeStyle = "defaultEdge;rounded=true;";
	
	/**
	 * Connecteur pour les trajets
	 */
	public static String edgeTrajet = edgeStyle+"dashed=true;";
	/**
	 * Connecteur route en double sens (=)
	 */
	public static String edgeRoute = edgeStyle+"startArrow=classic;endArrow=classic";
	/**
	 * Connecteur route en sens unique (>)
	 */
	public static String edgeRouteSensUnique = edgeStyle+"startArrow=none;endArrow=classic";
	/**
	 * Connecteur route en sens unique inverse (<)
	 */
	public static String edgeRouteSensUniqueInverse = edgeStyle+"startArrow=classic;endArrow=none";
	/**
	 * Connecteur route en sens interdit (+)
	 */
	public static String edgeRouteSensInterdit = edgeStyle+"startArrow=none;endArrow=none;dashed=true";


}
