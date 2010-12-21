package fr.crnan.videso3d.graphics;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.UserFacingText;

public interface Aerodrome{

	public Position getRefPosition();
	public UserFacingText getUserFacingText();
	public String getName();
	public String getAnnotationText();
}
