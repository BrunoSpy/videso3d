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

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.DragSelectEvent;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;


/**
 * Drag {@link Movable} objects<br />
 * Objects have to be registered before in order to be moved.
 * @see gov.nasa.worldwind.util.BasicDragger
 * @author Bruno Spyckerelle
 * @version 0.1.1
 */
public class DraggerListener implements SelectListener {
	
	private Set<Movable> draggables = new HashSet<Movable>();
	
	private final WorldWindow wwd;
    private boolean dragging = false;

    private Point dragRefCursorPoint;
    private Vec4 dragRefObjectPoint;
    private double dragRefAltitude;

    public DraggerListener(VidesoGLCanvas wwd){
    	if (wwd == null)
    	{
    		String msg = Logging.getMessage("nullValue.WorldWindow");
    		Logging.logger().severe(msg);
    		throw new IllegalArgumentException(msg);
    	}

    	this.wwd = wwd;
    }

    public boolean isDragging()
    {
        return this.dragging;
    }
	
	/**
	 * Allows this object to be dragged
	 * @param o
	 */
	public void addDraggableObject(Movable o){
		this.draggables.add(o);
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
            event.consume();
        }
        else if (event.getEventAction().equals(SelectEvent.DRAG))
        {
            DragSelectEvent dragEvent = (DragSelectEvent) event;
            Object topObject = dragEvent.getTopObject();
            if (topObject == null)
                return;

            if (!(topObject instanceof Movable))
                return;

            if(!isDraggable(topObject))
            	return;
            
            Movable dragObject = (Movable) topObject;
            View view = wwd.getView();
            Globe globe = wwd.getModel().getGlobe();

            // Compute dragged object ref-point in model coordinates.
            // Use the Icon and Annotation logic of elevation as offset above ground when below max elevation.
            Position refPos = dragObject.getReferencePosition();
            if (refPos == null)
                return;

            Vec4 refPoint = globe.computePointFromPosition(refPos);

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
            // Use intersection with sphere at reference altitude.
            Intersection inters[] = globe.intersect(ray, this.dragRefAltitude);
            if (inters != null)
                pickPos = globe.computePositionFromPoint(inters[0].getIntersectionPoint());

            if (pickPos != null)
            {
                // Intersection with globe. Move reference point to the intersection point,
                // but maintain current altitude.
                Position p = new Position(pickPos, dragObject.getReferencePosition().getElevation());
                this.doMove(p, dragObject);
            }
            this.dragging = true;
            event.consume();
        }
	}

    protected void doMove(Position p, Movable dragObject) {
    	dragObject.moveTo(p);
	}

	public Boolean isDraggable(Object o){
    	return this.draggables.contains(o);
    }
	
}
