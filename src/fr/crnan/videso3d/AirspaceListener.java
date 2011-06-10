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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.crnan.videso3d.formats.images.EditableSurfaceImage;
import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.Route2D;
import fr.crnan.videso3d.graphics.VPolygon;
import fr.crnan.videso3d.graphics.VidesoObject;
import fr.crnan.videso3d.graphics.editor.PolygonEditorsManager;
import fr.crnan.videso3d.ihm.AnalyzeUI;
import fr.crnan.videso3d.ihm.ContextPanel;
import fr.crnan.videso3d.ihm.components.AirspaceMenu;
import fr.crnan.videso3d.ihm.components.ImageMenu;
import fr.crnan.videso3d.layers.VAnnotationLayer;
import fr.crnan.videso3d.stip.StipController;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AbstractShape;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.render.SurfaceShape;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.Polygon;
import gov.nasa.worldwind.render.airspaces.editor.AirspaceControlPoint;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.MarkerAttributes;

/**
 * Listener d'évènements sur les airspaces et shapes
 * @author Bruno Spyckerelle
 * @version 0.4.4
 */
public class AirspaceListener implements SelectListener {

	/**
	 * Dernier objet mis en surbrillance
	 */
	private Object lastHighlit;
	/**
	 * Dernière annotation affichée
	 */
	private Annotation lastAnnotation;
	private Object lastAttrs;
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
		
		//suppression de la surbrillance
		if (lastHighlit != null
				&& (event.getTopObject() == null || !event.getTopObject().equals(lastHighlit)))
		{
			if(lastHighlit instanceof Airspace) {
				((Airspace)lastHighlit).setAttributes((AirspaceAttributes)lastAttrs);
			} else if(lastHighlit instanceof AbstractShape){
				((AbstractShape)lastHighlit).setAttributes((ShapeAttributes)lastAttrs);
			} else if(lastHighlit instanceof SurfaceShape){
				((SurfaceShape)lastHighlit).setAttributes((ShapeAttributes)lastAttrs);
			} else if(lastHighlit instanceof Balise2D){
				((Balise2D)lastHighlit).setAttributes((MarkerAttributes) lastAttrs);
			}
			lastHighlit = null;
		}

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
		
