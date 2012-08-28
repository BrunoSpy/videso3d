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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import fr.crnan.videso3d.graphics.VidesoObject;

import javax.swing.JSplitPane;
import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import java.awt.Component;
import java.util.List;

import javax.swing.Box;

/**
 * Abstract class that defines a modal IHM for adding objects to the view.<br />
 * The view is divided into 2 panes, the left one is for the help.<br/>
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public abstract class AddObjectDialog extends JDialog {

	private JPanel helpPanel;
	private JSplitPane splitPane;
	private JButton validate;
	
	/**
	 * @wbp.parser.constructor
	 */
	public AddObjectDialog(){
		super();
		this.setModal(true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		helpPanel = new TitledPanel("Aide");
		splitPane.setLeftComponent(new JScrollPane(helpPanel));
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		Component horizontalGlue = Box.createHorizontalGlue();
		panel.add(horizontalGlue);
		
		validate = new JButton("Valider");
		panel.add(validate);
		
		JButton cancel = new JButton("Annuler");
		cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				AddObjectDialog.this.dispose();
			}
		});
		panel.add(cancel);
		
	}
	
	public AddObjectDialog(String helpText, Component rightComponent){
		this.setHelpText(helpText);
		this.setContentComponent(rightComponent);
	}
	
	public void addValidateAction(ActionListener listener){
		this.validate.addActionListener(listener);
	}
	
	/**
	 * Sets the content panel at the right place
	 * @param rightPanel
	 */
	public void setContentComponent(Component rightPanel){
		this.splitPane.setRightComponent(rightPanel);
	}
	
	/**
	 * Sets HTML text to the help pane
	 * @param text
	 */
	public void setHelpText(String text){
		JLabel label = new JLabel(text);
		label.setVerticalAlignment(SwingConstants.TOP);
		this.helpPanel.add(label, BorderLayout.CENTER);
	}
	
	/**
	 * Show the dialog at the correct position.<br/>
	 * @param evt
	 * @return {@link JOptionPane#OK_OPTION} if import successful
	 */
	public int showDialog(MouseEvent evt){
		this.setLocation(evt.getXOnScreen(), evt.getYOnScreen());
		this.setVisible(true);
		return this.getResult();
	}
	
	/**
	 * Show the dialog at the correct position.<br/>
	 * @param evt
	 * @return {@link JOptionPane#OK_OPTION} if import successful
	 */
	public int showDialog(){
		this.setVisible(true);
		return this.getResult();
	}
	
	/**
	 * 
	 * @return Result of the import : {@link JOptionPane#OK_OPTION} if ok.
	 */
	protected abstract int getResult();
	
	/**
	 * 
	 * @return List of {@link VidesoObject}
	 */
	public abstract List<VidesoObject> getObjects();
	
	
}
