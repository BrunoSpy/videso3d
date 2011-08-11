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

import fr.crnan.videso3d.geom.LatLonCautra;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.CappedCylinder;
/**
 * Cylindre 3D
 * @author Bruno Spyckerelle
 * @version 0.2.0
 */
public class Cylinder extends CappedCylinder implements VidesoObject {

	private String name;
	
	private VidesoAnnotation annotation;
	
	private boolean highlighted = false;
	private AirspaceAttributes highlightAttrs;
	private AirspaceAttributes normalAttrs;
	
	public Cylinder(){
		super();
	}
	
	/**
	 * 
	 * @param name Nom
	 * @param center Centre du cylindre
	 * @param flinf Niveau infèrieur
	 * @param flsup Niveau supèrieur
	 * @param rayon Rayon du cylindre en NM
	 */
	public Cylinder(String name, LatLon center, int flinf, int flsup, double rayon){
		super(center, rayon*LatLonCautra.NM);
		this.setName(name);
		this.setAltitudes(flinf*30.48, flsup*30.48);
	}
	
	@Override
	public void setAnnotation(String text){
		if(annotation == null) {
			annotation = new VidesoAnnotation(text);
		} else {
			annotation.setText(text);
		}
	}
	
	@Override
	public VidesoAnnotation getAnnotation(Position pos){
		annotation.setPosition(pos);
		return annotation;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isHighlighted() {
		return this.highlighted;
	}

	@Override
	public void setHighlighted(boolean highlighted) {
		if(this.highlighted != highlighted){
			this.setAttributes(highlighted ? this.getHighlightAttributes() : this.getNormalAttributes());
			this.highlighted = highlighted;
		}
	}
	
    public AirspaceAttributes getNormalAttributes() {
        return this.normalAttrs;
    }

    public void setNormalAttributes(AirspaceAttributes normalAttrs) {
        this.normalAttrs = normalAttrs;
        if(!highlighted) this.setAttributes(this.normalAttrs);
    }
    
    public AirspaceAttributes getHighlightAttributes() {
        return highlightAttrs == null ? this.normalAttrs : this.highlightAttrs;
    }

    /**
     * Specifies highlight attributes.
     *
     * @param highlightAttrs highlight attributes. May be null, in which case default attributes are used.
     */
    public void setHighlightAttributes(AirspaceAttributes highlightAttrs) {
        this.highlightAttrs = highlightAttrs;
        if(highlighted) this.setAttributes(this.highlightAttrs);
    }
}
