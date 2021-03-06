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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import fr.crnan.videso3d.graphics.VidesoObject;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
/**
 * Change the name of a {@link VidesoObject}
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class ChangeNameDialog extends JDialog {

	private int result;
	
	private String nom;
	private JTextField nameField;
	
	public ChangeNameDialog(String name){
		this.setModal(true);
		
		getContentPane().setLayout(new BorderLayout());
		
		JPanel content = new JPanel();
		content.setBorder(new EmptyBorder(10, 0, 10, 0));
		
		getContentPane().add(content, BorderLayout.WEST);
		
		JLabel lblNom = new JLabel("Nom : ");
		
		nameField = new JTextField();
		nameField.setColumns(10);
		nameField.setText(name);
		
		GroupLayout gl_content = new GroupLayout(content);
		gl_content.setHorizontalGroup(
			gl_content.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_content.createSequentialGroup()
					
					.addComponent(lblNom)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(nameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					)
		);
		gl_content.setVerticalGroup(
			gl_content.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_content.createSequentialGroup()
					.addGroup(gl_content.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNom)
						.addComponent(nameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					)
		);
		content.setLayout(gl_content);
		
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.add(Box.createHorizontalGlue());
		JButton val = new JButton("Valider");
		val.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				result = JOptionPane.OK_OPTION;
				nom = nameField.getText();
				setVisible(false);
			}
		});
		buttons.add(val);
		JButton cnl = new JButton("Annuler");
		cnl.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				result = JOptionPane.CANCEL_OPTION;
				setVisible(false);
			}
		});
		buttons.add(cnl);
		getContentPane().add(buttons, BorderLayout.SOUTH);
		this.pack();
		
	}
	
	public int showDialog(MouseEvent evt){
		this.setLocation(evt.getXOnScreen(), evt.getYOnScreen());
		this.setVisible(true);
		return result;
	}
	
	public String getName(){
		return this.nom;
	}
	
}
