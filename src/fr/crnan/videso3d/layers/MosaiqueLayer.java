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
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.geom.LatLonCautra;
import fr.crnan.videso3d.graphics.PolygonAnnotation;
import fr.crnan.videso3d.graphics.SurfacePolygonAnnotation;
import fr.crnan.videso3d.graphics.VSurfacePolyline;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.UserFacingText;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
/**
 * Affiche une mosaique (STR ou STPV) en 2D ou en 3D</br>
 * Permet de colorier certains carrés et sous-carrés
 * @author Bruno Spyckerelle
 * @version 0.4.3
 */
@SuppressWarnings("serial")
public class MosaiqueLayer extends LayerSet {

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
	 * Mosaiques en 2D ou 3D.<br />
	 * 2D par défaut.
	 */
	private Boolean threeD = false;
	/**
	 * Layer pour les mosaiques 3D
	 */
	private FilterableAirspaceLayer airspaceLayer = new FilterableAirspaceLayer();
	/**
	 * Layer pour les mosaiques 2D
	 */
	private RenderableLayer shapeLayer = new RenderableLayer();
	/**
	 * Layer pour les numéros
	 */
	private TextLayer textLayer = new TextLayer("Numéros des mosaïques");
	/**
	 * Layer pour la grille
	 */
	private RenderableLayer grilleLayer = new RenderableLayer();
	
