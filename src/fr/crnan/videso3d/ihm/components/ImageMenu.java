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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.formats.images.EditableSurfaceImage;
import fr.crnan.videso3d.formats.images.ImageUtils;

import gov.nasa.worldwind.render.SurfaceImage;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 * Contextual menu for {@link SurfaceImage}
 * @author Bruno Spyckerelle
 * @version 0.0.2
 */
public class ImageMenu extends JPopupMenu {

	private SurfaceImage image;
	private VidesoGLCanvas wwd;
	
	public ImageMenu(SurfaceImage image, VidesoGLCanvas ww){
		this.image = image;
		this.wwd = ww;
		this.createMenu();
	}

	private void createMenu(){
		
		OpacityMenuItem opacityItem = new OpacityMenuItem(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				image.setOpacity(source.getValue()/100.0);
				wwd.redraw();
			}
		}, (int)(image.getOpacity()*100.0));

		this.add(opacityItem);
		this.add(new JSeparator());
		
		if(image instanceof EditableSurfaceImage){
			
			
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
			this.add(new JSeparator());
		} 
		
		JMenuItem delete = new JMenuItem("Supprimer");
		delete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				wwd.getImagesController().deleteImage(image);
			}
		});
		
		this.add(delete);
		
		JMenuItem save = new JMenuItem("Enregistrer en GeoTiff...");
		save.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				VFileChooser fileChooser = new VFileChooser();
				fileChooser.setMultiSelectionEnabled(false);
				if(fileChooser.showSaveDialog(getMenu()) == VFileChooser.APPROVE_OPTION){
					File file = fileChooser.getSelectedFile();
					if(!(file.exists()) || 
							(file.exists() &&
							JOptionPane.showConfirmDialog(null, "Le fichier existe déjà.\n\nSouhaitez-vous réellement l'écraser ?",
								"Confirmer la suppression du fichier précédent",
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)) {
						try {
							if(image instanceof EditableSurfaceImage){
								SurfaceImage si = ((EditableSurfaceImage) image).getEditor().getSurfaceImage();	
								ImageUtils.writeImageToFile(si.getSector(), (BufferedImage) si.getImageSource(), file);
							} else {
								ImageUtils.writeImageToFile(image.getSector(), (BufferedImage) image.getImageSource(), file);
							}
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
		this.add(save);
	}
	
	private JPopupMenu getMenu(){
		return this;
	}
}
