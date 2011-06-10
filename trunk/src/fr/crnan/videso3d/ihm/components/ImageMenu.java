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

import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.formats.images.EditableSurfaceImage;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.SurfaceImage;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 * Contextual menu for {@link SurfaceImage}
 * @author Bruno Spyckerelle
 * @version 0.0.1
 */
public class ImageMenu extends JPopupMenu {

	private Renderable image;
	private VidesoGLCanvas wwd;
	
	public ImageMenu(Renderable image, VidesoGLCanvas ww){
		this.image = image;
		this.wwd = ww;
		this.createMenu();
	}

	private void createMenu(){
		
		JMenu opacityItem = new JMenu("Opacité ...");
		JSlider slider = new JSlider();
		slider.setMaximum(100);
		slider.setMinimum(0);
		slider.setOrientation(JSlider.VERTICAL);
		slider.setMinorTickSpacing(10);
		slider.setMajorTickSpacing(20);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		opacityItem.add(slider);
		
		this.add(opacityItem);
		this.add(new JSeparator());
		
		if(image instanceof EditableSurfaceImage){
			slider.setValue((int)(((SurfaceImage) image).getOpacity()*100.0));
			slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					JSlider source = (JSlider)e.getSource();
					((SurfaceImage) image).setOpacity(source.getValue()/100.0);
					wwd.redraw();
				}
			});
			
			if(!((EditableSurfaceImage) image).getEditor().isArmed()){
				JMenuItem edit = new JMenuItem("Editer les contours");
				edit.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						((EditableSurfaceImage)image).getEditor().setArmed(true);
					}
				});
				this.add(edit);
			} else {
				JMenuItem stopEdit = new JMenuItem("Arrêter l'édition");
				stopEdit.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						((EditableSurfaceImage)image).getEditor().setArmed(false);
					}
				});
				this.add(stopEdit);
			}
		} else {
			slider.setValue((int)(((SurfaceImage) image).getOpacity()*100.0));
			slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					JSlider source = (JSlider)e.getSource();
					((SurfaceImage) image).setOpacity(source.getValue()/100.0);
					wwd.redraw();
				}
			});
			
			
		}
		
	}
	
}
