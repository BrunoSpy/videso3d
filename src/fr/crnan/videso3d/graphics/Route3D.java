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

import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.Curtain;
/**
 * Représentation 3D d'une route sous la forme d'un ruban
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class Route3D extends Curtain {

	/**
	 * Type de la route
	 */
	public static enum Type {FIR, UIR};
	/**
	 * Nom de la route
	 */
	private String name;
	
	public Route3D(){
		super();
		this.setDefaultMaterial();
	}
	
	public Route3D(Type type){
		this.setType(type);
		this.setDefaultMaterial();
	}
	
	public void setType(Type type){
		switch (type) {
		case FIR:
			this.setAltitudes(0, 5943); //FL0 à FL195, en mètres
			break;
		case UIR:
			this.setAltitudes(5943,20116 ); //FL195 à FL660, en mètres
			break;
		}
	}
	
	 private void setDefaultMaterial() {
		Color color = Color.CYAN;
		Color outline = makeBrighter(color);
		
        this.getAttributes().setDrawOutline(true);
        this.getAttributes().setMaterial(new Material(color));
        this.getAttributes().setOutlineMaterial(new Material(outline));
        this.getAttributes().setOpacity(0.8);
        this.getAttributes().setOutlineOpacity(0.9);
        this.getAttributes().setOutlineWidth(1.0);
	}

	public void setName(String name){
		 this.name = name;
		 this.setValue("Description", name);
		 
	 }
	
	public String getName(){
		return this.name;
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
