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

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.graphics.DatabaseVidesoObject;
import fr.crnan.videso3d.graphics.VidesoObject;
import fr.crnan.videso3d.ihm.ContextPanel;
import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.render.AbstractShape;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.render.SurfaceShape;
import gov.nasa.worldwind.render.airspaces.Airspace;

/**
 * Creates relevant menu according to an object
 * @author Bruno Spyckerelle
 * @version 0.0.1
 */
public class ContextualMenu extends JPopupMenu{

	private void buildOneElementMenu(Object o, ContextPanel context, VidesoGLCanvas wwd, MouseEvent event){
		
		if(o instanceof DatabaseVidesoObject){
			this.addComponents(new DatabaseVidesoObjectMenu((DatabaseVidesoObject) o, context, wwd));
			this.add(new JSeparator());
		}
		
		//si on peut le bouger, on ajoute le menu idoine, sauf si c'est une trajectoire
		if(o instanceof Movable && !(o instanceof Path)){
			this.addComponents(new MovableMenu((Movable) o, wwd, event));
		}
		
		if(o instanceof Airspace){
			this.addComponents(new AirspaceMenu((Airspace) o, wwd));
			this.add(new JSeparator());
		}
		
		if(o instanceof SurfaceImage){
			this.addComponents(new ImageMenu((SurfaceImage) o, wwd));
			this.add(new JSeparator());
		}
		
		if(o instanceof AbstractShape){
			this.addComponents(new AbstractShapeMenu((AbstractShape) o, wwd));
		}
		
		if(o instanceof SurfaceShape){
			this.addComponents(new SurfaceShapeMenu((SurfaceShape) o, wwd));
		}
		
		if(o instanceof PointPlacemark){
			this.addComponents(new PointPlacemarkMenu((PointPlacemark)o, wwd));
		}
		
		if(o instanceof VidesoObject){
			this.addComponents(new VidesoObjectMenu((VidesoObject) o, wwd, event));
		}
	}
	
	public ContextualMenu(List<?> objects, ContextPanel context, VidesoGLCanvas wwd, MouseEvent event){
		if(objects.size() == 1){
			this.buildOneElementMenu(objects.get(0), context, wwd, event);
		} else {
			this.addComponents(new MultipleSelectionMenu(objects, wwd));
		}
	}
	
	private void addComponents(JMenu menu){
		for(Component c : menu.getMenuComponents()){
			this.add(c);
		}

	}
	
}
