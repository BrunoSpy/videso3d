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

import java.net.URL;
import java.util.List;

import fr.crnan.videso3d.VidesoGLCanvas;
import fr.crnan.videso3d.graphics.VidesoAnnotation;
import fr.crnan.videso3d.graphics.VidesoObject;
import gov.nasa.worldwindx.examples.util.SurfaceImageEditor;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RestorableSupport;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.2.1
 */
public class EditableSurfaceImage extends SurfaceImage implements Restorable, VidesoObject {
	
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
     
     public EditableSurfaceImage() {
    	 super();
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

	@Override
	public String getRestorableState() {
		RestorableSupport rs = RestorableSupport.newRestorableSupport();
        this.doGetRestorableState(rs, null);

        return rs.getStateAsXml();
	}

	protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context){
		// Method is invoked by subclasses to have superclass add its state and only its state
		this.doMyGetRestorableState(rs, context);
	}

	private void doMyGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context){
		String imgSourceStr = null;
        Object imgSource = this.getImageSource();
        if (imgSource instanceof String || imgSource instanceof URL)
            imgSourceStr = imgSource.toString();

        if (imgSourceStr != null){
        	rs.addStateValueAsString(context, "imageSource", imgSourceStr);
        }
        
        rs.addStateValueAsLatLonList(context, "corners", this.corners);
        
	}
	
	@Override
	public void restoreState(String stateInXml) {
		if (stateInXml == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport rs;
        try
        {
            rs = RestorableSupport.parse(stateInXml);
        }
        catch (Exception e)
        {
            // Parsing the document specified by stateInXml failed.
            String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", stateInXml);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message, e);
        }

        this.doRestoreState(rs, null);	
		
	}
	
	protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)   {
		// Method is invoked by subclasses to have superclass add its state and only its state
		this.doMyRestoreState(rs, context);
	}

	private void doMyRestoreState(RestorableSupport rs, RestorableSupport.StateObject context){
		List<LatLon> corners = rs.getStateValueAsLatLonList(context, "corners");
		if(corners != null){
			this.setCorners(corners);
		}
		
		String imageSrc = rs.getStateValueAsString(context, "imageSource");
		if(imageSrc != null){
			this.setImageSource(imageSrc, corners);
		}
	}

	@Override
	public boolean isHighlighted() {
		return false;
	}

	@Override
	public void setHighlighted(boolean highlighted) {		
	}

	@Override
	public void setAnnotation(String text) {
		
	}

	@Override
	public VidesoAnnotation getAnnotation(Position pos) {
		return null;
	}

	@Override
	public Object getNormalAttributes() {
		return null;
	}

	@Override
	public Object getHighlightAttributes() {
		return null;
	}
}
