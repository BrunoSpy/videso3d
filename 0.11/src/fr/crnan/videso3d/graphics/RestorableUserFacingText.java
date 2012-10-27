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

import java.awt.Color;
import java.awt.Font;

import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.UserFacingText;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RestorableSupport;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class RestorableUserFacingText extends UserFacingText implements Restorable{

	public RestorableUserFacingText(){
		super("", Position.ZERO);
	}
	
	public RestorableUserFacingText(CharSequence text, Position textPosition) {
		super(text, textPosition);
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
		rs.addStateValueAsString(context, "text", this.getText().toString());
		rs.addStateValueAsPosition(context, "position", this.getPosition());
		rs.addStateValueAsDouble(context, "priority", this.getPriority());
		if(this.getColor() != null) rs.addStateValueAsColor(context, "color", this.getColor());
		rs.addStateValueAsBoolean(context, "visible", isVisible());

		 if(this.getFont() != null) {
	        	rs.addStateValueAsString(context, "labelFont", this.getFont().getFontName());
	        	rs.addStateValueAsInteger(context, "labelFontStyle", this.getFont().getStyle());
	        	rs.addStateValueAsInteger(context, "labelFontSize", this.getFont().getSize());
	        }
		
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
		String s = rs.getStateValueAsString(context, "text");
		if(s != null)
			this.setText(s);
		
		Position p = rs.getStateValueAsPosition(context, "position");
		if(p != null)
			this.setPosition(p);
		
		Double d = rs.getStateValueAsDouble(context, "priority");
		if(d != null)
			this.setPriority(d);
		
		Boolean b = rs.getStateValueAsBoolean(context, "visible");
		if(b != null)
			this.setVisible(b);
		
		Color c = rs.getStateValueAsColor(context, "color");
		if(c != null)
			this.setColor(c);

		s = rs.getStateValueAsString(context, "labelFont");
		Integer   i = rs.getStateValueAsInteger(context, "labelFontStyle");
		Integer i1 = rs.getStateValueAsInteger(context, "labelFontSize");
		if(s != null){
			Font f = new Font(s, i, i1);
			this.setFont(f);
		}

	}
	
}
