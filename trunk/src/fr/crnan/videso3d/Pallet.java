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

import java.awt.Color;
/**
 * Gestion des couleurs
 * @author Bruno Spyckerelle
 * @version 0.2.1
 */
public class Pallet {

	/**
	 * Couleurs par défaut
	 */
	private static String FOND_PAYS = "#FFFFFF"; //blanc
	private static String BALISE_MARKER = "#FFFFFF";
	private static String BALISE_TEXT = "#FFFFFF";
		
	public static Color ANNOTATION_BACKGROUND = new Color(1f, 1f, 1f, .7f);
	public static Color SIVColor = new Color(200,100,200);
	public static Color CTRColor = new Color(230,250,100);
	public static Color TMAColor = new Color(240,240,240);
	public static Color FIRColor = new Color(50,255,50);
	public static Color UIRColor = new Color(150,255,150);
	public static Color LTAColor = new Color(150,0,150);
	public static Color UTAColor = new Color(255,180,255);
	public static Color CTAColor = Color.pink;
	public static Color OCAColor = new Color(150,150,0);
	public static Color PRNColor = new Color(200,50,250);
	public static Color CTLColor = new Color(0,150,250);
	public static Color defaultColor = new Color(50,50,50);
	
	
	
	public static Color getColorFondPays(){
		return Color.decode(Configuration.getProperty(Configuration.COLOR_FOND_PAYS, FOND_PAYS));
	}
	
	public static Color getColorBaliseMarker(){
		return Color.decode(Configuration.getProperty(Configuration.COLOR_BALISE_MARKER, BALISE_MARKER));
	}
	
	public static Color getColorBaliseText(){
		return Color.decode(Configuration.getProperty(Configuration.COLOR_BALISE_TEXTE, BALISE_TEXT));
	}
	/**
	 * Change la couleur du fond pays
	 */
	public static void setColorFondPays(Color color){
		if(color == null) return;
		Configuration.setProperty(Configuration.COLOR_FOND_PAYS, Pallet.toHexString(color));
	}
	
	public static void setColorBaliseMarker(Color color) {
		if(color == null) return;
		Configuration.setProperty(Configuration.COLOR_BALISE_MARKER, Pallet.toHexString(color));
	}
	

	public static void setColorBaliseTexte(Color color) {
		if(color == null) return;
		Configuration.setProperty(Configuration.COLOR_BALISE_TEXTE, Pallet.toHexString(color));
	}
	
	/**
	 * Renvoie la chaine de caractère hexadécimale correspondant à la couleur <code>color</code>
	 * @param color
	 * @return Représentation hexadécimale de <code>color</code>
	 */
	public static String toHexString(Color color){
		return String.format( "#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue() );
	}
	
	/**
     * Derives a color by adding the specified offsets to the base color's 
     * hue, saturation, and brightness values.   The resulting hue, saturation,
     * and brightness values will be contrained to be between 0 and 1.
     * @param base the color to which the HSV offsets will be added
     * @param dH the offset for hue
     * @param dS the offset for saturation
     * @param dB the offset for brightness
     * @return Color with modified HSV values
     */
    public static Color deriveColorHSB(Color base, float dH, float dS, float dB) {
        float hsb[] = Color.RGBtoHSB(
                base.getRed(), base.getGreen(), base.getBlue(), null);

        hsb[0] += dH;
        hsb[1] += dS;
        hsb[2] += dB;
        return Color.getHSBColor(
                hsb[0] < 0? 0 : (hsb[0] > 1? 1 : hsb[0]),
                hsb[1] < 0? 0 : (hsb[1] > 1? 1 : hsb[1]),
                hsb[2] < 0? 0 : (hsb[2] > 1? 1 : hsb[2]));
                                               
    }
	
    public static Color makeBrighter(Color color)
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
        
        Color newColor = new Color(rgbInt);
        
        
        return new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), color.getAlpha());
    }



}
