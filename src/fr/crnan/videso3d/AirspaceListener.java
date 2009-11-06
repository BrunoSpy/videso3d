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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JColorChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import fr.crnan.videso3d.graphics.ObjectAnnotation;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceShape;
import gov.nasa.worldwind.render.airspaces.AbstractAirspace;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;

/**
 * Listener d'évènements sur les airspaces et shapes
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class AirspaceListener implements SelectListener {

	private Object lastHighlit;
	private Annotation lastAnnotation;
	private Object lastAttrs;
	private Object lastToolTip;
	
	private VidesoGLCanvas wwd;
	
	public AirspaceListener(VidesoGLCanvas wwd){
		this.wwd = wwd;
	}
	
	/* (non-Javadoc)
	 * @see gov.nasa.worldwind.event.SelectListener#selected(gov.nasa.worldwind.event.SelectEvent)
	 */
	@Override
	public void selected(final SelectEvent event) {

		if (lastHighlit != null
				&& (event.getTopObject() == null || !event.getTopObject().equals(lastHighlit)))
		{
			if(lastHighlit instanceof AbstractAirspace) {
				((AbstractAirspace)lastHighlit).setAttributes((AirspaceAttributes)lastAttrs);
			} else if(lastHighlit instanceof SurfaceShape){
				((SurfaceShape)lastHighlit).setAttributes((ShapeAttributes)lastAttrs);
			}
			lastHighlit = null;
		}

		if (lastToolTip != null
				&& (event.getTopObject() == null || !event.getTopObject().equals(lastHighlit)))
		{
			if(lastAnnotation != null) this.wwd.getAnnotationLayer().removeAnnotation(lastAnnotation);
			lastToolTip = null;
		}

		if(event.getEventAction() == SelectEvent.ROLLOVER){ //Hightlight object
			if(event.getTopObject() != null) {
				Object o = event.getTopObject();
				if (lastHighlit == o)
					return; 
				if (lastHighlit == null)
				{
					if(event.getTopObject() instanceof AbstractAirspace) {
						lastHighlit = (AbstractAirspace)o;
						lastAttrs = ((AbstractAirspace)lastHighlit).getAttributes();
						BasicAirspaceAttributes highliteAttrs = new BasicAirspaceAttributes((AirspaceAttributes) lastAttrs);
						highliteAttrs.setMaterial(new Material(Pallet.makeBrighter(((AirspaceAttributes)lastAttrs).getMaterial().getDiffuse())));
						((AbstractAirspace) lastHighlit).setAttributes(highliteAttrs);

					} else if (event.getTopObject() instanceof SurfaceShape) {
						lastHighlit = (SurfaceShape)o;
						lastAttrs = ((SurfaceShape)lastHighlit).getAttributes();
						BasicShapeAttributes highliteAttrs = new BasicShapeAttributes((ShapeAttributes) lastAttrs);
						highliteAttrs.setInteriorMaterial(new Material(Pallet.makeBrighter(((ShapeAttributes)lastAttrs).getInteriorMaterial().getDiffuse())));
						((SurfaceShape) lastHighlit).setAttributes(highliteAttrs);
					}
				}
			}
		} else if(event.getEventAction() == SelectEvent.HOVER){ //popup tooltip
			if(event.getTopObject() != null){
				Object o = event.getTopObject();
				if(lastToolTip == o)
					return;
				if(lastToolTip == null) {
					lastToolTip = o;
					Point point = event.getPickPoint();
					if(event.getTopObject() instanceof ObjectAnnotation){
						lastAnnotation = ((ObjectAnnotation)o).getAnnotation(this.wwd.getView().computePositionFromScreenPoint(point.x, point.y));
					} 
					if(lastAnnotation != null) this.wwd.getAnnotationLayer().addAnnotation(lastAnnotation);
					this.wwd.redraw();
				}
			}
		} else if(event.getEventAction() == SelectEvent.RIGHT_CLICK){
			final JPopupMenu menu = new JPopupMenu("Menu");
			JMenuItem colorItem = new JMenuItem("Couleur...");
			colorItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(lastAttrs instanceof AirspaceAttributes) {
						Color color = JColorChooser.showDialog(menu, "Couleur du secteur", ((AirspaceAttributes)lastAttrs).getMaterial().getDiffuse());
						if(color != null) {
							((AirspaceAttributes)lastAttrs).setMaterial(new Material(color));
							((AirspaceAttributes)lastAttrs).setOutlineMaterial(new Material(Pallet.makeBrighter(color)));
						}
					} else if(lastAttrs instanceof ShapeAttributes) {
						Color color = JColorChooser.showDialog(menu, "Couleur du secteur", ((ShapeAttributes)lastAttrs).getInteriorMaterial().getDiffuse());
						if(color != null) {
							((ShapeAttributes)lastAttrs).setInteriorMaterial(new Material(color));
							((ShapeAttributes)lastAttrs).setOutlineMaterial(new Material(Pallet.makeBrighter(color)));
							((SurfaceShape)event.getTopObject()).setAttributes((ShapeAttributes) lastAttrs);
						}
					}
				}
			});
			menu.add(colorItem);
			menu.add("Rechercher...");
			menu.show(wwd, event.getMouseEvent().getX(), event.getMouseEvent().getY());
		} else if (event.getEventAction() == SelectEvent.LEFT_DOUBLE_CLICK){
			
		}
	}
}
