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

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.2.0
 */
public class EpaisseurSpinner extends JSpinner {

	public EpaisseurSpinner(final WorldWindowGLCanvas wwd, final AirspaceAttributes attrs){
		super(new SpinnerNumberModel(attrs.getOutlineWidth(), 0.0, 10.0, 0.5));
		this.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if(!attrs.isDrawOutline())
					attrs.setDrawOutline(true);
				if(((Double)getValue()).doubleValue() == 0.0)
					attrs.setDrawOutline(false);
				else 
					attrs.setOutlineWidth((Double) getValue());
				wwd.redraw();
			}
		});
	}

	public EpaisseurSpinner(final WorldWindowGLCanvas wwd, final ShapeAttributes attrs) {
		
		super(new SpinnerNumberModel(attrs.getOutlineWidth(), 0.0, 10.0, 0.5));
		this.setValue(attrs.getOutlineWidth());
		this.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if(!attrs.isDrawOutline())
					attrs.setDrawOutline(true);
				if(((Double)getValue()).doubleValue() == 0.0)
					attrs.setDrawOutline(false);
				else 
					attrs.setOutlineWidth((Double) getValue());
				wwd.redraw();
			}
		});
	}
	
}
