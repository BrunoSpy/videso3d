package fr.crnan.videso3d.graphics;

import java.util.List;

import fr.crnan.videso3d.geom.LatLonCautra;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.airspaces.Polygon;
/**
 * Polygon avec Annotation intégrée
 * @author Bruno Spyckerelle
 * @version 0.1
 */
public class PolygonAnnotation extends Polygon {

	private GlobeAnnotation annotation;

	public PolygonAnnotation(List<LatLonCautra> locations) {
		super(locations);
	}

	public void setAnnotation(String text){
		if(annotation == null) {
			annotation = new GlobeAnnotation(text, Position.ZERO);
			annotation.setAlwaysOnTop(true);
		} else {
			annotation.setText(text);
		}
	}
	
	public GlobeAnnotation getAnnotation(Position pos){
		annotation.setPosition(pos);
		return annotation;
	}
	
}
