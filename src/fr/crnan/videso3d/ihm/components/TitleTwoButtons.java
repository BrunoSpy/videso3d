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

import java.awt.Font;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
/**
 * Title panel with two radio buttons
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class TitleTwoButtons extends JPanel {

	private JRadioButton firstButton;
	
	
	/**
	 * 
	 * @param title
	 * @param firstLabel
	 * @param secondLabel
	 * @param first True if the first button should be activated by default
	 */
	public TitleTwoButtons(String title, String firstLabel, String secondLabel, Boolean first){
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(0, 17, 1, 3));
		
		//Title
		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		this.add(titleLabel);
		
		//first button
		firstButton = new JRadioButton(firstLabel);
		firstButton.setSelected(first);
		
		//second button
		JRadioButton secondButton = new JRadioButton(secondLabel);
		
		//group
		ButtonGroup group = new ButtonGroup();
		group.add(firstButton);
		group.add(secondButton);
		
		JPanel groupPanel = new JPanel();
		groupPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.X_AXIS));
		groupPanel.add(Box.createHorizontalGlue());
		groupPanel.add(firstButton);
		groupPanel.add(secondButton);
		
		this.add(groupPanel);
	}
	
	/**
	 * Adds a listener to the first radio button
	 * @param listener
	 */
	public void addItemListener(ItemListener listener){
		firstButton.addItemListener(listener);
	}
}
