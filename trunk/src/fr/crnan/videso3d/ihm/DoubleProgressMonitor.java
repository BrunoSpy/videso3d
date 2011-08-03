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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class DoubleProgressMonitor extends JDialog {

	private final JPanel contentPanel = new JPanel();

	private JProgressBar mainProgressBar;
	private JProgressBar secondaryProgressBar;

	private JLabel note;
	
	private String mainNote;
	
	private boolean cancel = false;
	
	/**
	 * Create the dialog.
	 */
	public DoubleProgressMonitor(Component parentComponent,
            String title,
            String note,
            int min,
            int max) {
		
		this.setTitle(title);
		this.note = new JLabel(note);
		this.setAlwaysOnTop(true);
		
		setBounds(100, 100, 378, 196);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		{
			mainProgressBar = new JProgressBar(min, max);
			secondaryProgressBar = new JProgressBar();
			contentPanel.add(Box.createVerticalStrut(10));
			contentPanel.add(this.note);
			contentPanel.add(Box.createVerticalStrut(10));
			contentPanel.add(secondaryProgressBar);
			contentPanel.add(Box.createVerticalStrut(10));
			contentPanel.add(mainProgressBar);
		}
		
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
						cancel = true;
					}
				});
				buttonPane.add(cancelButton);
			}
		}
	}

	public JProgressBar getMainProgressBar(){
		return this.mainProgressBar;
	}
	
	public JProgressBar getSecondaryProgressBar(){
		return this.secondaryProgressBar;
	}
	
	public boolean isCanceled(){
		return cancel;
	}
	
	public void setMainNote(String note){
		this.mainNote = note;
		this.note.setText(note);
	}
	
	public void setSecondNote(String note){
		this.note.setText(this.mainNote + " : "+ note);
	}
}
