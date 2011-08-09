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

package fr.crnan.videso3d.ihm.components;

import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 * Slider for opacity
 * @author Bruno Spyckerelle
 * @version 0.2.0
 */
public class OpacitySlider extends JSlider {

	public OpacitySlider(final WorldWindowGLCanvas wwd, final AirspaceAttributes attrs, final boolean inner){
		super(0, 100);
		this.setValue(inner ? (int) (attrs.getOpacity()*100) : (int) (attrs.getOutlineOpacity()*100));
		this.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if(inner)
					attrs.setOpacity(getValue()/100.0);
				else
					attrs.setOutlineOpacity(getValue()/100.0);
				wwd.redraw();
			}
		});
	}

	public OpacitySlider(final WorldWindowGLCanvas wwd, final ShapeAttributes attrs, final boolean inner) {
		super(0, 100);
		this.setValue(inner ? (int) (attrs.getInteriorOpacity()*100) : (int) (attrs.getOutlineOpacity()*100));
		this.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if(inner)
					attrs.setInteriorOpacity(getValue()/100.0);
				else
					attrs.setOutlineOpacity(getValue()/100.0);
				wwd.redraw();
			}
		});
	}
}
