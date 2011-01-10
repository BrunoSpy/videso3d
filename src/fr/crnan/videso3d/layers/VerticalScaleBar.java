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

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.graphics.MovablePointPlacemark;
import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.DragSelectEvent;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RayCastingSupport;
/**
 * Vertical scalebar in FL, movable
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class VerticalScaleBar extends RenderableLayer implements SelectListener {

	private PointPlacemark top;

	private List<PointPlacemark> hundreds = new LinkedList<PointPlacemark>();

	private List<PointPlacemark> tens = new LinkedList<PointPlacemark>();

	private final WorldWindow wwd;
	private boolean dragging = false;

	private Point dragRefCursorPoint;
	private Vec4 dragRefObjectPoint;

	private double dragRefAltitude;

	private boolean detailed = false;
	
	public VerticalScaleBar(VidesoGLCanvas wd){

		this.wwd = wd;
		
		
		top = new MovablePointPlacemark(new Position(LatLon.ZERO, 600*30.48));
		top.setAltitudeMode(WorldWind.ABSOLUTE);
		top.setApplyVerticalExaggeration(true);
		top.setLabelText("FL 600");
		top.setLineEnabled(true);
		PointPlacemarkAttributes topAttr = new PointPlacemarkAttributes();
		topAttr.setLineWidth(2d);
		topAttr.setLineMaterial(Material.WHITE);
		topAttr.setUsePointAsDefaultImage(true);
		topAttr.setLabelScale(1.0);
		top.setAttributes(topAttr);
		this.addRenderable(top);

		PointPlacemarkAttributes hundredsAttributes = new PointPlacemarkAttributes();
		hundredsAttributes.setUsePointAsDefaultImage(true);
		hundredsAttributes.setLabelScale(0.8);
		for(int i = 1;i<6;i++){
			PointPlacemark p = new MovablePointPlacemark(new Position(LatLon.ZERO, 100*i*30.48));
			p.setLabelText("FL "+i*100);
			p.setAltitudeMode(WorldWind.ABSOLUTE);
			p.setApplyVerticalExaggeration(true);
			p.setLineEnabled(false);
			p.setAttributes(hundredsAttributes);
			hundreds.add(p);
			this.addRenderable(p);
		}

		PointPlacemarkAttributes tensAttributes = new PointPlacemarkAttributes();
		tensAttributes.setUsePointAsDefaultImage(true);
		tensAttributes.setLabelScale(0.7);
		for(int i = 1; i < 60;i++){
			if( i % 10 != 0){
				PointPlacemark p = new MovablePointPlacemark(new Position(LatLon.ZERO, 10*i*30.48));
				p.setLabelText(""+i*10);
				p.setAltitudeMode(WorldWind.ABSOLUTE);
				p.setApplyVerticalExaggeration(true);
				p.setLineEnabled(false);
				p.setAttributes(tensAttributes);
				tens.add(p);
			}
		}

		//changement de l'affichage en fonction du zoom
		this.wwd.getSceneController().addPropertyChangeListener(AVKey.VERTICAL_EXAGGERATION, new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
			
				if(wwd.getSceneController().getVerticalExaggeration() >= 4.0){
					if(!detailed){
						addRenderables(tens);
						detailed = true;
					}
					
				} else {
					for(PointPlacemark p : tens){
						removeRenderable(p);
					}
					detailed = false;
				}
			}
		});
		
	}

	@Override
	public void selected(SelectEvent event) {
		if (event == null)
		{
			String msg = Logging.getMessage("nullValue.EventIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		
		if (event.getEventAction().equals(SelectEvent.DRAG_END))
		{
			this.dragging = false;
		}
		else if (event.getEventAction().equals(SelectEvent.DRAG))
		{
			DragSelectEvent dragEvent = (DragSelectEvent) event;
			Object topObject = dragEvent.getTopObject();
			if (topObject == null){
				return;
			}

			if(topObject != top && !hundreds.contains(topObject) && !tens.contains(topObject)) {
				return;
			}
			
			if (!(topObject instanceof Movable)) {
				return;
			}
			

			Movable dragObject = (Movable) topObject;
			View view = wwd.getView();
			Globe globe = wwd.getModel().getGlobe();

			// Compute dragged object ref-point in model coordinates.
			// Use the Icon and Annotation logic of elevation as offset above ground when below max elevation.
			Position refPos = dragObject.getReferencePosition();
			if (refPos == null)
				return;

			Vec4 refPoint = null;
			if (refPos.getElevation() < globe.getMaxElevation())
				refPoint = wwd.getSceneController().getTerrain().getSurfacePoint(refPos);
			if (refPoint == null)
				refPoint = globe.computePointFromPosition(refPos);

			if (!this.isDragging())   // Dragging started
			{
				// Save initial reference points for object and cursor in screen coordinates
				// Note: y is inverted for the object point.
				this.dragRefObjectPoint = view.project(refPoint);
				// Save cursor position
				this.dragRefCursorPoint = dragEvent.getPreviousPickPoint();
                // Save start altitude
                this.dragRefAltitude = globe.computePositionFromPoint(refPoint).getElevation();
			}

			// Compute screen-coord delta since drag started.
			int dx = dragEvent.getPickPoint().x - this.dragRefCursorPoint.x;
			int dy = dragEvent.getPickPoint().y - this.dragRefCursorPoint.y;

			// Find intersection of screen coord (refObjectPoint + delta) with globe.
			double x = this.dragRefObjectPoint.x + dx;
			double y = event.getMouseEvent().getComponent().getSize().height - this.dragRefObjectPoint.y + dy - 1;
			Line ray = view.computeRayFromScreenPoint(x, y);
			Position pickPos = null;
			if (view.getEyePosition().getElevation() < globe.getMaxElevation() * 10)
			{
				// Use ray casting below some altitude
				// Try ray intersection with current terrain geometry
				Intersection[] intersections = wwd.getSceneController().getTerrain().intersect(ray);
				if (intersections != null && intersections.length > 0)
					pickPos = globe.computePositionFromPoint(intersections[0].getIntersectionPoint());
				else
					// Fallback on raycasting using elevation data
					pickPos = RayCastingSupport.intersectRayWithTerrain(globe, ray.getOrigin(), ray.getDirection(),
							200, 20);
			}
			if (pickPos == null)
			{
				// Use intersection with sphere at reference altitude.
				Intersection inters[] = globe.intersect(ray, this.dragRefAltitude);
				if (inters != null)
					pickPos = globe.computePositionFromPoint(inters[0].getIntersectionPoint());
			}

			if (pickPos != null)
			{
				// Intersection with globe. Move reference point to the intersection point,
				// but maintain current altitude.
		//		Position p = new Position(pickPos, dragObject.getReferencePosition().getElevation());
		//		dragObject.moveTo(p);
				this.movePointPlacemarks(pickPos);
			}
			this.dragging = true;
		}
	}

	private void movePointPlacemarks(LatLon latlon){
		top.setPosition(new Position(latlon, top.getPosition().getElevation()));
		for(PointPlacemark p : hundreds){
			p.setPosition(new Position(latlon, p.getPosition().getElevation()));
		}
		for(PointPlacemark p : tens){
			p.setPosition(new Position(latlon, p.getPosition().getElevation()));
		}
	}
	
    public boolean isDragging()
    {
        return this.dragging;
    }

    public void initializePosition(Position position){
    	this.movePointPlacemarks(position);
    	this.firePropertyChange(AVKey.LAYER, null, true);
    }
    
}
