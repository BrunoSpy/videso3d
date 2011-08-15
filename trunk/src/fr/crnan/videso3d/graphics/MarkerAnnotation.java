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

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RestorableSupport;

/**
 * {@link BasicMarker} avec {@link VidesoAnnotation} intégré
 * @author  Bruno Spyckerelle
 * @version 0.4.0
 */
public class MarkerAnnotation extends BasicMarker implements VidesoObject {

	private VidesoAnnotation annotation;
	
	private String name;

	private boolean highlighted = false;

	private RestorableMarkerAttributes normalAttrs;

	private RestorableMarkerAttributes highlightAttrs;
		
	public MarkerAnnotation(){
		super(Position.ZERO, new RestorableMarkerAttributes());
	}
	
	public MarkerAnnotation(Position position, RestorableMarkerAttributes attrs) {
		super(position, attrs);
	}

	public MarkerAnnotation(String annotation, Position position,
			RestorableMarkerAttributes attrs) {
		this(position, attrs);
		this.setAnnotation(annotation);
	}

	@Override
	public void setAnnotation(String text){
		if(annotation == null) {
			annotation = new VidesoAnnotation(text);
		} else {
			annotation.setText(text);
		}
	}
	
	@Override
	public VidesoAnnotation getAnnotation(Position pos){
		if(annotation == null){
			this.setAnnotation(this.name);
		}
		annotation.setPosition(pos);
		return annotation;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public boolean isHighlighted() {
		return this.highlighted ;
	}

	@Override
	public void setHighlighted(boolean highlighted) {
		if(this.highlighted != highlighted){
			this.setAttributes(highlighted ? this.getHighlightAttributes() : this.getNormalAttributes());
			this.highlighted = highlighted;
		}
	}
	
    public RestorableMarkerAttributes getNormalAttributes() {
    	if(this.normalAttrs == null)
    		this.normalAttrs = (RestorableMarkerAttributes) this.getAttributes();
        return this.normalAttrs;
    }

    public void setNormalAttributes(RestorableMarkerAttributes normalAttrs) {
        this.normalAttrs = normalAttrs;
        if(!highlighted) this.setAttributes(this.normalAttrs);
    }
    
    public RestorableMarkerAttributes getHighlightAttributes() {
    	if(this.highlightAttrs == null){
    		highlightAttrs = new RestorableMarkerAttributes(this.getNormalAttributes());
    		highlightAttrs.setMaterial(Material.YELLOW);
    	}
        return this.highlightAttrs;
    }

    /**
     * Specifies highlight attributes.
     *
     * @param highlightAttrs highlight attributes. May be null, in which case default attributes are used.
     */
    public void setHighlightAttributes(RestorableMarkerAttributes highlightAttrs) {
        this.highlightAttrs = highlightAttrs;
        if(highlighted) this.setAttributes(this.highlightAttrs);
    }

    /* ********************************************* */
    /* ************* Restorable ******************** */
    /* ********************************************* */
    
    @Override
    public String getRestorableState()
    {
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
    	if(this.getHeading() != null) rs.addStateValueAsDouble(context, "heading", this.getHeading().degrees);
    	if(this.getPitch() != null) rs.addStateValueAsDouble(context, "pitch", this.getPitch().degrees);
    	if(this.getRoll() != null) rs.addStateValueAsDouble(context, "roll", this.getRoll().degrees);
    	
    	rs.addStateValueAsString(context, "name", this.getName());
    	
        this.getNormalAttributes().getRestorableState(rs, rs.addStateObject(context, "normalAttributes"));
        if(highlightAttrs != null) {
        	this.highlightAttrs.getRestorableState(rs, rs.addStateObject(context, "highlightAttributes"));
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

    	Double heading = rs.getStateValueAsDouble(context, "heading");
    	if(heading != null)
    		this.setHeading(Angle.fromDegrees(heading));

    	Double roll = rs.getStateValueAsDouble(context, "roll");
    	if(roll != null)
    		this.setRoll(Angle.fromDegrees(roll));
    	
    	Double pitch = rs.getStateValueAsDouble(context, "pitch");
    	if(pitch != null)
    		this.setPitch(Angle.fromDegrees(pitch));

    	String name = rs.getStateValueAsString(context, "name");
    	if(name != null)
    		this.setName(name);
    	
    	RestorableSupport.StateObject so = rs.getStateObject(context, "normalAttributes");
    	if (so != null){
    		((RestorableMarkerAttributes) this.getNormalAttributes()).restoreState(rs, so);
    		this.setAttributes(this.getNormalAttributes());
    	}
    	
    	RestorableSupport.StateObject soh = rs.getStateObject(context, "highlightAttributes");
    	if (soh != null)
    		((RestorableMarkerAttributes) this.getHighlightAttributes()).restoreState(rs, soh);
    	
    	String annotation = rs.getStateValueAsString(context, "annotation");
    	if(annotation != null)
    		this.setAnnotation(annotation);
    }

}
