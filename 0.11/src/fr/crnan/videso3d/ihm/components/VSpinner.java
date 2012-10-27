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

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * {@link JSpinner} with min and max int values that responds to Mouse Wheel
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class VSpinner extends JSpinner {

	
	public VSpinner(int i, int j) {
		super();
		
		SpinnerNumberModel model = new SpinnerNumberModel(i, i, j, 1);
		this.setModel(model);
		
		this.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {

				if(e.getWheelRotation()<0){
					if(getModel().getNextValue() != null)
						setValue(getModel().getNextValue());
				} else {
					if(getModel().getPreviousValue() != null)
						setValue(getModel().getPreviousValue());
				}

			}
		});

	}

}
