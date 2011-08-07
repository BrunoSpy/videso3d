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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import fr.crnan.videso3d.layers.ScalebarLayerNM;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.render.airspaces.AbstractAirspace;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.Polygon;
import gov.nasa.worldwindx.examples.util.HighlightController;
/**
 * A very simple GLCanvas to displays one object at a time. <br/>
 * No mouse interaction allowed.
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class SimpleGLCanvas extends WorldWindowGLCanvas {

	private AbstractAirspace polygon;

	public SimpleGLCanvas(){
		super();
		this.setModel(new BasicModel());
				
		//disable selection
		this.addSelectListener(new HighlightController(this, SelectEvent.ROLLOVER));
		
		//lock axis
		this.getInputHandler().addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				e.consume();
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				e.consume();
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				e.consume();
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				e.consume();
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				e.consume();
			}
		});
		
		this.getInputHandler().addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				e.consume();
			}
		});
		
		this.getInputHandler().addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				e.consume();
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				e.consume();
			}
		});
		
		//hide compass, ..;
		for(Layer l : this.getModel().getLayers()){
			if(l instanceof CompassLayer)
				this.getModel().getLayers().remove(l);
			if(l instanceof ScalebarLayerNM)
				this.getModel().getLayers().remove(l);
			if(l instanceof WorldMapLayer)
				this.getModel().getLayers().remove(l);
		}
		
		this.getView().setEyePosition(Position.fromDegrees(49, 4, 1050e3));
		this.getView().setPitch(Angle.fromDegrees(70));
		this.getView().setHeading(Angle.fromDegrees(36.3));
	}
	
	public void addSamplePolygon(AirspaceAttributes attrs){
		ArrayList<LatLon> positions = new ArrayList<LatLon>();
		positions.add(Position.fromDegrees(46.4627, 1.2399));
		positions.add(Position.fromDegrees(46.4650, 5.3068));
		positions.add(Position.fromDegrees(49.2736, 5.4163));
		positions.add(Position.fromDegrees(49.2712, 1.1234));
		polygon = new Polygon(positions);
		polygon.setAttributes(attrs);
		polygon.setAltitudes(0, 250e3);
		AirspaceLayer layer = new AirspaceLayer();
		layer.addAirspace(polygon);
		this.getModel().getLayers().addIfAbsent(layer);
	}
	
	public AirspaceAttributes getAirspaceAttributes(){
		return polygon.getAttributes();
	}
}
