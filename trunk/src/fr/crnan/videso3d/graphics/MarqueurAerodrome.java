package fr.crnan.videso3d.graphics;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.DatabaseManager.Type;
import gov.nasa.worldwind.geom.Position;

public class MarqueurAerodrome extends Balise2D implements Aerodrome {

	
	public MarqueurAerodrome(int type, CharSequence name, Position position,String annotation, Type base) {
		super(name, position, annotation, base, type);
		this.setDatabaseType(DatabaseManager.Type.AIP);
		this.setType(type);
	}

	@Override
	public Position getRefPosition() {
		return this.getPosition();
	}

	@Override
	public String getAnnotationText() {
		return this.getAnnotation(null).getText();
	}

}
