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

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * {@link JButton} with Drop down menu.
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class DropDownButton extends JButton implements ChangeListener, PopupMenuListener, ActionListener, PropertyChangeListener{ 
	
	private final JButton mainButton = this; 
	private final JButton arrowButton = new JButton(new ImageIcon(getClass().getResource("/resources/arrow-down.png"))); 

	private final JPopupMenu popupMenu = new JPopupMenu();
	
	private boolean popupVisible = false; 


	
	public DropDownButton(Icon icon) {
		super(icon);
		init();
	}

	public DropDownButton(String text) {
		super(text);
		init();
	}	

	private void init(){ 
		//mainButton.getModel().addChangeListener(this); 
		
		arrowButton.getModel().addChangeListener(this); 
		arrowButton.addActionListener(this); 
		arrowButton.setMargin(new Insets(3, 0, 3, 0)); 
		mainButton.addPropertyChangeListener("enabled", this); //NOI18N 
	} 

	/*------------------------------[ PropertyChangeListener ]---------------------------------------------------*/ 

	public void propertyChange(PropertyChangeEvent evt){ 
		arrowButton.setEnabled(mainButton.isEnabled()); 
	} 

	/*------------------------------[ ChangeListener ]---------------------------------------------------*/ 

	public void stateChanged(ChangeEvent e){ 
//		if(e.getSource()==mainButton.getModel()){ 
//			if(popupVisible && !mainButton.getModel().isRollover()){ 
//				mainButton.getModel().setRollover(true); 
//				return; 
//			} 
//			arrowButton.getModel().setRollover(mainButton.getModel().isRollover()); 
//			arrowButton.setSelected(mainButton.getModel().isArmed() && mainButton.getModel().isPressed()); 
//		}else{ 
			if(popupVisible && !arrowButton.getModel().isSelected()){ 
				arrowButton.getModel().setSelected(true); 
				return; 
			} 
//			mainButton.getModel().setRollover(arrowButton.getModel().isRollover()); 
//		} 
	} 

	/*------------------------------[ ActionListener ]---------------------------------------------------*/ 

	public void actionPerformed(ActionEvent ae){ 
		JPopupMenu popup = getPopupMenu(); 
		popup.addPopupMenuListener(this); 
		popup.show(mainButton, 0, mainButton.getHeight()); 
	} 

	/*------------------------------[ PopupMenuListener ]---------------------------------------------------*/ 

	public void popupMenuWillBecomeVisible(PopupMenuEvent e){ 
		popupVisible = true; 
//		mainButton.getModel().setRollover(true); 
		arrowButton.getModel().setSelected(true); 
	} 

	public void popupMenuWillBecomeInvisible(PopupMenuEvent e){ 
		popupVisible = false; 

//		mainButton.getModel().setRollover(false); 
		arrowButton.getModel().setSelected(false); 
		((JPopupMenu)e.getSource()).removePopupMenuListener(this); // act as good programmer :)
	} 

	public void popupMenuCanceled(PopupMenuEvent e){ 
		popupVisible = false; 
	} 

	/*------------------------------[ Other Methods ]---------------------------------------------------*/ 

	public JPopupMenu getPopupMenu(){
		return popupMenu;
	}

	public JButton addToToolBar(JToolBar toolbar){ 
		toolbar.add(mainButton); 
		toolbar.add(arrowButton); 
		return mainButton; 
	} 
} 
