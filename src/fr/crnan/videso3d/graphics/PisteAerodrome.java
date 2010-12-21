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

import java.awt.Color;
import java.awt.Font;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.aip.AIP;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.Pallet;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.UserFacingText;

/**
 * 
 * @author Adrien Vidal
 *
 */
public class PisteAerodrome implements Aerodrome{

	private SurfacePolygonAnnotation inner, outer;
	private String name;
	private String annotationText;
	private UserFacingText text;
	private Position refPosition;
	
	public PisteAerodrome(int type, String name, String annotation, double lat1, double lon1, double lat2, double lon2, double largeur, Position ref){
		this.name = name;
		this.annotationText = annotation;
		this.refPosition = ref;
		
		computeRectangles(lat1, lon1, lat2, lon2, largeur);
		ShapeAttributes innerAttrs = new BasicShapeAttributes();
		innerAttrs.setInteriorMaterial(new Material(Color.WHITE));
		innerAttrs.setInteriorOpacity(0.8);
		this.inner.setAttributes(innerAttrs);
		this.inner.setAnnotation(annotation);
		this.inner.setDatabaseType(DatabaseManager.Type.AIP);
		this.inner.setType(type);
		this.inner.setName(name);
		ShapeAttributes outerAttrs = new BasicShapeAttributes();
		outerAttrs.setInteriorMaterial(new Material(new Color(0,0,150)));
		outerAttrs.setInteriorOpacity(0.4);
		outerAttrs.setOutlineMaterial(new Material(Color.BLACK));
		outerAttrs.setOutlineOpacity(1);
		outerAttrs.setDrawOutline(true);
		this.outer.setAttributes(outerAttrs);
		this.outer.setAnnotation(annotation);
		this.outer.setDatabaseType(DatabaseManager.Type.AIP);
		this.outer.setType(type);
		this.outer.setName(name);

		this.text = new UserFacingText(name, ref.add(Position.fromDegrees(-0.015, 0)));
		this.text.setFont(new Font("Sans Serif", Font.PLAIN, 9));
		this.text.setColor(Pallet.getColorBaliseText());
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public String getAnnotationText(){
		return annotationText;
	}
	
	@Override
	public UserFacingText getUserFacingText(){
		return text;
	}
	
	@Override
	public Position getRefPosition(){
		return refPosition;
	}
	
	public SurfacePolygon getInnerRectangle(){
		return inner;
	}
	
	public SurfacePolygon getOuterRectangle(){
		return outer;
	}
	
	private void computeRectangles(double lat1Temp, double lon1Temp, double lat2Temp, double lon2Temp, double largeur){
		//TODO passer ces calculs dans géométrie
		double aTemp = lon2Temp-lon1Temp;
		double bTemp = lat1Temp-lat2Temp;
		double longueurVecteur = Math.sqrt(aTemp*aTemp+bTemp*bTemp);
		double a = aTemp/longueurVecteur;
		double b = bTemp/longueurVecteur;
		double largeurCarte = ((double)largeur)/74080;
		
		double lat1 = lat1Temp;
		double lon1 = lon1Temp;
		double lat2 = lat2Temp;
		double lon2 = lon2Temp;
		//points du rectangle intérieur
		List<LatLon> rectInterieur = new LinkedList<LatLon>();
		LatLon pi1 = LatLon.fromDegrees(lat1+a*largeurCarte, lon1+b*largeurCarte);
		LatLon pi2 = LatLon.fromDegrees(lat1-a*largeurCarte, lon1-b*largeurCarte);
		LatLon pi3 = LatLon.fromDegrees(lat2-a*largeurCarte, lon2-b*largeurCarte);
		LatLon pi4 = LatLon.fromDegrees(lat2+a*largeurCarte, lon2+b*largeurCarte);
		rectInterieur.add(pi1);
		rectInterieur.add(pi2);
		rectInterieur.add(pi3);
		rectInterieur.add(pi4);
		this.inner = new SurfacePolygonAnnotation(rectInterieur,0);
		//points du rectangle extérieur				
		List<LatLon> rectExterieur = new LinkedList<LatLon>();
		LatLon pe1 = LatLon.fromDegrees(lat1+(2*a+b)*largeurCarte, lon1+(2*b-a)*largeurCarte);
		LatLon pe2 = LatLon.fromDegrees(lat1-(2*a-b)*largeurCarte, lon1-(2*b+a)*largeurCarte);
		LatLon pe3 = LatLon.fromDegrees(lat2-(2*a+b)*largeurCarte, lon2-(2*b-a)*largeurCarte);
		LatLon pe4 = LatLon.fromDegrees(lat2+(2*a-b)*largeurCarte, lon2+(2*b+a)*largeurCarte);
		rectExterieur.add(pe1);
		rectExterieur.add(pe2);
		rectExterieur.add(pe3);
		rectExterieur.add(pe4);
		this.outer = new SurfacePolygonAnnotation(rectExterieur,0);
	}

	
}
