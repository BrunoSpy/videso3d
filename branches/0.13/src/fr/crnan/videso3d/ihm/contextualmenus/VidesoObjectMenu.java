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

package fr.crnan.videso3d.ihm.contextualmenus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.graphics.DatabaseVidesoObject;
import fr.crnan.videso3d.graphics.VidesoAltitudeFilterablePath;
import fr.crnan.videso3d.graphics.VidesoObject;
import fr.crnan.videso3d.ihm.components.ChangeAnnotationDialog;
import fr.crnan.videso3d.ihm.components.ChangeNameDialog;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Annotation;
/**
 * 
 * @author Bruno Spyckerelle
 *
 */
public class VidesoObjectMenu extends JMenu {

	public VidesoObjectMenu(final VidesoObject object, final VidesoGLCanvas wwd, final MouseEvent event){

		if(!(object instanceof DatabaseVidesoObject || object instanceof VidesoAltitudeFilterablePath)){
			//	ne pas modifier le nom si l'objet est de type DatabaseVidesoObject
			// idem pour un VidesoPath
			JMenuItem changeName = new JMenuItem("Renommer...");
			changeName.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					ChangeNameDialog dialog = new ChangeNameDialog(object.getName());
					if(dialog.showDialog(event) == JOptionPane.OK_OPTION){
						object.setName(dialog.getName());
					}
				}
			});	
			this.add(changeName);
		}

		final Annotation annotation = object.getAnnotation(Position.ZERO);
		if(annotation != null) {

			JMenuItem changeAnnotation = new JMenuItem("Changer l'annotation...");
			changeAnnotation.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					ChangeAnnotationDialog dialog = new ChangeAnnotationDialog(annotation.getText());
					if(dialog.showDialog(event) == JOptionPane.OK_OPTION){
						object.setAnnotation(dialog.getAnnotationText());
					}
				}
			});
			this.add(changeAnnotation);
		}


		JMenuItem supprItem = new JMenuItem("Supprimer");				
		this.add(supprItem);
		supprItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				wwd.delete(object);
			}
		});


	}

}
