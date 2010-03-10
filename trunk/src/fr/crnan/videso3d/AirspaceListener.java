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

import fr.crnan.videso3d.graphics.Balise2D;
import fr.crnan.videso3d.graphics.ObjectAnnotation;
import fr.crnan.videso3d.graphics.Route2D;
import fr.crnan.videso3d.graphics.Route3D;
import fr.crnan.videso3d.graphics.Secteur3D;
import fr.crnan.videso3d.ihm.AnalyzeUI;
import fr.crnan.videso3d.ihm.ContextPanel;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceShape;
import gov.nasa.worldwind.render.airspaces.AbstractAirspace;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.MarkerAttributes;

/**
 * Listener d'évènements sur les airspaces et shapes
 * @author Bruno Spyckerelle
 * @version 0.3
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
			if(lastHighlit instanceof AbstractAirspace) {
				((AbstractAirspace)lastHighlit).setAttributes((AirspaceAttributes)lastAttrs);
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
						highliteAttrs.setOutlineMaterial(new Material(Pallet.makeBrighter(((ShapeAttributes)lastAttrs).getOutlineMaterial().getDiffuse())));
						((SurfaceShape) lastHighlit).setAttributes(highliteAttrs);
					} else if(event.getTopObject() instanceof Balise2D) {
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
		} else if(event.getEventAction() == SelectEvent.HOVER){ //popup tooltip
			if(event.getTopObject() != null){
				Object o = event.getTopObject();
				if(lastToolTip == o)
					return;
				if(lastToolTip == null) {
					lastToolTip = o;
					Point point = event.getPickPoint();
					if(event.getTopObject() instanceof ObjectAnnotation){
						lastAnnotation = ((ObjectAnnotation)o).getAnnotation(this.wwd.getView().computePositionFromScreenPoint(point.x, point.y-5)); //décalage de 5 pixels pour éviter le clignotement
					} 
					if(lastAnnotation != null) this.wwd.getAnnotationLayer().addAnnotation(lastAnnotation);
					this.wwd.redraw();
				}
			}
		} else if(event.getEventAction() == SelectEvent.RIGHT_CLICK){
			if(lastAttrs != null) {
				final JPopupMenu menu = new JPopupMenu("Menu");
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
				if(lastAttrs instanceof AirspaceAttributes){
					menu.add(colorItem);
					menu.add(opacityItem);
					colorItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							Color color = JColorChooser.showDialog(menu, "Couleur du secteur", ((AirspaceAttributes)lastAttrs).getMaterial().getDiffuse());
							if(color != null) {
								((AirspaceAttributes)lastAttrs).setMaterial(new Material(color));
								((AirspaceAttributes)lastAttrs).setOutlineMaterial(new Material(Pallet.makeBrighter(color)));
							}
						}
					});
					slider.setValue((int)(((AirspaceAttributes)lastAttrs).getOpacity()*100.0));
					slider.addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							JSlider source = (JSlider)e.getSource();
							((AirspaceAttributes)lastAttrs).setOpacity(source.getValue()/100.0);
							wwd.redraw();
						}
					});
					if(event.getTopObject() instanceof Secteur3D){
						JMenuItem contextItem = new JMenuItem("Informations...");				
						menu.add(contextItem);
						contextItem.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								context.showSecteur(((Secteur3D)event.getTopObject()).getName());
								context.open();
							}
						});
					} else if(event.getTopObject() instanceof Route3D) {
						JMenuItem contextItem = new JMenuItem("Informations...");				
						menu.add(contextItem);
						contextItem.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								context.showRoute(((Route3D)event.getTopObject()).getName());
								context.open();
							}
						});
					}
				} else if(lastAttrs instanceof ShapeAttributes){
					menu.add(colorItem);
					menu.add(opacityItem);
					colorItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							Color color = JColorChooser.showDialog(menu, "Couleur du secteur", ((ShapeAttributes)lastAttrs).getInteriorMaterial().getDiffuse());
							if(color != null) {
								((ShapeAttributes)lastAttrs).setInteriorMaterial(new Material(color));
								((ShapeAttributes)lastAttrs).setOutlineMaterial(new Material(Pallet.makeBrighter(color)));
								((SurfaceShape)event.getTopObject()).setAttributes((ShapeAttributes) lastAttrs);
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
								((SurfaceShape)event.getTopObject()).setAttributes((ShapeAttributes) lastAttrs);
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
								context.showRoute(((Route2D)event.getTopObject()).getName());
								context.open();
							}
						});
					}
					
				} else if(lastAttrs instanceof MarkerAttributes){
					JMenuItem contextItem = new JMenuItem("Informations...");				
					menu.add(contextItem);
					contextItem.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							context.showBalise(((Balise2D)event.getTopObject()).getName());
							context.open();
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
		} else if (event.getEventAction() == SelectEvent.LEFT_DOUBLE_CLICK){

		} else if (event.getEventAction() == SelectEvent.LEFT_CLICK){
			if(event.getTopObject() != null){ 
				Object o = event.getTopObject();
				if(o instanceof ObjectAnnotation){ //affichage du tooltip
					Point point = event.getPickPoint();
					this.wwd.getAnnotationLayer().addAnnotation(((ObjectAnnotation)o).getAnnotation(this.wwd.getView().computePositionFromScreenPoint(point.x, point.y-5)));
					this.wwd.redraw();
				} else if (o instanceof GlobeAnnotation){ //suppression de l'annotation
					this.wwd.getAnnotationLayer().removeAnnotation((GlobeAnnotation)o);
					this.wwd.redraw();
				}
			}
		} else if (event.getEventAction() == SelectEvent.DRAG){
			this.wwd.getView().getViewInputHandler().mouseDragged(event.getMouseEvent());
		}
	}
}
