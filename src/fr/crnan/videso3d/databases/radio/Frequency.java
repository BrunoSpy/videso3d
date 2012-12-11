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

package fr.crnan.videso3d.databases.radio;

/**
 * @author Mickael Papail
 * TODO : Gestion des couleurs individuelle pour chaque couverture.
 */


import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.Airspace;

import fr.crnan.videso3d.graphics.RadioCovPolygon;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;


public class Frequency {
	private String sectorName = null;
	private double freqValue = 0;	
	private RadioCovPolygon[] volumes = new RadioCovPolygon[4];
	private String[] antennas = new String[4];
	private Color color1 = new Color(178,242,251,0);
	private Color color2 = new Color(253,206,139,0);
	
	private ArrayList<Frequency> freqList = new ArrayList<Frequency>();
	private AirspaceLayer radioCovList;
	private Collection<Airspace> airspaces;
	

	public void setNorm1(RadioCovPolygon norm1) {
		volumes[0]=norm1;
		//this.setupDefaultMaterial(volumes[0],Color.GREEN);
		Color outlineColor = Color.BLUE;
		volumes[0].getAttributes().setDrawOutline(true);
		volumes[0].getAttributes().setMaterial(new Material(Color.BLUE));
		volumes[0].getAttributes().setOutlineMaterial(new Material(outlineColor));
		volumes[0].getAttributes().setOpacity(0.8);
		volumes[0].getAttributes().setOutlineOpacity(0.9);
		volumes[0].getAttributes().setOutlineWidth(1.0);
	}
	public void setNorm2(RadioCovPolygon norm2) {
		volumes[1]=norm2;
		this.setupDefaultMaterial(volumes[0],Color.BLUE);
	}
	public void setSec1(RadioCovPolygon sec1) {
		volumes[2]=sec1;
		this.setupDefaultMaterial(volumes[0],Color.GREEN);
	}
	public void setSec2(RadioCovPolygon sec2) {
		volumes[3]=sec2;		
		this.setupDefaultMaterial(volumes[0],Color.BLUE);
	}		
		
	private void setVolumes(RadioCovPolygon norm1, RadioCovPolygon sec1, RadioCovPolygon norm2, RadioCovPolygon sec2) {		
		if (norm1 != null) { volumes[0]=norm1; this.setupDefaultMaterial(volumes[0],color1);}
		if (sec1 !=null) { volumes[1]=sec1; this.setupDefaultMaterial(volumes[1],color2);}
		if (norm2 !=null) { volumes[2]=norm2; this.setupDefaultMaterial(volumes[2],color1);}
		if (sec2 !=null) { volumes[3]=sec2; this.setupDefaultMaterial(volumes[3],color2);}						
		/* volumes[2]=sec1;  //this.setupDefaultMaterial(volumes[2],Color.BLUE);
		volumes[3]=sec2;  //this.setupDefaultMaterial(volumes[3],Color.GREEN); */		
	}		
	
	public void setColors() {
		if (volumes[0] != null) {  this.setupDefaultMaterial(volumes[0],color1);}
		if (volumes[1] != null) {  this.setupDefaultMaterial(volumes[1],color2);}
		if (volumes[2] != null) {  this.setupDefaultMaterial(volumes[2],color1);}
		if (volumes[3] != null) {  this.setupDefaultMaterial(volumes[3],color2);}						
	}
	
	
	public Frequency(double freqValue, String sectorName, RadioCovPolygon norm1, RadioCovPolygon sec1, RadioCovPolygon norm2, RadioCovPolygon sec2 ) {
		this.freqValue=freqValue;
		this.sectorName=sectorName;		
		this.setVolumes(norm1,sec1,norm2,sec2); 		
	}
	
	public String getSectorName() {
		return this.sectorName;
	}
	
	public double getFreqValue() {
		return this.freqValue;
	}
	public String getInfos() {
		return (this.sectorName);
	}
	
	public RadioCovPolygon[] getVolumes() {
		return this.volumes;
	}
	
	private void setupDefaultMaterial(Airspace a, Color color)
    {
        Color outlineColor = color;
        // Color outlineColor = color.BLUE;
        a.getAttributes().setDrawOutline(true);
        a.getAttributes().setMaterial(new Material(color));
        a.getAttributes().setOutlineMaterial(new Material(outlineColor));
        a.getAttributes().setOpacity(0.1);
        a.getAttributes().setOutlineOpacity(0.9);
        a.getAttributes().setOutlineWidth(1.0);
    }
	
	
}
	
