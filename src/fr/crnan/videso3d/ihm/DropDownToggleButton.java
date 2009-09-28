package fr.crnan.videso3d.ihm;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuListener;


public abstract class DropDownToggleButton extends JButton implements ChangeListener, PopupMenuListener, ActionListener, PropertyChangeListener{ 
	
	private final JToggleButton mainButton = this; 
	private final JButton arrowButton = new JButton(new ImageIcon(getClass().getResource("dropdown.gif"))); 

	private boolean popupVisible = false; 

	public DropDownToggleButton(){ 
		mainButton.getModel().addChangeListener(this); 
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
		if(e.getSource()==mainButton.getModel()){ 
			if(popupVisible && !mainButton.getModel().isRollover()){ 
				mainButton.getModel().setRollover(true); 
				return; 
			} 
			arrowButton.getModel().setRollover(mainButton.getModel().isRollover()); 
			arrowButton.setSelected(mainButton.getModel().isArmed() && mainButton.getModel().isPressed()); 
		}else{ 
			if(popupVisible && !arrowButton.getModel().isSelected()){ 
				arrowButton.getModel().setSelected(true); 
				return; 
			} 
			mainButton.getModel().setRollover(arrowButton.getModel().isRollover()); 
		} 
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
		mainButton.getModel().setRollover(true); 
		arrowButton.getModel().setSelected(true); 
	} 

	public void popupMenuWillBecomeInvisible(PopupMenuEvent e){ 
		popupVisible = false; 

		mainButton.getModel().setRollover(false); 
		arrowButton.getModel().setSelected(false); 
		((JPopupMenu)e.getSource()).removePopupMenuListener(this); // act as good programmer :)
	} 

	public void popupMenuCanceled(PopupMenuEvent e){ 
		popupVisible = false; 
	} 

	/*------------------------------[ Other Methods ]---------------------------------------------------*/ 

	protected abstract JPopupMenu getPopupMenu(); 

	public JButton addToToolBar(JToolBar toolbar){ 
		toolbar.add(mainButton); 
		toolbar.add(arrowButton); 
		return mainButton; 
	} 
} 