	/**
	 * Création d'une mosaïque</br>
	 * @param annotationTitle {@link String} Titre des annotations des carrés
	 * @param grille {@link Boolean} Affichage de la grille
	 * @param origine {@link LatLonCautra} Origine de la mosaïque
	 * @param width {@link Integer} Nombre de carrés de côté
	 * @param height {@link Integer} Nombre de carrés de hauteur
	 * @param size {@link Integer} Taille d'un carré en NM
	 * @param hSens {@link Integer} Sens de parcours vertical
	 * @param vSens {@link Integer} Sens de parcours horizontal
	 * @param numSens {@link Integer} Sens de la numérotation (VERTICAL_FIRST : utilisé par le STR, HORIZONTAL_FIRST : utilisé par le STPV
	 * @param squares {@link List} Liste de (carré, sous-carré) à colorier. Si sous-carré == 0, on colorie tout le carré.
	 * @param altitudes {@link List} Liste de (plancher, plafond) associée à la liste des carrés précédente. En mètres. 
	 * @param numbers {@link Boolean} Affichage des numéros des carrés
	 * @param attr {@link ShapeAttributes} Attributs pour le coloriage des carrés
	 * @param base Base données d'origine
	 * @param type type de la mosaïque
	 */
	public MosaiqueLayer(String annotationTitle,
			Boolean grille, 
			LatLonCautra origine, 
			Integer width, 
			Integer height,
			Integer size, 
			int hSens, 
			int vSens, 
			int numSens,
			List<Couple<Integer, Integer>> squares,
			List<Couple<Double, Double>> altitudes,
			Boolean numbers,
			ShapeAttributes attr,
			AirspaceAttributes airspaceAttr, 
			DatabaseManager.Type base, 
			int type,
			String name){
		this.add(textLayer);
		this.add(shapeLayer);
		this.add(grilleLayer);
		
		int hsens = hSens == RIGHT_LEFT ? -1 : 1;
		int vsens = vSens == TOP_DOWN ? -1 : 1;
		textLayer.setMaxActiveAltitude(20e5);
		
//		shapeLayer.setPickEnabled(false);
		
		if(grille){
			//affichage des lignes
			for(int i=0; i<= height; i++){
				LatLonCautra start = LatLonCautra.fromCautra(origine.getCautra()[0], origine.getCautra()[1]+ i * size * vsens );
				
				LatLonCautra stop = LatLonCautra.fromCautra(origine.getCautra()[0]+width*size*hsens, origine.getCautra()[1] + i * size* vsens);
				
				LinkedList<LatLon> line = new LinkedList<LatLon>();
				line.add(start);
				line.add(stop);
				VSurfacePolyline ligne = new VSurfacePolyline(line);
				ligne.setDatabaseType(base);
				ligne.setType(type);
				ligne.setName(name);
				ligne.setAttributes(new BasicShapeAttributes());
				ligne.setClosed(false);
				this.grilleLayer.addRenderable(ligne);
			}
			//affichage des colonnes
			for(int i=0; i<= width; i++){
				
				LatLonCautra start = LatLonCautra.fromCautra(origine.getCautra()[0] + i * size * hsens, origine.getCautra()[1]);
				
				LatLonCautra stop = LatLonCautra.fromCautra(origine.getCautra()[0]+ i * size * hsens, origine.getCautra()[1] + height * size * vsens);
							
				LinkedList<LatLon> line = new LinkedList<LatLon>();
				line.add(start);
				line.add(stop);
				VSurfacePolyline col = new VSurfacePolyline(line);
				col.setDatabaseType(base);
				col.setType(type);
				col.setName(name);
				col.setAttributes(new BasicShapeAttributes());
				col.setClosed(false);
				this.grilleLayer.addRenderable(col);
			}
		}

		//affichage des numéros des carrés
		if(numbers){
			for(Integer i = 1; i <= width*height; i++){
				if(i == 1 || i%10 == 0){
					int colonne = 0;
					int ligne = 0;
					if(numSens == VERTICAL_FIRST) {
						colonne = i / height ;
						ligne = i % height -1;	
						if(ligne == -1){
							ligne = height -1;
							colonne--;
						}
					} else {
						colonne = i % width -1 ;
						ligne = i / width;	
						if(colonne == -1){
							colonne = width -1;
							ligne--;
						}
					}
					LatLonCautra latLon = LatLonCautra.fromCautra(origine.getCautra()[0] + (colonne * size + size/2)* hsens, origine.getCautra()[1] + (ligne * size +size/2) * vsens);
					UserFacingText text = new UserFacingText(i.toString(), new Position(latLon, 0));
					textLayer.addGeographicText(text);
				}
			}
		}
		
		//coloriage des carrés
		if(squares != null){
			Iterator<Couple<Integer, Integer>> iterator = squares.iterator();
			Iterator<Couple<Double, Double>> alt = altitudes.iterator();
			while(iterator.hasNext()){
				Couple<Integer, Integer> square = iterator.next();
				Couple<Double, Double> altitude = new Couple<Double, Double>(null, null);
				if(alt != null) altitude = alt.next();
				String annotation = null;
				if(annotationTitle != null) {
					annotation = "<p><b>"+annotationTitle+"</b></p>";
					annotation += "<p>Carré : "+square.getFirst()+ (square.getSecond() == 0 ? "" : "<br />Sous carré : "+square.getSecond()) ;
					annotation += "<br />Plafond : "+String.format("%3.0f", altitude.getSecond()/30.48)+
								  "<br />Plancher : "+String.format("%3.0f", altitude.getFirst()/30.48)+"</p>";
				}
				this.colorieCarre(annotation, origine, square.getFirst(), square.getSecond(), width, height, size, altitude.getFirst(), 
						altitude.getSecond(), hsens, vsens, numSens, attr, airspaceAttr, base, type, name);
			}
		}
	}
	/**
	 * Colorie un carré de la mosaïque	
	 * @param annotation {@link String} Texte de l'annotation
	 * @param origine {@link LatLonCautra} Origine de la mosaïque
	 * @param carre Numéro du carré
	 * @param sousCarre Numéro du sous-carré
	 * @param width {@link Integer} Nombre de carrés de côté
	 * @param height {@link Integer} Nombre de carrés de hauteur
	 * @param size {@link Integer} Taille d'un carré en NM
	 * @param plancher {@link Double} plancher du carré, en mètres
	 * @param plafond du carré, en mètres
	 * @param hsens {@link Integer} Sens de parcours vertical
	 * @param vsens {@link Integer} Sens de parcours horizontal
	 * @param numSens {@link Integer} Sens de la numérotation (VERTICAL_FIRST : utilisé par le STR, HORIZONTAL_FIRST : utilisé par le STPV
	 * @param attr {@link ShapeAttributes} Attributs des carrés
	 */
	private void colorieCarre(String annotation,
			LatLonCautra origine,
			Integer carre,
			Integer sousCarre, 
			Integer width, 
			Integer height, 
			Integer size, 
			Double plancher,
			Double plafond,
			int hsens, 
			int vsens, 
			int numSens, ShapeAttributes attr, AirspaceAttributes airspaceAttr,
			DatabaseManager.Type base,
			int type,
			String name){

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

			
				SurfacePolygonAnnotation polygon = new SurfacePolygonAnnotation(locations);
				polygon.setDatabaseType(base);
				polygon.setName(name);
				polygon.setType(type);
				//polygon.setName(annotation.split("[<>]")[4]);
				if(attr != null) polygon.setAttributes(attr);
				if(annotation != null) polygon.setAnnotation(annotation);
				this.shapeLayer.addRenderable(polygon);
		
				PolygonAnnotation polyg = new PolygonAnnotation(locations);
				polyg.setDatabaseType(base);
				polyg.setType(type);
				polyg.setName(name);
				//polyg.setName(annotation.split("[<>]")[4]);
				polyg.setAltitudes(plancher, plafond);
				if(annotation != null) polyg.setAnnotation(annotation);
				if(airspaceAttr != null) polyg.setAttributes(airspaceAttr);
				this.airspaceLayer.addAirspace(polyg);
			
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

	
				SurfacePolygonAnnotation polygon = new SurfacePolygonAnnotation(locations);
				polygon.setDatabaseType(base);
				polygon.setName(name);
				polygon.setType(type);
				//polygon.setName(annotation.split("[<>]")[4]);
				if(attr != null) polygon.setAttributes(attr);
				if(annotation != null) polygon.setAnnotation(annotation);
				this.shapeLayer.addRenderable(polygon);
		
				PolygonAnnotation polyg = new PolygonAnnotation(locations);
				polyg.setDatabaseType(base);
				polyg.setType(type);
				polyg.setName(name);
				//polyg.setName(annotation.split("[<>]")[4]);
				polyg.setAltitudes(plancher, plafond);
				if(airspaceAttr != null) polyg.setAttributes(airspaceAttr);
				if(annotation != null) polyg.setAnnotation(annotation);
				this.airspaceLayer.addAirspace(polyg);
			
		}
	}

	/**
	 * Mosaiques en 2D ou 3D
	 * @param b True si 3D
	 */
	public void set3D(Boolean b){
		if(this.threeD != b) {
			this.threeD = b;
			if(b){
				this.remove(shapeLayer);
				this.add(airspaceLayer);
			} else {
				this.remove(airspaceLayer);
				this.add(shapeLayer);
			}
		}
	}
}
