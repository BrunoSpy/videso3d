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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.Polygon;
/**
 * Représentation 3D d'un secteur de contrôle
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class Secteur3D extends Polygon {

	/*
	 * Nom du secteur
	 */
	private String name;
	
	private GlobeAnnotation annotation;
	
	/**
	 * Crée un secteur 3D
	 * @param name Nom du secteur
	 * @param plancher Plancher en niveaux
	 * @param plafond Plafond en niveaux
	 */
	public Secteur3D(String name, Integer plancher, Integer plafond){
		this.setName(name);
		this.setNiveaux(plancher, plafond);
		this.setDefaultMaterial();
		
		this.annotation = new GlobeAnnotation("Secteur "+name+"\nPlancher : FL"+plancher+", plafond : FL"+plafond, Position.ZERO);
	}

	public void setNiveaux(Integer plancher, Integer plafond){
		this.setAltitudes(plancher * 30.48,	plafond * 30.48);
	}
	
	private void setName(String name) {
		this.name = name;
		this.setValue("description", "Secteur "+name);
	}
	
	public GlobeAnnotation getAnnotation(Position pos){
		annotation.setPosition(pos);
		return annotation;
	}
	
	/*---------- Pour tests -----------*/
	
	 private void setDefaultMaterial() {
			Color color = Color.CYAN;
			Color outline = makeBrighter(color);
			
	        this.getAttributes().setDrawOutline(true);
	        this.getAttributes().setMaterial(new Material(color));
	        this.getAttributes().setOutlineMaterial(new Material(outline));
	        this.getAttributes().setOpacity(0.8);
	        this.getAttributes().setOutlineOpacity(0.9);
	        this.getAttributes().setOutlineWidth(3.0);
		}
	 
	 private static Color makeBrighter(Color color)
	    {
	        float[] hsbComponents = new float[3];
	        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbComponents);
	        float hue = hsbComponents[0];
	        float saturation = hsbComponents[1];
	        float brightness = hsbComponents[2];

	        saturation /= 3f;
	        brightness *= 3f;

	        if (saturation < 0f)
	            saturation = 0f;

	        if (brightness > 1f)
	            brightness = 1f;

	        int rgbInt = Color.HSBtoRGB(hue, saturation, brightness);
	        
	        return new Color(rgbInt);
	    }
}

