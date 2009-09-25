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
import fr.crnan.videso3d.geom.LatLonCautra;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.SurfaceShapeLayer;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;
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
	 * Mode de parcours utilisé par le STR
	 */
	public static final int VERTICAL_FIRST = 5;
	/**
	 * Mode de parcours utilisé par le STPV
	 */
	public static final int HORIZONTAL_FIRST = 6;
	
	/**
	 * Création d'une mosaïque</br>
	 * @param grille {@link Boolean} Affichage de la grille
	 * @param origine {@link LatLonCautra} Origine de la mosaïque
	 * @param width {@link Integer} Nombre de carrés de côté
	 * @param height {@link Integer} Nombre de carrés de hauteur
	 * @param size {@link Integer} Taille d'un carré en NM
	 * @param hSens {@link Integer} Sens de parcours vertical
	 * @param vSens {@link Integer} Sens de parcours horizontal
	 * @param numSens {@link Integer} Sens de la numérotation (VERTICAL_FIRST : utilisé par le STR, HORIZONTAL_FIRST : utilisé par le STPV
	 * @param squares {@link List} Liste de (carré, sous-carré) à colorier. Si sous-carré == 0, on colorie tout le carré. 
	 * @param numbers {@link Boolean} Affichage des numéros des carrés
	 * @param textLayer {@link TextLayer} Layer recevant les numéros des carrés. Ne doit pas être <code>null</code> si numbers est <code>true</code>.
	 * @param attr {@link ShapeAttributes} Attributs pour le coloriage des carrés.
	 */
	public MosaiqueLayer(Boolean grille, 
			LatLonCautra origine, 
			Integer width, 
			Integer height,
			Integer size, 
			int hSens, 
			int vSens, 
			int numSens,
			List<Couple<Integer, Integer>> squares,
			Boolean numbers,
			TextLayer textLayer,
			ShapeAttributes attr){

		int hsens = hSens == RIGHT_LEFT ? -1 : 1;
		int vsens = vSens == TOP_DOWN ? -1 : 1;

		if(grille){
			//affichage des lignes
			for(int i=0; i<= height; i++){
				LatLonCautra start = LatLonCautra.fromCautra(origine.getCautra()[0], origine.getCautra()[1]+ i * size * vsens );
				
				LatLonCautra stop = LatLonCautra.fromCautra(origine.getCautra()[0]+width*size*hsens, origine.getCautra()[1] + i * size* vsens);
				
				LinkedList<LatLon> line = new LinkedList<LatLon>();
				line.add(start);
				line.add(stop);
				SurfacePolyline ligne = new SurfacePolyline(line);
				ligne.setClosed(false);
				this.addRenderable(ligne);
			}
			//affichage des colonnes
			for(int i=0; i<= width; i++){
				
				LatLonCautra start = LatLonCautra.fromCautra(origine.getCautra()[0] + i * size * hsens, origine.getCautra()[1]);
				
				LatLonCautra stop = LatLonCautra.fromCautra(origine.getCautra()[0]+ i * size * hsens, origine.getCautra()[1] + height * size * vsens);
							
				LinkedList<LatLon> line = new LinkedList<LatLon>();
				line.add(start);
				line.add(stop);
				SurfacePolyline col = new SurfacePolyline(line);
				col.setClosed(false);
				this.addRenderable(col);
			}
		}

		//affichage des numéros des carrés
		if(numbers){
			//TODO afficher les nombres
		}
		
		//coloriage des carrés
		if(squares != null){
			Iterator<Couple<Integer, Integer>> iterator = squares.iterator();
			while(iterator.hasNext()){
				Couple<Integer, Integer> square = iterator.next();
				this.colorieCarre(origine, square.getFirst(), square.getSecond(), width, height, size, hsens, vsens, numSens, attr );
			}
		}
	}
	/**
	 * Colorie un carré de la mosaïque	
	 * @param origine {@link LatLonCautra} Origine de la mosaïque
	 * @param carre Numéro du carré
	 * @param sousCarre Numéro du sous-carré
	 * @param width {@link Integer} Nombre de carrés de côté
	 * @param height {@link Integer} Nombre de carrés de hauteur
	 * @param size {@link Integer} Taille d'un carré en NM
	 * @param hSens {@link Integer} Sens de parcours vertical
	 * @param vSens {@link Integer} Sens de parcours horizontal
	 * @param numSens {@link Integer} Sens de la numérotation (VERTICAL_FIRST : utilisé par le STR, HORIZONTAL_FIRST : utilisé par le STPV
	 * @param color Couleur
	 */
	private void colorieCarre(LatLonCautra origine,
			Integer carre,
			Integer sousCarre, 
			Integer width, 
			Integer height, 
			Integer size, 
			int hsens, 
			int vsens, 
			int numSens, ShapeAttributes attr){
		int colonne, ligne; //colonne et ligne du carré. Débute à 0.
		if(numSens == VERTICAL_FIRST) {
			colonne = carre / height;
			ligne = carre % height - 1;
			if(ligne == -1){
				ligne = height -1 ;
				colonne--;
			}
		} else {
			colonne = carre / width;
			ligne = carre %  width - 1;
			if(colonne == -1){
				colonne = width -1;
				ligne--;
			}
		}

		if(sousCarre == 0){
			//on colorie tout le carré
			
			List<LatLonCautra> locations = new LinkedList<LatLonCautra>();
			locations.add(LatLonCautra.fromCautra(origine.getCautra()[0] + colonne * size * hsens, origine.getCautra()[1] + ligne * size * vsens));
			locations.add(LatLonCautra.fromCautra(origine.getCautra()[0] + (colonne * size + size)  * hsens, origine.getCautra()[1] + ligne * size * vsens));
			locations.add(LatLonCautra.fromCautra(origine.getCautra()[0] + (colonne * size + size) * hsens, origine.getCautra()[1] + (ligne * size + size)*vsens));
			locations.add(LatLonCautra.fromCautra(origine.getCautra()[0] + (colonne * size) * hsens, origine.getCautra()[1] + (ligne * size + size)*vsens));

			
			SurfacePolygon polygon = new SurfacePolygon(locations);
			if(attr != null) polygon.setAttributes(attr);
			this.addRenderable(polygon);
		} else {
			//on ne colorie que le sous-carré
			//un sous-carré a une taille de size/4 car un carré est toujours découpé en 16 sous-carré
			int sousColonne = sousCarre / 4 ;
			int sousLigne = sousCarre % 4 - 1;
			if(sousLigne == -1){
				sousLigne = 3;
				sousColonne--;
			}
			List<LatLonCautra> locations = new LinkedList<LatLonCautra>();
			locations.add(LatLonCautra.fromCautra(origine.getCautra()[0] + (colonne * size + sousColonne * (size/4) )* hsens, origine.getCautra()[1] + (ligne * size + sousLigne *(size/4) )* vsens));
			locations.add(LatLonCautra.fromCautra(origine.getCautra()[0] + (colonne * size + sousColonne * (size/4) + (size/4))  * hsens, origine.getCautra()[1] + (ligne * size  + sousLigne *(size/4))* vsens));
			locations.add(LatLonCautra.fromCautra(origine.getCautra()[0] + (colonne * size + sousColonne * (size/4) + (size/4)) * hsens, origine.getCautra()[1] + (ligne * size  + sousLigne *(size/4) + size/4)*vsens));
			locations.add(LatLonCautra.fromCautra(origine.getCautra()[0] + (colonne * size + sousColonne * (size/4)) * hsens, origine.getCautra()[1] + (ligne * size + sousLigne *(size/4) + size/4)*vsens));

			
			SurfacePolygon polygon = new SurfacePolygon(locations);
			polygon.setAttributes(attr);
			this.addRenderable(polygon);
		}
	}

}
