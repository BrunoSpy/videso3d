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
import javax.swing.ImageIcon;
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
 * @version 0.1.1
 */
public class DoubleProgressMonitor extends JDialog {

	private final JPanel progressPanel = new JPanel();

	private JProgressBar mainProgressBar;
	private JProgressBar secondaryProgressBar;

	private JLabel note;
	
	private String mainNote;
	
	private boolean cancel = false;
	private JPanel contentPanel;
	private JPanel iconPanel;
	
	/**
	 * Create the dialog.
	 */
	public DoubleProgressMonitor(Component parentComponent,
            String title,
            String note,
            int min,
            int max) {
		
		this.setTitle(title);
		this.setAlwaysOnTop(true);
		
		setBounds(100, 100, 390, 165);
		getContentPane().setLayout(new BorderLayout());
		
		contentPanel = new JPanel();
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		
		iconPanel = new JPanel();
		iconPanel.setBorder(new EmptyBorder(0, 5, 0, 5));
		iconPanel.setLayout(new BorderLayout(0, 0));
		JLabel label = new JLabel(new ImageIcon(getClass().getResource("/resources/dialog-information.png")));
		iconPanel.add(label);
		contentPanel.add(iconPanel, BorderLayout.WEST);
		this.note = new JLabel(note);
		contentPanel.add(progressPanel);
		progressPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		{
			mainProgressBar = new JProgressBar(min, max);
			secondaryProgressBar = new JProgressBar();
			progressPanel.add(Box.createVerticalStrut(10));
			progressPanel.add(this.note);
			progressPanel.add(Box.createVerticalStrut(10));
			progressPanel.add(secondaryProgressBar);
			progressPanel.add(Box.createVerticalStrut(10));
			progressPanel.add(mainProgressBar);
		}
		progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton cancelButton = new JButton("Annuler");
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
