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

import java.util.HashSet;
import java.util.Set;

import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerRenderer;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.Logging;
/**
 * MarkerLayer avec une possibilité d'ajouter des Marker à l'ensemble existant
 * @author Bruno Spyckerelle
 * @version 0.3.0
 */
public class BaliseMarkerLayer extends AbstractLayer {
	
	
	private MarkerRenderer markerRenderer = new MarkerRenderer();
	private Set<Marker> markers;

	public BaliseMarkerLayer(){
		super();
		this.setKeepSeparated(false);
		this.setMinActiveAltitude(0);
		//inutile d'afficher le point avant 1000km d'altitude
		this.setMaxActiveAltitude(10e5);
		this.markers = new HashSet<Marker>();
	}

	public BaliseMarkerLayer(Iterable<Marker> markers){
		this();
		for(Marker m : markers){
			this.markers.add(m);
		}
	}

	public Iterable<Marker> getMarkers(){
		return markers;
	}


	/**
	 * 
	 * @param markers If null, remove all markers
	 */
	public void setMarkers(Iterable<Marker> markers) {
		this.markers.clear();
		if(markers != null) {			
			for(Marker m : markers){
				this.markers.add(m);
			}
		}
	}

	public double getElevation()	{
		return this.getMarkerRenderer().getElevation();
	}

	public void setElevation(double elevation){
		this.getMarkerRenderer().setElevation(elevation);
	}

    public boolean isOverrideMarkerElevation(){
		return this.getMarkerRenderer().isOverrideMarkerElevation();
	}

	public void setOverrideMarkerElevation(boolean overrideMarkerElevation){
		this.getMarkerRenderer().setOverrideMarkerElevation(overrideMarkerElevation);
	}

	public boolean isKeepSeparated(){
		return this.getMarkerRenderer().isKeepSeparated();
	}

	public void setKeepSeparated(boolean keepSeparated)	{
		this.getMarkerRenderer().setKeepSeparated(keepSeparated);
	}

	public boolean isEnablePickSizeReturn()	{
		return this.getMarkerRenderer().isEnablePickSizeReturn();
	}

	public void setEnablePickSizeReturn(boolean enablePickSizeReturn)	{
		this.getMarkerRenderer().setEnablePickSizeReturn(enablePickSizeReturn);
	}

	/**
	 * Opacity is not applied to layers of this type because each marker has an attribute set with opacity control.
	 *
	 * @param opacity the current opacity value, which is ignored by this layer.
	 */
	 @Override
	 public void setOpacity(double opacity) {
		 super.setOpacity(opacity);
	 }

	 /**
	  * Returns the layer's opacity value, which is ignored by this layer because each of its markers has an attribute
	  * with its own opacity control.
	  *
	  * @return The layer opacity, a value between 0 and 1.
	  */
	 @Override
	 public double getOpacity() {
		 return super.getOpacity();
	 }

	 protected MarkerRenderer getMarkerRenderer() {
		 return markerRenderer;
	 }

	 protected void setMarkerRenderer(MarkerRenderer markerRenderer) {
		 this.markerRenderer = markerRenderer;
	 }

	 protected void doRender(DrawContext dc){
		 this.draw(dc, null);
	 }

	 @Override
	 protected void doPick(DrawContext dc, java.awt.Point pickPoint) {
		 this.draw(dc, pickPoint);
	 }

	 protected void draw(DrawContext dc, java.awt.Point pickPoint) {
		 if (this.markers == null)
			 return;

		 if (dc.getVisibleSector() == null)
			 return;

		 SectorGeometryList geos = dc.getSurfaceGeometry();
		 if (geos == null)
			 return;

		 // Adds markers to the draw context's ordered renderable queue. During picking, this gets the pick point and the
		 // current layer from the draw context.
		 this.getMarkerRenderer().render(dc, this.markers);
	 }

	 @Override
	 public String toString() {
		 return Logging.getMessage("layers.MarkerLayer.Name");
	 }



	 /**
	  * Ajoute un {@link Marker} à l'ensemble existant
	  * @param marker {@link Marker} à ajouter
	  */	
	 public void addMarker(Marker marker){
		this.markers.add(marker);
	 }

	 /**
	  * Enlève un marker au layer. Si il n'existe pas, ne fait rien.
	  * @param marker Marker à enlever
	  */
	 public void removeMarker(Marker marker){
		this.markers.remove(marker);
	 }

	 public void removeAllMarkers(){
		 this.markers.clear();
	 }

}
