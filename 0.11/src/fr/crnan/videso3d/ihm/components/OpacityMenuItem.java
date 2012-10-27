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

import javax.swing.JMenu;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

/**
 * Menu item to change opacity of an item
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class OpacityMenuItem extends JMenu {
	
	private JSlider slider;

	/**
	 * 
	 * @param listener
	 * @param firstValue
	 */
	public OpacityMenuItem(ChangeListener listener, int firstValue){
		this();	
		this.setValue(firstValue);
		this.addChangeListener(listener);
		
	}
	
	/**
	 * Creates the menu item.<br />
	 */
	public OpacityMenuItem(){
		super("Opacité...");
		
		this.slider = new JSlider();
		slider.setMaximum(100);
		slider.setMinimum(0);
		slider.setOrientation(JSlider.VERTICAL);
		slider.setMinorTickSpacing(10);
		slider.setMajorTickSpacing(20);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		this.add(slider);
	}
	
	public void addChangeListener(ChangeListener listener){
		this.slider.addChangeListener(listener);
	}
	
	public void setValue(int value){
		this.slider.setValue(value);
	}
	
}
