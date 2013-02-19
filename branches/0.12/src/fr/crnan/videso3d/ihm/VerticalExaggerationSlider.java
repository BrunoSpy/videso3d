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

package fr.crnan.videso3d.ihm;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.crnan.videso3d.VidesoGLCanvas;
/**
 * Slider to change WWD's vertical exaggeration
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class VerticalExaggerationSlider extends JSlider {

	private VidesoGLCanvas wwd;
	
	public VerticalExaggerationSlider(VidesoGLCanvas ww){
		this.wwd = ww;		
		int MIN_VE = 1;
		int MAX_VE = 8;
		int curVe = (int) this.wwd.getSceneController().getVerticalExaggeration();
		curVe = curVe < MIN_VE ? MIN_VE : (curVe > MAX_VE ? MAX_VE : curVe);
		
		this.setMinimum(MIN_VE);
		this.setMaximum(MAX_VE);
		this.setValue(curVe);
		
		this.setMajorTickSpacing(1);
		this.setPaintTicks(false);
		this.setSnapToTicks(true);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(1, new JLabel("x1"));
		labelTable.put(2, new JLabel("x2"));
		labelTable.put(4, new JLabel("x4"));
		labelTable.put(8, new JLabel("x8"));
		this.setLabelTable(labelTable);
		this.setPaintLabels(true);
		this.addPropertyChangeListener("orientation", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setOrientation((Integer)evt.getNewValue());
			}
		});
		this.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				double ve = ((JSlider) e.getSource()).getValue();
				wwd.getSceneController().setVerticalExaggeration(ve == 1.0 ? 1.01 : ve);
			}
		});
	}
	
}
