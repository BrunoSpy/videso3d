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

package fr.crnan.videso3d.graphics;

import fr.crnan.videso3d.geom.LatLonUtils;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RestorableSupport;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.3
 */
public class Balise3D extends PointPlacemark implements Balise {

	private String name;
	
	private VidesoAnnotation annotation;

	private boolean locationsVisible = false;
	
	public Balise3D(){
		this(Position.ZERO);
	}
	
	public Balise3D(Position pos){
		super(pos);
		this.setApplyVerticalExaggeration(true);
		this.setLineEnabled(true);
		this.setLinePickWidth(200);
		this.setEnableBatchPicking(false);
		this.setAttributes(new RestorablePointPlacemarkAttributes());
		this.setHighlightAttributes(new RestorablePointPlacemarkAttributes());
		this.setAltitudeMode(WorldWind.ABSOLUTE);
	}
	
	public Balise3D(CharSequence name, Position position, String annotation){
		this(position);
		this.setName((String)name);		
		this.setValue(AVKey.DISPLAY_NAME, annotation);
		RestorablePointPlacemarkAttributes ppa = new RestorablePointPlacemarkAttributes();
		ppa.setLineWidth(2d);
		ppa.setLineMaterial(Material.WHITE);
		ppa.setUsePointAsDefaultImage(true);
		ppa.setLabelScale(0.7);
		if(annotation == null){
			this.setAnnotation((String) name);
		} else {
			this.setAnnotation(annotation);
		}
		this.setAttributes(ppa);
		
		RestorablePointPlacemarkAttributes ppaH = new RestorablePointPlacemarkAttributes(ppa);
		ppaH.setLineMaterial(Material.YELLOW);
		this.setHighlightAttributes(ppaH);
	}
	
	public Balise3D(String balise, Position position) {
		this(balise,  position, null);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setAnnotation(String text) {
		if(annotation == null) {
			annotation = new VidesoAnnotation(text);
		} else {
			annotation.setText(text);
		}
	}

	@Override
	public VidesoAnnotation getAnnotation(Position pos) {
		annotation.setPosition(pos);
		return annotation;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		this.setLabelText((String) name);
	}

	@Override
	public RestorablePointPlacemarkAttributes getNormalAttributes() {
		return (RestorablePointPlacemarkAttributes) this.getAttributes();
	}
	
	 /* ********************************************* */
    /* ************* Restorable ******************** */
    /* ********************************************* */
    
    @Override
    public String getRestorableState() {
        RestorableSupport rs = RestorableSupport.newRestorableSupport();
        this.doGetRestorableState(rs, null);

        return rs.getStateAsXml();
    }

    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        // Method is invoked by subclasses to have superclass add its state and only its state
        this.doMyGetRestorableState(rs, context);
    }

    private void doMyGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context) {
    	
    	if(this.getPosition() != null) rs.addStateValueAsPosition(context, "position", this.getPosition());
    	
    	rs.addStateValueAsString(context, "name", this.getName());
    	
        this.getNormalAttributes().getRestorableState(rs, rs.addStateObject(context, "normalAttributes"));
        if(highlightAttrs != null) {
        	((RestorablePointPlacemarkAttributes) this.highlightAttrs).getRestorableState(rs, rs.addStateObject(context, "highlightAttributes"));
        }
        
        rs.addStateValueAsString(context, "annotation", this.annotation.getText(), true);
    }

    @Override
    public void restoreState(String stateInXml)
    {
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

    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context) {
    	// Method is invoked by subclasses to have superclass add its state and only its state
    	this.doMyRestoreState(rs, context);
    }

    private void doMyRestoreState(RestorableSupport rs, RestorableSupport.StateObject context) {
    	Position pos = rs.getStateValueAsPosition(context, "position");
    	if(pos != null)
    		this.setPosition(pos);

    	String name = rs.getStateValueAsString(context, "name");
    	if(name != null)
    		this.setName(name);
    	
    	RestorableSupport.StateObject so = rs.getStateObject(context, "normalAttributes");
    	if (so != null){
    		((RestorablePointPlacemarkAttributes) this.getNormalAttributes()).restoreState(rs, so);
    		this.setAttributes(this.getNormalAttributes());
    	}
    	
    	RestorableSupport.StateObject soh = rs.getStateObject(context, "highlightAttributes");
    	if (soh != null)
    		((RestorablePointPlacemarkAttributes) this.getHighlightAttributes()).restoreState(rs, soh);
    	
    	String annotation = rs.getStateValueAsString(context, "annotation");
    	if(annotation != null)
    		this.setAnnotation(annotation);
    }

    @Override
	public boolean isLocationVisible() {
		return locationsVisible;
	}
	
	@Override
	public void setLocationVisible(boolean visible){
		if(!visible && locationsVisible){
			locationsVisible = false;
			this.setLabelText(name);
		}else if(visible && !locationsVisible){
			locationsVisible = true;
			this.setLabelText(name+", "+LatLonUtils.toLatLonToString(position));
		}
	}	
}
