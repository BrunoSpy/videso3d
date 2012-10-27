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

package fr.crnan.videso3d.formats.images;

import fr.crnan.videso3d.VidesoGLCanvas;
import gov.nasa.worldwindx.examples.util.SurfaceImageEditor;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.SurfaceImage;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.2
 */
public class EditableSurfaceImage extends SurfaceImage{
	
	private String name;
	
     private SurfaceImageEditor editor;

     public EditableSurfaceImage(String name, Object imageSource, Sector sector, VidesoGLCanvas wwd){
    	 
    	 super(imageSource, sector);
    	 
         this.editor = new SurfaceImageEditor(wwd, this);
         this.editor.setArmed(false);
         
         this.setName(name);

     }

     public EditableSurfaceImage(String name, SurfaceImage si, VidesoGLCanvas wwd){
    	 this(name, si.getImageSource(), si.getSector(), wwd);    	 
     }
     
     public SurfaceImageEditor getEditor()  {
         return this.editor;
     }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
