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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;


import fr.crnan.videso3d.graphics.DatabaseVidesoObject;
import fr.crnan.videso3d.graphics.VPolygon;
import fr.crnan.videso3d.graphics.VidesoAnnotation;
import fr.crnan.videso3d.graphics.VidesoObject;
import fr.crnan.videso3d.ihm.ContextPanel;
import fr.crnan.videso3d.ihm.contextualmenus.ContextualMenu;
import fr.crnan.videso3d.layers.VAnnotationLayer;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.airspaces.Airspace;

/**
 * Listener d'évènements sur les airspaces et shapes
 * @author Bruno Spyckerelle
 * @version 0.7.0
 */
public class AirspaceListener implements SelectListener {

	/**
	 * Dernière annotation affichée
	 */
	private Annotation lastAnnotation;
	/**
	 * Dernier objet pour lequel on a affiché un tooltip
	 */
	private Object lastToolTip;

	final private VidesoGLCanvas wwd;

	final private ContextPanel context;

	private boolean lock = false;
	
	public AirspaceListener(VidesoGLCanvas wwd, ContextPanel context){
		this.wwd = wwd;
		this.context = context;
	}

	/* (non-Javadoc)
	 * @see gov.nasa.worldwind.event.SelectListener#selected(gov.nasa.worldwind.event.SelectEvent)
	 */
	@Override
	public void selected(final SelectEvent event) {

		//suppression tooltip on rollover
		if (lastToolTip != null										//un tooltip doit préalablement exister
				&& (event.getTopObject() == null  					//l'objet survolé doit exister
				|| !event.getTopObject().equals(lastToolTip))) 	//l'objet survolé doit être différent du précédent

		{
			if(lastAnnotation != null) {
				this.wwd.getAnnotationLayer().removeAnnotation(lastAnnotation);
				lastAnnotation = null;
			}
			lastToolTip = null;
		}
		
		//ne rien faire si locké
		if(lock)
			return;

		if(event.getTopObject() == null)
			return;

		if(event.getEventAction() == SelectEvent.HOVER){ //popup tooltip
			this.doHover(event.getTopObject(),event.getPickPoint());
		} else if(event.getEventAction() == SelectEvent.RIGHT_CLICK){
			final Object o = event.getTopObject();
			
			JPopupMenu menu;
									
			if(this.wwd.getSelectedObjects().size() != 0){
				menu = new ContextualMenu(this.wwd.getSelectedObjects(), context, wwd, event.getMouseEvent()){
					/* (non-Javadoc)
					 * @see javax.swing.JPopupMenu#setVisible(boolean)
					 */
					@Override
					public void setVisible(boolean arg0) {
						super.setVisible(arg0);
						lock = arg0;
					}	
				};
				
			} else {
				List<Object> list = new ArrayList<Object>();
				list.add(o);
				menu = new ContextualMenu(list, context, wwd, event.getMouseEvent()){
					/* (non-Javadoc)
					 * @see javax.swing.JPopupMenu#setVisible(boolean)
					 */
					@Override
					public void setVisible(boolean arg0) {
						super.setVisible(arg0);
						lock = arg0;
					}	
				};
					
			}
			
			menu.show(wwd, event.getMouseEvent().getX(), event.getMouseEvent().getY());

		} else if (event.getEventAction() == SelectEvent.LEFT_DOUBLE_CLICK){ //ouverture du contexte
			this.doDoubleClick(event.getTopObject());
		} else if (event.getEventAction() == SelectEvent.LEFT_CLICK){
			this.doLeftClick(event.getTopObject(), event.getPickPoint());
		} 
	}

	
	private void doLeftClick(Object o, Point point){
		if(o instanceof VidesoObject){
			
			//affichage du tooltip
			Position pos = null;
			if(o instanceof VPolygon) {
				pos = this.wwd.computePositionFromScreenPoint(new Point(point.x, point.y-5), (VPolygon) o);
			} else if( o instanceof Airspace) {
				pos = this.wwd.computePositionFromScreenPoint(new Point(point.x, point.y-5), (Airspace)o);
			} else if (o instanceof Path) {
				pos = this.wwd.computePositionFromScreenPoint(new Point(point.x, point.y-5), (Path)o);
			} else {
				pos = this.wwd.getView().computePositionFromScreenPoint(point.x, point.y-5);//décalage de 5 pixels pour éviter le clignotement
			}
			if(((VidesoObject)o).getAnnotation(pos) != null){
				this.wwd.getAnnotationLayer().addAnnotation(((VidesoObject)o).getAnnotation(pos));
			}
			this.wwd.redraw();
		} else if (o instanceof GlobeAnnotation){ //suppression de l'annotation
			this.wwd.getAnnotationLayer().removeAnnotation((GlobeAnnotation)o);
			this.wwd.redraw();
		} 
	}

	private void doDoubleClick(Object o){
		if(o instanceof DatabaseVidesoObject){
			this.context.showInfo(((DatabaseVidesoObject) o).getDatabaseType(), ((DatabaseVidesoObject) o).getType(), ((DatabaseVidesoObject) o).getName());
		}
	}

	private void doHover(Object o, Point point){
		if(lastToolTip == o)
			return;
		if(lastToolTip == null) {
			lastToolTip = o;
			if(o instanceof VidesoObject){
				Position pos = null;
				if(o instanceof VPolygon) {
					pos = this.wwd.computePositionFromScreenPoint(new Point(point.x, point.y-5), (VPolygon) o);
				} else if( o instanceof Airspace) {
					pos = this.wwd.computePositionFromScreenPoint(new Point(point.x, point.y-5), (Airspace)o);
				} else {
					pos = this.wwd.getView().computePositionFromScreenPoint(point.x, point.y-5);//décalage de 5 pixels pour éviter le clignotement
				}

				VidesoAnnotation a = ((VidesoObject)o).getAnnotation(pos);
				if(a!=null){
					a.getAttributes().setVisible(true);
					if(!((VAnnotationLayer)this.wwd.getAnnotationLayer()).contains(a)){
						//on ne modifie lastAnnotation que si l'annotation n'a pas déjà été ajoutée
						//(notamment lors d'un clic gauche)
						lastAnnotation = a;
					}
				}
			} 
			if(lastAnnotation != null) this.wwd.getAnnotationLayer().addAnnotation(lastAnnotation);
			this.wwd.redraw();
		}
	}


}
