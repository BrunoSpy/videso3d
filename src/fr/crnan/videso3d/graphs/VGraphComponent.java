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
package fr.crnan.videso3d.graphs;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.2
 */
public class VGraphComponent extends mxGraphComponent {

	public VGraphComponent(mxGraph graph) {
		super(graph);
				
		this.getVerticalScrollBar().setUnitIncrement(20); //20px est environ la taille d'un swimlane
		
		this.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {

				if (e.isControlDown()) {
					if (e.getWheelRotation() < 0) {
						zoomIn();
					}
					else {
						zoomOut();
					}
				} 
			}
		});
		
		this.addKeyListener(new KeyGraphComponentListener(graph));

	}

}
