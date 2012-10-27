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

package fr.crnan.videso3d;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
/**
 * SplashScreen avec barre de progression
 * @author Bruno Spyckerelle
 * @version 0.1
 */
@SuppressWarnings("serial")
public class SplashScreen extends JWindow{
	
	private ImageIcon splashImage  = new ImageIcon(getClass().getResource("/resources/splash_videso.png"));
	private JLabel image = new JLabel();
	private JProgressBar progressBar = new JProgressBar();
	
	public SplashScreen(){
		super();
		image.setIcon(splashImage);
		progressBar.setStringPainted(true);
		progressBar.setMaximum(100);
		this.getContentPane().add(image, BorderLayout.CENTER);
		this.getContentPane().add(progressBar, BorderLayout.SOUTH);
		
		Toolkit tk = this.getToolkit();
		int x = (tk.getScreenSize().width - 400)/2;
		int y = (tk.getScreenSize().height - 250)/2;
		this.setLocation(x, y);
		this.setSize(400, 250);
		
		this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				//on cache le splash screen si on clique dessus
				setVisible(false);
			}
		});
	}
	/**
	 * Mise à jour de la barre de progression
	 * @param msg Message à afficher sur la barre de progression
	 * @param value Valeur de la barre de progression (max = 100);
	 */
	public void setStatus(String msg, int value){
		progressBar.setString(msg);
		progressBar.setValue(value);
	}
	
	
}
