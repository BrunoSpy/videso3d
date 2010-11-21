package fr.crnan.videso3d.graphics;

import java.util.List;

import fr.crnan.videso3d.geom.LatLonCautra;

public class Secteur2D extends SurfacePolygonAnnotation implements Secteur {

	private int type;
	
	private String name;
	
	public Secteur2D(List<LatLonCautra> locations) {
		super(locations);
		// TODO Auto-generated constructor stub
	}

	@Override
	public fr.crnan.videso3d.DatabaseManager.Type getDatabaseType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDatabaseType(fr.crnan.videso3d.DatabaseManager.Type type) {
		// TODO Auto-generated method stub
		
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
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

}
