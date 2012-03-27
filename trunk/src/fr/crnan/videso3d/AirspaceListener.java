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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import fr.crnan.videso3d.DatabaseManager.Type;
import fr.crnan.videso3d.exsa.STRController;
import fr.crnan.videso3d.graphics.DatabaseRoute2D;
import fr.crnan.videso3d.graphics.DatabaseVidesoObject;
import fr.crnan.videso3d.graphics.Route;
import fr.crnan.videso3d.graphics.VPolygon;
import fr.crnan.videso3d.graphics.VidesoAnnotation;
import fr.crnan.videso3d.graphics.VidesoObject;
import fr.crnan.videso3d.ihm.AnalyzeUI;
import fr.crnan.videso3d.ihm.ContextPanel;
import fr.crnan.videso3d.ihm.ShapeAttributesDialog;
import fr.crnan.videso3d.ihm.components.AirspaceMenu;
import fr.crnan.videso3d.ihm.components.ImageMenu;
import fr.crnan.videso3d.ihm.components.MultipleSelectionMenu;
import fr.crnan.videso3d.layers.VAnnotationLayer;
import fr.crnan.videso3d.skyview.SkyViewController;
import fr.crnan.videso3d.stpv.StpvController;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AbstractShape;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.render.SurfaceShape;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.markers.Marker;

/**
 * Listener d'évènements sur les airspaces et shapes
 * @author Bruno Spyckerelle
 * @version 0.5.1
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
			if(this.wwd.getSelectedObjects().size() != 0){
				MultipleSelectionMenu menu = new MultipleSelectionMenu(this.wwd.getSelectedObjects(), wwd);
				menu.show(wwd, event.getMouseEvent().getX(), event.getMouseEvent().getY());
				return;
			}
			
			if(o instanceof Airspace){
				AirspaceMenu menu = new AirspaceMenu((Airspace)o, 
						(AirspaceAttributes) ((Airspace)o).getAttributes(), 
						context, wwd){

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
			} else if(o instanceof SurfaceImage){
				ImageMenu imageMenu = new ImageMenu((SurfaceImage) o, wwd){
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
				JMenuItem colorItem = new JMenuItem("Propriétés graphiques...");

				//Ajout des listeners en fonction du type d'objet
				if(o instanceof SurfaceShape || o instanceof AbstractShape){
					menu.add(colorItem);
					colorItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if(o instanceof SurfaceShape){
								new ShapeAttributesDialog(((SurfaceShape) o).getAttributes(), 
										((SurfaceShape) o).getHighlightAttributes()).setVisible(true);
							} else if (o instanceof AbstractShape){
								new ShapeAttributesDialog(((AbstractShape) o).getAttributes(), 
										((AbstractShape) o).getHighlightAttributes()).setVisible(true);
							}
							wwd.redraw();
						}
					});
					
					if(o instanceof DatabaseRoute2D){
						JMenuItem contextItem = new JMenuItem("Informations...");				
						menu.add(contextItem);
						contextItem.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								DatabaseRoute2D route = (DatabaseRoute2D) o;
								context.showInfo(route.getDatabaseType(), route.getType(), route.getName());
							}
						});
						
					}
				} else if(o instanceof Marker || o instanceof PointPlacemark){
					if(o instanceof DatabaseVidesoObject){
						JMenuItem contextItem = new JMenuItem("Informations...");				
						menu.add(contextItem);
						contextItem.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								context.showInfo(((DatabaseVidesoObject) o).getDatabaseType(),
										((DatabaseVidesoObject) o).getType(), ((VidesoObject)o).getName());
							}
						});

						if(((DatabaseVidesoObject) o).getDatabaseType().equals(Type.STIP)){
							JMenu analyseItem = new JMenu("Analyse");
							JMenuItem analyseIti = new JMenuItem("Itinéraires");
							JMenuItem analyseTrajet = new JMenuItem("Trajets");
							JMenuItem analyseRoute = new JMenuItem("Routes");
							JMenuItem analyseBalise = new JMenuItem("Balise");
							analyseBalise.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent arg0) {
									AnalyzeUI.showResults("balise", ((VidesoObject)o).getName());
								}
							});
							analyseItem.add(analyseBalise);
							analyseIti.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent arg0) {
									AnalyzeUI.showResults("iti", ((VidesoObject)o).getName());
								}
							});
							analyseItem.add(analyseIti);
							analyseTrajet.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {
									AnalyzeUI.showResults("trajet", ((VidesoObject)o).getName());
								}
							});
							analyseItem.add(analyseTrajet);
							analyseRoute.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {
									AnalyzeUI.showResults("route", ((VidesoObject)o).getName());
								}
							});
							analyseItem.add(analyseRoute);
							menu.add(analyseItem);
						}
						
					}
					
				}
				final VidesoController c = DatasManager.getController(((DatabaseVidesoObject) o).getDatabaseType());
				if(o instanceof DatabaseVidesoObject && 
						!(c instanceof STRController || c instanceof StpvController || (c instanceof SkyViewController && o instanceof Route))){
					final int type = ((DatabaseVidesoObject) o).getType();
					final String name = ((DatabaseVidesoObject) o).getName();
					final boolean locationsVisible = c.areLocationsVisible(type, name);
					JMenuItem locationsItem = new JMenuItem((locationsVisible ? "Cacher" : "Afficher") +" les coordonnées");
					menu.add(locationsItem);
					locationsItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							c.setLocationsVisible(type, name, !locationsVisible);
						}
					});
				}
				JMenuItem supprItem = new JMenuItem("Supprimer");				
				menu.add(supprItem);
				supprItem.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if(o instanceof DatabaseVidesoObject){
							DatasManager.getController(((DatabaseVidesoObject) o).getDatabaseType()).hideObject(((DatabaseVidesoObject) o).getType(), ((DatabaseVidesoObject) o).getName());
						}
					}
				});
				menu.show(wwd, event.getMouseEvent().getX(), event.getMouseEvent().getY());
			}
		} else if (event.getEventAction() == SelectEvent.LEFT_DOUBLE_CLICK){ //ouverture du contexte
			this.doDoubleClick(event.getTopObject());
		} else if (event.getEventAction() == SelectEvent.LEFT_CLICK){
			this.doLeftClick(event.getTopObject(), event.getPickPoint());
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
