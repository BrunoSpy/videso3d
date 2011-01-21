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

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.SwingConstants;

import org.jdesktop.swingx.multislider.JXMultiBoundedRangeModel;
import org.jdesktop.swingx.multislider.JXMultiSlider;

import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.layers.AltitudeFilterableLayer;
import gov.nasa.worldwind.layers.Layer;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class AltitudeRangeSlider extends JXMultiSlider {

	private final VidesoGLCanvas wwd;
	
	public AltitudeRangeSlider(VidesoGLCanvas wd){
		this.wwd = wd;
		this.setModel(new JXMultiBoundedRangeModel(0, 800, 0, 0, 800));
		this.setMinorTickSpacing(5);
		this.setSnapToTicks(true);
		this.setOrientation(SwingConstants.VERTICAL);
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				setToolTipText("<html><b>Plafond : </b>"+getOuterValue()+
								"<br /><b>Plancher : </b>"+getInnerValue());
				for(Layer l : wwd.getModel().getLayers()){
					if(l instanceof AltitudeFilterableLayer){
						((AltitudeFilterableLayer) l).setMaximumViewableAltitude(getOuterValue()*30.47);
						((AltitudeFilterableLayer) l).setMinimumViewableAltitude(getInnerValue()*30.47);
					}
				}
			}
		});
	}

}
