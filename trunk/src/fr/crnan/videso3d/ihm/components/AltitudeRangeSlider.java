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

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;

import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.multislider.JXMultiBoundedRangeModel;
import org.jdesktop.swingx.multislider.JXMultiSlider;

import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.layers.AltitudeFilterableLayer;
import fr.crnan.videso3d.layers.LayerSet;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;

/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.2.0
 */
public class AltitudeRangeSlider extends JXMultiSlider {

	private final VidesoGLCanvas wwd;
	
	private JToolTip toolTip;
	private Popup popup;
	
	public AltitudeRangeSlider(VidesoGLCanvas wd){
		this.wwd = wd;
		this.setModel(new JXMultiBoundedRangeModel(0, 800, 0, 0, 800));
		this.setMinorTickSpacing(5);
		this.setSnapToTicks(true);
		this.setOrientation(SwingConstants.VERTICAL);
		
		toolTip = this.createToolTip();
		
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent arg0) {}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				setToolTipText("<html><b>Plafond : </b>"+getOuterValue()+
						"<br /><b>Plancher : </b>"+getInnerValue());
				showTooltip(e);
				filterLayers(wwd.getModel().getLayers());
			}
		});


		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {	}

			@Override
			public void mouseEntered(MouseEvent arg0) {	}

			@Override
			public void mouseExited(MouseEvent arg0) {
				if(popup != null) popup.hide();
			}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent arg0) {}
		});
	}

	private void filterLayers(Iterable<Layer> layer){
		Iterator<Layer> iterator = layer.iterator();
		while(iterator.hasNext()){
			Layer l = iterator.next();
			if(l instanceof AltitudeFilterableLayer){
				((AltitudeFilterableLayer) l).setMaximumViewableAltitude(getOuterValue()*30.47);
				((AltitudeFilterableLayer) l).setMinimumViewableAltitude(getInnerValue()*30.47);
			} else if(l instanceof LayerSet) {
				filterLayers((LayerList)l);
			}
		}
	}
	
	private void showTooltip(MouseEvent event){
		toolTip.setTipText( getToolTipText(event) );
		Point toolTipLocation = new Point(event.getXOnScreen(), event.getYOnScreen());

		if (popup != null) popup.hide();

		PopupFactory factory = PopupFactory.getSharedInstance();
		popup = factory.getPopup(this, toolTip, toolTipLocation.x, toolTipLocation.y);
		popup.show();
	}
	
}
