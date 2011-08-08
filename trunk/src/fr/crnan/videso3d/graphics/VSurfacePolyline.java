package fr.crnan.videso3d.graphics;

import java.util.LinkedList;

import fr.crnan.videso3d.DatabaseManager.Type;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.SurfacePolyline;

public class VSurfacePolyline extends SurfacePolyline implements VidesoObject {

	private Type dataBaseType;
	private int type;
	private String name;
	
	public VSurfacePolyline(LinkedList<LatLon> line) {
		super(line);
	}

	@Override
	public void setAnnotation(String text) {}

	@Override
	public VidesoAnnotation getAnnotation(Position pos) {
		return null;
	}

	@Override
	public Type getDatabaseType() {
		return this.dataBaseType;
	}

	@Override
	public void setDatabaseType(Type type) {
		this.dataBaseType = type;
	}

	@Override
	public void setType(int type) {
		this.type = type;	
	}

	@Override
	public int getType() {
		return this.type;
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
	public Object getNormalAttributes() {
		return this.getAttributes();
	}

}
