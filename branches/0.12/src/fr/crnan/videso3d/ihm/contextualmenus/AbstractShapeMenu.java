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

import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.graphics.editor.ShapeEditorsManager;
import fr.crnan.videso3d.ihm.ShapeAttributesDialog;
import gov.nasa.worldwind.render.AbstractShape;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwindx.examples.shapebuilder.RigidShapeEditor;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class AbstractShapeMenu extends JMenu {

	public AbstractShapeMenu(final AbstractShape shape, VidesoGLCanvas wwd){
		
		JMenuItem colorItem = new JMenuItem("Propriétés graphiques...");
		colorItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				new ShapeAttributesDialog(shape.getAttributes(), 
						shape.getHighlightAttributes()).setVisible(true);
			}
		});
		
		this.add(colorItem);
		
		//Edition des shapes
		//TODO étendre à d'autres objets que les Ellipsoid
		if(shape instanceof Ellipsoid && !ShapeEditorsManager.isEditor(shape)){
			boolean isEditing = ShapeEditorsManager.isEditing(shape);
			
			JMenu editShape = new JMenu("Editer...");
			
						
			if(!isEditing ||
				(isEditing && !ShapeEditorsManager.getEditMode(shape).equals(RigidShapeEditor.TRANSLATION_MODE))) {
				JMenuItem editMove = new JMenuItem("Position");
				editMove.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						ShapeEditorsManager.editShape(shape, RigidShapeEditor.TRANSLATION_MODE);
					}
				});
				editShape.add(editMove);
			}
			
			if(!isEditing ||
					(isEditing && !ShapeEditorsManager.getEditMode(shape).equals(RigidShapeEditor.ROTATION_MODE))) {
				JMenuItem editMove = new JMenuItem("Rotation");
				editMove.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						ShapeEditorsManager.editShape(shape, RigidShapeEditor.ROTATION_MODE);
					}
				});
				editShape.add(editMove);
			}

			if(!isEditing ||
					(isEditing && !ShapeEditorsManager.getEditMode(shape).equals(RigidShapeEditor.SCALE_MODE))) {
				JMenuItem editMove = new JMenuItem("Echelle");
				editMove.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						ShapeEditorsManager.editShape(shape, RigidShapeEditor.SCALE_MODE);
					}
				});
				editShape.add(editMove);
			}
			
			if(!isEditing ||
					(isEditing && !ShapeEditorsManager.getEditMode(shape).equals(RigidShapeEditor.SKEW_MODE))) {
				JMenuItem editMove = new JMenuItem("Inclinaison");
				editMove.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						ShapeEditorsManager.editShape(shape, RigidShapeEditor.SKEW_MODE);
					}
				});
				editShape.add(editMove);
			}
			
			this.add(editShape);
				
			if(isEditing){
				JMenuItem stopEditShape = new JMenuItem("Terminer l'édition");
				stopEditShape.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						ShapeEditorsManager.stopEditShape(shape);
					}
				});
				this.add(stopEditShape);
			} 	
		}
		
	}
	
}
