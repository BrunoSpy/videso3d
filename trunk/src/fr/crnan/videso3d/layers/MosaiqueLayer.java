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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.Couple;
import fr.crnan.videso3d.Point;
import fr.crnan.videso3d.Point.Type;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.SurfaceShapeLayer;
import gov.nasa.worldwind.render.SurfacePolyline;
/**
 * Affiche une mosaique (STR ou STPV)</br>
 * Permet de colorier certains carrés et sous-carrés
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class MosaiqueLayer extends SurfaceShapeLayer {

	/**
	 * La grille est dessinée de bas en haut
	 */
	public static final int BOTTOM_UP = 1;
	/**
	 * La grille est dessinée de haut en bas
	 */
	public static final int TOP_DOWN = 2;
	/**
	 * La grille est dessinée de droite à gauche
	 */
	public static final int RIGHT_LEFT = 3;
	/**
	 * La grille est dessinée de gauche à droite
	 */
	public static final int LEFT_RIGHT = 4;
	/**
	 * Création d'un mosaïque
	 * @param grille {@link Boolean} Affichage de la grille
	 * @param origine {@link LatLon} Origine de la mosaïque
	 * @param width {@link Integer} Nombre de carrés de côté
	 * @param height {@link Integer} Nombre de carrés de hauteur
	 * @param size {@link Integer} Taille d'un carré en NM
	 * @param hSens {@link Integer} Sens de parcours vertical
	 * @param vSens {@link Integer} Sens de parcours horizontal
	 * @param squares {@link List} Liste de (carré, sous-carré) à colorier. Si sous-carré == 0, on colorie tout le carré. 
	 * @param numbers {@link Boolean} Affichage des numéros des carrés
	 * @param textLayer {@link TextLayer} Layer recevant les numéros des carrés. Ne doit pas être <code>null</code> si numbers est <code>true</code>.
	 */
	public MosaiqueLayer(Boolean grille, 
			LatLon origine, 
			Integer width, 
			Integer height,
			Integer size, 
			int hSens, 
			int vSens, 
			List<Couple<Integer, Integer>> squares,
			Boolean numbers,
			TextLayer textLayer){

		this(grille, new Point(origine.latitude.degrees, origine.longitude.degrees, Point.Type.Stéréographique), width, height, size, hSens, vSens, squares, numbers, textLayer);
	}

	/**
	 * Création d'une mosaïque</br>
	 * @param grille {@link Boolean} Affichage de la grille
	 * @param origine {@link Point} Origine de la mosaïque
	 * @param width {@link Integer} Nombre de carrés de côté
	 * @param height {@link Integer} Nombre de carrés de hauteur
	 * @param size {@link Integer} Taille d'un carré en NM
	 * @param hSens {@link Integer} Sens de parcours vertical
	 * @param vSens {@link Integer} Sens de parcours horizontal
	 * @param squares {@link List} Liste de (carré, sous-carré) à colorier. Si sous-carré == 0, on colorie tout le carré. 
	 * @param numbers {@link Boolean} Affichage des numéros des carrés
	 * @param textLayer {@link TextLayer} Layer recevant les numéros des carrés. Ne doit pas être <code>null</code> si numbers est <code>true</code>.
	 */
	public MosaiqueLayer(Boolean grille, 
			Point origine, 
			Integer width, 
			Integer height,
			Integer size, 
			int hSens, 
			int vSens, 
			List<Couple<Integer, Integer>> squares,
			Boolean numbers,
			TextLayer textLayer){

		int hsens = hSens == RIGHT_LEFT ? -1 : 1;
		int vsens = vSens == TOP_DOWN ? -1 : 1;

		if(grille){
			//affichage des lignes
			for(int i=0; i<= height; i++){
				Point start = new Point(origine.coordonneesCautra().getFirst(),
						origine.coordonneesCautra().getSecond() + i * size * vsens,
						Type.Cautra );
				Point stop = new Point(origine.coordonneesCautra().getFirst()+width*size*hsens, 
						origine.coordonneesCautra().getSecond() + i * size* vsens, 
						Type.Cautra);
				LinkedList<LatLon> line = new LinkedList<LatLon>();
				line.add(LatLon.fromDegrees(start.coordonneesStereo().getFirst(), start.coordonneesStereo().getSecond()));
				line.add(LatLon.fromDegrees(stop.coordonneesStereo().getFirst(), stop.coordonneesStereo().getSecond()));
				SurfacePolyline ligne = new SurfacePolyline(line);
				ligne.setClosed(false);
				this.addRenderable(ligne);
			}
			//affichage des colonnes
			for(int i=0; i<= width; i++){
				Point start = new Point(origine.coordonneesCautra().getFirst() + i * size * hsens,
						origine.coordonneesCautra().getSecond(),
						Type.Cautra );
				Point stop = new Point(origine.coordonneesCautra().getFirst()+ i * size * hsens, 
						origine.coordonneesCautra().getSecond() + height * size * vsens, 
						Type.Cautra);
				LinkedList<LatLon> line = new LinkedList<LatLon>();
				line.add(LatLon.fromDegrees(start.coordonneesStereo().getFirst(), start.coordonneesStereo().getSecond()));
				line.add(LatLon.fromDegrees(stop.coordonneesStereo().getFirst(), stop.coordonneesStereo().getSecond()));
				SurfacePolyline col = new SurfacePolyline(line);
				col.setClosed(false);
				this.addRenderable(col);
			}
		}

		//affichage des numéros des carrés
		if(numbers){
			
		}
		
		//coloriage des carrés
		if(squares != null){
			Iterator<Couple<Integer, Integer>> iterator = squares.iterator();
			while(iterator.hasNext()){
				Couple<Integer, Integer> square = iterator.next();
				if(square.getSecond() == 0){
					//on colorie tout le carré
				} else {
					//on ne colorie que le sous-carré
				}
			}
		}
	}

}
