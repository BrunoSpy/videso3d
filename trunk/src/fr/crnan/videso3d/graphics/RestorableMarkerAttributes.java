package fr.crnan.videso3d.graphics;

import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.util.RestorableSupport;
import gov.nasa.worldwind.util.RestorableSupport.StateObject;

public class RestorableMarkerAttributes extends BasicMarkerAttributes {

	public RestorableMarkerAttributes(
			RestorableMarkerAttributes attrs) {
		super(attrs);
	}

	public RestorableMarkerAttributes() {
		super();
	}

	public void getRestorableState(RestorableSupport rs,
			StateObject addStateObject) {
		// TODO Auto-generated method stub
		
	}

	public void restoreState(RestorableSupport rs, StateObject so) {
		// TODO Auto-generated method stub
		
	}

}
