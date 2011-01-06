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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fr.crnan.videso3d.Configuration;
import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.Pallet;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.UserFacingText;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.Marker;

/**
 * Balise 2D projetée sur le terrain.<br />
 * A ajouter à un BaliseLayer pour être affichée sur le globe.
 * @author Bruno Spyckerelle
 * @version 0.5.2
 */
public class Balise2D extends MarkerAnnotation implements Balise{
		
	private UserFacingText text;
	
	/**
	 * Crée une balise 2D
	 * @param name Nom de la balise
	 * @param position Position de la balise
	 * @param annotation Annotation associée
	 */
	public Balise2D(CharSequence name, Position position, String annotation, DatabaseManager.Type base, int type){
		super(position, new BasicMarkerAttributes());
		this.setDatabaseType(base);
		this.setType(type);
		this.setName((String) name);
		this.getAttributes().setMarkerPixels(3);
		this.getAttributes().setMaterial(new Material(Pallet.getColorBaliseMarker()));
		
		if(annotation == null){
			this.setAnnotation("Balise "+name);
		} else {
			this.setAnnotation(annotation);
		}
		
		this.text = new UserFacingText(name, new Position(Angle.fromDegrees(position.latitude.degrees+0.01), position.longitude, position.elevation));
		this.text.setFont(new Font("Sans Serif", Font.PLAIN, 9));
		this.text.setColor(Pallet.getColorBaliseText());

		Configuration.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals(Configuration.COLOR_BALISE_MARKER)){
					getAttributes().setMaterial(new Material(Pallet.getColorBaliseMarker()));
				} else if(evt.getPropertyName().equals(Configuration.COLOR_BALISE_TEXTE)){
					text.setColor(Pallet.getColorBaliseText());
				}
			}
		});
	}
	
	/**
	 * Crée une balise 2D
	 * @param name Nom de la balise
	 * @param position {@link LatLon} Position de la balise
	 */
	public Balise2D(CharSequence name, Position position, DatabaseManager.Type base, int type){
		this(name, position, null, base, type);
	}
	
	public UserFacingText getUserFacingText(){
		return this.text;
	}
	
	public Marker getMarker(){
		return this;
	}
	
	/**
	 * Met en valeur la balise
	 * @param bool
	 */
	public void highlight(boolean bool){
		if(bool) {
			this.getAttributes().setMaterial(Material.YELLOW);
			this.text.setColor(Color.YELLOW);
		} else {
			this.getAttributes().setMaterial(Material.WHITE);
			this.text.setColor(Color.WHITE);
		}
	}
	
}