		if(event.getEventAction() == SelectEvent.ROLLOVER){ //Hightlight object
				this.doRollOver(event.getTopObject());
		} else if(event.getEventAction() == SelectEvent.HOVER){ //popup tooltip
				this.doHover(event.getTopObject(),event.getPickPoint());
		} else if(event.getEventAction() == SelectEvent.RIGHT_CLICK){
			if(lastAttrs != null) {
				if(lastAttrs instanceof AirspaceAttributes){
					AirspaceMenu menu = new AirspaceMenu((Airspace)event.getTopObject(), (AirspaceAttributes)lastAttrs, context, wwd){

						/* (non-Javadoc)
						 * @see javax.swing.JPopupMenu#setVisible(boolean)
						 */
						@Override
						public void setVisible(boolean arg0) {
							super.setVisible(arg0);
							lock = arg0;
						}						
					};
					menu.show(wwd, event.getMouseEvent().getX(), event.getMouseEvent().getY());
				} else {
					final JPopupMenu menu = new JPopupMenu("Menu"){

						/* (non-Javadoc)
						 * @see javax.swing.JPopupMenu#setVisible(boolean)
						 */
						@Override
						public void setVisible(boolean arg0) {
							super.setVisible(arg0);
							lock = arg0;
						}


					};
					JMenuItem colorItem = new JMenuItem("Couleur...");


					JMenu opacityItem = new JMenu("Opacité ...");
					JSlider slider = new JSlider();
					slider.setMaximum(100);
					slider.setMinimum(0);
					slider.setOrientation(JSlider.VERTICAL);
					slider.setMinorTickSpacing(10);
					slider.setMajorTickSpacing(20);
					slider.setPaintLabels(true);
					slider.setPaintTicks(true);
					opacityItem.add(slider);

					//Ajout des listeners en fonction du type d'objet
					if(lastAttrs instanceof ShapeAttributes){
						menu.add(colorItem);
						menu.add(opacityItem);
						colorItem.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								Color color = JColorChooser.showDialog(menu, "Couleur du secteur", ((ShapeAttributes)lastAttrs).getInteriorMaterial().getDiffuse());
								if(color != null) {
									((ShapeAttributes)lastAttrs).setInteriorMaterial(new Material(color));
									((ShapeAttributes)lastAttrs).setOutlineMaterial(new Material(Pallet.makeBrighter(color)));
									if(event.getTopObject() instanceof AbstractShape){
										((AbstractShape)event.getTopObject()).setAttributes((ShapeAttributes) lastAttrs);
									} else if(event.getTopObject() instanceof SurfaceShape){
										((SurfaceShape)event.getTopObject()).setAttributes((ShapeAttributes) lastAttrs);
									}
								}
							}
						});
						slider.setValue((int)(((ShapeAttributes)lastAttrs).getInteriorOpacity()*100.0));
						slider.addChangeListener(new ChangeListener() {
							@Override
							public void stateChanged(ChangeEvent e) {
								JSlider source = (JSlider)e.getSource();
								if(!source.getValueIsAdjusting()){
									((ShapeAttributes)lastAttrs).setInteriorOpacity(source.getValue()/100.0);
									if(event.getTopObject() instanceof AbstractShape){
										((AbstractShape)event.getTopObject()).setAttributes((ShapeAttributes) lastAttrs);
									} else if(event.getTopObject() instanceof SurfaceShape){
										((SurfaceShape)event.getTopObject()).setAttributes((ShapeAttributes) lastAttrs);
									}
									wwd.redraw();
								}
							}
						});
						if(event.getTopObject() instanceof Route2D){
							JMenuItem contextItem = new JMenuItem("Informations...");				
							menu.add(contextItem);
							contextItem.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {
									Route2D route = (Route2D) event.getTopObject();
									context.showInfo(route.getDatabaseType(), route.getType(), route.getName());
								}
							});
							JMenuItem supprItem = new JMenuItem("Supprimer");				
							menu.add(supprItem);
							supprItem.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {
									Route2D r = (Route2D) event.getTopObject();
									DatasManager.getController(r.getDatabaseType()).hideObject(r.getType(), r.getName());
								}
							});
						}

					} else if(lastAttrs instanceof MarkerAttributes){
						JMenuItem contextItem = new JMenuItem("Informations...");				
						menu.add(contextItem);
						contextItem.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								context.showInfo(fr.crnan.videso3d.DatabaseManager.Type.STIP, StipController.BALISES, ((Balise2D)event.getTopObject()).getName());
							}
						});
						JMenu analyseItem = new JMenu("Analyse");
						JMenuItem analyseIti = new JMenuItem("Itinéraires");
						JMenuItem analyseTrajet = new JMenuItem("Trajets");
						JMenuItem analyseRoute = new JMenuItem("Routes");
						JMenuItem analyseBalise = new JMenuItem("Balise");
						analyseBalise.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent arg0) {
								AnalyzeUI.showResults("balise", ((Balise2D)event.getTopObject()).getName());
							}
						});
						analyseItem.add(analyseBalise);
						analyseIti.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent arg0) {
								AnalyzeUI.showResults("iti", ((Balise2D)event.getTopObject()).getName());
							}
						});
						analyseItem.add(analyseIti);
						analyseTrajet.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								AnalyzeUI.showResults("trajet", ((Balise2D)event.getTopObject()).getName());
							}
						});
						analyseItem.add(analyseTrajet);
						analyseRoute.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								AnalyzeUI.showResults("route", ((Balise2D)event.getTopObject()).getName());
							}
						});
						analyseItem.add(analyseRoute);
						menu.add(analyseItem);
					}
					menu.show(wwd, event.getMouseEvent().getX(), event.getMouseEvent().getY());
				}
			} else if(event.getTopObject() instanceof SurfaceImage){
				ImageMenu imageMenu = new ImageMenu((SurfaceImage) event.getTopObject(), wwd){
					/* (non-Javadoc)
					 * @see javax.swing.JPopupMenu#setVisible(boolean)
					 */
					@Override
					public void setVisible(boolean arg0) {
						super.setVisible(arg0);
						lock = arg0;
					}
				};
				imageMenu.show(wwd, event.getMouseEvent().getX(), event.getMouseEvent().getY());
			}
		} else if (event.getEventAction() == SelectEvent.LEFT_DOUBLE_CLICK){ //ouverture du contexte
			this.doDoubleClick(event.getTopObject());
		} else if (event.getEventAction() == SelectEvent.LEFT_CLICK){
			this.doLeftClick(event.getTopObject(), event.getPickPoint());
		} else if (event.getEventAction() == SelectEvent.DRAG){
			if(!(event.getTopObject() instanceof Annotation) &&  //ne pas transférer l'évènement pour les annotations
				!(this.wwd.getMeasureTool().isArmed()) && //pas de transfert si l'alidad est activé
			 	!(event.getTopObject() instanceof PointPlacemark) &&
			 	!(event.getTopObject() instanceof BasicMarker) &&
			 	!(event.getTopObject() instanceof Polygon && PolygonEditorsManager.isEditing((Polygon) event.getTopObject())) &&
			 	!(event.getTopObject() instanceof AirspaceControlPoint) &&
			 	!(event.getTopObject() instanceof EditableSurfaceImage )){
				this.wwd.getView().getViewInputHandler().mouseDragged(event.getMouseEvent());
			}
		} 
	}

	private void doLeftClick(Object o, Point point){
		if(o instanceof VidesoObject){ //affichage du tooltip
			Position pos = null;
			if(o instanceof VPolygon) {
				pos = this.wwd.computePositionFromScreenPoint(new Point(point.x, point.y-5), (VPolygon) o);
			} else if( o instanceof Airspace) {
				pos = this.wwd.computePositionFromScreenPoint(new Point(point.x, point.y-5), (Airspace)o);
			} else {
				pos = this.wwd.getView().computePositionFromScreenPoint(point.x, point.y-5);//décalage de 5 pixels pour éviter le clignotement
			}
			this.wwd.getAnnotationLayer().addAnnotation(((VidesoObject)o).getAnnotation(pos));
			this.wwd.redraw();
		} else if (o instanceof GlobeAnnotation){ //suppression de l'annotation
			this.wwd.getAnnotationLayer().removeAnnotation((GlobeAnnotation)o);
			this.wwd.redraw();
		}
	}

	private void doRollOver(Object o){
		if (lastHighlit == o)
			return; 
		if (lastHighlit == null)
		{
			if(o instanceof Airspace) {
				lastHighlit = (Airspace)o;
				lastAttrs = ((Airspace)lastHighlit).getAttributes();
				BasicAirspaceAttributes highliteAttrs = new BasicAirspaceAttributes((AirspaceAttributes) lastAttrs);
				highliteAttrs.setMaterial(new Material(Pallet.makeBrighter(((AirspaceAttributes)lastAttrs).getMaterial().getDiffuse())));
				((Airspace) lastHighlit).setAttributes(highliteAttrs);
			} else if (o instanceof AbstractShape) {
				lastHighlit = (AbstractShape)o;
				lastAttrs = ((AbstractShape)lastHighlit).getAttributes();
				BasicShapeAttributes highliteAttrs = new BasicShapeAttributes((ShapeAttributes) lastAttrs);
				highliteAttrs.setInteriorMaterial(new Material(Pallet.makeBrighter(((ShapeAttributes)lastAttrs).getInteriorMaterial().getDiffuse())));
				highliteAttrs.setOutlineMaterial(new Material(Pallet.makeBrighter(((ShapeAttributes)lastAttrs).getOutlineMaterial().getDiffuse())));
				highliteAttrs.setOutlineWidth(2.0);
				((AbstractShape) lastHighlit).setAttributes(highliteAttrs);
			} else if (o instanceof SurfaceShape) {
				lastHighlit = (SurfaceShape)o;
				lastAttrs = ((SurfaceShape)lastHighlit).getAttributes();
				BasicShapeAttributes highliteAttrs = new BasicShapeAttributes((ShapeAttributes) lastAttrs);
				highliteAttrs.setInteriorMaterial(new Material(Pallet.makeBrighter(((ShapeAttributes)lastAttrs).getInteriorMaterial().getDiffuse())));
				highliteAttrs.setOutlineMaterial(new Material(Pallet.makeBrighter(((ShapeAttributes)lastAttrs).getOutlineMaterial().getDiffuse())));
				highliteAttrs.setOutlineWidth(2.0);
				((SurfaceShape) lastHighlit).setAttributes(highliteAttrs);
			} else if(o instanceof Balise2D) {
				lastHighlit = (Balise2D)o;
				lastAttrs = ((Balise2D)lastHighlit).getAttributes();
				BasicMarkerAttributes  highliteAttrs = new BasicMarkerAttributes((BasicMarkerAttributes) lastAttrs);
				highliteAttrs.setMaterial(new Material(Pallet.makeBrighter(((MarkerAttributes)lastAttrs).getMaterial().getDiffuse())));
				((Balise2D)lastHighlit).setAttributes(highliteAttrs);
			} else {
				lastHighlit = null;
				lastAttrs = null;
			}
		}
	}

	private void doDoubleClick(Object o){
		if(o instanceof VidesoObject){
			this.context.showInfo(((VidesoObject) o).getDatabaseType(), ((VidesoObject) o).getType(), ((VidesoObject) o).getName());
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

				Annotation a = ((VidesoObject)o).getAnnotation(pos); 
				a.getAttributes().setVisible(true);
				if(!((VAnnotationLayer)this.wwd.getAnnotationLayer()).contains(a)){
					//on ne modifie lastAnnotation que si l'annotation n'a pas déjà été ajoutée
					//(notamment lors d'un clic gauche)
					lastAnnotation = a;
				}
			} 
			if(lastAnnotation != null) this.wwd.getAnnotationLayer().addAnnotation(lastAnnotation);
			this.wwd.redraw();
		}
	}
	

}
