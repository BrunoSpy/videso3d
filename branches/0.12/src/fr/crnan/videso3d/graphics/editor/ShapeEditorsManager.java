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
package fr.crnan.videso3d.graphics.editor;

import java.util.HashMap;

import fr.crnan.videso3d.VidesoGLCanvas;
import gov.nasa.worldwind.render.AbstractShape;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwindx.examples.shapebuilder.AbstractShapeEditor;
import gov.nasa.worldwindx.examples.shapebuilder.RigidShapeEditor;

/**
 * Manage shape editors
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public final class ShapeEditorsManager {

	private VidesoGLCanvas wwd;
		
	private static ShapeEditorsManager instance = new ShapeEditorsManager();
	
	private HashMap<AbstractShape, AbstractShapeEditor> editors = new HashMap<AbstractShape, AbstractShapeEditor>();
	
	private ShapeEditorsManager(){
		super();
	}


	public static void setWWD(VidesoGLCanvas wwd){
		instance.wwd = wwd;
	}
	
	/**
	 * Adds the shape to a layer and arms its editor
	 * @param shape
	 * @param Mode : {@link RigidShapeEditor#TRANSLATION_MODE}, {@link RigidShapeEditor#SCALE_MODE},
	 *  {@link RigidShapeEditor#SKEW_MODE}, {@link RigidShapeEditor#ROTATION_MODE}
	 */
	public static void editShape(AbstractShape shape, String mode){
		//don't edit a shape already being edited in the same mode
		if(isEditing(shape) && instance.editors.get(shape).getEditMode().equals(mode))
			return;
		
		if(isEditing(shape)) {
			instance.editors.get(shape).setEditMode(mode);
		} else {
			AbstractShapeEditor editor = null;
			if(shape instanceof Ellipsoid){
				editor = new EllipsoidFactory().createEditor(shape);
			}
			if(editor == null)
				return;
			editor.setArmed(true);
			editor.setWorldWindow(instance.wwd);
			editor.setEditMode(mode);
			instance.wwd.toggleLayer(editor, true);

			instance.editors.put(shape, editor);
		}
	}
	
	public static void stopEditShape(AbstractShape shape){
		if(isEditing(shape)){
			AbstractShapeEditor editor = instance.editors.get(shape);
			editor.setArmed(false);
			editor.setWorldWindow(null);
			instance.wwd.removeLayer(editor);
			instance.editors.remove(shape);
		}
	}
	
	public static boolean isEditing(AbstractShape shape){
		return instance.editors.containsKey(shape);
	}
	
	public static boolean isEditor(AbstractShape shape){
		for(AbstractShapeEditor editor : instance.editors.values()){
			if(editor.isControlPoint(shape))
				return true;
		}
		return false;
	}
	
	public static String getEditMode(AbstractShape shape){
		return instance.editors.get(shape).getEditMode();
	}
}
